"""Entraîne et exporte les modèles LR et SVM — pipeline du notebook v4."""

from __future__ import annotations

import json
import time
from pathlib import Path

import joblib
import numpy as np
import pandas as pd
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import (
    accuracy_score,
    f1_score,
    precision_recall_curve,
    precision_score,
    recall_score,
    roc_auc_score,
)
from sklearn.model_selection import GridSearchCV, StratifiedKFold, train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.svm import SVC

from preprocessing import (
    RANDOM_STATE,
    get_categorical_choices,
    get_numeric_defaults,
    load_raw_dataframe,
    prepare_model_dataframe,
    save_shared_metadata,
)

BASE_DIR = Path(__file__).resolve().parent.parent
DATA_PATH = BASE_DIR / "notebooks" / "dataIBM.csv"
ARTIFACTS_DIR = BASE_DIR / "artifacts"


def train_and_tune(
    model,
    param_grid: dict,
    X_train: pd.DataFrame,
    y_train: pd.Series,
    X_test: pd.DataFrame,
    y_test: pd.Series,
    name: str,
) -> dict:
    cv = StratifiedKFold(n_splits=5, shuffle=True, random_state=RANDOM_STATE)
    grid = GridSearchCV(model, param_grid, scoring="f1", cv=cv, n_jobs=-1, verbose=0)

    started = time.time()
    grid.fit(X_train, y_train)
    train_time = time.time() - started

    best = grid.best_estimator_
    y_proba = best.predict_proba(X_test)[:, 1]

    precisions, recalls, thresholds = precision_recall_curve(y_test, y_proba)
    f1_scores = 2 * (precisions * recalls) / (precisions + recalls + 1e-9)
    best_idx = int(np.argmax(f1_scores[:-1]))
    best_threshold = float(thresholds[best_idx]) if len(thresholds) > 0 else 0.5
    y_pred_opt = (y_proba >= best_threshold).astype(int)

    return {
        "model_name": name,
        "best_params": grid.best_params_,
        "best_threshold": round(best_threshold, 3),
        "needs_scaling": True,
        "metrics": {
            "accuracy": float(accuracy_score(y_test, y_pred_opt)),
            "precision": float(precision_score(y_test, y_pred_opt, zero_division=0)),
            "recall": float(recall_score(y_test, y_pred_opt)),
            "f1": float(f1_score(y_test, y_pred_opt)),
            "roc_auc": float(roc_auc_score(y_test, y_proba)),
        },
        "train_time_s": round(train_time, 2),
        "estimator": best,
    }


def save_model_bundle(model_key: str, result: dict, scaler: StandardScaler) -> None:
    model_dir = ARTIFACTS_DIR / model_key
    model_dir.mkdir(parents=True, exist_ok=True)

    joblib.dump(result["estimator"], model_dir / "model.joblib")
    joblib.dump(scaler, model_dir / "scaler.joblib")

    metadata = {
        "model_name": result["model_name"],
        "best_params": result["best_params"],
        "best_threshold": result["best_threshold"],
        "needs_scaling": result["needs_scaling"],
        "metrics": result["metrics"],
        "train_time_s": result["train_time_s"],
    }
    (model_dir / "metadata.json").write_text(
        json.dumps(metadata, indent=2), encoding="utf-8"
    )


def main() -> None:
    raw_df = load_raw_dataframe(DATA_PATH)
    model_df = prepare_model_dataframe(raw_df)

    y = model_df["Attrition"]
    X = model_df.drop(columns=["Attrition"])

    X_train_raw, X_test_raw, y_train, y_test = train_test_split(
        X,
        y,
        test_size=0.20,
        stratify=y,
        random_state=RANDOM_STATE,
    )

    X_train_enc = pd.get_dummies(X_train_raw, drop_first=True, dtype=int)
    X_test_enc = pd.get_dummies(X_test_raw, drop_first=True, dtype=int)
    X_test_enc = X_test_enc.reindex(columns=X_train_enc.columns, fill_value=0)

    scaler = StandardScaler()
    X_train_sc = pd.DataFrame(
        scaler.fit_transform(X_train_enc),
        columns=X_train_enc.columns,
        index=X_train_enc.index,
    )
    X_test_sc = pd.DataFrame(
        scaler.transform(X_test_enc),
        columns=X_test_enc.columns,
        index=X_test_enc.index,
    )

    feature_columns = X_train_enc.columns.tolist()
    raw_columns = [col for col in X.columns]
    categorical_choices = get_categorical_choices(model_df)
    defaults = get_numeric_defaults(model_df)
    save_shared_metadata(
        ARTIFACTS_DIR,
        feature_columns,
        raw_columns,
        categorical_choices,
        defaults,
    )

    print("[1/2] Logistic Regression...")
    lr = LogisticRegression(
        max_iter=3000, random_state=RANDOM_STATE, class_weight="balanced"
    )
    lr_grid = {"C": [0.01, 0.1, 1.0, 10.0], "solver": ["liblinear", "lbfgs"]}
    lr_result = train_and_tune(
        lr, lr_grid, X_train_sc, y_train, X_test_sc, y_test, "Logistic Regression"
    )
    save_model_bundle("logistic_regression", lr_result, scaler)
    print(
        f"       F1={lr_result['metrics']['f1']:.3f} | "
        f"threshold={lr_result['best_threshold']} | "
        f"params={lr_result['best_params']}"
    )

    print("[2/2] SVM (RBF)...")
    svm = SVC(
        kernel="rbf",
        probability=True,
        random_state=RANDOM_STATE,
        class_weight="balanced",
    )
    svm_grid = {"C": [0.1, 1.0, 10.0], "gamma": ["scale", 0.01, 0.1]}
    svm_result = train_and_tune(
        svm, svm_grid, X_train_sc, y_train, X_test_sc, y_test, "SVM (RBF)"
    )
    save_model_bundle("svm", svm_result, scaler)
    print(
        f"       F1={svm_result['metrics']['f1']:.3f} | "
        f"threshold={svm_result['best_threshold']} | "
        f"params={svm_result['best_params']}"
    )

    print("\nArtifacts sauvegardés dans:", ARTIFACTS_DIR)


if __name__ == "__main__":
    main()
