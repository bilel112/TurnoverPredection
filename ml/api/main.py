"""API FastAPI — prédiction d'attrition (Logistic Regression + SVM)."""

from __future__ import annotations

import json
from pathlib import Path
from typing import Any

import joblib
import pandas as pd
from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware

from preprocessing import apply_defaults, encode_features, normalize_employee_record
from schemas import (
    EmployeeProfile,
    HealthResponse,
    ModelInfo,
    PredictionRequestSimple,
    PredictionResponse,
)

BASE_DIR = Path(__file__).resolve().parent.parent
ARTIFACTS_DIR = BASE_DIR / "artifacts"

app = FastAPI(
    title="Turnover — Prédiction d'attrition",
    description=(
        "API de prédiction d'attrition employé basée sur le notebook IBM HR. "
        "Deux modèles disponibles : **Logistic Regression** et **SVM (RBF)**."
    ),
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

MODEL_REGISTRY: dict[str, dict[str, Any]] = {}
FEATURE_COLUMNS: list[str] = []
DEFAULTS: dict[str, Any] = {}


def _load_json(path: Path) -> Any:
    with path.open(encoding="utf-8") as handle:
        return json.load(handle)


def load_artifacts() -> None:
    global FEATURE_COLUMNS, DEFAULTS

    shared_dir = ARTIFACTS_DIR / "shared"
    FEATURE_COLUMNS = _load_json(shared_dir / "feature_columns.json")
    DEFAULTS = _load_json(shared_dir / "defaults.json")

    for model_key in ("logistic_regression", "svm"):
        model_dir = ARTIFACTS_DIR / model_key
        MODEL_REGISTRY[model_key] = {
            "model": joblib.load(model_dir / "model.joblib"),
            "scaler": joblib.load(model_dir / "scaler.joblib"),
            "metadata": _load_json(model_dir / "metadata.json"),
        }


@app.on_event("startup")
def startup() -> None:
    load_artifacts()


def _risk_level(probability: float, threshold: float) -> str:
    if probability >= 0.7:
        return "HIGH"
    if probability >= threshold:
        return "MEDIUM"
    return "LOW"


def _build_message(will_leave: bool, probability: float, model_name: str) -> str:
    pct = round(probability * 100, 1)
    if will_leave:
        return (
            f"[{model_name}] Risque élevé d'attrition — probabilité de départ : {pct}%"
        )
    return f"[{model_name}] L'employé devrait rester — probabilité de départ : {pct}%"


def _predict_with_model(model_key: str, record: dict[str, Any]) -> PredictionResponse:
    if model_key not in MODEL_REGISTRY:
        raise HTTPException(
            status_code=503,
            detail=(
                f"Modèle '{model_key}' non disponible. Lancez d'abord train_models.py."
            ),
        )

    bundle = MODEL_REGISTRY[model_key]
    metadata = bundle["metadata"]
    threshold = float(metadata["best_threshold"])
    model_name = metadata["model_name"]

    complete = apply_defaults(normalize_employee_record(record), DEFAULTS)
    encoded = encode_features(complete, FEATURE_COLUMNS)
    scaled = pd.DataFrame(
        bundle["scaler"].transform(encoded),
        columns=FEATURE_COLUMNS,
    )
    probability = float(bundle["model"].predict_proba(scaled)[0, 1])
    will_leave = probability >= threshold

    return PredictionResponse(
        model=model_name,
        probability=round(probability, 4),
        willLeave=will_leave,
        riskLevel=_risk_level(probability, threshold),
        message=_build_message(will_leave, probability, model_name),
        prediction="Leave" if will_leave else "Stay",
        threshold=threshold,
    )


def _profile_to_record(profile: EmployeeProfile) -> dict[str, Any]:
    return profile.to_dataset_record()


@app.get("/health", response_model=HealthResponse, tags=["Système"])
def health() -> HealthResponse:
    return HealthResponse(status="ok", models_loaded=list(MODEL_REGISTRY.keys()))


@app.get("/models", response_model=list[ModelInfo], tags=["Modèles"])
def list_models() -> list[ModelInfo]:
    return [
        ModelInfo(
            model_key=key,
            model_name=bundle["metadata"]["model_name"],
            best_threshold=bundle["metadata"]["best_threshold"],
            metrics=bundle["metadata"]["metrics"],
        )
        for key, bundle in MODEL_REGISTRY.items()
    ]


@app.post(
    "/predict/logistic-regression",
    response_model=PredictionResponse,
    tags=["Prédiction"],
    summary="Prédire avec Logistic Regression",
)
def predict_logistic_regression(profile: EmployeeProfile) -> PredictionResponse:
    return _predict_with_model("logistic_regression", _profile_to_record(profile))


@app.post(
    "/predict/svm",
    response_model=PredictionResponse,
    tags=["Prédiction"],
    summary="Prédire avec SVM (RBF)",
    description="Prédit si l'employé va partir (Leave) ou rester (Stay) avec le SVM.",
)
def predict_svm(profile: EmployeeProfile) -> PredictionResponse:
    return _predict_with_model("svm", _profile_to_record(profile))


@app.post(
    "/predict",
    response_model=PredictionResponse,
    tags=["Prédiction"],
    summary="Prédire avec le modèle par défaut (SVM)",
    description="Endpoint compatible avec le backend Spring Boot qui envoie un profil simplifié.",
)
def predict_default(request: PredictionRequestSimple) -> PredictionResponse:
    record = request.model_dump(by_alias=True, exclude_none=True)
    return _predict_with_model("svm", record)


@app.post(
    "/predict/compare",
    response_model=dict[str, PredictionResponse],
    tags=["Prédiction"],
    summary="Comparer LR et SVM sur le même profil",
    description="Retourne les prédictions des deux modèles côte à côte.",
)
def predict_compare(profile: EmployeeProfile) -> dict[str, PredictionResponse]:
    record = _profile_to_record(profile)
    return {
        "logistic_regression": _predict_with_model("logistic_regression", record),
        "svm": _predict_with_model("svm", record),
    }
