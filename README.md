# projet-devops

Bibliothèque en microservices : vrais livres et couvertures importés depuis Open Library, emprunts et retours.

## Lancer

```powershell
minikube start -p biblio --cpus 4 --memory 8192
istioctl install --set profile=default -y
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.32/samples/addons/prometheus.yaml
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.32/samples/addons/kiali.yaml
kubectl apply -f https://github.com/bitnami-labs/sealed-secrets/releases/download/v0.41.2/controller.yaml
kubectl create namespace argocd
kubectl apply -n argocd --server-side -f https://raw.githubusercontent.com/argoproj/argo-cd/v3.6.3/manifests/install.yaml
kubectl apply -f argocd/application.yaml          # ArgoCD déploie k8s/overlays/minikube
kubectl -n istio-system port-forward svc/istio-ingressgateway 8080:80   # http://localhost:8080
kubectl -n istio-system port-forward svc/kiali 20001:20001              # http://localhost:20001
```

## Vérifier en local

```powershell
python scripts/course.py verify                # tests + Checkstyle/lint + politique de chaîne logicielle
python scripts/course.py supply-chain-policy   # actions épinglées par SHA, images de base par digest
docker buildx build --platform linux/amd64,linux/arm64 -f catalogue-service/Dockerfile .
```

`main` est protégée : toute modification passe par une pull request avec les contrôles verts.

## Implémenté

- **catalogue-service** (Spring Boot 4, Java 21) : REST paginé + serveur gRPC, import Open Library, réservation atomique
- **emprunts-service** (Spring Boot 4) : REST + client gRPC vers le catalogue, retour à 14 jours, statut en retard
- **gRPC** : contrat partagé `proto/catalogue.proto`
- **front** : React + Vite + shadcn, catalogue paginé, mes emprunts
- **Base de données** : H2 en local, PostgreSQL (StatefulSet + volume) dans K8s, une base par service
- **Tests** : JUnit/MockMvc, gRPC in-process, test de concurrence (31 tests)
- **Docker** : builds multi-stage, images non-root sur Docker Hub (`yassinelahmar/biblio-*`)
- **Kubernetes** : Kustomize (base + overlay), Deployments, Services, Secrets, ConfigMap, probes, limites, initContainer
- **Istio** : Gateway + VirtualService, mTLS STRICT, AuthorizationPolicy, ServiceEntry, circuit breaker, injection de panne (`k8s/demo`)
- **Observabilité** : Prometheus + Kiali
- **Sécurité** : RBAC, NetworkPolicies, pods durcis, Sealed Secrets, Trivy, kubescape NSA 100 % (détails : `securite/`)
- **CI** (structure de `charroux/rental`) : un workflow par microservice (`ci-catalogue`, `ci-emprunts`, `ci-front`) déclenché par chemin, actions épinglées par SHA, tests + Checkstyle/lint avec rapports en artefacts
- **Images** : buildx multi-arch (amd64 + arm64), publiées sur Docker Hub et GHCR
- **Supply chain** : signature Cosign sans clé, SBOM SPDX, attestations de provenance et de SBOM, vérification en CI, scan Trivy du digest
- **Dépôt** : Gitleaks, dependency review, Dependabot, manifests validés (kubeconform + kubescape)
- **CD** : ArgoCD (GitOps), la CI épingle le digest signé dans `k8s/overlays/minikube`
