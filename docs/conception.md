# Conception

Bibliothèque en microservices, avec de vrais livres importés depuis Open Library.

- **catalogue-service** (Spring Boot) : livres et couvertures Open Library, REST + serveur gRPC (disponibilité, réserver, libérer)
- **emprunts-service** (Spring Boot) : emprunter et rendre, retour à 14 jours, client gRPC vers le catalogue
- **front** (React, shadcn) : catalogue et mes emprunts
- **Base de données** : H2 en local, PostgreSQL dans K8s (une base par service)
- **Docker** : builds multi-stage, images non-root sur Docker Hub
- **Kubernetes** : Deployments, Services, ConfigMaps, Secrets, StatefulSet Postgres, probes, limites
- **Istio** : gateway, mTLS strict, AuthorizationPolicy (qui appelle qui)
- **Sécurité** : RBAC, Trivy, kubescape, access token Docker Hub
- **Hors périmètre** : cloud, login, gestion des membres
