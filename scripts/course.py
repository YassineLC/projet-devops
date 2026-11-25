#!/usr/bin/env python3
"""Commandes locales du projet (modèle : scripts/course.py de charroux/rental).

Usage : python scripts/course.py <commande>

  prerequisites         vérifie Python, Java, Node et Docker
  test-catalogue        tests Gradle du catalogue
  test-emprunts         tests Gradle des emprunts
  test-front            build TypeScript + Vite du front
  test                  tous les tests
  lint-catalogue        Checkstyle du catalogue
  lint-emprunts         Checkstyle des emprunts
  lint-front            oxlint du front
  lint                  tous les contrôles qualité
  supply-chain-policy   refuse les actions sans SHA complet et les FROM sans digest
  verify                test + lint + supply-chain-policy (s'arrête au premier échec)

Sans JDK 21 local, Gradle est lancé dans le conteneur eclipse-temurin:21-jdk.
"""
from __future__ import annotations

import os
import re
import shutil
import subprocess
import sys
from pathlib import Path

RACINE = Path(__file__).resolve().parent.parent
WINDOWS = os.name == "nt"
SERVICES_JAVA = {"catalogue": "catalogue-service", "emprunts": "emprunts-service"}
IMAGE_JDK = "eclipse-temurin:21-jdk"

ACTION = re.compile(r"""^\s*-?\s*uses:\s*['"]?([^'"\s#]+)""", re.MULTILINE)
SHA_COMPLET = re.compile(r"@[0-9a-f]{40}$")
DIGEST = re.compile(r"@sha256:[0-9a-f]{64}$")


# ---------- politique de chaîne logicielle ----------

def actions_mutables(texte: str) -> list[str]:
    """Actions distantes non épinglées sur un commit complet de 40 caractères."""
    return [ref for ref in ACTION.findall(texte)
            if not ref.startswith(("./", "docker://")) and not SHA_COMPLET.search(ref)]


def from_mutables(texte: str) -> list[str]:
    """Images de base sans digest SHA-256 (les références à une étape précédente sont acceptées)."""
    etapes: set[str] = set()
    fautes = []
    for ligne in texte.splitlines():
        mots = [m for m in ligne.split() if not m.startswith("--")]
        if len(mots) < 2 or mots[0].lower() != "from":
            continue
        image = mots[1]
        if image.lower() not in etapes and not DIGEST.search(image):
            fautes.append(image)
        if len(mots) >= 4 and mots[2].lower() == "as":
            etapes.add(mots[3].lower())
    return fautes


def supply_chain_policy() -> int:
    fautes = []
    for fichier in sorted((RACINE / ".github" / "workflows").glob("*.yml")):
        fautes += [f"{fichier.relative_to(RACINE)} : {r}" for r in actions_mutables(fichier.read_text("utf-8"))]
    for fichier in sorted(RACINE.glob("*/Dockerfile")):
        fautes += [f"{fichier.relative_to(RACINE)} : FROM {r}" for r in from_mutables(fichier.read_text("utf-8"))]
    for faute in fautes:
        print(f"Référence mutable -> {faute}")
    if fautes:
        print(f"Politique de chaîne logicielle : {len(fautes)} référence(s) mutable(s)")
        return 1
    print("Politique de chaîne logicielle : toutes les références sont immuables")
    return 0


# ---------- exécution ----------

def lancer(commande: list[str], dossier: Path = RACINE) -> int:
    print(f"\n$ {' '.join(commande)}   ({dossier.relative_to(RACINE) or '.'})", flush=True)
    return subprocess.call(commande, cwd=dossier, shell=WINDOWS and commande[0] in ("npm", "npx"))


def version_java() -> int:
    try:
        sortie = subprocess.run(["java", "-version"], capture_output=True, text=True).stderr
    except FileNotFoundError:
        return 0
    trouve = re.search(r'version "(\d+)(?:\.(\d+))?', sortie)
    if not trouve:
        return 0
    majeure = int(trouve.group(1))
    return int(trouve.group(2)) if majeure == 1 else majeure


def gradle(service: str, taches: str) -> int:
    dossier = RACINE / SERVICES_JAVA[service]
    if version_java() >= 21:
        wrapper = "gradlew.bat" if WINDOWS else "./gradlew"
        return lancer([str(dossier / wrapper) if WINDOWS else wrapper, *taches.split(), "--no-daemon"], dossier)
    # Repli : Gradle dans Docker (le module proto partagé est monté avec le service)
    return lancer(["docker", "run", "--rm", "-v", f"{RACINE}:/w", "-v", "projet-devops-gradle:/root/.gradle",
                   "-w", f"/w/{SERVICES_JAVA[service]}", IMAGE_JDK, "sh", "-c",
                   f"sed -i 's/\\r$//' gradlew && chmod +x gradlew && ./gradlew {taches} --no-daemon"])


def front(script: str) -> int:
    dossier = RACINE / "front"
    if not (dossier / "node_modules").exists() and lancer(["npm", "ci"], dossier) != 0:
        return 1
    return lancer(["npm", "run", script], dossier)


def prerequisites() -> int:
    manquants = [outil for outil in ("java", "node", "npm", "docker", "git") if not shutil.which(outil)]
    print(f"Python {sys.version.split()[0]}")
    print(f"Java {version_java() or 'absent'} (21 requis, sinon repli Docker)")
    for outil in manquants:
        print(f"Manquant : {outil}")
    return 1 if {"docker", "git", "node"} & set(manquants) else 0


def enchainer(*etapes) -> int:
    for etape in etapes:
        if etape() != 0:
            return 1
    return 0


COMMANDES = {
    "prerequisites": prerequisites,
    "test-catalogue": lambda: gradle("catalogue", "clean test"),
    "test-emprunts": lambda: gradle("emprunts", "clean test"),
    "test-front": lambda: front("build"),
    "lint-catalogue": lambda: gradle("catalogue", "checkstyleMain checkstyleTest"),
    "lint-emprunts": lambda: gradle("emprunts", "checkstyleMain checkstyleTest"),
    "lint-front": lambda: front("lint"),
    "supply-chain-policy": supply_chain_policy,
}
COMMANDES["test"] = lambda: enchainer(COMMANDES["test-catalogue"], COMMANDES["test-emprunts"], COMMANDES["test-front"])
COMMANDES["lint"] = lambda: enchainer(COMMANDES["lint-catalogue"], COMMANDES["lint-emprunts"], COMMANDES["lint-front"])
COMMANDES["verify"] = lambda: enchainer(COMMANDES["test"], COMMANDES["lint"], supply_chain_policy)


def main(arguments: list[str]) -> int:
    if len(arguments) != 1 or arguments[0] not in COMMANDES:
        print(__doc__)
        return 2
    return 1 if COMMANDES[arguments[0]]() != 0 else 0


if __name__ == "__main__":
    sys.exit(main(sys.argv[1:]))
