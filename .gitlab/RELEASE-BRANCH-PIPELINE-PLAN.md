# Release Branch Pipeline — Piano di Implementazione

## Contesto

- `main` e' il branch di sviluppo attivo (3.1.0+).
- `3.0.x` e' un branch di release per bugfix destinato a clienti con versione 3.0.0.
  Verra' abbandonato quando non ci saranno piu' clienti su quella versione.
- `porting-*` su `3.0.x` sono branch di fix, creati da `3.0.x`, con MR verso `3.0.x`.
  Non vanno mai su `main`.
- `k9-stable-2` e' l'unico ambiente di deploy per `3.0.x`.
  Non esistono ambienti personali per sviluppatori su questo branch.
- La logica utente-specifica (998/999/996-SNAPSHOT per Michele, Mirko, Lorenzo, ecc.)
  non si applica su `3.0.x`: nessun dev ha un env dedicato.
- In futuro potranno esistere altri branch di release (`3.1.x`, `4.0.x`, ecc.)
  con la stessa struttura, eventualmente puntando ad ambienti diversi.

---

## Comportamento atteso per branch di release

| Trigger                              | Build     | Push immagine          | Restart         | Scan/Quality     |
|--------------------------------------|-----------|------------------------|-----------------|------------------|
| `porting-*` MR verso `3.0.x`        | compile   | no                     | no              | build-verifier   |
| push/merge su `3.0.x`               | build     | tag `3.0.x-SNAPSHOT`  | k9-stable-2     | no               |
| tag `3.0.1`, `3.0.2`, ...           | build     | tag versione esatta    | k9-stable-2     | container-scan   |

---

## File modificati

### 1. `.gitlab/ci/child-rules.yaml`

Aggiungere due nuove rule che si affiancano a quelle esistenti senza toccarle.

```yaml
# Merge/push su qualsiasi release branch (3.0.x, 3.1.x, ...)
# oppure tag che corrisponde al pattern N.N.N
.rules:child-deploy-release:
  rules:
    - if: '$CI_COMMIT_BRANCH =~ /^\d+\.\d+\.x$/'
    - if: '$CI_COMMIT_TAG =~ /^\d+\.\d+\.\d+/'

# MR da porting-* verso un release branch
# Il discriminante e' CI_MERGE_REQUEST_TARGET_BRANCH_NAME, disponibile
# nel parent pipeline e passato come variabile al child tramite trigger.
.rules:child-verify-release-mr:
  rules:
    - if: '$CI_PIPELINE_SOURCE == "parent_pipeline" && $RELEASE_MR == "true"'
```

Le rule esistenti (`child-deploy-main-tag`, `child-verify-mr`, ecc.) rimangono invariate.

---

### 2. `.gitlab/ci/backend.yaml`, `frontend.yaml`, `ai.yaml`, `common.yaml`

Per ogni trigger job, aggiungere due rule in coda a quelle esistenti.

Esempio su `Trigger Datasource` (stesso schema per tutti i trigger):

```yaml
Trigger Datasource:
  stage: trigger
  rules:
    # ... regole esistenti invariate ...

    # AGGIUNTA: push/merge su release branch
    - if: '$CI_COMMIT_BRANCH =~ /^\d+\.\d+\.x$/'
      changes: *datasource_changes

    # AGGIUNTA: MR da porting-* verso release branch — compile only
    - if: '$CI_MERGE_REQUEST_TARGET_BRANCH_NAME =~ /^\d+\.\d+\.x$/'
      changes: *datasource_changes
      variables:
        RELEASE_MR: "true"
  trigger:
    include: "/.gitlab/.gitlab-ci-datasource.yaml"
    strategy: depend
```

Per i trigger dei moduli che non esistono in `3.0.x` (es. `agentic-rag-module`,
`docling-processor`, `chunk-evaluation-module`) queste due rule non vanno aggiunte.
Il job child non verra' mai invocato su quel branch, quindi non si rompe nulla.

Trigger frontend (`search-frontend`, `admin-ui`, `tenant-ui`, `talk-to`): stessa aggiunta.
`openk9-chatbot` non e' presente in `3.0.x`: non si aggiunge nulla.

Per i trigger helm in `common.yaml`: stessa aggiunta, solo per i chart presenti in `3.0.x`.

---

### 3. `.gitlab/.gitlab-templates.yaml`

