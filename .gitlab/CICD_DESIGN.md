# 🚀 OpenK9 Deployment Process - Detailed Flows

## 📋 Overview

Questo documento fornisce diagrammi dettagliati dei flussi di deployment in OpenK9, con focus su:
- Pipeline CI/CD e trigger esterni
- Logica di restart tramite ArgoCD
- Flussi specifici per utente (Mirko, Lorenzo, Luca)

---

## 🏗️ Architettura Pipeline Generale

```mermaid
graph TB
    subgraph "Developer Workspace"
        DEV[👨‍💻 Developer]
        GIT[📝 Git Push]
    end
    
    subgraph "GitLab CI/CD - OpenK9 Repo"
        MAIN_PIPELINE[🎯 Main Pipeline<br/>.gitlab-ci.yaml]
        TRIGGER_DETECT{🔍 Detect Changes}
        
        subgraph "Component Pipelines"
            BE[🔧 Backend Modules]
            FE[🎨 Frontend Modules]
            AI[🤖 AI Modules]
        end
        
        BUILD[🏗️ Build & Push Image]
        RESTART_TPL[🔄 Restart Template<br/>.restart_job_template]
    end
    
    subgraph "External Pipeline via Token"
        EXT_PIPELINE[🌐 External Restart Pipeline]
        USER_VALIDATE{👤 Validate User}
        NS_RESOLVE{🎯 Resolve Namespaces}
        ARGO_CALL[⚙️ ArgoCD Call]
    end
    
    subgraph "ArgoCD"
        ARGO_UPDATE[📝 Update image.tag]
        ARGO_RESTART[🔄 Restart Deployment]
    end
    
    subgraph "Kubernetes Namespaces"
        K9_BE[☸️ k9-backend]
        K9_BE01[☸️ k9-backend01]
        K9_TEST[☸️ k9-test]
        K9_AI[☸️ k9-ai]
        K9_FE[☸️ k9-frontend]
    end
    
    DEV --> GIT
    GIT --> MAIN_PIPELINE
    MAIN_PIPELINE --> TRIGGER_DETECT
    
    TRIGGER_DETECT --> BE
    TRIGGER_DETECT --> FE
    TRIGGER_DETECT --> AI
    
    BE --> BUILD
    FE --> BUILD
    AI --> BUILD
    
    BUILD --> RESTART_TPL
    
    RESTART_TPL -->|curl POST<br/>with TOKEN| EXT_PIPELINE
    
    EXT_PIPELINE --> USER_VALIDATE
    USER_VALIDATE --> NS_RESOLVE
    NS_RESOLVE --> ARGO_CALL
    
    ARGO_CALL --> ARGO_UPDATE
    ARGO_UPDATE --> ARGO_RESTART
    
    ARGO_RESTART --> K9_BE
    ARGO_RESTART --> K9_BE01
    ARGO_RESTART --> K9_TEST
    ARGO_RESTART --> K9_AI
    ARGO_RESTART --> K9_FE
```

---

## 🔄 Restart Template Logic

### Come Funziona il Restart

```mermaid
sequenceDiagram
    participant Pipeline as 🎯 OpenK9 Pipeline
    participant Template as 🔄 Restart Template
    participant Token as 🔑 RESTART_TRIGGER_TOKEN
    participant External as 🌐 External Pipeline
    participant ArgoCD as ⚙️ ArgoCD CLI
    participant K8s as ☸️ Kubernetes
    
    Pipeline->>Template: Job completed, trigger restart
    Template->>Template: Read variables:<br/>- COMPONENT_NAME<br/>- COMPONENT_TYPE<br/>- IMAGE_TAG<br/>- GITLAB_USER_LOGIN<br/>- CI_COMMIT_BRANCH
    
    Template->>External: curl -X POST<br/>-F "token=${RESTART_TRIGGER_TOKEN}"<br/>-F "variables[TRIGGER_JOB]=restart-{component}"<br/>-F "variables[COMPONENT_TYPE]={type}"<br/>-F "variables[IMAGE_TAG]={tag}"<br/>-F "variables[GITLAB_USER_LOGIN]={user}"<br/>-F "variables[CI_COMMIT_BRANCH]={branch}"
    
    External->>External: Extract COMPONENT_NAME from TRIGGER_JOB
    External->>External: Determine target namespace(s)
    
    loop For each namespace
        External->>External: Build ArgoCD app name<br/>e.g., datasource-backend, admin-ui-frontend
        
        External->>ArgoCD: argocd app get {app-name}
        ArgoCD-->>External: ✅ App exists
        
        External->>ArgoCD: argocd app set {app-name}<br/>--helm-set image.tag={IMAGE_TAG}
        ArgoCD-->>External: ✅ Helm value updated
        
        External->>ArgoCD: argocd app actions run {app-name} restart<br/>--kind Deployment --namespace {ns}
        ArgoCD->>K8s: Restart deployment in namespace
        K8s-->>ArgoCD: ✅ Restarted
        ArgoCD-->>External: ✅ Action completed
    end
    
    External-->>Template: ✅ All restarts complete
    Template-->>Pipeline: ✅ Pipeline complete
```


