# Software Platforms Project's manual

## Getting Started

### Prerequisites
- Docker and Docker Compose
- Git (optional)
- Unix-based shell environment (Linux/macOS recommended)
- Python  3.7+
- Homebrew (if curl or jq not installed, macOS only)

### Clone the Repository
```bash
git clone https://gitlab.com/giancarlopadoan-group/softplat-project
```

---
# Default initialization (fully automatized, just Guardian pages)
## Build and Start the Services
Since the project uses **custom Docker images**, all services must be built before execution.

```bash
cd softplat-project/scripts
chmod +x init.sh
./init.sh
```

This script:
- Builds all services
- Starts the application
- Retrieves a sample set of articles from **The Guardian API**
- [OPTIONAL] Retrieves a sample set of articles from **OpenWebIndex**, see below.

---

# Use OpenWebIndex Data 
### Optional, not automatized due to authentication constraints: you need a valid account to download OWI's datasets)

##### Since owilix is still a research project, service interruptions are not uncommon. For this reason, a sample dataset is provided, and can be obtained using the apposite flag (See Optional Flags chapter). The dataset is filtered to include only pages catalogued with the "Computer" tag and according to the curlie classification (https://curlie.org/en). Further information is provided and explained in the rest of the documentation.

If you want to work also with datasets from **OpenWebIndex**, follow the procedure below instead of the default initialization's one.

### Steps

1. Start the `owilix` service:
```bash
sudo docker compose -f owi-docker-compose.yml run --rm -it owilix bash
```
**Important:** Remove any existing owilix docker images, if any, before rebuilding.

2. Download a dataset:
```bash
owilix --yes remote pull all/internalID=<DATASET-ID> --threads=10 --language=eng
```
**DATASET-ID needs to be replaced**. Eg: if you visit the websited pointed below this line you can find a command like this to download one of the available: `owilix remote pull all/internalID=a246f480-cc1f-11f0-9752-f6a03915313d`. In this example `a246f480-cc1f-11f0-9752-f6a03915313d` would be the <DATASET-ID> value.

*Official web page with available datasets to download*: https://openwebindex.eu/owler/our_datasets  

Select datasets of type **curlie_full**. **With our command a filtering is already performed at download time: you will download only pages in english!**

3. Exit the container after the download completes. You will find your downloaded dataset in the `/softplat-project/all_data/raw_data/public/curlie_full` directory.

4. Restart the application. **Before proceeding watch the **--owi-sample** optional flag section:
```bash
cd ./scripts
sudo chmod +x init.sh
./init.sh
```
**Both for the OWI's and The Guardian's pages we perform another filtering phase that consists in retaining only the pages with scientific topics.**
- **Speaking of OWI**: we chose to retain only the pages classified with the *"Computers'"* tag according to the curlie convention. To perform custom filtering of the dataset based on the curlie_labels you can either use our provided script `owi_filter.py` in the *scripts* folder (that is based on the official OWI's script https://opencode.it4i.eu/openwebsearcheu-public/owi-cli/-/blob/main/owilix/cli/query.py?ref_type=heads. It is necessary to only change the value of the `category` variable accordingly) or you can use the official command line `owilix` searching the **slice** command in the official documentation here: https://opencode.it4i.eu/openwebsearcheu-public/owi-cli.
- **Speaking of the Guardian**: we chose to retain only the pages with the following tags 
    "science/science"
    "technology/technology"
    "advertising/research"
    "technology/computing"
    "technology/artificialintelligence"
    "technology/software"
    "technology/games"
    "technology/internet"
    "technology/data-security"
    "technology/hacking"
    "technology/data-protection"
    "artanddesign/graphic-design"
    "artanddesign/digital-art"
  To perform custom filtering you can change this tags accordingly to your needs in the script `.../softplat-project/scripts/guardianCrawler.sh`. This is the official docs page: https://open-platform.theguardian.com/documentation/tag.


5. At the end of the script's execution and the then of the asynchronous import process(es) you will have both Guardian's and Owi's data (of the dataset that you chose) in your instance of the application!


## Optional Flags
Force Guardian crawl. This flag is used to overwrite an existing Guardian's dataset, if the dataset is not present the default behaviour of the init script is to crawl it anyway (even without the flag). **The flag is intended just to update an already existing dataset, not to download it for the first time**:
```bash
./init.sh --guardian-force
```
---

Force OpenWebIndex sample dataset. This flag is designed to download a test dataset pre-filtered containing only tags related to "Computers" according to curlie.org classification. **If this flag is not provided you will have to download and (if needed) filter your owi dataset otherwise you will import just Guardian pages.**:
```bash
./init.sh --owi-sample
```

---

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

3. You can see teh import status uing the dedicated REST call: http://localhost:8080/api/v1/importer/status/{jobId} where **{jobId} needs to be replaced** with the alpha-numerical value that is returned in the prompt when an import process is executed. 

Eg. Import started. Job ID: 1bd8c0de-aac9-47b9-bef0-dba772e91bbf

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
```bash
sudo docker compose up -d --build
```
---
