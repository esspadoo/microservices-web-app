#!/usr/bin/env python3
"""
Export CIFF + Parquet dataset to crawler-style JSONL format.

STREAMING VERSION:
- Safe for very large Parquet files
- Supports .parquet and .parquet.gz
- Uses multiprocessing to speed up conversion
- Avoids loading entire datasets into memory
"""

from concurrent.futures import ProcessPoolExecutor, as_completed
import multiprocessing as mp
import json
import os
import logging
from pathlib import Path
from typing import List
from typing import Optional
import shutil


import pyarrow.parquet as pq
import pyarrow as pa

# -------------------------------------------------
# Logging configuration
# -------------------------------------------------
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# -------------------------------------------------
# Dataset discovery helpers
# -------------------------------------------------
def find_ciff_files(dataset_path: str) -> List[str]:
    """
    Recursively search for CIFF files (.ciff, .ciff.gz) inside the dataset.

    This function is used only for discovery and logging purposes.
    CIFF files are NOT processed further in this script.

    Args:
        dataset_path (str): Root dataset directory

    Returns:
        List[str]: List of CIFF file paths
    """

    dataset_dir = Path(dataset_path)
    files = list(dataset_dir.rglob("*.ciff")) + list(dataset_dir.rglob("*.ciff.gz"))
    logger.info(f"Found {len(files)} CIFF files")
    return [str(f) for f in files]


def find_parquet_files(dataset_path: str) -> List[str]:
    """
    Recursively locate all Parquet files (.parquet, .parquet.gz)
    inside the dataset directory.

    Args:
        dataset_path (str): Root dataset directory

    Returns:
        List[str]: List of Parquet file paths
    """

    dataset_dir = Path(dataset_path)
    files = list(dataset_dir.rglob("*.parquet")) + list(dataset_dir.rglob("*.parquet.gz"))
    logger.info(f"Found {len(files)} Parquet files")

     # Log a preview of found files
    for f in files[:5]:
        logger.info(f"  - {f}")
    if len(files) > 5:
        logger.info(f"  ... and other {len(files) - 5} files")
    return [str(f) for f in files]


# -------------------------------------------------
# Schema inspection and column detection
# -------------------------------------------------
def detect_columns(schema):
    """
    Automatically detect URL, title, and content columns
    from a Parquet schema.

    Heuristics are used to:
    - identify real URL columns
    - avoid ID/hash columns mistakenly detected as URLs
    - locate title and main text fields

    Args:
        schema (pyarrow.Schema): Arrow schema from a Parquet file

    Returns:
        tuple: (url_column, title_column, content_column)

    Raises:
        RuntimeError: If required columns cannot be detected
    """

    cols = [f.name for f in schema]

    # Strong URL signals used by OWI / Common Crawl pipelines
    URL_CANDIDATES = [
        "url",
        "source_url",
        "document_url",
        "warc_target_uri",
        "target_uri",
        "fetch_url",
        "cc_url"
    ]

   # Columns that represent IDs and must never be treated as URLs
    ID_EXCLUDES = [
        "id",
        "docid",
        "document_id",
        "hash",
        "ccid",
        "uid"
    ]

    def is_real_url(colname: str) -> bool:
        """
        Decide whether a column name represents a real URL.
        """

        cl = colname.lower()
        if any(bad == cl or bad in cl for bad in ID_EXCLUDES):
            return False
        return any(good == cl or good in cl for good in URL_CANDIDATES)

    # Detect URL column
    url_col = next((c for c in cols if is_real_url(c)), None)

    # Detect title-like column (optional)
    title_col = next(
        (c for c in cols if any(k in c.lower() for k in ["title", "subject", "heading"])),
        None
    )

    # Detect main content column (mandatory)
    content_col = next(
        (c for c in cols if any(k in c.lower() for k in ["content", "text", "body", "document"])),
        None
    )

    if not url_col:
        raise RuntimeError(
            f"No real URL column found. Possible columns: {cols}"
        )

    if not content_col:
        raise RuntimeError(
            f"No content column found. Possible columns: {cols}"
        )

    return url_col, title_col, content_col

# -------------------------------------------------
# Parquet to JSONL streaming worker
# -------------------------------------------------