---

## 🌿 Feature Branch: Mirko Zizzari (Backend Developer)

### Scenario: Mirko sviluppa feature su datasource

```mermaid
sequenceDiagram
    autonumber
    participant Mirko as 👨‍💻 Mirko Zizzari
    participant Git as 📝 GitLab
    participant Pipeline as 🎯 Datasource Pipeline
    participant Build as 🏗️ Build Stage
    participant Registry as 📦 Container Registry
    participant Restart as 🔄 Restart Template
    participant External as 🌐 External Pipeline
    participant ArgoCD as ⚙️ ArgoCD
    participant Backend as ☸️ k9-backend
    
    Mirko->>Git: git push origin 1234-datasource-feature
    Git->>Pipeline: Trigger .gitlab-ci-datasource.yaml
    
    Pipeline->>Pipeline: Detect branch: 1234-datasource-feature
    Pipeline->>Pipeline: Match pattern: ^[0-9]+-.*
    Pipeline->>Pipeline: Check user: mirko.zizzari
    
    Pipeline->>Build: mvn package -pl app/datasource<br/>-Dquarkus.container-image.build=true<br/>-Dquarkus.container-image.push=true<br/>-Dquarkus.container-image.tag=999-SNAPSHOT
    
    Build->>Registry: Push openk9-datasource:999-SNAPSHOT
    Registry-->>Build: ✅ Image pushed
    
    Build->>Restart: Trigger restart_job_template
    Restart->>Restart: Variables:<br/>- COMPONENT_NAME=datasource<br/>- COMPONENT_TYPE=backend<br/>- IMAGE_TAG=999-SNAPSHOT<br/>- GITLAB_USER_LOGIN=mirko.zizzari<br/>- CI_COMMIT_BRANCH=1234-datasource-feature
    
    Restart->>External: curl POST with TOKEN<br/>All variables passed
    
    External->>External: User: mirko.zizzari
    External->>External: Lookup: MIRKO_ZIZZARI_NAMESPACES
    External->>External: ➡️ Resolve: k9-backend k9-backend-oracle
    External->>External: Component: backend → use k9-backend
    External->>External: Build app name: datasource-backend
    
    External->>ArgoCD: argocd app get datasource-backend
    ArgoCD-->>External: ✅ App found
    
    External->>ArgoCD: argocd app set datasource-backend<br/>--helm-set image.tag=999-SNAPSHOT
    ArgoCD-->>External: ✅ Helm value updated
    
    External->>ArgoCD: argocd app actions run datasource-backend restart<br/>--kind Deployment --namespace k9-backend
    ArgoCD->>Backend: Restart deployment
    Backend->>Backend: Rolling update with 999-SNAPSHOT
    Backend-->>ArgoCD: ✅ Restarted
    ArgoCD-->>External: ✅ Action completed
    
    External-->>Restart: ✅ Success
    Restart-->>Pipeline: ✅ Complete
    Pipeline-->>Git: ✅ Pipeline passed
    Git-->>Mirko: 📧 Deployment successful in k9-backend
```

### Mirko: Main Branch Merge

