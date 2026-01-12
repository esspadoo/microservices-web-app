#!/usr/bin/env python3


"""
This script processes a dataset of Parquet files and extracts only records
belonging to a specific high-level category (default: "Computers").

High-level workflow:
1. Traverse a dataset directory structure.
2. Locate folders named `language=eng`.
3. Read all `.parquet` files inside those folders.
4. Filter rows whose `curlielabels_en` field contains the desired category.
5. Save the filtered Parquet files while preserving the original folder structure.

Input directory example:
  ../all_data/raw_data/
    └── dataset_name/
        └── .../
            └── language=eng/
                └── *.parquet

Output directory example:
  ../all_data/owi_data/
    └── dataset_name_language_eng/
        └── .../
            └── *.parquet
"""

import os
import pandas as pd

def filter_parquet_by_category(src_dir, dest_dir, category="Computers"):
    """
    Filter all Parquet files inside `src_dir`, keeping only rows whose
    `curlielabels_en` column contains the specified category.

    The directory structure under `src_dir` is preserved in `dest_dir`.

    Args:
        src_dir (str): Source directory containing Parquet files.
        dest_dir (str): Destination directory for filtered Parquet files.
        category (str): Category keyword to filter on (default: "Computers").

    Returns:
        bool: True if at least one filtered file was saved, False otherwise.
    """

    # Tracks whether any output file is produced
    any_file_saved = False

    # Recursively walk through the source directory
    for root, _, files in os.walk(src_dir):
        for file in files:

            # Process only Parquet files
            if file.endswith(".parquet"):
                src_file = os.path.join(root, file)

                # Load the Parquet file into a DataFrame
                try:
                    df = pd.read_parquet(src_file, engine='auto')

                    # Proceed only if the expected label column exists
                    if 'curlielabels_en' in df.columns:


                       # Build a boolean mask selecting rows that contain the category
                        # Handles both list-like labels and string values safely
                        mask = df['curlielabels_en'].apply(
                            lambda x: any(category in str(label) for label in x) if isinstance(x, (list, tuple, pd.Series)) else category in str(x)
                        )

                        # Apply the filter
                        filtered_df = df[mask]

                        # Save only if the result is not empty
                        if not filtered_df.empty:

                             # Preserve the relative directory structure
                            rel_path = os.path.relpath(root, src_dir)
                            dest_subdir = os.path.join(dest_dir, rel_path)

                            # Create destination directories if needed
                            os.makedirs(dest_subdir, exist_ok=True)

                            # Write filtered Parquet file
                            dest_file = os.path.join(dest_subdir, file)
                            filtered_df.to_parquet(dest_file)
                            any_file_saved = True
                except Exception as e:
                    # Fail gracefully on corrupt or incompatible files
                    print(f"Warning: Could not process {src_file}: {e}")
    return any_file_saved

def copy_and_filter_language_eng(base_dir, dest_dir, category="Computers"):
    """
    For each immediate subdirectory of `base_dir`, locate the first folder
    named `language=eng` and filter all Parquet files found inside it.

    Each dataset is saved into a separate output directory named:
        <dataset_name>_language_eng

    Args:
        base_dir (str): Root directory containing multiple datasets.
        dest_dir (str): Destination directory for filtered datasets.
        category (str): Category keyword to filter on.
    """

    # Iterate over top-level dataset folders
    for src_name in os.listdir(base_dir):
        src_path = os.path.join(base_dir, src_name)
        
        # Only process directories
        if os.path.isdir(src_path):

            # Recursively search for "language=eng"
            for root, dirs, _ in os.walk(src_path):
                for dir_name in dirs:
                    if dir_name.lower() == "language=eng":
                        found_path = os.path.join(root, dir_name)

                        # Output folder name is derived from dataset name
                        dest_path = os.path.join(dest_dir, f"{src_name}_language_eng")

                        # Filter Parquet files inside language=eng
                        saved = filter_parquet_by_category(found_path, dest_path, category)
                        
                        # Log result
                        if saved:
                            print(f"Saved filtered files from {found_path} to {dest_path}")
                        else:
                            print(f"No '{category}' pages found in {found_path}, skipping.")
                        break  # Stop after the first language=eng (there is just one for dataset)
                # Stop outer walk once language=eng is found
                else:
                    continue
                break

def main():
    """
    Entry point of the script.

    Defines input/output directories and the target category,
    then starts the filtering process.
    """
    base_directory = "../all_data/raw_data"
    destination_directory = "../all_data/owi_data"

    # High-level category to extract
    category = "Computers"

    copy_and_filter_language_eng(base_directory, destination_directory, category)

# Run only if executed as a script (not imported)
if __name__ == "__main__":
    main()