def process_parquet_file(parquet_path: str, output_dir: str, worker_id: int) -> int:
    """
    Convert a single Parquet file into crawler-style JSONL format.

    Processing is done in streaming batches to avoid high memory usage.
    Each worker writes to its own temporary output file.

    Args:
        parquet_path (str): Path to the Parquet file
        output_dir (str): Temporary output directory
        worker_id (int): Worker index (used for output file naming)

    Returns:
        int: Number of documents exported
    """

    output_path = os.path.join(output_dir, f"part-{worker_id:05d}.jsonl")
    count = 0

    with open(output_path, "w", encoding="utf-8") as out_f:
        # Open Parquet file in streaming mode
        pf = pq.ParquetFile(parquet_path)

        # Automatically detect column names
        url_col, title_col, content_col = detect_columns(pf.schema_arrow)

        # Iterate over record batches
        for batch in pf.iter_batches(batch_size=10_000):
            table = pa.Table.from_batches([batch])
            columns = table.to_pydict()

            # Process each row inside the batch
            for i in range(batch.num_rows):
                entry = {
                    "url": str(columns[url_col][i]),
                    "title": str(columns[title_col][i]) if title_col else None,
                    "main_content": str(columns[content_col][i])
                }
                json.dump(entry, out_f, ensure_ascii=False)
                out_f.write("\n")
                count += 1

    logger.info(f"{parquet_path} → {count} documenti")
    return count

# -------------------------------------------------
# Parallel streaming + merge phase
# -------------------------------------------------
def stream_parquet_to_json_parallel(
    parquet_files: List[str],
    output_path: str,
    workers: Optional[int] = None
):

"""
    Convert multiple Parquet files into a single JSONL file
    using parallel processing.

    Workflow:
    1. Each worker converts one Parquet file → partial JSONL
    2. All partial files are merged sequentially
    3. Temporary files are removed

    Args:
        parquet_files (List[str]): List of Parquet files to process
        output_path (str): Final JSONL output path
        workers (Optional[int]): Number of worker processes
    """
    if workers is None:
        # Leave one CPU free by default
        workers = max(1, mp.cpu_count() - 1)

    # Temporary directory for worker outputs
    output_dir = output_path + ".parts"
    os.makedirs(output_dir, exist_ok=True)

    total_docs = 0
    try:
         # Parallel processing phase
        with ProcessPoolExecutor(max_workers=workers) as executor:
            futures = {
                executor.submit(process_parquet_file, path, output_dir, i): path
                for i, path in enumerate(parquet_files)
            }

            for future in as_completed(futures):
                total_docs += future.result()

        # Merge phase (single-threaded, deterministic)
        with open(output_path, "w", encoding="utf-8") as out_f:
            for part_file in sorted(Path(output_dir).glob("part-*.jsonl")):
                with open(part_file, "r", encoding="utf-8") as pf:
                    for line in pf:
                        out_f.write(line)

        logger.info("=" * 60)
        logger.info("EXPORT COMPLETED (PARALLEL)")
        logger.info(f"Exported document: {total_docs}")
        logger.info(f"Output: {output_path}")
        logger.info("=" * 60)
    finally:
        # Always clean up temporary directory
        if Path(output_dir).exists():
            shutil.rmtree(output_dir)
            logger.info(f"Tmp folder removed: {output_dir}")

# -------------------------------------------------
# Entry point
# -------------------------------------------------
def main():
"""
    Entry point of the script.

    - Validates dataset path
    - Discovers Parquet files
    - Launches parallel streaming export
    """

    DATASET_BASE_PATH = "../all_data/owi_data/"
    OUTPUT_PATH = "../all_data/owi.json"

    if not os.path.exists(DATASET_BASE_PATH):
        logger.error(f"Folder not found: {DATASET_BASE_PATH}")
        return

    find_ciff_files(DATASET_BASE_PATH)  # solo discovery/log
    parquet_files = find_parquet_files(DATASET_BASE_PATH)

    if not parquet_files:
        logger.error("No Parquet files found!")
        return

    stream_parquet_to_json_parallel(parquet_files, OUTPUT_PATH)

# Execute only when run as a script
if __name__ == "__main__":
    main()

