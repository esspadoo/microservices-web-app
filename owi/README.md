# OWI (Open Web Index)
-**Search application**: application with more complex functionality than traditional search consisting of search queries and results
![Search Application schema](images/schemaSearchApplication.jpg)

**(Image taken from the Open web index search's presentation at the link https://vimeo.com/1082089186)**
- **Owilix permits to download index shards from OWI**. Owilix is a command line tool with command structure. Richiede registration, end user license, ethical self-assessment.

## Structure of the index (OWI e index shards)
Index consists of CIFF and parquet files. 
- **CIFF file** is an inverted index: it contains all the words contained in the web documents with a reference to the document. Its structure is like the following example:

| Term   | (Document, frequency of the term in the document)   |
|--------|-----------------------------------------------------|
| Term 1 | (docID A, 1) (docID B, 5)                           |
| Term 2 | (docID C, 2)                                        |
| …      | …                                                   |


- **Parquet file**: contains all the metadata of each document. Its structure is like the following example:

| Document | Lang | Full text     | WARC date | Location | Curlie topic |
|----------|------|---------------|-----------|----------|--------------|
| docID A  | eng  | … text …      | YY-MM-DD  | …        | …            |
| docID B  | deu  | … text …      | YY-MM-DD  | …        | …            |
| …        | …    | … text …      | YY-MM-DD  | …        | …            |


## Mosaic (MOdular Search Application based on Index fraCtions)
Generic implementation of a vertical search engine (special search engine related to a specific domain or purpose, such as product search). Uses index shards from the OWI.
![Mosaic workflow](images/Mosaic_workflow.png)
**(Image taken from the Mosaic's web page: https://mosaic.ows.eu/)**

The Ciff file is imported and then converted to a Lucene index. The parquet key file is imported into a database. When a search happens the search is first performed over the Lucene index, then some further filtering is performed and the result is generated. So using all the additional metadata the result is available via rest API, which can be retrieved in a simple web interface or more complex application. 

# How to create a search application
![Generic schema of a search application](images/General_schema_of_a_search_application.png)
You have to define the use case and the purpose of the application and then you need to define the search domain. Based on the defined search domain you can download an index chart from the open web index, then you can import it into mosaic. After that you have basically an out of the box search engine with a specific index chart. Mosaic is a modular system that allows to include modules and filtering further metadata. For example, searching for certain topics you can create your own simple or not so simple web interface and in order to search in this index chart  you can create a more complex application. So you can use mosaic as a basic search system but then doing something further with the search research. 

### Data pipelines
**INDEX Pipeline**:
- OWILIX allows filtering based on language and curlie labels (a category used to classify websites such as `['geo', 'science', 'earth', 'bio']`);
- **Output files**: CIFF file and Parquet file (the one that contains metadata such as curlie labels and geolocation info);
- **Filters out web pages** with no EO (Earth Observation) keywords using **TaxoTagger** (a tagger that takes in input a text and returns a set of top 10 keywords, each one with a score). So if we have X documents, for each one we use the tagger that generates the keywords. If a webpage has at least one keyword that would score greater than a threshold we keep it otherwise we drop it.  
- **Having this filtered data, we use mosaic that takes CIFF + PARQUET files and it creates a Lucene index (a data structure that enables fast and efficient searching of large volumes of data)**.

# Useful resources and references
- Introductory video released by the Open Web Index on the potential of their project and how to create search applications using the Open Web Index. https://vimeo.com/1082089186
- OWI's platform to download their datasets: https://openwebindex.eu/owler/our_datasets
- GitLab like site with owi-cli's project presentation: https://opencode.it4i.eu/openwebsearcheu-public/owi-cli;
- Owi-cli's installation documentation (we suggest the number 1 method): https://openwebsearcheu-public.pages.it4i.eu/owi-cli/install


# Usage and installation
A Docker container for OWI (OWilIx) data processing applications with Python 3.11 and required scientific libraries.

## Overview
This container provides a lightweight environment for running OWI-related data processing tasks. It includes Python 3.11, essential system tools, and the required Python packages for working with OWI and LEXIS systems.

## Features
- **Base Image**: Python 3.11-slim (Debian Bookworm);
- **Minimal Footprint**: only essential packages installed;
- **Pre-configured Environment**: all required Python packages pre-installed;
- **Dataset Ready**: includes a dedicated dataset directory structure;

## Prerequisites
- Docker or Docker Desktop installed
- Git (for cloning the repository)
- Minimum 2GB RAM recommended

## Quick Start
### 1. Build the Container
```bash
# Clone the repository
git clone https://gitlab.com/giancarlopadoan-group/softplat-project.git
cd owi

# Build the Docker image
docker build -t owi-container .
```
### 2. Download the wanted dataset
```bash
# Example of command to download a specified dataset
root@dockerContainer:/app/dataset/# owilix remote pull all/internalID=3fad40fc-c68b-11f0-a6f8-f6a03915313d
```

OWI's datasets: https://openwebindex.eu/owler/our_datasets
OWIlix commands: https://openwebsearcheu-public.pages.it4i.eu/owi-cli/commands.html#command-overview