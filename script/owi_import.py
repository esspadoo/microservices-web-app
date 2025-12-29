#!/usr/bin/env python3
"""
Export CIFF + Parquet dataset to crawler-style JSON format.
Versione STREAMING – sicura per file molto grandi e .gz
"""

from concurrent.futures import ProcessPoolExecutor, as_completed
import multiprocessing as mp
import json
import os
import logging
from pathlib import Path
from typing import List

import pyarrow.parquet as pq
import pyarrow as pa

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)


def find_ciff_files(dataset_path: str) -> List[str]:
    dataset_dir = Path(dataset_path)
    files = list(dataset_dir.rglob("*.ciff")) + list(dataset_dir.rglob("*.ciff.gz"))
    logger.info(f"Trovati {len(files)} file CIFF")
    return [str(f) for f in files]


def find_parquet_files(dataset_path: str) -> List[str]:
    dataset_dir = Path(dataset_path)
    files = list(dataset_dir.rglob("*.parquet")) + list(dataset_dir.rglob("*.parquet.gz"))
    logger.info(f"Trovati {len(files)} file Parquet")
    for f in files[:5]:
        logger.info(f"  - {f}")
    if len(files) > 5:
        logger.info(f"  ... e altri {len(files) - 5} file")
    return [str(f) for f in files]


def detect_columns(schema):
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

    # Columns that are IDs and must NEVER be treated as URLs
    ID_EXCLUDES = [
        "id",
        "docid",
        "document_id",
        "hash",
        "ccid",
        "uid"
    ]

    def is_real_url(colname: str) -> bool:
        cl = colname.lower()
        if any(bad == cl or bad in cl for bad in ID_EXCLUDES):
            return False
        return any(good == cl or good in cl for good in URL_CANDIDATES)

    url_col = next((c for c in cols if is_real_url(c)), None)

    title_col = next(
        (c for c in cols if any(k in c.lower() for k in ["title", "subject", "heading"])),
        None
    )

    content_col = next(
        (c for c in cols if any(k in c.lower() for k in ["content", "text", "body", "document"])),
        None
    )

    if not url_col:
        raise RuntimeError(
            f"Nessuna colonna URL reale trovata. Colonne disponibili: {cols}"
        )

    if not content_col:
        raise RuntimeError(
            f"Nessuna colonna di contenuto trovata. Colonne disponibili: {cols}"
        )

    return url_col, title_col, content_col

def process_parquet_file(parquet_path: str, output_dir: str, worker_id: int) -> int:
    output_path = os.path.join(output_dir, f"part-{worker_id:05d}.jsonl")
    count = 0

    with open(output_path, "w", encoding="utf-8") as out_f:
        pf = pq.ParquetFile(parquet_path)
        url_col, title_col, content_col = detect_columns(pf.schema_arrow)

        for batch in pf.iter_batches(batch_size=10_000):
            table = pa.Table.from_batches([batch])
            columns = table.to_pydict()

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

def stream_parquet_to_json_parallel(
    parquet_files: List[str],
    output_path: str,
    workers: int | None = None
):
    if workers is None:
        workers = max(1, mp.cpu_count() - 1)

    output_dir = output_path + ".parts"
    os.makedirs(output_dir, exist_ok=True)

    total_docs = 0
    try:
        with ProcessPoolExecutor(max_workers=workers) as executor:
            futures = {
                executor.submit(process_parquet_file, path, output_dir, i): path
                for i, path in enumerate(parquet_files)
            }

            for future in as_completed(futures):
                total_docs += future.result()

        # Merge phase (single-thread, safe)
        with open(output_path, "w", encoding="utf-8") as out_f:
            for part_file in sorted(Path(output_dir).glob("part-*.jsonl")):
                with open(part_file, "r", encoding="utf-8") as pf:
                    for line in pf:
                        out_f.write(line)

        logger.info("=" * 60)
        logger.info("EXPORT COMPLETATO (PARALLELO)")
        logger.info(f"Documenti esportati: {total_docs}")
        logger.info(f"Output: {output_path}")
        logger.info("=" * 60)
    finally:
        # Cleanup temporary folder
        if Path(output_dir).exists():
            shutil.rmtree(output_dir)
            logger.info(f"Rimossa cartella temporanea: {output_dir}")

"""
def stream_parquet_to_json(parquet_files: List[str], output_path: str):
    total_docs = 0

    with open(output_path, "w", encoding="utf-8") as out_f:
        for parquet_path in parquet_files:
            logger.info(f"Streaming: {parquet_path}")

            try:
                pf = pq.ParquetFile(parquet_path)

                url_col, title_col, content_col = detect_columns(pf.schema_arrow)

                for batch in pf.iter_batches(batch_size=10_000):
                    table = pa.Table.from_batches([batch])
                    columns = table.to_pydict()

                    row_count = batch.num_rows

                    for i in range(row_count):
                        entry = {
                            "url": str(columns[url_col][i]) if columns.get(url_col) else f"doc_{total_docs}",
                            "title": str(columns[title_col][i]) if columns.get(title_col) else None,
                            "main_content": str(columns[content_col][i]) if columns.get(content_col) else ""
                        }

                        json.dump(entry, out_f, ensure_ascii=False)
                        out_f.write("\n")
                        total_docs += 1

                    if total_docs % 100_000 == 0:
                        logger.info(f"  Processati {total_docs} documenti")

            except Exception as e:
                logger.error(f"Errore su {parquet_path}: {e}")

    logger.info("=" * 60)
    logger.info("EXPORT COMPLETATO")
    logger.info(f"Documenti esportati: {total_docs}")
    logger.info(f"Output: {output_path}")
    logger.info("=" * 60)
"""

def main():
    DATASET_BASE_PATH = "../all_data/owi_data/"
    OUTPUT_PATH = "../all_data/owi_output.json"

    if not os.path.exists(DATASET_BASE_PATH):
        logger.error(f"Cartella non trovata: {DATASET_BASE_PATH}")
        return

    find_ciff_files(DATASET_BASE_PATH)  # solo discovery/log
    parquet_files = find_parquet_files(DATASET_BASE_PATH)

    if not parquet_files:
        logger.error("Nessun file Parquet trovato")
        return

    stream_parquet_to_json_parallel(parquet_files, OUTPUT_PATH)


if __name__ == "__main__":
    main()

