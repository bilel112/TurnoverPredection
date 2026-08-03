package com.ooredoo.turnover.controller;

import com.ooredoo.turnover.dto.PredictionRequestDTO;
import com.ooredoo.turnover.dto.PredictionResponseDTO;
import com.ooredoo.turnover.service.PredictionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/predict")
@CrossOrigin(origins = "*")
public class PredictionController {

    private final PredictionService predictionService;

    public PredictionController(PredictionService predictionService) {
        this.predictionService = predictionService;
    }

    @PostMapping
    public ResponseEntity<PredictionResponseDTO> predict(@RequestBody PredictionRequestDTO requestDTO) {
        return ResponseEntity.ok(predictionService.predict(requestDTO));
    }
}
