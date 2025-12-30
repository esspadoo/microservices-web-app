#!/usr/bin/env python3

import os
import shutil

def copy_language_eng_folders(base_dir, dest_dir):
    """
    For each immediate subdirectory in base_dir, recursively find the first folder
    named 'language=eng' (case-insensitive) and copy it to dest_dir.
    """
    if not os.path.exists(dest_dir):
        os.makedirs(dest_dir)

    # Iterate over immediate subdirectories
    for src_name in os.listdir(base_dir):
        src_path = os.path.join(base_dir, src_name)
        if os.path.isdir(src_path):
            # Recursively scan this src_dir
            for root, dirs, files in os.walk(src_path):
                for dir_name in dirs:
                    if dir_name.lower() == "language=eng":
                        found_path = os.path.join(root, dir_name)
                        # Copy the folder to destination
                        dest_path = os.path.join(dest_dir, f"{src_name}_language_eng")

                        if os.path.exists(dest_path):
                            print(f"Skipping {src_name}, already copied.")
                        else:
                            shutil.copytree(found_path, dest_path)
                            print(f"Copied {found_path} to {dest_path}")
                        # Stop scanning this src_dir after first match
                        break
                else:
                    # Continue walking if inner loop didn't break
                    continue
                break

def main():
    base_directory = "../all_data/raw_data"
    destination_directory = "../all_data/owi_data"

    copy_language_eng_folders(base_directory, destination_directory)

if __name__ == "__main__":
    main()
