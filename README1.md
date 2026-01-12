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


# Project's structure
![Project's Pipeline](./documentation/Immagini%20presentazione/pipeline_diagram.png)

# **GETTING STARTED**
To get started with a seamingless installation of the application, use the followings commands.<br/>
Download the project with the following command.<br/>
```
git clone https://gitlab.com/giancarlopadoan-group/softplat-project
```

# Compile & Start the services
**Since we have custom images** we need to build and start them all using the following command<br />

```
cd softplat-project-main/scripts
chmod +x init.sh && ./init.sh 
```

The previous script, startup all the services, and retrieve some articles via the Guardian API.<br/><br/>

## OpenWebIndex Data
**[This step is not mandatory]**<br/>
If you want to work also with OpenWebIndex dataset follow **INSTEAD** this manual procedure.<br/>

1. From the project root directory start the owilix service:<br/>`sudo docker compose -f owi-docker-compose.yml run --rm -it owilix bash`
   - **!IMPORTANT**: if you already have other images of owilix make sure to delete them and re-build following the above procedure otherwise the service will not work properly  
2. After the service boot up, download a preferred dataset:<br/>`owilix --yes remote pull all/internalID=PASTE-YOUR-DATASET-ID --threads=10 --language=eng` and follow the instruction prompted.<br/>
   - You can choose your dataset between the ones provided here: [OWI Datasets](https://openwebindex.eu/owler/our_datasets)<br/>
    Make sure to select the **curlie_full** ones. 
3. When the download is complete the container/service can be closed.
4. Now from the project root folder run the following command: <br/>
```
   cd ./scripts
   sudo chmod +x init.sh && ./init.sh
```
- If you want to force the crawl of THE GUARDIAN data just use the flag **--guardian-force** like this: 
```
   cd ./scripts
   sudo chmod +x init.sh && ./init.sh --guardian-force
```

- If you want to force the update/download of the inferer model just use the flag **--model-update** like this:
```
   cd ./scripts
   sudo chmod +x init.sh && ./init.sh --model-update
```

## RE-START THE APPLICATION
If you have already initialised the setup and the app in a previou scenario and you need only to start the application just run:
```
sudo docker compose up -d --build
```

## CLOSE THE APPLICATION
To effectively close the application and all its services run the following command:
```
sudo docker compose down
```
<br/><br/>

# LOAD A DATASET

To load a dataset and use it in the application you have to perform the following steps:

1. Start the application
```
sudo docker compose up -d --build
```

2. Load them using the imported via POST call to the service
```
curl -X POST http://localhost:8080/api/v1/importer/import \
  -F "file=@{YOUR_DATASET_FILENAME.JSON}" \
  -F "indexName={YOUR_INDEX_NAME}"
```
3. Wait until the dataset is loaded and then you can use it in the application.

# TRAIN YOUR OWN MODEL
We have provided also the possibility to train your own model, in order to be able to use the application for different types of datasets.<br/>

to do that you have to perform the following steps:<br/>

1. Open the trainer module and place your train set in the src folder.<br/>
**The train dataset have to match this name and filetype [train_set.json]**

2. A stoplist is already provided, if you want to use a modified one place it in the ./scr/resources/ folder. <br/>
**The stoplist have to be match this name and filetype [stoplist.txt]**

3. You can then starts the module and when the training concludes it will provide both an **inferer.model** and a **model.pipe** files. Both this files, in order to be used in the application have to be placed in the inferer module under the path: inferer/src/main/resources/inferer/

4. The stoplist used have to be placed in the inferer module under the path: inferer/src/main/resources/

5. You can now start the application with your own model.
