import pandas as pd
import json
import re
import sys
import os

def clean_text(text):
    """Pulisce il testo per Mallet: rimuove HTML, caratteri insoliti e normalizza"""
    if not isinstance(text, str):
        return ""
    
    # 1. Rimuove tag HTML
    text = re.sub(r'<[^>]+>', ' ', text)
    
    # 2. Rimuove caratteri Unicode insoliti (Line Separator, Paragraph Separator, etc.)
    unusual_chars = ['\u2028', '\u2029', '\u000B', '\u000C', '\u0085']
    for char in unusual_chars:
        text = text.replace(char, ' ')
    
    # 3. Rimuove caratteri di controllo
    text = re.sub(r'[\x00-\x08\x0B\x0C\x0E-\x1F\x7F]', '', text)
    
    # 4. Decodifica entità HTML comuni
    html_entities = {
        '&nbsp;': ' ', '&amp;': '&', '&lt;': '<', '&gt;': '>',
        '&quot;': '"', '&#39;': "'", '&apos;': "'",
        '&cent;': 'c', '&pound;': '£', '&euro;': '€',
        '&copy;': '(c)', '&reg;': '(r)', '&#34;': '"',
        '&#38;': '&', '&#60;': '<', '&#62;': '>',
        '&#160;': ' ', '&#xa0;': ' '
    }
    
    for entity, replacement in html_entities.items():
        text = text.replace(entity, replacement)
    
    # 5. Normalizza spazi bianchi (importante per Mallet)
    text = re.sub(r'\s+', ' ', text).strip()
    
    return text

def jsonl_to_mallet_txt(json_file, txt_file):
    """
    Converti file JSONL in formato TXT per Mallet
    Formato: ogni riga = documento (contenuto testuale pulito)
    """
    
    print(f"Leggo {json_file}...")
    
    # Leggi tutti i documenti dal JSONL
    documents = []
    with open(json_file, 'r', encoding='utf-8') as f:
        for line_num, line in enumerate(f, 1):
            line = line.strip()
            if line:
                try:
                    doc = json.loads(line)
                    documents.append(doc)
                except json.JSONDecodeError:
                    print(f"Attenzione: riga {line_num} ignorata (JSON non valido)")
    
    if not documents:
        print("Errore: Nessun documento valido trovato!")
        return False
    
    print(f"Trovati {len(documents)} documenti")
    
    # Cerca i campi di testo più comuni
    all_fields = set()
    for doc in documents:
        all_fields.update(doc.keys())
    
    # Priorità dei campi per estrarre il testo
    text_fields_priority = [
        'main_content', 'content', 'text', 'body', 'article',
        'description', 'summary', 'abstract', 'title'
    ]
    
    # Trova il campo migliore per il testo
    text_field = None
    for field in text_fields_priority:
        if field in all_fields:
            text_field = field
            break
    
    if not text_field:
        # Se nessun campo standard, usa il primo campo stringa
        for field in all_fields:
            if any(isinstance(doc.get(field), str) for doc in documents):
                text_field = field
                break
    
    if not text_field:
        print("Errore: Nessun campo di testo trovato nei documenti!")
        return False
    
    print(f"Uso il campo '{text_field}' per il testo")
    
    # Pulisci e scrivi i documenti
    print("Pulisco il testo...")
    written_count = 0
    
    with open(txt_file, 'w', encoding='utf-8') as f_out:
        for i, doc in enumerate(documents):
            # Estrai il testo
            text = doc.get(text_field, '')
            if not text:
                # Se il campo principale è vuoto, prova a combinare altri campi
                text_parts = []
                for field in ['title', 'description', 'summary']:
                    if field in doc and doc[field]:
                        text_parts.append(str(doc[field]))
                text = ' '.join(text_parts)
            
            # Pulisci il testo
            clean = clean_text(str(text))
            
            # Salva solo se c'è testo
            if clean and len(clean.strip()) > 0:
                f_out.write(clean + '\n')
                written_count += 1
    
    print(f"Scritti {written_count} documenti in {txt_file}")
    print("Fatto!")
    return True

def main():
    """Funzione principale - input: file.json, output: file.txt"""
    
    if len(sys.argv) != 3:
        print("Uso: python3 script.py <file_input.json> <file_output.txt>")
        print("Esempio: python3 script.py provaConv.json output.txt")
        sys.exit(1)
    
    input_file = sys.argv[1]
    output_file = sys.argv[2]
    
    # Verifica input
    if not os.path.exists(input_file):
        print(f"ERRORE: File '{input_file}' non trovato!")
        sys.exit(1)
    
    # Assicurati che l'output sia .txt
    if not output_file.endswith('.txt'):
        output_file = output_file + '.txt'
    
    # Esegui conversione
    try:
        success = jsonl_to_mallet_txt(input_file, output_file)
        if not success:
            sys.exit(1)
    except Exception as e:
        print(f"ERRORE: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
