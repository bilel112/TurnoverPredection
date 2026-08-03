# Getting Started

### Reference Documentation
For further reference, please consider the following sections:

* [Official Apache Maven documentation](https://maven.apache.org/guides/index.html)
* [Spring Boot Maven Plugin Reference Guide](https://docs.spring.io/spring-boot/3.5.16/maven-plugin)
* [Create an OCI image](https://docs.spring.io/spring-boot/3.5.16/maven-plugin/build-image.html)
* [Spring Web](https://docs.spring.io/spring-boot/3.5.16/reference/web/servlet.html)
* [Spring Data JPA](https://docs.spring.io/spring-boot/3.5.16/reference/data/sql.html#data.sql.jpa-and-spring-data)
* [Validation](https://docs.spring.io/spring-boot/3.5.16/reference/io/validation.html)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/rest/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)
* [Accessing data with MySQL](https://spring.io/guides/gs/accessing-data-mysql/)
* [Validation](https://spring.io/guides/gs/validating-form-input/)

### Maven Parent overrides

Due to Maven's design, elements are inherited from the parent POM to the project POM.
While most of the inheritance is fine, it also inherits unwanted elements like `<license>` and `<developers>` from the parent.
To prevent this, the project POM contains empty overrides for these elements.
If you manually switch to a different parent and actually want the inheritance, you need to remove those overrides.

## Security: JWT and RBAC

### Configuration

The backend reads these properties from `src/main/resources/application.properties`:

```properties
jwt.secret=ChangeThisToAStrongSecretKeyThatIsAtLeast64CharactersLongForHS512
jwt.expiration-ms=86400000
```

For production, replace `jwt.secret` with a secret stored outside the repository (for example, an environment variable or a deployment secret). The key must be long enough for HS512.

### Default test accounts

`DataInitializer` creates these accounts only when the username does not already exist:

| Username | Password | Role |
|---|---|---|
| `admin` | `admin123` | `ADMIN` |
| `rh1` | `rh123` | `HR` |
| `manager` | `manager123` | `MANAGER` |
| `employee` | `employee123` | `EMPLOYEE` |

If an account already exists in MariaDB, its password is not replaced by the initializer.

### Postman verification

1. Start the backend:

```powershell
cd C:\Users\bilel\Music\turnover\turnover
.\mvnw.cmd spring-boot:run
```

2. Login with `POST http://localhost:8081/api/auth/login`, body type `raw > JSON`:

```json
{
	"username": "admin",
	"password": "admin123"
}
```

3. Copy `token` from the response. For protected requests, choose Postman `Authorization > Bearer Token` and paste only the token.

4. Expected access rules:

| Request | Without token | `ADMIN` | `HR` |
|---|---:|---:|---:|
| `GET /api/users` | 401 | 200 | 403 |
| `GET /api/roles` | 401 | 200 | 403 |
| `GET /api/employees` | 401 | 200 | 200 |
| `POST /api/employees` | 401 | 201 | 201 |

Unauthorized and forbidden responses are JSON objects containing `status`, `error`, `message`, and `path`.

### Automated verification

Run the backend tests with:

```powershell
cd C:\Users\bilel\Music\turnover\turnover
.\mvnw.cmd test
```

The security integration tests cover login, unauthenticated access (`401`), and role denial (`403`).

## Dynamic Turnover Scoring Module

### Overview

The scoring module calculates a **0-100 point turnover risk** for each employee based on **8 configurable business factors**. It's independent from the ML prediction model and stored in the database for historical tracking.

### Risk Levels

| Level | Points | Color | Meaning |
|-------|--------|-------|---------|
| **HIGH** | ≥ 55 | 🔴 Red | Immediate attention needed |
| **MEDIUM** | 30-54 | 🟡 Yellow | Monitor closely |
| **LOW** | < 30 | 🟢 Green | Stable employee |

### Scoring Factors (Configurable)

Edit `src/main/resources/application.properties` to adjust weights:

```properties
# 8 factors with configurable weights (default values shown)
turnover.scoring.short-tenure-weight=20
turnover.scoring.many-companies-weight=15
turnover.scoring.overtime-weight=12
turnover.scoring.low-salary-weight=14
turnover.scoring.low-job-satisfaction-weight=12
turnover.scoring.low-environment-satisfaction-weight=8
turnover.scoring.no-recent-promotion-weight=10
turnover.scoring.low-stock-option-weight=9
```

The scoring popup now displays a total computed from the visible active factors, and this calculation is aligned with the backend rules. After changing the weights, restart the backend so the next scoring calculation is regenerated.

### Frontend Usage (React)

1. **View Employee List** → Open Employee Manager
2. **Click the 📊 Scoring button** next to any employee
3. **Modal opens showing**:
   - Current score with color coding
   - All risk factors (numbered list)
   - Scoring configuration details
   - Last 10 historical calculations (expandable)

### Backend API Endpoints

**Get current score for employee:**
```http
GET /api/turnover-scoring/employees/{employeeId}/score
Authorization: Bearer {JWT_TOKEN}

Response (200):
{
  "score": 45,
  "riskLevel": "MEDIUM",
  "riskLabel": "Risque Moyen",
  "reasons": [
    "Ancienneté courte",
    "Heures supplémentaires",
    "Salaire bas"
  ],
  "calculatedAt": "2026-07-26T17:06:29"
}
```

**Get scoring history:**
```http
GET /api/turnover-scoring/employees/{employeeId}/history
Authorization: Bearer {JWT_TOKEN}

Response (200):
[
  {
    "score": 45,
    "riskLevel": "MEDIUM",
    "riskLabel": "Risque Moyen",
    "reasons": ["Ancienneté courte", "Heures supplémentaires"],
    "calculatedAt": "2026-07-26T17:06:29"
  },
  // ... up to 10 previous scores
]
```

### Database Schema

Table: `employee_risk_scores`
```sql
CREATE TABLE employee_risk_scores (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  employee_id BIGINT NOT NULL REFERENCES employees(id),
  score INT NOT NULL,
  risk_level VARCHAR(20),          -- 'HIGH', 'MEDIUM', 'LOW'
  risk_label VARCHAR(50),           -- French labels
  calculated_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  reasons VARCHAR(1000)             -- Semicolon-separated reasons
);
```

### Testing the Module

1. **Backend unit tests:**
   ```powershell
   cd C:\Users\bilel\Music\turnover\turnover
   .\mvnw.cmd -Dtest=DynamicTurnoverScoringServiceImplTest test
   ```

2. **Manual testing via API:**
   - Start backend: `.\mvnw.cmd spring-boot:run`
   - Start frontend: `cd frontend && npm run dev`
   - Login as HR/Manager/Admin
   - View any employee's score in the modal

3. **Modify weights and test:**
   - Edit `application.properties`
   - Restart backend
   - Score will recalculate with new weights

