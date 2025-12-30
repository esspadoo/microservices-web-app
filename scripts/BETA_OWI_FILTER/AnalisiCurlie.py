#!/usr/bin/env python3
# Program to check if the filtering has been successful.

import pandas as pd
import os

folder = "./Prove/3b5892ea-9a30-11f0-a6f8-f6a03915313d_language_eng/"
dfs = []

if os.path.exists(folder):
    print(f"Testing folder: {folder}")
    for root, _, files in os.walk(folder):
        for file in files:
            if file.endswith(".parquet"):
                file_path = os.path.join(root, file)
                try:
                    df = pd.read_parquet(file_path, engine='auto')
                    dfs.append(df)
                    print(f"Read {file_path}, shape {df.shape}")
                except Exception as e:
                    print(f"Failed to read {file_path}: {e}")
    
    if dfs:
        all_data = pd.concat(dfs, ignore_index=True)
        print(f"\nCombined DataFrame shape: {all_data.shape}")
        print(f"Columns: {list(all_data.columns)}")
        if 'curlielabels_en' in all_data.columns:
            print(f"First row curlielabels_en: {all_data['curlielabels_en'].iloc[0]}")
    else:
        print("No parquet files found or all failed to read.")
else:
    print(f"Folder not found: {folder}")

# Primi 100
print("Primi 100 curlielabels_en:")
print(all_data['curlielabels_en'].head(100))

# Ultimi 100
print("\nUltimi 100 curlielabels_en:")
print(all_data['curlielabels_en'].tail(100))

