package com.ooredoo.turnover.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ooredoo.turnover.dto.EmployeeDTO;
import com.ooredoo.turnover.dto.RecommendationItemDTO;
import com.ooredoo.turnover.dto.RecommendationResponseDTO;
import com.ooredoo.turnover.entity.Employee;
import com.ooredoo.turnover.service.DynamicTurnoverScoreResult;
import com.ooredoo.turnover.service.DynamicTurnoverScoringService;
import com.ooredoo.turnover.service.EmployeeService;
import com.ooredoo.turnover.service.RecommendationService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RecommendationServiceImpl implements RecommendationService {

    private final RestTemplate restTemplate;
    private final DynamicTurnoverScoringService scoringService;
    private final EmployeeService employeeService;
    private final String openRouterUrl;
    private final String openRouterKey;
    private final String modelName;
    private final ObjectMapper objectMapper;

    public RecommendationServiceImpl(RestTemplate restTemplate,
                                     DynamicTurnoverScoringService scoringService,
                                     EmployeeService employeeService,
                                     @Value("${openrouter.api.url}") String openRouterUrl,
                                     @Value("${openrouter.api.key}") String openRouterKey,
                                     @Value("${openrouter.api.model}") String modelName) {
        this.restTemplate = restTemplate;
        this.scoringService = scoringService;
        this.employeeService = employeeService;
        this.openRouterUrl = openRouterUrl;
        this.openRouterKey = openRouterKey;
        this.modelName = modelName;
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public RecommendationResponseDTO generateRecommendationsForEmployee(Long employeeId) {
        EmployeeDTO employeeDTO = employeeService.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + employeeId));

        // Build score and reasons
        DynamicTurnoverScoreResult scoreResult = scoringService.calculateScore(toEmployee(employeeDTO));
        List<String> reasons = scoreResult.getReasons();

        // Build prompt context
        String prompt = buildPrompt(employeeDTO, scoreResult, reasons);

        // Call OpenRouter
        String aiResponse = callOpenRouter(prompt);

        // Parse AI response
        RecommendationResponseDTO response = parseAiResponse(aiResponse);
        if (response == null) {
            response = buildFallbackResponse(employeeDTO, scoreResult, reasons);
        }

        response.setEmployeeId(employeeId);
        response.setScore(scoreResult.getScore());
        response.setRiskLevel(scoreResult.getRiskLevel());
        response.setRiskLabel(scoreResult.getRiskLabel());

        return response;
    }

    private Employee toEmployee(EmployeeDTO dto) {
        Employee employee = new Employee();
        employee.setId(dto.getId());
        employee.setEmployeeNumber(dto.getEmployeeNumber());
        employee.setAge(dto.getAge());
        employee.setDepartment(dto.getDepartment());
        employee.setJobRole(dto.getJobRole());
        employee.setMonthlyIncome(dto.getMonthlyIncome());
        employee.setYearsAtCompany(dto.getYearsAtCompany());
        employee.setDistanceFromHome(dto.getDistanceFromHome());
        employee.setOvertime(dto.getOvertime());
        employee.setJobSatisfaction(dto.getJobSatisfaction());
        employee.setEnvironmentSatisfaction(dto.getEnvironmentSatisfaction());
        employee.setAttrition(dto.getAttrition());
        employee.setBusinessTravel(dto.getBusinessTravel());
        employee.setMaritalStatus(dto.getMaritalStatus());
        employee.setJobLevel(dto.getJobLevel());
        employee.setTotalWorkingYears(dto.getTotalWorkingYears());
        employee.setYearsInCurrentRole(dto.getYearsInCurrentRole());
        employee.setYearsWithCurrManager(dto.getYearsWithCurrManager());
        employee.setYearsSinceLastPromotion(dto.getYearsSinceLastPromotion());
        employee.setStockOptionLevel(dto.getStockOptionLevel());
        employee.setNumCompaniesWorked(dto.getNumCompaniesWorked());
        employee.setTrainingTimesLastYear(dto.getTrainingTimesLastYear());
        employee.setWorkLifeBalance(dto.getWorkLifeBalance());
        employee.setEducationField(dto.getEducationField());
        employee.setEducation(dto.getEducation());
        employee.setDailyRate(dto.getDailyRate());
        employee.setHourlyRate(dto.getHourlyRate());
        employee.setPercentSalaryHike(dto.getPercentSalaryHike());
        employee.setPerformanceRating(dto.getPerformanceRating());
        employee.setGender(dto.getGender());
        return employee;
    }

    private String buildPrompt(EmployeeDTO employeeDTO, DynamicTurnoverScoreResult scoreResult, List<String> reasons) {
        StringBuilder sb = new StringBuilder();
        sb.append("Tu es un assistant RH spécialisé dans la rétention des talents.\n");
        sb.append("Tu as les informations d'un employé et ses raisons de risque de départ.\n");
        sb.append("Génère 3 à 5 recommandations RH précises, avec un titre, une raison et une action concrète.\n");
        sb.append("Réponds en JSON avec les champs : summary, aiSummary, recommendations[].\n\n");

        sb.append("Contexte de l'employé :\n");
        sb.append(String.format("- ID : %s\n", employeeDTO.getId()));
        sb.append(String.format("- Département : %s\n", employeeDTO.getDepartment()));
        sb.append(String.format("- Poste : %s\n", employeeDTO.getJobRole()));
        sb.append(String.format("- Salaire mensuel : %s\n", employeeDTO.getMonthlyIncome()));
        sb.append(String.format("- Ancienneté (années) : %s\n", employeeDTO.getYearsAtCompany()));
        sb.append(String.format("- Satisfaction travail : %s\n", employeeDTO.getJobSatisfaction()));
        sb.append(String.format("- Satisfaction environnement : %s\n", employeeDTO.getEnvironmentSatisfaction()));
        sb.append(String.format("- Heures supplémentaires : %s\n", employeeDTO.getOvertime()));
        sb.append(String.format("- Dernière promotion : %s années\n", employeeDTO.getYearsSinceLastPromotion()));
        sb.append(String.format("- Options : %s\n", employeeDTO.getStockOptionLevel()));
        sb.append(String.format("- Nombre d'entreprises travaillées : %s\n", employeeDTO.getNumCompaniesWorked()));
        sb.append(String.format("- Mobilité interne / business travel : %s\n", employeeDTO.getBusinessTravel()));
        sb.append(String.format("- Conditions de vie / work-life balance : %s\n", employeeDTO.getWorkLifeBalance()));
        sb.append(String.format("- Performance : %s\n", employeeDTO.getPerformanceRating()));
        sb.append(String.format("- Formation dernière année : %s\n", employeeDTO.getTrainingTimesLastYear()));
        sb.append(String.format("- État marital : %s\n", employeeDTO.getMaritalStatus()));
        sb.append(String.format("- Niveau d'études : %s\n", employeeDTO.getEducationField()));
        sb.append(String.format("- Genre : %s\n", employeeDTO.getGender()));
        sb.append("\n");

        sb.append(String.format("Score de risque : %s (%s)\n", scoreResult.getScore(), scoreResult.getRiskLabel()));
        sb.append("Raisons du score :\n");
        for (String reason : reasons) {
            sb.append(String.format("- %s\n", reason));
        }
        sb.append("\n");
        sb.append("Sors uniquement un objet JSON valide. Ne renvoie pas de texte explicatif hors JSON.\n");
        sb.append("Ne mets pas de bloc de code Markdown, pas de backticks, pas de texte hors du JSON.\n");
        sb.append("Si tu ne peux pas générer 3 recommandations, renvoie au moins deux recommandations.\n");
        return sb.toString();
    }

    private String callOpenRouter(String prompt) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(openRouterKey);
        headers.add("X-OpenRouter-Title", "Turnover HR Assistant");
        headers.add("HTTP-Referer", "http://localhost:8081");

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> body = new HashMap<>();
        body.put("model", modelName);
        body.put("messages", List.of(message));
        body.put("max_tokens", 700);
        body.put("temperature", 0.0);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(openRouterUrl, request, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode root = objectMapper.readTree(response.getBody());
                JsonNode contentNode = root.path("choices").get(0).path("message").path("content");
                return contentNode.isTextual() ? contentNode.asText() : null;
            }
        } catch (HttpStatusCodeException httpEx) {
            System.err.println("OpenRouter API error response: " + httpEx.getStatusCode());
            System.err.println(httpEx.getResponseBodyAsString());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }

    private RecommendationResponseDTO parseAiResponse(String aiResponse) {
        if (aiResponse == null || aiResponse.isBlank()) {
            return null;
        }

        String normalized = normalizeAiResponse(aiResponse);
        if (normalized.isBlank()) {
            return null;
        }

        try {
            JsonNode root = objectMapper.readTree(normalized);
            RecommendationResponseDTO response = new RecommendationResponseDTO();
            response.setSummary(getTextValue(root, "summary", "Résumé non disponible."));
            response.setAiSummary(getTextValue(root, "aiSummary", response.getSummary()));

            JsonNode recs = findRecommendationsNode(root);
            List<RecommendationItemDTO> recommendations = new ArrayList<>();
            if (recs != null && recs.isArray()) {
                for (JsonNode rec : recs) {
                    RecommendationItemDTO item = new RecommendationItemDTO();
                    item.setPriority(getTextValue(rec, "priority", getTextValue(rec, "importance", "medium")).toLowerCase());
                    item.setTitle(getTextValue(rec, "title", getTextValue(rec, "name", "Recommendation")));
                    item.setReason(getTextValue(rec, "reason", getTextValue(rec, "description", "")));
                    item.setAction(getTextValue(rec, "action", getTextValue(rec, "recommendation", "")));
                    recommendations.add(item);
                }
            }
            response.setRecommendations(recommendations);
            return response;
        } catch (Exception ex) {
            System.err.println("Unable to parse AI response as JSON. Raw response:\n" + aiResponse);
            ex.printStackTrace();
        }
        return null;
    }

    private String getTextValue(JsonNode node, String fieldName, String defaultValue) {
        JsonNode valueNode = node.path(fieldName);
        if (valueNode.isTextual()) {
            return valueNode.asText();
        }
        if (valueNode.isNumber()) {
            return valueNode.asText();
        }
        return defaultValue;
    }

    private JsonNode findRecommendationsNode(JsonNode root) {
        JsonNode recommendationsNode = root.path("recommendations");
        if (recommendationsNode.isMissingNode() || recommendationsNode.isNull()) {
            recommendationsNode = root.path("recommendation");
        }
        if (recommendationsNode.isMissingNode() || recommendationsNode.isNull()) {
            recommendationsNode = root.path("results");
        }
        return recommendationsNode.isMissingNode() ? null : recommendationsNode;
    }

    private String normalizeAiResponse(String aiResponse) {
        String cleaned = aiResponse.replaceAll("(?s)```json\\s*", "");
        cleaned = cleaned.replaceAll("(?s)```", "");
        cleaned = cleaned.replace("`", "").trim();

        int start = cleaned.indexOf('{');
        int end = cleaned.lastIndexOf('}');
        if (start >= 0 && end > start) {
            cleaned = cleaned.substring(start, end + 1);
        }
        return cleaned.trim();
    }

    private RecommendationResponseDTO buildFallbackResponse(EmployeeDTO employeeDTO, DynamicTurnoverScoreResult scoreResult, List<String> reasons) {
        RecommendationResponseDTO response = new RecommendationResponseDTO();
        response.setSummary("Impossible d'obtenir une réponse de l'IA. Voici des recommandations basées sur les règles internes.");
        response.setAiSummary("Échec de l'appel OpenRouter, recommandations basées sur les règles internes.");
        response.setRecommendations(List.of(
                new RecommendationItemDTO("high", "Entretien RH recommandé", "Faible satisfaction et risque élevé.", "Planifier un entretien RH dans les 7 prochains jours."),
                new RecommendationItemDTO("high", "Revue salariale ou prime de rétention", "Salaire bas détecté.", "Évaluer une augmentation ou une prime ciblée."),
                new RecommendationItemDTO("medium", "Accompagnement managérial", "Heures supplémentaires et environnement faible.", "Mettre en place un suivi régulier avec le manager.")));
        return response;
    }
}
