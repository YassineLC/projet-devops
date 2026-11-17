# Conception — Bibliothèque en microservices

## Intention

Projet DevOps noté. L'objectif est d'intégrer un maximum de technologies imposées par le sujet (microservices REST + gRPC, Docker, Kubernetes, gateway, service mesh, base de données, sécurité du cluster). L'appli reste basique mais cohérente : c'est le back et l'infra qui comptent.

Thème : une bibliothèque avec de **vrais livres** importés depuis Open Library (titres, auteurs, vraies couvertures).

## Objectifs / non-objectifs

**Objectifs**
- Couvrir tous les paliers du sujet jusqu'à 18/20 : un service, gateway, deuxième service relié, base de données, sécurisation (RBAC, mTLS Istio, registry, outils de contrôle).
- Bonus : gRPC entre services, front React.
- Tout tourne en local sur minikube et se déploie avec des commandes simples et reproductibles.

**Non-objectifs**
- Déploiement cloud.
- Authentification des utilisateurs finaux (login, JWT) : l'emprunteur est un simple nom saisi.
- Gestion des membres, des pénalités ou des réservations.
- Front élaboré : il reste simple (shadcn, propre, sans plus).

**Anti-objectifs** (raté même si ça marche)
- Une mention de Claude dans le dépôt.
- Du boilerplate écrit à la main au lieu des outils de scaffolding officiels.
- Un README surchargé.

## Contraintes

