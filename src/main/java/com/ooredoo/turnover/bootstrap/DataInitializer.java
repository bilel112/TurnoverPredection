package com.ooredoo.turnover.bootstrap;

import com.ooredoo.turnover.entity.Employee;
import com.ooredoo.turnover.repository.EmployeeRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final EmployeeRepository employeeRepository;

    public DataInitializer(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    public void run(String... args) throws Exception {

        if (employeeRepository.count() > 0) {
            log.info("Données déjà présentes ({} employés)", employeeRepository.count());
            return;
        }

        log.info("Début de l'import du dataset IBM HR Attrition (séparateur ;) ...");

        int imported = 0;
        int skipped = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(getClass().getResourceAsStream("/data/ibm_hr_attrition.csv")),
                        StandardCharsets.UTF_8))) {

            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                // Utilisation du point-virgule
                String[] values = line.split(";", -1); // -1 pour garder les champs vides

                if (values.length < 20) {
                    skipped++;
                    continue;
                }

                // ... (partie haute identique)

                try {
                    Employee emp = new Employee();

                    emp.setEmployeeNumber(parseInt(values[9]));
                    emp.setAge(parseInt(values[0]));
                    emp.setAttrition("Yes".equalsIgnoreCase(values[1].trim()));
                    emp.setDepartment(values[4].trim());
                    emp.setDistanceFromHome(parseInt(values[5]));
                    emp.setJobRole(values[15].trim());
                    emp.setMonthlyIncome(parseDouble(values[18]));
                    emp.setOvertime("Yes".equalsIgnoreCase(values[22].trim()));
                    emp.setYearsAtCompany(parseInt(values[31]));

                    emp.setEnvironmentSatisfaction(parseInt(values[10]));
                    emp.setJobSatisfaction(parseInt(values[16]));

                    // Nouveaux champs importants
                    emp.setBusinessTravel(values[2].trim());
                    emp.setMaritalStatus(values[17].trim());
                    emp.setJobLevel(parseInt(values[14]));
                    emp.setTotalWorkingYears(parseInt(values[28]));
                    emp.setYearsInCurrentRole(parseInt(values[32]));
                    emp.setYearsWithCurrManager(parseInt(values[34]));
                    emp.setYearsSinceLastPromotion(parseInt(values[33]));
                    emp.setStockOptionLevel(parseInt(values[27]));
                    emp.setNumCompaniesWorked(parseInt(values[20]));
                    emp.setTrainingTimesLastYear(parseInt(values[29]));
                    emp.setWorkLifeBalance(parseInt(values[30]));
                    emp.setEducationField(values[7].trim());
                    emp.setEducation(parseInt(values[6]));
                    emp.setGender(values[11].trim());

                    // Optionnels
                    emp.setDailyRate(parseInt(values[3]));
                    emp.setHourlyRate(parseInt(values[12]));
                    emp.setPercentSalaryHike(parseInt(values[23]));
                    emp.setPerformanceRating(parseInt(values[24]));

                    employeeRepository.save(emp);

                    imported++;
                    if (imported % 300 == 0) {
                        log.info("{} employés importés...", imported);
                    }

                } catch (Exception e) {
                    skipped++;
                }
            }

            log.info("Import terminé ! {} employés importés | {} lignes ignorées", imported, skipped);

        } catch (Exception e) {
            log.error("Erreur critique pendant l'import", e);
        }
    }

    private Integer parseInt(String s) {
        try {
            return s != null && !s.trim().isEmpty() ? Integer.parseInt(s.trim()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseDouble(String s) {
        try {
            return s != null && !s.trim().isEmpty() ? Double.parseDouble(s.trim()) : null;
        } catch (Exception e) {
            return null;
        }
    }
}