Aggiungere un nuovo template di build specifico per release branch.
La differenza rispetto a `.maven-build-logic` e' che:
- non c'e' logica utente (nessun 998/999-SNAPSHOT)
- il tag SNAPSHOT usa il nome del branch come prefisso (`3.0.x-SNAPSHOT`)
- su tag ufficiale usa la versione esatta dal pom

```yaml
.maven-build-logic-release:
  script:
    - |
      echo "Release Build for: $COMPONENT"

      VERSION=$(mvn help:evaluate -Dexpression=project.version -q -DforceStdout)
      echo "VERSION=$VERSION"
      echo "$VERSION" > $CI_PROJECT_DIR/.version

      BASE_CMD="mvn $MAVEN_CLI_OPTS package \
        -Dmaven.repo.local=$CI_PROJECT_DIR/.m2/repository \
        -pl ../vendor/hibernate-rx-multitenancy/deployment,app/$COMPONENT -am"

      if [[ -n "$CI_COMMIT_TAG" ]]; then
        echo "Tag release: push $VERSION"
        eval "$BASE_CMD \
          -Dquarkus.container-image.build=true \
          -Dquarkus.container-image.push=true"

      elif [[ "$CI_COMMIT_BRANCH" =~ ^[0-9]+\.[0-9]+\.x$ ]]; then
        SNAP_TAG="${CI_COMMIT_BRANCH}-SNAPSHOT"
        echo "Release branch: push $SNAP_TAG"
        eval "$BASE_CMD \
          -Dquarkus.container-image.build=true \
          -Dquarkus.container-image.push=true \
          -Dquarkus.container-image.tag=$SNAP_TAG"
      fi
```

Analogo per frontend: la logica kaniko gia' presente nei child yaml verra' estesa
(vedi punto 4), non serve un template separato per frontend.

---

### 4. Child pipeline yaml — moduli presenti in `3.0.x`

Per ogni modulo presente in `3.0.x`, aggiungere tre job al child yaml esistente.
I job esistenti rimangono invariati.

Esempio su `.gitlab/.gitlab-ci-datasource.yaml`:

```yaml
# --- RELEASE BRANCH JOBS ---

Build Datasource Release:
  extends: .build_template
  variables:
    COMPONENT: "datasource"
  stage: build
  script:
    - cd core
    - !reference [.maven-build-logic-release, script]
  artifacts:
    paths:
      - .version
    expire_in: 24 hours
  rules:
    - !reference [.rules:child-deploy-release, rules]

Build Verifier Datasource Release MR:
  extends: .maven-verifier-template
  stage: build-verifier
  variables:
    COMPONENT: "datasource"
  script:
    - !reference [.maven-build-verifier-logic, script]
  rules:
    - !reference [.rules:child-verify-release-mr, rules]

Trigger Restart Datasource Release:
  extends: .restart_job_template
  dependencies:
    - Build Datasource Release
  variables:
    COMPONENT_NAME: "datasource"
    COMPONENT_TYPE: "backend"
    TARGET_ENV: "k9-stable-2"
  rules:
    - !reference [.rules:child-deploy-release, rules]
```

Per i child frontend (es. `.gitlab-ci-search-frontend.yaml`), il job di build
usa kaniko. La logica di push viene estesa con un nuovo ramo condizionale:

```yaml
# Nel job Build Search Frontend, nello script esistente, aggiungere:
elif [[ "$CI_COMMIT_BRANCH" =~ ^[0-9]+\.[0-9]+\.x$ ]]; then
  SNAP_TAG="${CI_COMMIT_BRANCH}-SNAPSHOT"
  KANIKO_PUSH="--destination $CI_REGISTRY_NAME/openk9/openk9-search-frontend:$SNAP_TAG"
  echo "Release branch: push $SNAP_TAG"
elif [[ -n "$CI_COMMIT_TAG" && "$CI_COMMIT_TAG" =~ ^[0-9]+\.[0-9]+\.[0-9]+ ]]; then
  source $CI_PROJECT_DIR/version.env
  KANIKO_PUSH="--destination $CI_REGISTRY_NAME/openk9/openk9-search-frontend:$VERSION"
  echo "Release tag: push $VERSION"
```

E aggiungere i job `Build Verifier Release MR` e `Trigger Restart Release`
con le nuove rules, analoghi al pattern backend.

---

## File toccati — riepilogo

