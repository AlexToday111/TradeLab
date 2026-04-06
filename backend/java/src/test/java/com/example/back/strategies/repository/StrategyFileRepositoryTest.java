package com.example.back.strategies.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.back.strategies.entity.StrategyFileEntity;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest(
    properties = {
        "spring.datasource.url=jdbc:h2:mem:strategies;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;NON_KEYWORDS=INTERVAL",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
    }
)
class StrategyFileRepositoryTest {

    @Autowired
    private StrategyFileRepository strategyFileRepository;

    @Test
    void savesAndLoadsStrategyFile() {
        StrategyFileEntity entity = new StrategyFileEntity();
        entity.setName("EMA");
        entity.setFileName("ema.py");
        entity.setStoragePath("/tmp/ema.py");
        entity.setStatus(StrategyFileEntity.StrategyStatus.VALID);
        entity.setParametersSchemaJson("{\"period\":{\"type\":\"integer\"}}");
        entity.setCreatedAt(Instant.parse("2024-01-01T00:00:00Z"));

        StrategyFileEntity saved = strategyFileRepository.save(entity);

        assertThat(strategyFileRepository.findById(saved.getId())).isPresent();
        assertThat(strategyFileRepository.findById(saved.getId()).orElseThrow().getFileName()).isEqualTo("ema.py");
    }
}
