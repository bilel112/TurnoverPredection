"""Pipeline de prétraitement — aligné sur NotebookComplet.ipynb."""

from __future__ import annotations

import json
from pathlib import Path
from typing import Any

import pandas as pd

RANDOM_STATE = 42

COLS_TO_DROP = [
    "EmployeeCount",
    "Over18",
    "StandardHours",
    "EmployeeNumber",
    "DailyRate",
    "HourlyRate",
    "MonthlyRate",
    "PerformanceRating",
]

BUSINESS_TRAVEL_MAP = {
    "Travel_Rarely": "Travel",
    "Travel_Frequently": "Travel",
    "Travel": "Travel",
    "Non-Travel": "Non-Travel",
}

RARE_ROLE_THRESHOLD = 100
RARE_ROLES = ["Sales Representative", "Research Director", "Human Resources"]


def load_raw_dataframe(csv_path: Path) -> pd.DataFrame:
    return pd.read_csv(csv_path, delimiter=";")


def prepare_model_dataframe(df: pd.DataFrame) -> pd.DataFrame:
    """Nettoyage + regroupements identiques au notebook."""
    prepared = df.copy()
    prepared = prepared.drop(columns=[c for c in COLS_TO_DROP if c in prepared.columns])
    prepared["Attrition"] = prepared["Attrition"].map({"Yes": 1, "No": 0})

    prepared["BusinessTravel"] = prepared["BusinessTravel"].replace(
        {"Travel_Rarely": "Travel", "Travel_Frequently": "Travel"}
    )

    role_counts = prepared["JobRole"].value_counts()
    rare_roles = role_counts[role_counts < RARE_ROLE_THRESHOLD].index.tolist()
    prepared["JobRole"] = prepared["JobRole"].apply(
        lambda x: "Other" if x in rare_roles else x
    )
    return prepared


def normalize_employee_record(record: dict[str, Any]) -> dict[str, Any]:
    """Normalise les clés camelCase (Java) et les valeurs catégorielles."""
    key_map = {
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

    normalized: dict[str, Any] = {}
    for key, value in record.items():
        if value is None:
            continue
        target_key = key_map.get(key, key)
        normalized[target_key] = value

    if "OverTime" in normalized and isinstance(normalized["OverTime"], bool):
        normalized["OverTime"] = "Yes" if normalized["OverTime"] else "No"

    if "BusinessTravel" in normalized:
        travel = str(normalized["BusinessTravel"])
        normalized["BusinessTravel"] = BUSINESS_TRAVEL_MAP.get(travel, travel)

    if "JobRole" in normalized and normalized["JobRole"] in RARE_ROLES:
        normalized["JobRole"] = "Other"

    return normalized


def apply_defaults(record: dict[str, Any], defaults: dict[str, Any]) -> dict[str, Any]:
    return {key: record.get(key, defaults[key]) for key in defaults}


def encode_features(record: dict[str, Any], feature_columns: list[str]) -> pd.DataFrame:
    frame = pd.DataFrame([record])
    encoded = pd.get_dummies(frame, drop_first=True, dtype=int)
    return encoded.reindex(columns=feature_columns, fill_value=0)


def get_categorical_choices(df: pd.DataFrame) -> dict[str, list[Any]]:
    cat_cols = df.drop(columns=["Attrition"]).select_dtypes(include=["object"]).columns
    return {col: sorted(df[col].dropna().unique().tolist()) for col in cat_cols}


def get_numeric_defaults(df: pd.DataFrame) -> dict[str, Any]:
    features = df.drop(columns=["Attrition"])
    defaults = features.median(numeric_only=True).to_dict()
    for col in features.select_dtypes(include=["object"]).columns:
        defaults[col] = features[col].mode().iloc[0]
    return defaults


def save_shared_metadata(
    artifacts_dir: Path,
    feature_columns: list[str],
    raw_columns: list[str],
    categorical_choices: dict[str, list[Any]],
    defaults: dict[str, Any],
) -> None:
    shared_dir = artifacts_dir / "shared"
    shared_dir.mkdir(parents=True, exist_ok=True)

    (shared_dir / "feature_columns.json").write_text(
        json.dumps(feature_columns, indent=2), encoding="utf-8"
    )
    (shared_dir / "raw_input_columns.json").write_text(
        json.dumps(raw_columns, indent=2), encoding="utf-8"
    )
    (shared_dir / "categorical_choices.json").write_text(
        json.dumps(categorical_choices, indent=2, ensure_ascii=False), encoding="utf-8"
    )
    (shared_dir / "defaults.json").write_text(
        json.dumps(defaults, indent=2, ensure_ascii=False), encoding="utf-8"
    )