```mermaid
sequenceDiagram
    autonumber
    participant Mirko as 👨‍💻 Mirko Zizzari
    participant Git as 📝 GitLab
    participant Pipeline as 🎯 Datasource Pipeline
    participant Build as 🏗️ Build Stage
    participant Restart as 🔄 Restart Template
    participant External as 🌐 External Pipeline
    participant ArgoCD as ⚙️ ArgoCD
    
    participant BE01 as ☸️ k9-backend01
    participant TEST as ☸️ k9-test
    participant AI as ☸️ k9-ai
    participant FE as ☸️ k9-frontend
    
    Note over Mirko,Git: Mirko merges to main<br/>k9-backend already has the changes
    
    Mirko->>Git: Merge to main
    Git->>Pipeline: Trigger on main branch
    
    Pipeline->>Build: Build with VERSION=3.1.0-SNAPSHOT
    Build->>Build: Push image to registry
    
    Build->>Restart: Trigger restart
    Restart->>Restart: Branch: main<br/>Component: backend<br/>IMAGE_TAG=3.1.0-SNAPSHOT
    
    Note over Restart,External: Main branch: restart ALL namespaces<br/>EXCEPT k9-backend (already deployed)
    
    Note over Restart,External: Backend component on main:<br/>NAMESPACES="k9-ai k9-frontend"<br/>(excludes k9-backend where merge originated)
    
    par Restart k9-ai
        Restart->>External: Single call with all variables
        External->>External: Loop: namespace k9-ai
        External->>External: Build app: datasource-ai
        External->>ArgoCD: argocd app set datasource-ai<br/>--helm-set image.tag=3.1.0-SNAPSHOT
        External->>ArgoCD: argocd app actions run datasource-ai restart<br/>--kind Deployment --namespace k9-ai
        ArgoCD->>AI: Restart deployment
        AI-->>ArgoCD: ✅ Deployed
    and Restart k9-frontend
        External->>External: Loop: namespace k9-frontend
        External->>External: Build app: datasource-frontend
        External->>ArgoCD: argocd app set datasource-frontend<br/>--helm-set image.tag=3.1.0-SNAPSHOT
        External->>ArgoCD: argocd app actions run datasource-frontend restart<br/>--kind Deployment --namespace k9-frontend
        ArgoCD->>FE: Restart deployment
        FE-->>ArgoCD: ✅ Deployed
    end
    
    ArgoCD-->>Restart: ✅ All deployments complete
    Restart-->>Pipeline: ✅ Success
    Pipeline-->>Mirko: 📧 Deployed to all namespaces except k9-backend
```


---

## 🎨 Feature Branch: Lorenzo Venneri (Frontend Developer)

### Scenario: Lorenzo sviluppa feature su admin-ui

```mermaid
sequenceDiagram
    autonumber
    participant Lorenzo as 👨‍💻 Lorenzo Venneri
    participant Git as 📝 GitLab
    participant Pipeline as 🎯 Admin-UI Pipeline
    participant Build as 🏗️ Build Stage
    participant Registry as 📦 Container Registry
    participant Restart as 🔄 Restart Template
    participant External as 🌐 External Pipeline
    participant ArgoCD as ⚙️ ArgoCD
    participant Frontend as ☸️ k9-frontend
    
    Lorenzo->>Git: git push origin 5678-admin-ui-feature
    Git->>Pipeline: Trigger .gitlab-ci-admin-frontend.yaml
    
    Pipeline->>Pipeline: Detect branch: 5678-admin-ui-feature
    Pipeline->>Pipeline: Match pattern: ^[0-9]+-.*
    Pipeline->>Pipeline: Check user: lorenzo.venneri
    
    Pipeline->>Build: yarn build<br/>docker build<br/>docker push with tag 996-SNAPSHOT
    
    Build->>Registry: Push openk9-admin-ui:996-SNAPSHOT
    Registry-->>Build: ✅ Image pushed
    
    Build->>Restart: Trigger restart_job_template
    Restart->>Restart: Variables:<br/>- COMPONENT_NAME=admin-ui<br/>- COMPONENT_TYPE=frontend<br/>- IMAGE_TAG=996-SNAPSHOT<br/>- GITLAB_USER_LOGIN=lorenzo.venneri<br/>- CI_COMMIT_BRANCH=5678-admin-ui-feature
    
    Restart->>External: curl POST with TOKEN<br/>All variables passed
    
    External->>External: User: lorenzo.venneri
    External->>External: Lookup: LORENZO_VENNERI_NAMESPACES
    External->>External: ➡️ Resolve: k9-frontend
    External->>External: Build app name: admin-ui-frontend
    
    External->>ArgoCD: argocd app get admin-ui-frontend
    ArgoCD-->>External: ✅ App found
    
    External->>ArgoCD: argocd app set admin-ui-frontend<br/>--helm-set image.tag=996-SNAPSHOT
    ArgoCD-->>External: ✅ Helm value updated
    
    External->>ArgoCD: argocd app actions run admin-ui-frontend restart<br/>--kind Deployment --namespace k9-frontend
    ArgoCD->>Frontend: Restart deployment
    Frontend->>Frontend: Rolling update with 996-SNAPSHOT
    Frontend-->>ArgoCD: ✅ Restarted
    ArgoCD-->>External: ✅ Action completed
    
    External-->>Restart: ✅ Success
    Restart-->>Pipeline: ✅ Complete
    Pipeline-->>Git: ✅ Pipeline passed
    Git-->>Lorenzo: 📧 Deployment successful in k9-frontend
```