- **Poste** : Windows 11, Docker Desktop (12 CPU, environ 7,4 Go de RAM alloués), minikube 1.39, kubectl 1.34.
- **Java** : Java 8 seulement en local et pas de Maven. Tout se compile **dans Docker** (build multi-stage). Le Maven Wrapper généré par Spring Initializr permet aussi de builder en local si un JDK 21 est installé plus tard.
- **istioctl** n'est pas installé : il faut l'installer.
- **RAM** : Istio, 2 services Spring Boot, Postgres et le front, ça fait beaucoup pour 7,4 Go. Il faut limiter la JVM (`-XX:MaxRAMPercentage=75`, limites mémoire d'environ 512 Mi par service) et lancer minikube avec environ 6 Go.
- **Scaffolding** avec les outils officiels : Spring Initializr (start.spring.io), `npm create vite`, CLI `shadcn`.
- **Commits** en français, une ligne courte : « Création de… », « Modification liée à… ». Aucune attribution Claude.

## Approches envisagées

| Approche | Résumé | Compromis |
|---|---|---|
| **A. Istio partout** (choisie) | Istio Ingress Gateway, sidecars Envoy, mTLS STRICT, AuthorizationPolicy | Une seule brique couvre gateway, mesh et chiffrement, comme les exemples du prof. Lourd en RAM. |
| B. NGINX Ingress puis Istio | Ingress minikube d'abord, Istio ajouté pour le mTLS | Démarrage plus léger, mais deux gateways et une migration. |
| C. Sans mesh | Ingress NGINX + TLS à la main | mTLS « proxy » du palier 18 très pénible. Écartée. |

**Choix de l'utilisateur : A.** Communication : **REST** pour le front et l'extérieur, **gRPC** entre emprunts et catalogue (recommandation acceptée).

## Architecture

```
Navigateur ──HTTP──▶ Istio Ingress Gateway
                       ├── /            ──▶ front (nginx, React)
                       ├── /api/livres  ──▶ catalogue-service :8080 (REST)
                       └── /api/emprunts──▶ emprunts-service  :8080 (REST)

emprunts-service ──gRPC (mTLS)──▶ catalogue-service :9090
catalogue-service ──HTTPS──▶ openlibrary.org (import)
catalogue-service, emprunts-service ──▶ PostgreSQL (une base par service)
Navigateur ──▶ covers.openlibrary.org (images de couverture)
```

Tout est dans le namespace `bibliotheque`, avec le label `istio-injection=enabled`.

### catalogue-service (Spring Boot, Java 21)
- Dépendances Initializr : Web, Data JPA, PostgreSQL, H2, Validation, Actuator, Spring gRPC.
- Entité `Livre` : id, isbn, titre, auteurs, annee, couvertureUrl, exemplairesTotal, exemplairesDisponibles.
- REST :
  - `GET /api/livres?q=` : liste, avec recherche par titre ou auteur.
  - `GET /api/livres/{id}` : détail d'un livre.
  - `POST /api/livres/import?q=...&limit=` : interroge `openlibrary.org/search.json`, crée les livres absents (dédoublonnés par ISBN ou clé Open Library). Couverture : `covers.openlibrary.org/b/id/{cover_i}-L.jpg`.
- Au démarrage, si la base est vide, import automatique de quelques requêtes (configurables) pour avoir un catalogue garni. Le nombre d'exemplaires par défaut est configurable.
- gRPC (`catalogue.proto`, partagé) :
  - `GetLivre(id)` : renvoie le livre et sa disponibilité.
  - `ReserverExemplaire(id)` : décrémente le stock de façon atomique et renvoie une erreur `FAILED_PRECONDITION` s'il n'y a plus d'exemplaire.
  - `LibererExemplaire(id)` : incrémente le stock.

### emprunts-service (Spring Boot, Java 21)
- Dépendances : Web, Data JPA, PostgreSQL, H2, Validation, Actuator, Spring gRPC (client).
- Entité `Emprunt` : id, livreId, emprunteur, dateEmprunt, dateRetourPrevue (+14 jours, configurable), dateRetour, statut (EN_COURS, RENDU, EN_RETARD calculé).
- REST :
  - `POST /api/emprunts {livreId, emprunteur}` : appelle `ReserverExemplaire` en gRPC, puis enregistre l'emprunt. Réponse 409 si le livre n'est plus disponible, 404 s'il est inconnu.
  - `GET /api/emprunts?emprunteur=` : liste des emprunts.
  - `POST /api/emprunts/{id}/retour` : marque l'emprunt rendu et appelle `LibererExemplaire`.
- L'adresse du catalogue gRPC est configurable (`catalogue-service:9090` dans K8s, `localhost:9090` en local).

### front (React + Vite + TypeScript + Tailwind + shadcn)
- Page **Catalogue** : grille de cartes avec la couverture, le titre, l'auteur et le nombre d'exemplaires disponibles, une barre de recherche et un bouton « Emprunter » (le nom est demandé dans une boîte de dialogue).
- Page **Mes emprunts** : saisie d'un nom, tableau des emprunts avec le statut et un bouton « Rendre ».
- Build statique servi par nginx (image non-root). Il appelle les chemins relatifs `/api/...`, donc pas de CORS derrière la gateway. En dev local, le proxy Vite redirige vers les services.

### Base de données
- Profil **local** (sans K8s) : H2 en mémoire, comme le suggère le sujet.
- Profil **k8s** : un seul PostgreSQL 16 en StatefulSet avec un PVC. Un script d'init (ConfigMap) crée 2 bases et 2 utilisateurs (`catalogue`, `emprunts`), chacun limité à sa base. Les identifiants sont dans des Secrets.

## Docker
- Un Dockerfile multi-stage par service : stage de build `maven:3-eclipse-temurin-21`, stage d'exécution `eclipse-temurin:21-jre`, utilisateur non-root.
- Le front : build Node, puis exécution `nginxinc/nginx-unprivileged`.
- Images publiées sur Docker Hub sous `<compte>/biblio-catalogue`, `<compte>/biblio-emprunts` et `<compte>/biblio-front`, avec un tag de version (pas seulement `latest`).

## Kubernetes (`k8s/`, organisé avec Kustomize)
- Namespace, Deployments, Services (ports nommés `http` et `grpc` pour qu'Istio détecte le protocole), ConfigMaps, Secrets, StatefulSet Postgres.
- Probes liveness et readiness via Actuator, requests et limits sur chaque conteneur.
- `securityContext` : runAsNonRoot, readOnlyRootFilesystem (avec un emptyDir `/tmp`), capabilities supprimées, allowPrivilegeEscalation=false.
- Un ServiceAccount par service, `automountServiceAccountToken: false`.

## Istio
- Installation avec `istioctl install --set profile=default` (istiod et ingress gateway).
- `Gateway` et `VirtualService` pour le routage par chemin décrit plus haut. Accès depuis le poste via `minikube tunnel`.
- `PeerAuthentication` STRICT sur le namespace : mTLS obligatoire entre les pods.
- `AuthorizationPolicy` :
  - le port gRPC du catalogue n'accepte que le ServiceAccount d'emprunts ;
  - les ports REST n'acceptent que l'ingress gateway (et emprunts si besoin) ;
  - Postgres n'accepte que les deux services ;
  - tout le reste est refusé par défaut.
- `ServiceEntry` pour `openlibrary.org` : la sortie vers l'extérieur est déclarée explicitement.

## Sécurité du cluster
- **RBAC** : un rôle `lecteur` (lecture seule dans `bibliotheque`) et un rôle `deployeur` (gestion des Deployments, Services et ConfigMaps, sans accès aux Secrets), liés à des utilisateurs ou ServiceAccounts de démo. On le montre avec `kubectl auth can-i --as=...`.
- **Registry** : publication avec un access token Docker Hub (pas le mot de passe), scan des images avec Docker Scout ou Trivy.
- **Outils de contrôle** : Trivy (images), kubescape (manifests et cluster). Résultats documentés.
- mTLS et AuthorizationPolicy (section Istio).

## README
Minimaliste : une ligne de description, les commandes pour lancer le projet, puis un **résumé court des mesures implémentées** (une ligne par techno ou mesure, sans en faire trop).

## Tests
- Tests unitaires et de tranche (JUnit, `@WebMvcTest`, `@DataJpaTest` sur H2) pour la logique métier : import Open Library (client simulé), réservation et libération atomiques, création et retour d'un emprunt, cas 404 et 409.
- Test d'intégration gRPC catalogue ↔ emprunts en processus.
- Vérification finale sur minikube : parcours complet via la gateway, mTLS vérifié, un appel interdit bien refusé.

## Laissé au constructeur
- Organisation des packages, noms exacts des DTO, gestion d'erreurs détaillée.
- Version exacte de Spring Boot et de Spring gRPC : la dernière stable proposée par Initializr où la dépendance Spring gRPC est disponible.
- Scripts d'aide (build, push, deploy) en PowerShell si utile.
- Choix précis des requêtes d'import initiales.
