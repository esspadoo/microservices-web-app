#!/bin/bash

# Script per installare e configurare l'ambiente OWI
set -e  # Esci immediatamente in caso di errore

echo "=== Attivazione Conda ==="
source /opt/conda/bin/activate

echo "=== Creazione ambiente conda 'owi' ==="
# Accetto i termini di servizio per poter usare conda
conda tos accept --override-channels --channel https://repo.anaconda.com/pkgs/main
conda tos accept --override-channels --channel https://repo.anaconda.com/pkgs/r

# Creo l'ambiente
conda create -n owi pip python=3.11 -y

echo "=== Attivazione ambiente owi ==="
source /opt/conda/bin/activate owi

echo "=== Installazione py4lexis ==="
pip install py4lexis --index-url https://opencode.it4i.eu/api/v4/projects/107/packages/pypi/simple

echo "=== Installazione owilix ==="
pip install owilix --index-url https://opencode.it4i.eu/api/v4/projects/92/packages/pypi/simple

echo "=== Verifica installazione ==="
owilix --help

echo "=== Installazione completata con successo! ==="