### Lorenzo: Main Branch Merge

```mermaid
sequenceDiagram
    autonumber
    participant Lorenzo as 👨‍💻 Lorenzo Venneri
    participant Git as 📝 GitLab
    participant Pipeline as 🎯 Admin-UI Pipeline
    participant Build as 🏗️ Build Stage
    participant Restart as 🔄 Restart Template
    participant External as 🌐 External Pipeline
    participant ArgoCD as ⚙️ ArgoCD
    
    participant BE as ☸️ k9-backend
    participant BE01 as ☸️ k9-backend01
    participant TEST as ☸️ k9-test
    participant AI as ☸️ k9-ai
    
    Note over Lorenzo,Git: Lorenzo merges to main<br/>k9-frontend already has the changes
    
    Lorenzo->>Git: Merge to main
    Git->>Pipeline: Trigger on main branch
    
    Pipeline->>Build: Build with VERSION=1.2.3
    Build->>Build: Push image to registry
    
    Build->>Restart: Trigger restart
    Restart->>Restart: Branch: main<br/>Component: frontend<br/>IMAGE_TAG=1.2.3
    
    Note over Restart,External: Main branch: restart ALL namespaces<br/>EXCEPT k9-frontend (already deployed)
    
    par Restart k9-backend
        Restart->>External: curl POST NAMESPACE=k9-backend
        External->>ArgoCD: Update k9-backend/admin-ui
        ArgoCD->>BE: Restart with 1.2.3
        BE-->>ArgoCD: ✅ Deployed
    and Restart k9-backend01
        Restart->>External: curl POST NAMESPACE=k9-backend01
        External->>ArgoCD: Update k9-backend01/admin-ui
        ArgoCD->>BE01: Restart with 1.2.3
        BE01-->>ArgoCD: ✅ Deployed
    and Restart k9-test
        Restart->>External: curl POST NAMESPACE=k9-test
        External->>ArgoCD: Update k9-test/admin-ui
        ArgoCD->>TEST: Restart with 1.2.3
        TEST-->>ArgoCD: ✅ Deployed
    and Restart k9-ai
        Restart->>External: curl POST NAMESPACE=k9-ai
        External->>ArgoCD: Update k9-ai/admin-ui
        ArgoCD->>AI: Restart with 1.2.3
        AI-->>ArgoCD: ✅ Deployed
    end
    
    ArgoCD-->>Restart: ✅ All deployments complete
    Restart-->>Pipeline: ✅ Success
    Pipeline-->>Lorenzo: 📧 Deployed to all namespaces except k9-frontend
```


---

## 🤖 Feature Branch: Luca Callocchia (AI Developer)

### Scenario: Luca sviluppa feature su rag-module