| File                                          | Operazione  | Note                                              |
|-----------------------------------------------|-------------|---------------------------------------------------|
| `.gitlab/ci/child-rules.yaml`                 | modifica    | +2 rule: deploy-release, verify-release-mr        |
| `.gitlab/ci/backend.yaml`                     | modifica    | +2 rule per ogni trigger dei moduli in 3.0.x      |
| `.gitlab/ci/frontend.yaml`                    | modifica    | +2 rule per search-frontend, admin-ui, tenant-ui, talk-to |
| `.gitlab/ci/ai.yaml`                          | modifica    | +2 rule per rag-module (unico modulo AI in 3.0.x) |
| `.gitlab/ci/common.yaml`                      | modifica    | +2 rule per helm chart presenti in 3.0.x          |
| `.gitlab/.gitlab-templates.yaml`              | modifica    | +1 template maven-build-logic-release             |
| `.gitlab/.gitlab-ci-datasource.yaml`          | modifica    | +3 job release                                    |
| `.gitlab/.gitlab-ci-searcher.yaml`            | modifica    | +3 job release                                    |
| `.gitlab/.gitlab-ci-ingestion.yaml`           | modifica    | +3 job release                                    |
| `.gitlab/.gitlab-ci-tenant-manager.yaml`      | modifica    | +3 job release                                    |
| `.gitlab/.gitlab-ci-file-manager.yaml`        | modifica    | +3 job release                                    |
| `.gitlab/.gitlab-ci-entity-manager.yaml`      | modifica    | +3 job release                                    |
| `.gitlab/.gitlab-ci-resources-validator.yaml` | modifica    | +3 job release                                    |
| `.gitlab/.gitlab-ci-tika.yaml`                | modifica    | +3 job release                                    |
| `.gitlab/.gitlab-ci-api-gateway.yaml`         | modifica    | +3 job release                                    |
| `.gitlab/.gitlab-ci-search-frontend.yaml`     | modifica    | +logica kaniko release + 2 job release            |
| `.gitlab/.gitlab-ci-admin-frontend.yaml`      | modifica    | +logica kaniko release + 2 job release            |
| `.gitlab/.gitlab-ci-tenant-frontend.yaml`     | modifica    | +logica kaniko release + 2 job release            |
| `.gitlab/.gitlab-ci-talk-to.yaml`             | modifica    | +logica kaniko release + 2 job release            |
| `.gitlab/.gitlab-ci-rag-module.yaml`          | modifica    | +3 job release (se presente in 3.0.x)             |

Moduli NON presenti in `3.0.x` — nessuna modifica alla pipeline release:
- `api-gateway` (assente su 3.0.x)
- `agentic-rag-module` (assente su 3.0.x)
- `chunk-evaluation-module` (assente su 3.0.x)
- `docling-processor` (intera cartella `enrichers/` assente su 3.0.x)
- `quarkus-openk9-base-connector` (libreria base, non deployabile)

Moduli presenti su `3.0.x` — pipeline release da includere:

**Backend:** `datasource`, `ingestion`, `searcher`, `tenant-manager`, `file-manager`, `resources-validator`, `tika`, `k8s-client`

**AI:** `rag-module`, `embedding-modules`

**Frontend:** `search-frontend`, `admin-ui`, `tenant-ui`, `talk-to`, `openk9-chatbot`

**Connectors:** `openk9-crawler` (web), `email-connector`, `database-connector`, `youtube-connector`, `gitlab-connector`, `minio-connector`, `rest-api-connector`

> Nota: rispetto alla versione precedente di questo piano, `k8s-client`, `embedding-modules` e `openk9-chatbot` sono stati spostati nella lista dei presenti. `api-gateway` è stato aggiunto agli assenti.

---

## Estensione futura (3.1.x, 4.0.x, ...)

Le rule introdotte usano regex generica `/^\d+\.\d+\.x$/` e `/^\d+\.\d+\.\d+/`,
quindi funzionano automaticamente per qualsiasi futuro branch di release.

Se un futuro release branch deve puntare ad un ambiente diverso da `k9-stable-2`,
il mapping si gestisce nel template `.restart_job_template` tramite la variabile
`TARGET_ENV` gia' prevista, oppure con una logica `case` sul branch name nel
restart job, senza toccare le rules.

---

## Cosa non cambia

- Tutta la pipeline di `main` rimane invariata.
- La logica utente-specifica (998/999/996-SNAPSHOT) rimane solo per `main`.
- I job esistenti nei child yaml non vengono modificati, solo affiancati dai nuovi.
- Il file entry point `.gitlab/.gitlab-ci.yaml` non si tocca.
