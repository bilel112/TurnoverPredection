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
        RecommendationResponseDTO response = parseAiResponse(aiResponse, employeeDTO, scoreResult);
        if (response == null) {
            response = buildFallbackResponse(employeeDTO, scoreResult, reasons);
        } else {
            response = enforceRecommendationPolicy(response, employeeDTO, scoreResult, reasons);
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
        List<String> expectedPriorities = determineRecommendationPriorities(employeeDTO, scoreResult);
        int expectedCount = expectedPriorities.size();
        sb.append(String.format("Génère exactement %d recommandations RH précises, avec un titre, une raison et une action concrète.\n", expectedCount));
        sb.append("Le nombre et la gravité des recommandations doivent refléter la vraie sévérité métier :\n");
        sb.append("- si le salaire est très bas, la satisfaction est faible, les heures supplémentaires sont élevées, ou le work-life balance est mauvais, une recommandation de priorité high est attendue\n");
        sb.append("- si le problème est modéré, utiliser medium\n");
        sb.append("- si le profil reste globalement stable, utiliser low\n");
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
        sb.append("N'ajoute jamais plus de recommandations que le niveau de risque ne l'exige.\n");
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

    private RecommendationResponseDTO parseAiResponse(String aiResponse, EmployeeDTO employeeDTO, DynamicTurnoverScoreResult scoreResult) {
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
            return enforceRecommendationPolicy(response, employeeDTO, scoreResult, List.of());
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

    List<String> determineRecommendationPriorities(EmployeeDTO employeeDTO, DynamicTurnoverScoreResult scoreResult) {
        if (scoreResult == null) {
            return List.of("low", "low", "low");
        }
        Integer score = scoreResult.getScore();
        String riskLevel = scoreResult.getRiskLevel();
        List<String> basePriorities;
        
        // Generate 3-5 recommendations based on risk level
        if ((score != null && score >= 70) || "HIGH".equalsIgnoreCase(riskLevel)) {
            // HIGH risk: 5 recommendations (low, medium, medium, high, high)
            basePriorities = new ArrayList<>(List.of("low", "medium", "medium", "high", "high"));
        } else if ((score != null && score >= 35) || "MEDIUM".equalsIgnoreCase(riskLevel)) {
            // MEDIUM risk: 4 recommendations (low, low, medium, medium)
            basePriorities = new ArrayList<>(List.of("low", "low", "medium", "medium"));
        } else {
            // LOW risk: 3 recommendations (all low)
            basePriorities = new ArrayList<>(List.of("low", "low", "low"));
        }

        String dominantPriority = inferDominantPriority(employeeDTO);
        if ("high".equals(dominantPriority)) {
            // Boost the last 1-2 priorities to "high" for very severe cases
            List<String> adjusted = new ArrayList<>(basePriorities);
            if (adjusted.size() > 0) {
                adjusted.set(adjusted.size() - 1, "high");
            }
            if (adjusted.size() > 1) {
                adjusted.set(adjusted.size() - 2, "high");
            }
            return adjusted;
        }
        if ("medium".equals(dominantPriority)) {
            // Boost the last 1-2 priorities to "medium" for moderate severity
            List<String> adjusted = new ArrayList<>(basePriorities);
            if (adjusted.size() > 0) {
                adjusted.set(adjusted.size() - 1, "medium");
            }
            if (adjusted.size() > 1 && !"high".equals(adjusted.get(adjusted.size() - 2))) {
                adjusted.set(adjusted.size() - 2, "medium");
            }
            return adjusted;
        }
        return basePriorities;
    }

    private String inferDominantPriority(EmployeeDTO employeeDTO) {
        if (employeeDTO == null) {
            return "low";
        }

        int severityScore = 0;
        if (employeeDTO.getMonthlyIncome() != null) {
            if (employeeDTO.getMonthlyIncome() < 2000) {
                severityScore += 4;
            } else if (employeeDTO.getMonthlyIncome() < 3500) {
                severityScore += 2;
            } else if (employeeDTO.getMonthlyIncome() < 5000) {
                severityScore += 1;
            }
        }
        if (employeeDTO.getJobSatisfaction() != null) {
            if (employeeDTO.getJobSatisfaction() <= 2) {
                severityScore += 3;
            } else if (employeeDTO.getJobSatisfaction() == 3) {
                severityScore += 2;
            } else if (employeeDTO.getJobSatisfaction() == 4) {
                severityScore += 1;
            }
        }
        if (employeeDTO.getEnvironmentSatisfaction() != null) {
            if (employeeDTO.getEnvironmentSatisfaction() <= 2) {
                severityScore += 2;
            } else if (employeeDTO.getEnvironmentSatisfaction() == 3) {
                severityScore += 1;
            }
        }
        if (employeeDTO.getWorkLifeBalance() != null) {
            if (employeeDTO.getWorkLifeBalance() <= 2) {
                severityScore += 2;
            } else if (employeeDTO.getWorkLifeBalance() == 3) {
                severityScore += 1;
            }
        }
        if (employeeDTO.getOvertime() != null && Boolean.TRUE.equals(employeeDTO.getOvertime())) {
            severityScore += 2;
        }
        if (employeeDTO.getYearsSinceLastPromotion() != null && employeeDTO.getYearsSinceLastPromotion() >= 3) {
            severityScore += 1;
        }
        if (employeeDTO.getStockOptionLevel() != null && employeeDTO.getStockOptionLevel() <= 0) {
            severityScore += 1;
        }

        if (severityScore >= 7) {
            return "high";
        }
        if (severityScore >= 3) {
            return "medium";
        }
        return "low";
    }

    private RecommendationResponseDTO enforceRecommendationPolicy(RecommendationResponseDTO response, EmployeeDTO employeeDTO, DynamicTurnoverScoreResult scoreResult, List<String> reasons) {
        if (response == null) {
            return response;
        }

        List<String> expectedPriorities = determineRecommendationPriorities(employeeDTO, scoreResult);
        List<RecommendationItemDTO> recommendations = response.getRecommendations() == null
                ? new ArrayList<>()
                : new ArrayList<>(response.getRecommendations());

        while (recommendations.size() < expectedPriorities.size()) {
            recommendations.add(buildRecommendationForPriority(expectedPriorities.get(recommendations.size()), employeeDTO, scoreResult, reasons));
        }
        if (recommendations.size() > expectedPriorities.size()) {
            recommendations = new ArrayList<>(recommendations.subList(0, expectedPriorities.size()));
        }

        for (int i = 0; i < recommendations.size(); i++) {
            RecommendationItemDTO item = recommendations.get(i);
            if (item == null) {
                item = buildRecommendationForPriority(expectedPriorities.get(i), employeeDTO, scoreResult, reasons);
                recommendations.set(i, item);
            }
            item.setPriority(normalizePriority(item.getPriority(), expectedPriorities.get(i)));
            if (item.getTitle() == null || item.getTitle().isBlank()) {
                item.setTitle(defaultTitleForPriority(expectedPriorities.get(i), employeeDTO));
            }
            if (item.getReason() == null || item.getReason().isBlank()) {
                item.setReason(defaultReasonForPriority(expectedPriorities.get(i), employeeDTO, scoreResult));
            }
            if (item.getAction() == null || item.getAction().isBlank()) {
                item.setAction(defaultActionForPriority(expectedPriorities.get(i), employeeDTO));
            }
        }

        response.setRecommendations(recommendations);
        response.setSummary(buildSummary(scoreResult, recommendations));
        response.setAiSummary(buildAiSummary(scoreResult, recommendations));
        return response;
    }

    private String normalizePriority(String priority, String defaultPriority) {
        // Force the expected priority; ignore what the AI said
        // This ensures each recommendation has exactly the priority we calculated
        return defaultPriority;
    }

    private RecommendationItemDTO buildRecommendationForPriority(String priority, EmployeeDTO employeeDTO, DynamicTurnoverScoreResult scoreResult, List<String> reasons) {
        RecommendationItemDTO item = new RecommendationItemDTO();
        item.setPriority(priority);
        item.setTitle(defaultTitleForPriority(priority, employeeDTO));
        item.setReason(defaultReasonForPriority(priority, employeeDTO, scoreResult));
        item.setAction(defaultActionForPriority(priority, employeeDTO));
        return item;
    }

    private String defaultTitleForPriority(String priority, EmployeeDTO employeeDTO) {
        if (employeeDTO != null && employeeDTO.getMonthlyIncome() != null && employeeDTO.getMonthlyIncome() < 2000) {
            return "Révision salariale prioritaire";
        }
        if (employeeDTO != null && employeeDTO.getJobSatisfaction() != null && employeeDTO.getJobSatisfaction() <= 2) {
            return "Amélioration de la satisfaction au travail";
        }
        if (employeeDTO != null && Boolean.TRUE.equals(employeeDTO.getOvertime())) {
            return "Réduction des heures supplémentaires";
        }
        return switch (priority) {
            case "high" -> "Entretien RH prioritaire";
            case "medium" -> "Suivi manager renforcé";
            default -> "Action de rétention simple";
        };
    }

    private String defaultReasonForPriority(String priority, EmployeeDTO employeeDTO, DynamicTurnoverScoreResult scoreResult) {
        String riskLabel = scoreResult != null && scoreResult.getRiskLabel() != null ? scoreResult.getRiskLabel() : "risque non défini";
        if (employeeDTO != null && employeeDTO.getMonthlyIncome() != null && employeeDTO.getMonthlyIncome() < 2000) {
            return "Le salaire actuel est très bas par rapport au marché, ce qui augmente le risque de départ.";
        }
        if (employeeDTO != null && employeeDTO.getJobSatisfaction() != null && employeeDTO.getJobSatisfaction() <= 2) {
            return "La satisfaction au travail est faible, ce qui peut entraîner un désengagement progressif.";
        }
        if (employeeDTO != null && Boolean.TRUE.equals(employeeDTO.getOvertime())) {
            return "Les heures supplémentaires fréquentes peuvent générer un épuisement professionnel et un risque de départ.";
        }
        return switch (priority) {
            case "high" -> "Le profil présente un risque élevé de départ et mérite une action rapide.";
            case "medium" -> "Le profil montre des signaux intermédiaires qui appellent un suivi ciblé.";
            default -> "Le profil est globalement stable, mais une action de prévention reste utile pour le maintenir.";
        } + " Niveau actuel : " + riskLabel + ".";
    }

    private String defaultActionForPriority(String priority, EmployeeDTO employeeDTO) {
        if (employeeDTO != null && employeeDTO.getMonthlyIncome() != null && employeeDTO.getMonthlyIncome() < 2000) {
            return "Évaluer une augmentation salariale ou une prime de rétention dans le prochain cycle RH.";
        }
        if (employeeDTO != null && employeeDTO.getJobSatisfaction() != null && employeeDTO.getJobSatisfaction() <= 2) {
            return "Mettre en place un entretien RH régulier et proposer des ajustements de mission ou de management.";
        }
        if (employeeDTO != null && Boolean.TRUE.equals(employeeDTO.getOvertime())) {
            return "Réorganiser les priorités et réduire les heures supplémentaires pour améliorer l’équilibre travail-vie personnelle.";
        }
        return switch (priority) {
            case "high" -> "Planifier un entretien RH dans les 7 prochains jours et définir un plan de rétention.";
            case "medium" -> "Organiser un suivi managérial et un échange sur les conditions de travail.";
            default -> "Maintenir un suivi régulier et valoriser les points de stabilité du poste.";
        };
    }

    private String buildSummary(DynamicTurnoverScoreResult scoreResult, List<RecommendationItemDTO> recommendations) {
        int count = recommendations == null ? 0 : recommendations.size();
        String label = scoreResult != null && scoreResult.getRiskLabel() != null ? scoreResult.getRiskLabel() : "non défini";
        return String.format("Recommandations générées pour un risque %s (%d action%s).", label, count, count > 1 ? "s" : "");
    }

    private String buildAiSummary(DynamicTurnoverScoreResult scoreResult, List<RecommendationItemDTO> recommendations) {
        int count = recommendations == null ? 0 : recommendations.size();
        String label = scoreResult != null && scoreResult.getRiskLabel() != null ? scoreResult.getRiskLabel() : "non défini";
        return String.format("Le niveau de risque %s a conduit à %d recommandation%s adaptée%s au contexte RH.", label, count, count > 1 ? "s" : "", count > 1 ? "s" : "");
    }

    private RecommendationResponseDTO buildFallbackResponse(EmployeeDTO employeeDTO, DynamicTurnoverScoreResult scoreResult, List<String> reasons) {
        RecommendationResponseDTO response = new RecommendationResponseDTO();
        response.setSummary("Impossible d'obtenir une réponse de l'IA. Voici des recommandations basées sur les règles internes.");
        response.setAiSummary("Échec de l'appel OpenRouter, recommandations basées sur les règles internes.");
        List<String> expectedPriorities = determineRecommendationPriorities(employeeDTO, scoreResult);
        List<RecommendationItemDTO> recommendations = new ArrayList<>();
        for (String priority : expectedPriorities) {
            recommendations.add(buildRecommendationForPriority(priority, employeeDTO, scoreResult, reasons));
        }
        response.setRecommendations(recommendations);
        return response;
    }
}
