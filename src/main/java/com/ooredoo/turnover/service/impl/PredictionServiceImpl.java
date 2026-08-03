package com.ooredoo.turnover.service.impl;

import com.ooredoo.turnover.dto.PredictionRequestDTO;
import com.ooredoo.turnover.dto.PredictionResponseDTO;
import com.ooredoo.turnover.service.PredictionService;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;

import java.util.HashMap;
import java.util.Map;

@Service
public class PredictionServiceImpl implements PredictionService {

    private final RestTemplate restTemplate;

    public PredictionServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public PredictionResponseDTO predict(PredictionRequestDTO requestDTO) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("age", requestDTO.getAge());
        payload.put("overtime", requestDTO.getOvertime());
        payload.put("monthlyIncome", requestDTO.getMonthlyIncome());
        payload.put("distanceFromHome", requestDTO.getDistanceFromHome());
        payload.put("jobSatisfaction", requestDTO.getJobSatisfaction());
        payload.put("environmentSatisfaction", requestDTO.getEnvironmentSatisfaction());
        payload.put("yearsAtCompany", requestDTO.getYearsAtCompany());
        payload.put("totalWorkingYears", requestDTO.getTotalWorkingYears());
        payload.put("stockOptionLevel", requestDTO.getStockOptionLevel());
        payload.put("jobLevel", requestDTO.getJobLevel());
        payload.put("yearsInCurrentRole", requestDTO.getYearsInCurrentRole());
        payload.put("yearsWithCurrManager", requestDTO.getYearsWithCurrManager());
        payload.put("yearsSinceLastPromotion", requestDTO.getYearsSinceLastPromotion());
        payload.put("numCompaniesWorked", requestDTO.getNumCompaniesWorked());
        payload.put("trainingTimesLastYear", requestDTO.getTrainingTimesLastYear());
        payload.put("workLifeBalance", requestDTO.getWorkLifeBalance());
        payload.put("education", requestDTO.getEducation());
        payload.put("performanceRating", requestDTO.getPerformanceRating());
        payload.put("maritalStatus", requestDTO.getMaritalStatus());
        payload.put("businessTravel", requestDTO.getBusinessTravel());
        payload.put("jobRole", requestDTO.getJobRole());

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<PredictionResponseDTO> response = restTemplate.postForEntity(
                    "http://127.0.0.1:8001/predict",
                    request,
                    PredictionResponseDTO.class
            );
            if (response.getBody() != null) {
                return response.getBody();
            }
        } catch (ResourceAccessException ex) {
            ex.printStackTrace();
        }

        PredictionResponseDTO fallback = new PredictionResponseDTO();
        fallback.setModel("SVM");
        fallback.setProbability(0.0);
        fallback.setWillLeave(false);
        fallback.setRiskLevel("LOW");
        fallback.setMessage("Le service Python n’est pas accessible. Vérifiez la commande de lancement du modèle.");
        return fallback;
    }
}
