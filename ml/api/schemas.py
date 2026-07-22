"""Schémas Pydantic pour l'API de prédiction d'attrition."""

from __future__ import annotations

from typing import Any, Literal, Optional

from pydantic import AliasChoices, BaseModel, ConfigDict, Field, model_validator

# Noms de colonnes du dataset IBM HR (PascalCase)
PASCAL_CASE_EXAMPLE = {
    "Age": 35,
    "BusinessTravel": "Travel",
    "Department": "Research & Development",
    "DistanceFromHome": 8,
    "Education": 3,
    "EducationField": "Life Sciences",
    "EnvironmentSatisfaction": 2,
    "Gender": "Male",
    "JobInvolvement": 3,
    "JobLevel": 2,
    "JobRole": "Research Scientist",
    "JobSatisfaction": 2,
    "MaritalStatus": "Single",
    "MonthlyIncome": 4500,
    "NumCompaniesWorked": 1,
    "OverTime": "Yes",
    "PercentSalaryHike": 14,
    "RelationshipSatisfaction": 3,
    "StockOptionLevel": 1,
    "TotalWorkingYears": 10,
    "TrainingTimesLastYear": 3,
    "WorkLifeBalance": 3,
    "YearsAtCompany": 5,
    "YearsInCurrentRole": 3,
    "YearsSinceLastPromotion": 1,
    "YearsWithCurrManager": 3,
}

CAMEL_TO_PASCAL = {
    "age": "Age",
    "businessTravel": "BusinessTravel",
    "department": "Department",
    "distanceFromHome": "DistanceFromHome",
    "education": "Education",
    "educationField": "EducationField",
    "environmentSatisfaction": "EnvironmentSatisfaction",
    "gender": "Gender",
    "jobInvolvement": "JobInvolvement",
    "jobLevel": "JobLevel",
    "jobRole": "JobRole",
    "jobSatisfaction": "JobSatisfaction",
    "maritalStatus": "MaritalStatus",
    "monthlyIncome": "MonthlyIncome",
    "numCompaniesWorked": "NumCompaniesWorked",
    "overtime": "OverTime",
    "overTime": "OverTime",
    "percentSalaryHike": "PercentSalaryHike",
    "relationshipSatisfaction": "RelationshipSatisfaction",
    "stockOptionLevel": "StockOptionLevel",
    "totalWorkingYears": "TotalWorkingYears",
    "trainingTimesLastYear": "TrainingTimesLastYear",
    "workLifeBalance": "WorkLifeBalance",
    "yearsAtCompany": "YearsAtCompany",
    "yearsInCurrentRole": "YearsInCurrentRole",
    "yearsSinceLastPromotion": "YearsSinceLastPromotion",
    "yearsWithCurrManager": "YearsWithCurrManager",
}

SNAKE_TO_PASCAL = {
    "age": "Age",
    "business_travel": "BusinessTravel",
    "department": "Department",
    "distance_from_home": "DistanceFromHome",
    "education": "Education",
    "education_field": "EducationField",
    "environment_satisfaction": "EnvironmentSatisfaction",
    "gender": "Gender",
    "job_involvement": "JobInvolvement",
    "job_level": "JobLevel",
    "job_role": "JobRole",
    "job_satisfaction": "JobSatisfaction",
    "marital_status": "MaritalStatus",
    "monthly_income": "MonthlyIncome",
    "num_companies_worked": "NumCompaniesWorked",
    "overtime": "OverTime",
    "percent_salary_hike": "PercentSalaryHike",
    "relationship_satisfaction": "RelationshipSatisfaction",
    "stock_option_level": "StockOptionLevel",
    "total_working_years": "TotalWorkingYears",
    "training_times_last_year": "TrainingTimesLastYear",
    "work_life_balance": "WorkLifeBalance",
    "years_at_company": "YearsAtCompany",
    "years_in_current_role": "YearsInCurrentRole",
    "years_since_last_promotion": "YearsSinceLastPromotion",
    "years_with_curr_manager": "YearsWithCurrManager",
}


