package com.ooredoo.turnover.service.impl;

import com.ooredoo.turnover.config.DynamicScoringProperties;
import com.ooredoo.turnover.entity.AlertConfig;
import com.ooredoo.turnover.repository.AlertConfigRepository;
import com.ooredoo.turnover.service.AlertConfigService;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AlertConfigServiceImpl implements AlertConfigService {

    private final AlertConfigRepository alertConfigRepository;
    private final DynamicScoringProperties scoringProperties;

    public AlertConfigServiceImpl(AlertConfigRepository alertConfigRepository,
                                  DynamicScoringProperties scoringProperties) {
        this.alertConfigRepository = alertConfigRepository;
        this.scoringProperties = scoringProperties;
    }

    @PostConstruct
    public void init() {
        AlertConfig config = getConfig();
        applyConfigToProperties(config);
    }

    @Override
    public AlertConfig getConfig() {
        List<AlertConfig> allConfigs = alertConfigRepository.findAll();
        if (!allConfigs.isEmpty()) {
            return allConfigs.get(0);
        }
        return createDefaultConfig();
    }

    @Override
    public AlertConfig updateConfig(AlertConfig config) {
        AlertConfig existing = getConfig();
        existing.setHighThreshold(config.getHighThreshold());
        existing.setMediumThreshold(config.getMediumThreshold());
        existing.setTrendDelta(config.getTrendDelta());
        existing.setEvaluationIntervalMs(config.getEvaluationIntervalMs());
        AlertConfig saved = alertConfigRepository.save(existing);
        applyConfigToProperties(saved);
        return saved;
    }

    private AlertConfig createDefaultConfig() {
        AlertConfig defaultConfig = AlertConfig.builder()
                .highThreshold(scoringProperties.getHighThreshold())
                .mediumThreshold(scoringProperties.getMediumThreshold())
                .trendDelta(scoringProperties.getTrendDelta())
                .evaluationIntervalMs(scoringProperties.getEvaluationIntervalMs())
                .build();
        return alertConfigRepository.save(defaultConfig);
    }

    private void applyConfigToProperties(AlertConfig config) {
        scoringProperties.setHighThreshold(config.getHighThreshold());
        scoringProperties.setMediumThreshold(config.getMediumThreshold());
        scoringProperties.setTrendDelta(config.getTrendDelta());
        scoringProperties.setEvaluationIntervalMs(config.getEvaluationIntervalMs());
    }
}
