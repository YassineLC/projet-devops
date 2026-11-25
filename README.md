# projet-devops

Bibliothèque en microservices : vrais livres et couvertures importés depuis Open Library, emprunts et retours.

## Lancer

```powershell
minikube start -p biblio --cpus 4 --memory 8192
istioctl install --set profile=default -y
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.32/samples/addons/prometheus.yaml
kubectl apply -f https://raw.githubusercontent.com/istio/istio/release-1.32/samples/addons/kiali.yaml
kubectl create namespace argocd
kubectl apply -n argocd --server-side -f https://raw.githubusercontent.com/argoproj/argo-cd/v3.6.3/manifests/install.yaml
kubectl apply -f argocd/application.yaml          # ArgoCD déploie k8s/overlays/minikube
kubectl -n istio-system port-forward svc/istio-ingressgateway 8080:80   # http://localhost:8080
kubectl -n istio-system port-forward svc/kiali 20001:20001              # http://localhost:20001
```

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
- **Sécurité** : RBAC, NetworkPolicies, pods durcis, Trivy, kubescape NSA 100 % (détails : `securite/`)
- **CI** : GitHub Actions (tests, build front, validation des manifests, images, scan Trivy, push Docker Hub)
- **CD** : ArgoCD (GitOps), la CI met à jour le tag d'image dans `k8s/overlays/minikube`
