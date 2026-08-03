package com.ooredoo.turnover.service;

import com.ooredoo.turnover.entity.AlertConfig;

public interface AlertConfigService {
    AlertConfig getConfig();
    AlertConfig updateConfig(AlertConfig config);
}
