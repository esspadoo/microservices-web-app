
To create a single node deployment of Elasticsearch:
```bash
docker run -p 127.0.0.1:9200:9200 -d --name elasticsearch \
  -e "discovery.type=single-node" \
  -e "xpack.security.enabled=false" \
  -e "xpack.license.self_generated.type=trial" \
  -v "elasticsearch-data:/usr/share/elasticsearch/data" \
  docker.elastic.co/elasticsearch/elasticsearch:8.15.0
```

Note that the above command starts the service with **authentication and encryption disabled**, which means that anyone who connects to the service will be given access.

# Create an index
An index is a collection of documents that share similar characteristics. An index in elasticsearch is comparable to a database in a relational database system but it is optimized to store and query high amounts of textual and structured data.