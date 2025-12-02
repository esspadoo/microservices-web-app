import pandas as pd
import json
import sys
import os

def jsonl_to_csv_pandas(jsonl_file, csv_file):
    # Leggi il file JSONL
    data = []
    with open(jsonl_file, 'r', encoding='utf-8') as f:
        for line in f:
            if line.strip():
                data.append(json.loads(line))
    
    # Converti in DataFrame
    df = pd.DataFrame(data)
    
    # Opzionale: pulisci i dati
    df = df.fillna('')  # Sostituisci NaN con stringa vuota
    
    # Salva come CSV
    df.to_csv(csv_file, index=False, encoding='utf-8')
    print(f"File convertito: {csv_file}")
    print(f"Numero di righe: {len(df)}")
    print(f"Colonne: {list(df.columns)}")

if __name__ == "__main__":
    # Verifica se sono stati passati argomenti da riga di comando
    if len(sys.argv) == 3:
        input_file = sys.argv[1]
        output_file = sys.argv[2]
    else:
        # Se non ci sono argomenti, chiedi all'utente
        input_file = input("Inserisci il nome del file JSON di input: ").strip()
        output_file = input("Inserisci il nome del file CSV di output: ").strip()
    
    # Verifica che il file di input esista
    if not os.path.exists(input_file):
        print(f"ERRORE: Il file '{input_file}' non esiste!")
        print(f"Directory corrente: {os.getcwd()}")
        print("File disponibili:")
        for file in os.listdir('.'):
            if file.endswith('.json'):
                print(f"  - {file}")
        sys.exit(1)
    
    # Esegui la conversione
    try:
        jsonl_to_csv_pandas(input_file, output_file)
    except Exception as e:
        print(f"Errore durante la conversione: {e}")
        sys.exit(1)