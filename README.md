# Software Platforms Project

## Overview
This project was developed as part of the **Software Platforms** course.
The objective is to design and implement a distributed platform that collects documents from one or more online sources, identifies documents related to **science and technology**, stores them, makes them searchable, and extracts useful representations for expert users.

The system follows a **service-oriented architecture**, emphasizing modularity, reproducibility, and documented design patterns.

---

## Project Objectives
- Collect documents from external online sources
- Identify and filter documents related to science and technology
- Store documents in a persistent database
- Provide search and indexing capabilities
- Perform topic modeling to extract meaningful representations
- Support expert users through structured data access and analysis


---

## Project Requirements
- Use **multiple services** (microservice-oriented approach)
- Use **Docker** for containerized deployment (recommended)
- Services implemented primarily in **Java**
- Apply and document **design patterns and architectural choices**
  - Adapter pattern and other relevant patterns must be explicitly documented
  - Architecture documentation is a **core evaluation component**

---

## Evaluation Criteria
The project is evaluated based on:
- **Documentation**
- **Code quality**
- **Reproducibility**
- **Final presentation**
  - Every group member must actively participate
  - A slide-based presentation (e.g., PowerPoint) is recommended

---

## Project Structure
The following diagram illustrates the system pipeline:

![Project Pipeline](./documentation/Immagini%20presentazione/pipeline_diagram.png)

---

## Getting Started

### Prerequisites
- Docker and Docker Compose
- Git
- Unix-based shell environment (Linux/macOS recommended)
- Python  3+

### Clone the Repository
```bash
git clone https://gitlab.com/giancarlopadoan-group/softplat-project
```

---

## Build and Start the Services
Since the project uses **custom Docker images**, all services must be built before execution.

```bash
cd softplat-project-main/scripts
chmod +x init.sh
./init.sh
```

This script:
- Builds all services
- Starts the application
- Retrieves a sample set of articles from **The Guardian API**

---

## OpenWebIndex Data (Optional)

This step is optional.

If you want to work with datasets from **OpenWebIndex**, follow the procedure below instead of the default initialization.

### Steps

1. Start the `owilix` service:
```bash
sudo docker compose -f owi-docker-compose.yml run --rm -it owilix bash
```
**Important:** Remove any existing owilix images before rebuilding.

2. Download a dataset:
```bash
owilix --yes remote pull all/internalID=PASTE-YOUR-DATASET-ID --threads=10 --language=eng
```
Available datasets: https://openwebindex.eu/owler/our_datasets  
Select datasets of type **curlie_full**.

3. Exit the container after the download completes.

4. Restart the application:
```bash
cd ./scripts
sudo chmod +x init.sh
./init.sh
```

### Optional Flags
Force Guardian crawl:
```bash
./init.sh --guardian-force
```

Force model update:
```bash
./init.sh --model-update
```

---

## Restart the Application
```bash
sudo docker compose up -d --build
```

---

## Stop the Application
```bash
sudo docker compose down
```

---

## Load a Dataset

1. Start the application:
```bash
sudo docker compose up -d --build
```

2. Import the dataset:
```bash
curl -X POST http://localhost:8080/api/v1/importer/import \
  -F "file=@YOUR_DATASET_FILENAME.json" \
  -F "indexName=YOUR_INDEX_NAME"
```

3. Wait for the import to complete.

---

## Train a Custom Model

1. Place your training dataset in:
```
trainer/src/train_set.json
```

2. Optional stoplist:
```
trainer/src/resources/stoplist.txt
```

3. Run the trainer module.

4. Generated files:
- inferer.model
- model.pipe

5. Move them to:
```
inferer/src/main/resources/inferer/
```

6. Place the stoplist in:
```
inferer/src/main/resources/
```

7. Restart the application.

---
