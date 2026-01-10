package it.unipd.search.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class ElasticsearchIndexInitializer {

    private final ElasticsearchClient client;

    public ElasticsearchIndexInitializer(ElasticsearchClient client) {
        this.client = client;
    }

    @PostConstruct
    public void init() throws IOException {
        createGuardianIndex();
        createOwiIndex();
    }

    private void createGuardianIndex() throws IOException {
        if (client.indices().exists(e -> e.index("guardian")).value()) {
            return;
        }

        client.indices().create(c -> c
                .index("guardian")
                .mappings(m -> m
                        .properties("id", p -> p.keyword(k -> k))
                        .properties("url", p -> p.keyword(k -> k))
                        .properties("title", p -> p.text(t -> t))
                        .properties("main_content", p -> p.text(t -> t))
                )
        );
    }

    private void createOwiIndex() throws IOException {
        if (client.indices().exists(e -> e.index("owi")).value()) {
            return;
        }

        client.indices().create(c -> c
                .index("owi")
                .mappings(m -> m
                        .properties("url", p -> p.keyword(k -> k))
                        .properties("title", p -> p.text(t -> t))
                        .properties("main_content", p -> p.text(t -> t))
                )
        );
    }
}
