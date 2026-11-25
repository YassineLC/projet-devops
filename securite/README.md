# Sécurité

- **mTLS STRICT** (Istio) : un client hors mesh est rejeté (connexion coupée)
- **AuthorizationPolicy** : refus par défaut ; gRPC du catalogue réservé à `emprunts` ; Postgres réservé à `catalogue`/`emprunts` (sinon `403 RBAC: access denied`)
- **NetworkPolicies** : refus par défaut + flux autorisés uniquement (gateway → API/front, emprunts → catalogue:9090, services → postgres, catalogue → HTTPS externe)
- **RBAC** : rôles `lecteur` et `deployeur` (sans accès aux Secrets) ; ServiceAccounts applicatifs sans droit sur l'API ni token monté
- **Pods** : non-root, système de fichiers en lecture seule, capabilities supprimées, seccomp `RuntimeDefault`, limites CPU/mémoire
- **Images** : publiées avec un access token Docker Hub, scannées avec Trivy
- **kubescape** (framework NSA) : 85 % → 100 %

```powershell
kubescape scan framework nsa <(kubectl kustomize k8s/overlays/minikube)
docker run --rm -v //var/run/docker.sock:/var/run/docker.sock aquasec/trivy image --severity HIGH,CRITICAL yassinelahmar/biblio-catalogue:<tag>
kubectl auth can-i get secrets -n bibliotheque --as=system:serviceaccount:bibliotheque:dev-deployeur   # no
```