```mermaid
sequenceDiagram
    autonumber
    participant Luca as 👨‍💻 Luca Callocchia
    participant Git as 📝 GitLab
    participant Pipeline as 🎯 RAG Module Pipeline
    participant Build as 🏗️ Build Stage
    participant Registry as 📦 Container Registry
    participant Restart as 🔄 Restart Template
    participant External as 🌐 External Pipeline
    participant ArgoCD as ⚙️ ArgoCD
    participant AI as ☸️ k9-ai
    
    Luca->>Git: git push origin 9012-rag-improvements
    Git->>Pipeline: Trigger .gitlab-ci-rag-module.yaml
    
    Pipeline->>Pipeline: Detect branch: 9012-rag-improvements
    Pipeline->>Pipeline: Match pattern: ^[0-9]+-.*
    Pipeline->>Pipeline: Check user: luca.callocchia
    
    Pipeline->>Build: docker build<br/>docker push with tag 997-SNAPSHOT
    
    Build->>Registry: Push openk9-rag-module:997-SNAPSHOT
    Registry-->>Build: ✅ Image pushed
    
    Build->>Restart: Trigger restart_job_template
    Restart->>Restart: Variables:<br/>- COMPONENT_NAME=rag-module<br/>- COMPONENT_TYPE=ai<br/>- IMAGE_TAG=997-SNAPSHOT<br/>- GITLAB_USER_LOGIN=luca.callocchia<br/>- CI_COMMIT_BRANCH=9012-rag-improvements
    
    Restart->>External: curl POST with TOKEN<br/>All variables passed
    
    External->>External: User: luca.callocchia
    External->>External: Lookup: LUCA_CALLOCCHIA_NAMESPACES
    External->>External: ➡️ Resolve: k9-ai
    External->>External: Build app name: rag-module-ai
    
    External->>ArgoCD: argocd app get rag-module-ai
    ArgoCD-->>External: ✅ App found
    
    External->>ArgoCD: argocd app set rag-module-ai<br/>--helm-set image.tag=997-SNAPSHOT
    ArgoCD-->>External: ✅ Helm value updated
    
    External->>ArgoCD: argocd app actions run rag-module-ai restart<br/>--kind Deployment --namespace k9-ai
    ArgoCD->>AI: Restart deployment
    AI->>AI: Rolling update with 997-SNAPSHOT
    AI-->>ArgoCD: ✅ Restarted
    ArgoCD-->>External: ✅ Action completed
    
    External-->>Restart: ✅ Success
    Restart-->>Pipeline: ✅ Complete
    Pipeline-->>Git: ✅ Pipeline passed
    Git-->>Luca: 📧 Deployment successful in k9-ai
```

### Luca: Main Branch Merge

```mermaid
sequenceDiagram
    autonumber
    participant Luca as 👨‍💻 Luca Callocchia
    participant Git as 📝 GitLab
    participant Pipeline as 🎯 RAG Module Pipeline
    participant Build as 🏗️ Build Stage
    participant Restart as 🔄 Restart Template
    participant External as 🌐 External Pipeline
    participant ArgoCD as ⚙️ ArgoCD
    
    participant BE as ☸️ k9-backend
    participant BE01 as ☸️ k9-backend01
    participant TEST as ☸️ k9-test
    participant FE as ☸️ k9-frontend
    
    Note over Luca,Git: Luca merges to main<br/>k9-ai already has the changes
    
    Luca->>Git: Merge to main
    Git->>Pipeline: Trigger on main branch
    
    Pipeline->>Build: Build with VERSION=2.0.0
    Build->>Build: Push image to registry
    
    Build->>Restart: Trigger restart
    Restart->>Restart: Branch: main<br/>Component: ai<br/>IMAGE_TAG=2.0.0
    
    Note over Restart,External: Main branch: restart ALL namespaces<br/>EXCEPT k9-ai (already deployed)
    
    par Restart k9-backend
        Restart->>External: curl POST NAMESPACE=k9-backend
        External->>ArgoCD: Update k9-backend/rag-module
        ArgoCD->>BE: Restart with 2.0.0
        BE-->>ArgoCD: ✅ Deployed
    and Restart k9-backend01
        Restart->>External: curl POST NAMESPACE=k9-backend01
        External->>ArgoCD: Update k9-backend01/rag-module
        ArgoCD->>BE01: Restart with 2.0.0
        BE01-->>ArgoCD: ✅ Deployed
    and Restart k9-test
        Restart->>External: curl POST NAMESPACE=k9-test
        External->>ArgoCD: Update k9-test/rag-module
        ArgoCD->>TEST: Restart with 2.0.0
        TEST-->>ArgoCD: ✅ Deployed
    and Restart k9-frontend
        Restart->>External: curl POST NAMESPACE=k9-frontend
        External->>ArgoCD: Update k9-frontend/rag-module
        ArgoCD->>FE: Restart with 2.0.0
        FE-->>ArgoCD: ✅ Deployed
    end
    
    ArgoCD-->>Restart: ✅ All deployments complete
    Restart-->>Pipeline: ✅ Success
    Pipeline-->>Luca: 📧 Deployed to all namespaces except k9-ai
```


