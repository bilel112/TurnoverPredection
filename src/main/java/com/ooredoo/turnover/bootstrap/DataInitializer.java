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
        // Ne pas ré-importer si la table contient déjà des données
        if (employeeRepository.count() > 0) {
            log.info("Les données employés sont déjà présentes ({})", employeeRepository.count());
            return;
        }

        log.info("Début de l'import du dataset IBM HR Attrition...");

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(
                        Objects.requireNonNull(getClass().getResourceAsStream("/data/dataIBM.csv")),
                        StandardCharsets.UTF_8))) {

            String line;
            boolean isHeader = true;
            int imported = 0;

            while ((line = reader.readLine()) != null) {
                if (isHeader) {
                    isHeader = false;
                    continue; // sauter l'en-tête
                }

                String[] values = line.split(",");

                if (values.length < 10) continue; // ligne invalide

                try {
                    Employee employee = new Employee();

                    employee.setEmployeeNumber(Integer.parseInt(values[0].trim())); // EmployeeNumber
                    employee.setAge(Integer.parseInt(values[1].trim()));           // Age
                    employee.setAttrition("Yes".equalsIgnoreCase(values[2].trim())); // Attrition
                    employee.setDepartment(values[4].trim());                       // Department
                    employee.setDistanceFromHome(Integer.parseInt(values[5].trim()));
                    employee.setJobRole(values[8].trim());                         // JobRole
                    employee.setMonthlyIncome(Double.parseDouble(values[17].trim())); // MonthlyIncome
                    employee.setOvertime("Yes".equalsIgnoreCase(values[20].trim()));
                    employee.setYearsAtCompany(Integer.parseInt(values[12].trim()));

                    // Satisfaction (colonnes importantes)
                    employee.setEnvironmentSatisfaction(Integer.parseInt(values[9].trim()));
                    employee.setJobSatisfaction(Integer.parseInt(values[16].trim()));

                    employeeRepository.save(employee);
                    imported++;

                    if (imported % 200 == 0) {
                        log.info("{} employés importés...", imported);
                    }

                } catch (Exception e) {
                    log.warn("Erreur sur la ligne : {} - {}", line, e.getMessage());
                }
            }

            log.info("Import terminé avec succès ! {} employés importés.", imported);

        } catch (Exception e) {
            log.error("Erreur lors de l'import du dataset", e);
        }
    }
}