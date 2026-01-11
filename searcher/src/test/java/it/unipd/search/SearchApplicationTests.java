package it.unipd.search;

import it.unipd.search.config.ElasticsearchIndexInitializer;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class SearchApplicationTests {

    @MockitoBean // or @MockBean for older Spring Boot versions
    private ElasticsearchIndexInitializer elasticsearchIndexInitializer;

    @Test
    void contextLoads() {
    }

}