---

## 🔄 Namespace Resolution Logic

### Feature Branch Decision Tree

```mermaid
graph TD
    START[🚀 Feature Branch Push] --> USER_CHECK{User?}
    
    USER_CHECK -->|mirko.zizzari| MIRKO_COMP{Component Type?}
    USER_CHECK -->|michele.bastianelli| MICHELE_COMP{Component Type?}
    USER_CHECK -->|luca.callocchia| LUCA_COMP{Component Type?}
    USER_CHECK -->|lorenzo.venneri<br/>giorgio.bartolomeo| LORENZO_COMP{Component Type?}
    USER_CHECK -->|other| ERROR_USER[❌ Unknown User]
    
    MIRKO_COMP -->|backend| MIRKO_BE[🎯 k9-backend<br/>999-SNAPSHOT]
    MIRKO_COMP -->|backend-oracle| MIRKO_ORACLE[🎯 k9-backend-oracle<br/>999-SNAPSHOT]
    MIRKO_COMP -->|test| MIRKO_TEST[🎯 k9-test<br/>latest]
    MIRKO_COMP -->|other| ERROR_COMP[❌ Invalid Component]
    
    MICHELE_COMP -->|backend| MICHELE_BE[🎯 k9-backend01<br/>998-SNAPSHOT]
    MICHELE_COMP -->|other| ERROR_COMP
    
    LUCA_COMP -->|ai| LUCA_AI[🎯 k9-ai<br/>997-SNAPSHOT]
    LUCA_COMP -->|other| ERROR_COMP
    
    LORENZO_COMP -->|frontend| LORENZO_FE[🎯 k9-frontend<br/>996-SNAPSHOT]
    LORENZO_COMP -->|other| ERROR_COMP
    
    MIRKO_BE --> DEPLOY[🚀 Deploy to Namespace]
    MIRKO_ORACLE --> DEPLOY
    MIRKO_TEST --> DEPLOY
    MICHELE_BE --> DEPLOY
    LUCA_AI --> DEPLOY
    LORENZO_FE --> DEPLOY
    
    DEPLOY --> SUCCESS[✅ Deployed]
    
    ERROR_USER --> FAIL[❌ Pipeline Failed]
    ERROR_COMP --> FAIL
    
    style SUCCESS fill:#e1ffe1
    style FAIL fill:#ffe1e1
```

### Main Branch Namespace Exclusion Logic

```mermaid
graph TD
    START[🚀 Main Branch Merge] --> COMP_TYPE{Component Type?}
    
    COMP_TYPE -->|backend| BE_LOGIC[Backend Component]
    COMP_TYPE -->|frontend| FE_LOGIC[Frontend Component]
    COMP_TYPE -->|ai| AI_LOGIC[AI Component]
    
    BE_LOGIC --> BE_NS{Deploy to?}
    BE_NS -->|✅| BE_OTHER[k9-backend01<br/>k9-test<br/>k9-ai<br/>k9-frontend]
    BE_NS -->|❌ SKIP| BE_SKIP[k9-backend<br/>Already deployed]
    
    FE_LOGIC --> FE_NS{Deploy to?}
    FE_NS -->|✅| FE_OTHER[k9-backend<br/>k9-backend01<br/>k9-test<br/>k9-ai]
    FE_NS -->|❌ SKIP| FE_SKIP[k9-frontend<br/>Already deployed]
    
    AI_LOGIC --> AI_NS{Deploy to?}
    AI_NS -->|✅| AI_OTHER[k9-backend<br/>k9-backend01<br/>k9-test<br/>k9-frontend]
    AI_NS -->|❌ SKIP| AI_SKIP[k9-ai<br/>Already deployed]
    
    BE_OTHER --> DEPLOY[🚀 Deploy]
    FE_OTHER --> DEPLOY
    AI_OTHER --> DEPLOY
    
    DEPLOY --> SUCCESS[✅ All Deployed]
    
    style BE_OTHER fill:#e1ffe1
    style FE_OTHER fill:#e1ffe1
    style AI_OTHER fill:#e1ffe1
    style BE_SKIP fill:#fff4e1
    style FE_SKIP fill:#fff4e1
    style AI_SKIP fill:#fff4e1
```


