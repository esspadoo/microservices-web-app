#!/usr/bin/env python3

import os
import pandas as pd

def filter_parquet_by_category(src_dir, dest_dir, category="Computers"):
    """
    Filter all .parquet files in src_dir to keep only rows with curlielabels_en
    containing the given category, and save them in dest_dir preserving structure.
    """
    any_file_saved = False

    for root, _, files in os.walk(src_dir):
        for file in files:
            if file.endswith(".parquet"):
                src_file = os.path.join(root, file)
                try:
                    df = pd.read_parquet(src_file, engine='auto')
                    if 'curlielabels_en' in df.columns:
                        # Mantains just the rows with the selected categories
                        mask = df['curlielabels_en'].apply(
                            lambda x: any(category in str(label) for label in x) if isinstance(x, (list, tuple, pd.Series)) else category in str(x)
                        )
                        filtered_df = df[mask]
                        if not filtered_df.empty:
                            rel_path = os.path.relpath(root, src_dir)
                            dest_subdir = os.path.join(dest_dir, rel_path)
                            os.makedirs(dest_subdir, exist_ok=True)
                            dest_file = os.path.join(dest_subdir, file)
                            filtered_df.to_parquet(dest_file)
                            any_file_saved = True
                except Exception as e:
                    print(f"Warning: Could not process {src_file}: {e}")
    return any_file_saved

def copy_and_filter_language_eng(base_dir, dest_dir, category="Computers"):
    """
    For each immediate subfolder of base_dir, find the first folder “language=eng”
    and filter all .parquet files for the category.
    """
    for src_name in os.listdir(base_dir):
        src_path = os.path.join(base_dir, src_name)
        if os.path.isdir(src_path):
            for root, dirs, _ in os.walk(src_path):
                for dir_name in dirs:
                    if dir_name.lower() == "language=eng":
                        found_path = os.path.join(root, dir_name)
                        dest_path = os.path.join(dest_dir, f"{src_name}_language_eng")
                        saved = filter_parquet_by_category(found_path, dest_path, category)
                        if saved:
                            print(f"Saved filtered files from {found_path} to {dest_path}")
                        else:
                            print(f"No '{category}' pages found in {found_path}, skipping.")
                        break  # Stop after the first language=eng (there is just one for dataset)
                else:
                    continue
                break

def main():
    base_directory = "../all_data/raw_data"
    destination_directory = "../all_data/owi_data"
    category = "Computers"  # Macro-categoria desired

    copy_and_filter_language_eng(base_directory, destination_directory, category)

if __name__ == "__main__":
    main()

