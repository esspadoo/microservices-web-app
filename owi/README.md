The following command provide a sequencial way to download a dataset using owilix-cli from the owi database, and to export the database as a JsonL file.

[NOTE] <br />
**A specific dataset denoted with internalID=XX has to be choosen, here two smalls datasets to test (20MB and 274.1KB, both "curlie_full" and "public") are provided**

## USAGE

- **Start container with attached shell** <br />
  `sudo docker compose run --entrypoint ../bin/bash owilix`

- **Pull the raw dataset (first is the miss-labeled one, the second is the correct-labeled one)** <br />
  `owilix --yes remote pull all/internalID=f79bf6c8-52fe-11f0-a4a5-528c047b29ff num_threads=10` <br />
  `owilix --yes remote pull all/internalID=fc4f5c20-ca02-11f0-a6f8-f6a03915313d num_threads=10`

- **Make a query selecting curlielabels_en IS NOT NULL: MISS-LABELED DATASET**<br />
  `owilix query less --local all/internalID=f79bf6c8-52fe-11f0-a4a5-528c047b29ff "select=url,curlielabels_en,curlielabels" "where=curlielabels_en is not NULL"`<br />

- **Make a query selecting curlielabels_en IS NOT NULL: CORRECT-LABELED DATASET**<br />
  `owilix query less --local all/internalID=fc4f5c20-ca02-11f0-a6f8-f6a03915313d "select=url,curlielabels_en,curlielabels" "where=curlielabels is not NULL"`<br />

- **Make a query selecting topic-related curlielabels (in this case "Computers..."): MISS-LABELED DATASET**<br />
  `owilix query less --local all/internalID=f79bf6c8-52fe-11f0-a4a5-528c047b29ff "select=url,curlielabels_en, curlielabels" "where=length(list_filter(curlielabels_en, x -> x LIKE 'Computers%')) > 0"`<br />

- **Make a query selecting topic-related curlielabels (in this case "Computers..."): CORRECT-LABELED DATASET)**<br />
  `owilix query less --local all/internalID=fc4f5c20-ca02-11f0-a6f8-f6a03915313d "select=url,curlielabels_en, curlielabels" "where=length(list_filter(curlielabels_en, x -> x LIKE 'Computers%')) > 0"`<br />


[NOT NEEDED, USED FOR FUTURE IMPLEMENTATIONS]
- **OWILIX, export like a single CIFF file** <br />
  `owilix query less --local all/internalID=f79bf6c8-52fe-11f0-a4a5-528c047b29ff as_json=True json_file=$PWD/all_data/json_out.json`<br />

- **Slicing of the selected owilix's dataset(s). We want to have only the pages with curlielabels_en=Computers... (after Computers there can be everything)** <br />
  `owilix query slice --local all/internalID=fc4f5c20-ca02-11f0-a6f8-f6a03915313d "where=length(list_filter(curlielabels_en, x -> x LIKE 'Computers%')) > 0" collection_name=provaSlicing`








