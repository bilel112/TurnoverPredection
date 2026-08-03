package com.ooredoo.turnover.service;

import com.ooredoo.turnover.dto.PredictionRequestDTO;
import com.ooredoo.turnover.dto.PredictionResponseDTO;

public interface PredictionService {
    PredictionResponseDTO predict(PredictionRequestDTO requestDTO);
}