---








### Image Tags per Scenario

| Scenario | Backend | Frontend | AI |
|----------|---------|----------|-----|
| **Feature - Mirko** | 999-SNAPSHOT | - | - |
| **Feature - Michele** | 998-SNAPSHOT | - | - |
| **Feature - Lorenzo/Giorgio** | - | 996-SNAPSHOT | - |
| **Feature - Luca** | - | - | 997-SNAPSHOT |
| **Main Branch** | from .version | from version.env | from config.env |
| **Git Tag** | tag name | tag name | tag name |

### Pipeline Variables Passate all'External Pipeline

```bash
# Variabili passate da OpenK9 Pipeline
TRIGGER_JOB=restart-{component-name}     # e.g., restart-datasource
COMPONENT_TYPE={backend|frontend|ai}     # Component category
IMAGE_TAG={tag}                          # Image tag to deploy
GITLAB_USER_LOGIN={username}             # GitLab user who triggered
CI_COMMIT_BRANCH={branch-name}           # Source branch

# External Pipeline estrae:
COMPONENT_NAME="${TRIGGER_JOB#restart-}" # Rimuove "restart-" prefix

# External Pipeline determina namespace:
# - Feature branch: lookup USER_NAMESPACES variable
# - Main branch: determina da COMPONENT_TYPE (esclude namespace di origine)
```

### External Pipeline: Namespace Resolution Logic

```bash
# Feature Branch
if [[ "$CI_COMMIT_BRANCH" =~ ^[0-9]+-.*$ ]]; then
  USER_VAR=$(echo "${GITLAB_USER_LOGIN}" | tr '.-' '_' | tr '[:lower:]' '[:upper:]')_NAMESPACES
  USER_NAMESPACES=$(eval echo \$${USER_VAR})
  # e.g., MIRKO_ZIZZARI_NAMESPACES="k9-backend k9-backend-oracle"
  NAMESPACES="$USER_NAMESPACES"
fi

# Main Branch
if [[ "$CI_COMMIT_BRANCH" == "main" ]]; then
  case "$COMPONENT_TYPE" in
    "backend")
      NAMESPACES="k9-ai k9-frontend"  # Excludes k9-backend
      ;;
    "frontend")
      NAMESPACES="k9-backend k9-backend01 k9-test k9-ai"  # Excludes k9-frontend
      ;;
    "ai")
      NAMESPACES="k9-backend k9-backend01 k9-test k9-frontend"  # Excludes k9-ai
      ;;
  esac
fi

# ArgoCD App Name Generation
get_argocd_app_name() {
  local component=$1
  local target_ns=$2
  
  case "$target_ns" in
    "k9-backend") echo "${component}-backend" ;;
    "k9-backend01") echo "${component}-backend01" ;;
    "k9-backend-oracle") echo "${component}-oracle" ;;
    "k9-ai") echo "${component}-ai" ;;
    "k9-frontend") echo "${component}-frontend" ;;
  esac
}

# Restart Loop
for ns in $NAMESPACES; do
  ARGOCD_APP_NAME=$(get_argocd_app_name "$COMPONENT_NAME" "$ns")
  
  argocd app set "$ARGOCD_APP_NAME" --helm-set image.tag=$IMAGE_TAG
  argocd app actions run "$ARGOCD_APP_NAME" restart \
    --kind Deployment --namespace "$ns"
done
```

---

## 🔗 Related Documentation

- [Main Deployment Process](DEPLOYMENT-PROCESS.md) - Overview semplificato
- [Pipeline Architecture](README.md) - Struttura pipeline
- [GitLab CI Templates](.gitlab-templates.yaml) - Template riutilizzabili
- [Component Pipelines](.gitlab/) - Pipeline specifiche per componente

---

**Last Updated**: December 3, 2025 s 