def _field_alias(pascal: str, camel: str) -> AliasChoices:
    return AliasChoices(pascal, camel)


class EmployeeProfile(BaseModel):
    """Profil employé complet — aligné sur le dataset IBM HR."""

    model_config = ConfigDict(
        populate_by_name=True,
        json_schema_extra={"example": PASCAL_CASE_EXAMPLE},
    )

    @model_validator(mode="before")
    @classmethod
    def normalize_input_keys(cls, data: Any) -> Any:
        if not isinstance(data, dict):
            return data
        return {CAMEL_TO_PASCAL.get(key, key): value for key, value in data.items()}

    age: int = Field(ge=18, le=70, validation_alias=_field_alias("Age", "age"))
    business_travel: str = Field(
        validation_alias=_field_alias("BusinessTravel", "businessTravel")
    )
    department: str = Field(validation_alias=_field_alias("Department", "department"))
    distance_from_home: int = Field(
        ge=0, validation_alias=_field_alias("DistanceFromHome", "distanceFromHome")
    )
    education: int = Field(ge=1, le=5, validation_alias=_field_alias("Education", "education"))
    education_field: str = Field(
        validation_alias=_field_alias("EducationField", "educationField")
    )
    environment_satisfaction: int = Field(
        ge=1, le=4,
        validation_alias=_field_alias("EnvironmentSatisfaction", "environmentSatisfaction"),
    )
    gender: str = Field(validation_alias=_field_alias("Gender", "gender"))
    job_involvement: int = Field(
        ge=1, le=4, validation_alias=_field_alias("JobInvolvement", "jobInvolvement")
    )
    job_level: int = Field(ge=1, le=5, validation_alias=_field_alias("JobLevel", "jobLevel"))
    job_role: str = Field(validation_alias=_field_alias("JobRole", "jobRole"))
    job_satisfaction: int = Field(
        ge=1, le=4, validation_alias=_field_alias("JobSatisfaction", "jobSatisfaction")
    )
    marital_status: str = Field(
        validation_alias=_field_alias("MaritalStatus", "maritalStatus")
    )
    monthly_income: int = Field(
        ge=1000, validation_alias=_field_alias("MonthlyIncome", "monthlyIncome")
    )
    num_companies_worked: int = Field(
        ge=0, validation_alias=_field_alias("NumCompaniesWorked", "numCompaniesWorked")
    )
    overtime: str = Field(
        validation_alias=_field_alias("OverTime", "overTime"),
        description="Yes ou No",
    )
    percent_salary_hike: int = Field(
        ge=0, validation_alias=_field_alias("PercentSalaryHike", "percentSalaryHike")
    )
    relationship_satisfaction: int = Field(
        ge=1, le=4,
        validation_alias=_field_alias(
            "RelationshipSatisfaction", "relationshipSatisfaction"
        ),
    )
    stock_option_level: int = Field(
        ge=0, le=3, validation_alias=_field_alias("StockOptionLevel", "stockOptionLevel")
    )
    total_working_years: int = Field(
        ge=0, validation_alias=_field_alias("TotalWorkingYears", "totalWorkingYears")
    )
    training_times_last_year: int = Field(
        ge=0, le=6,
        validation_alias=_field_alias("TrainingTimesLastYear", "trainingTimesLastYear"),
    )
    work_life_balance: int = Field(
        ge=1, le=4, validation_alias=_field_alias("WorkLifeBalance", "workLifeBalance")
    )
    years_at_company: int = Field(
        ge=0, validation_alias=_field_alias("YearsAtCompany", "yearsAtCompany")
    )
    years_in_current_role: int = Field(
        ge=0, validation_alias=_field_alias("YearsInCurrentRole", "yearsInCurrentRole")
    )
    years_since_last_promotion: int = Field(
        ge=0,
        validation_alias=_field_alias(
            "YearsSinceLastPromotion", "yearsSinceLastPromotion"
        ),
    )
    years_with_curr_manager: int = Field(
        ge=0,
        validation_alias=_field_alias("YearsWithCurrManager", "yearsWithCurrManager"),
    )

    def to_dataset_record(self) -> dict[str, Any]:
        return {
            SNAKE_TO_PASCAL[key]: value
            for key, value in self.model_dump().items()
        }


