package com.ooredoo.turnover.bootstrap;

import com.ooredoo.turnover.entity.Employee;
import com.ooredoo.turnover.entity.Role;
import com.ooredoo.turnover.entity.User;
import com.ooredoo.turnover.repository.EmployeeRepository;
import com.ooredoo.turnover.repository.RoleRepository;
import com.ooredoo.turnover.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;

@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final EmployeeRepository employeeRepository;
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(EmployeeRepository employeeRepository, RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.employeeRepository = employeeRepository;
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialisation des rôles
        if (roleRepository.count() == 0) {
            roleRepository.save(new Role(null, "ADMIN"));
            roleRepository.save(new Role(null, "HR"));
            roleRepository.save(new Role(null, "MANAGER"));
            roleRepository.save(new Role(null, "EMPLOYEE"));
            log.info("Rôles par défaut initialisés : ADMIN, HR, MANAGER, EMPLOYEE");
        }

        // Normalize existing role names and ensure canonical roles exist
        List<Role> existingRoles = roleRepository.findAll();
        for (Role role : existingRoles) {
            String normalized = role.getName() == null ? "" : role.getName().trim().toUpperCase();
            if (!normalized.equals(role.getName())) {
                role.setName(normalized);
                roleRepository.save(role);
            }
        }
        for (String canonicalRole : List.of("ADMIN", "HR", "MANAGER", "EMPLOYEE")) {
            roleRepository.findByName(canonicalRole)
                    .orElseGet(() -> roleRepository.save(new Role(null, canonicalRole)));
        }

        // Initialisation des comptes par défaut
        if (!userRepository.existsByUsername("admin")) {
            Role adminRole = roleRepository.findByName("ADMIN")
                    .orElseGet(() -> roleRepository.save(new Role(null, "ADMIN")));
            User admin = new User();
            admin.setUsername("admin");
            admin.setEmail("admin@ooredoo.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole(adminRole);
            userRepository.save(admin);
            log.info("Utilisateur par défaut initialisé : admin / admin123");
        }

        if (!userRepository.existsByUsername("manager")) {
            Role managerRole = roleRepository.findByName("MANAGER")
                    .orElseGet(() -> roleRepository.save(new Role(null, "MANAGER")));
            User manager = new User();
            manager.setUsername("manager");
            manager.setEmail("manager@ooredoo.com");
            manager.setPassword(passwordEncoder.encode("manager123"));
            manager.setRole(managerRole);
            userRepository.save(manager);
            log.info("Utilisateur par défaut initialisé : manager / manager123");
        }

        if (!userRepository.existsByUsername("rh1")) {
            Role hrRole = roleRepository.findByName("HR")
                    .orElseGet(() -> roleRepository.save(new Role(null, "HR")));
            User hrUser = new User();
            hrUser.setUsername("rh1");
            hrUser.setEmail("rh1@ooredoo.com");
            hrUser.setPassword(passwordEncoder.encode("rh123"));
            hrUser.setRole(hrRole);
            userRepository.save(hrUser);
            log.info("Utilisateur par défaut initialisé : rh1 / rh123");
        }

        if (!userRepository.existsByUsername("employee")) {
            Role employeeRole = roleRepository.findByName("EMPLOYEE")
                    .orElseGet(() -> roleRepository.save(new Role(null, "EMPLOYEE")));
            User employeeUser = new User();
            employeeUser.setUsername("employee");
            employeeUser.setEmail("employee@ooredoo.com");
            employeeUser.setPassword(passwordEncoder.encode("employee123"));
            employeeUser.setRole(employeeRole);
            userRepository.save(employeeUser);
            log.info("Utilisateur par défaut initialisé : employee / employee123");
        }
        
        // If users exist with plain text passwords, upgrade them to BCrypt on startup
        List<User> existingUsers = userRepository.findAll();
        for (User user : existingUsers) {
            String password = user.getPassword();
            if (password != null && !password.startsWith("$2a$") && !password.startsWith("$2b$") && !password.startsWith("$2y$")) {
                user.setPassword(passwordEncoder.encode(password));
                userRepository.save(user);
                log.info("Mot de passe encodé pour l'utilisateur : {}", user.getUsername());
            }
        }

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