FROM docker.elastic.co/elasticsearch/elasticsearch:8.2.0
RUN bin/elasticsearch-plugin install analysis-nori