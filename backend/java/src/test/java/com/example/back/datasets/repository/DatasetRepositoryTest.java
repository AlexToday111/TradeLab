package com.example.back.datasets.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.back.datasets.entity.DatasetEntity;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

@DataJpaTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:datasets;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=INTERVAL",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
    }
)
class DatasetRepositoryTest {

    @Autowired
    private DatasetRepository datasetRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    void findAllByOrderByCreatedAtDescReturnsNewestFirst() throws Exception {
        entityManager.persist(dataset("dataset-1", "First"));
        entityManager.flush();
        Thread.sleep(5L);
        entityManager.persist(dataset("dataset-2", "Second"));
        entityManager.flush();

        var datasets = datasetRepository.findAllByOrderByCreatedAtDesc();

        assertThat(datasets).extracting(DatasetEntity::getId).containsExactly("dataset-2", "dataset-1");
    }

    private DatasetEntity dataset(String id, String name) {
        DatasetEntity entity = new DatasetEntity();
        entity.setId(id);
        entity.setName(name);
        entity.setPayload("{\"id\":\"" + id + "\",\"name\":\"" + name + "\"}");
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());
        return entity;
    }
}
