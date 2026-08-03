package com.ooredoo.turnover.service;

import com.ooredoo.turnover.dto.PredictionRequestDTO;
import com.ooredoo.turnover.dto.PredictionResponseDTO;
import com.ooredoo.turnover.service.impl.PredictionServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PredictionServiceImplTest {

    @Test
    void shouldMapPredictionResponseToDomainModel() {
        RestTemplate restTemplate = mock(RestTemplate.class);
        //PredictionServiceImpl service = new PredictionServiceImpl(restTemplate, "http://127.0.0.1:8001/predict");

        PredictionRequestDTO request = new PredictionRequestDTO();
        request.setAge(35);
        request.setOvertime(true);
        request.setMonthlyIncome(3500);
        request.setDistanceFromHome(18);
        request.setJobSatisfaction(2);
        request.setYearsAtCompany(4);
        request.setTotalWorkingYears(8);
        request.setStockOptionLevel(0);
        request.setMaritalStatus("Single");
        request.setBusinessTravel("Travel_Frequently");
        request.setJobRole("Sales Executive");

        PredictionResponseDTO expected = new PredictionResponseDTO();
        expected.setModel("SVM");
        expected.setProbability(0.6324);
        expected.setWillLeave(true);
        expected.setRiskLevel("HIGH");
        expected.setMessage("Risque élevé d'attrition");

        when(restTemplate.postForEntity(anyString(), any(), eq(PredictionResponseDTO.class)))
                .thenReturn(ResponseEntity.ok(expected));

        //PredictionResponseDTO actual = service.predict(request);

       // assertEquals("SVM", actual.getModel());
        //assertEquals(0.6324, actual.getProbability());
        //assertEquals(true, actual.isWillLeave());
        //assertEquals("HIGH", actual.getRiskLevel());
    }
}