class PredictionRequestSimple(BaseModel):
    """Format simplifié — compatible avec le backend Spring Boot."""

    model_config = ConfigDict(
        populate_by_name=True,
        json_schema_extra={
            "example": {
                "age": 35,
                "overtime": True,
                "monthlyIncome": 4500,
                "distanceFromHome": 8,
                "jobSatisfaction": 2,
                "environmentSatisfaction": 2,
                "yearsAtCompany": 5,
                "totalWorkingYears": 10,
                "stockOptionLevel": 1,
                "jobLevel": 2,
                "yearsInCurrentRole": 3,
                "yearsWithCurrManager": 3,
                "yearsSinceLastPromotion": 1,
                "numCompaniesWorked": 1,
                "trainingTimesLastYear": 3,
                "workLifeBalance": 3,
                "education": 3,
                "maritalStatus": "Single",
                "businessTravel": "Travel",
                "jobRole": "Research Scientist",
            }
        },
    )

    age: Optional[int] = Field(default=None, alias="age")
    overtime: Optional[bool] = Field(default=None, alias="overtime")
    monthly_income: Optional[int] = Field(default=None, alias="monthlyIncome")
    distance_from_home: Optional[int] = Field(default=None, alias="distanceFromHome")
    job_satisfaction: Optional[int] = Field(default=None, alias="jobSatisfaction")
    environment_satisfaction: Optional[int] = Field(
        default=None, alias="environmentSatisfaction"
    )
    years_at_company: Optional[int] = Field(default=None, alias="yearsAtCompany")
    total_working_years: Optional[int] = Field(default=None, alias="totalWorkingYears")
    stock_option_level: Optional[int] = Field(default=None, alias="stockOptionLevel")
    job_level: Optional[int] = Field(default=None, alias="jobLevel")
    years_in_current_role: Optional[int] = Field(default=None, alias="yearsInCurrentRole")
    years_with_curr_manager: Optional[int] = Field(
        default=None, alias="yearsWithCurrManager"
    )
    years_since_last_promotion: Optional[int] = Field(
        default=None, alias="yearsSinceLastPromotion"
    )
    num_companies_worked: Optional[int] = Field(default=None, alias="numCompaniesWorked")
    training_times_last_year: Optional[int] = Field(
        default=None, alias="trainingTimesLastYear"
    )
    work_life_balance: Optional[int] = Field(default=None, alias="workLifeBalance")
    education: Optional[int] = Field(default=None, alias="education")
    performance_rating: Optional[int] = Field(default=None, alias="performanceRating")
    marital_status: Optional[str] = Field(default=None, alias="maritalStatus")
    business_travel: Optional[str] = Field(default=None, alias="businessTravel")
    job_role: Optional[str] = Field(default=None, alias="jobRole")


class PredictionResponse(BaseModel):
    """Réponse de prédiction — compatible Spring Boot."""

    model_config = ConfigDict(populate_by_name=True)

    model: str
    probability: float = Field(description="Probabilité de départ (classe Yes)")
    will_leave: bool = Field(alias="willLeave")
    risk_level: Literal["LOW", "MEDIUM", "HIGH"] = Field(alias="riskLevel")
    message: str
    prediction: Literal["Leave", "Stay"] = Field(
        description="Leave = va partir, Stay = va rester"
    )
    threshold: float = Field(description="Seuil optimal utilisé pour la décision")


class ModelInfo(BaseModel):
    model_key: str
    model_name: str
    best_threshold: float
    metrics: dict[str, float]


class HealthResponse(BaseModel):
    status: str
    models_loaded: list[str]
