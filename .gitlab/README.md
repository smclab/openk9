# Pipeline CI/CD — Branch di Release (`3.0.x`)

> Questo README documenta **solo** la pipeline del branch di release.
> La pipeline di sviluppo (su `main`) è documentata nel `README.md` di `main`
> e ha logica diversa (feature branch designati/generic, container scanning su tag,
> npm publish per chatbot, ecc.).
>
> `main` e `3.0.x` sono **due binari paralleli e indipendenti**.

---

## Indice

- [Contesto](#contesto)
- [I 4 contesti che attivano la pipeline](#i-4-contesti-che-attivano-la-pipeline)
- [Workflow tipico (dal bug al deploy)](#workflow-tipico-dal-bug-al-deploy)
- [Moduli presenti su 3.0.x](#moduli-presenti-su-30x)
- [Esclusione cross-domain](#esclusione-cross-domain)
- [Versioning delle immagini](#versioning-delle-immagini)
- [Sicurezza & Quality](#sicurezza--quality)
- [Struttura dei file CI](#struttura-dei-file-ci)
- [Rule riusabili (`child-rules.yaml`)](#rule-riusabili-child-rulesyaml)
- [Job presenti in ogni child](#job-presenti-in-ogni-child)
- [Estensione a branch futuri (3.1.x, 4.0.x, ...)](#estensione-a-branch-futuri-31x-40x-)
- [Cosa NON gira su 3.0.x](#cosa-non-gira-su-30x)
- [Debug: la mia pipeline è partita?](#debug-la-mia-pipeline-è-partita)

---

## Contesto

`3.0.x` è il **branch di release** per i clienti su versione 3.0. Ci vivono solo
fix che vengono "portati" da `main` (porting). Non si fa sviluppo nuovo qui.

- I branch di lavoro nascono **da `3.0.x`** e si chiamano `porting-<issue>-<slug>`.
- L'unico ambiente di deploy automatico è **`k9-stable-2`**.
- Il rilascio in produzione è gestito **manualmente** (ArgoCD/ops) — la pipeline
  produce solo gli artefatti (immagine versionata su Docker Hub).

---

## I 4 contesti che attivano la pipeline

| # | Evento | Cosa gira | Push immagine | Restart | Skopeo |
|---|--------|-----------|---------------|---------|--------|
| **A** | push `3.0.x` (= merge MR) | `Build <X> Release` + `Container Scanning <X>` + dep check globali | sì, tag = versione SNAPSHOT | sì, `k9-stable-2` | no |
| **B** | tag `3.0.N` | `Build <X> Release` + `Copy <X> to DockerHub` | sì, tag = `$CI_COMMIT_TAG` | **no** | sì → `smclab/*` |
| **C** | push `porting-*` (no MR) | `Build Verifier <X>` (compile only) | no | no | no |
| **D** | MR `porting-* → 3.0.x` | `Build Verifier <X>` (compile only) | no | no | no |

Tutto il resto (push su `main`, feature branch `1234-...`, MR generiche, tag non semver)
**non triggera nulla** su `3.0.x`.

---

## Workflow tipico (dal bug al deploy)

### 1. Crea il branch di porting

```bash
git checkout 3.0.x
git pull
git checkout -b porting-2076-pipeline-branch-release-3-0-x-general-fix
```

> Convenzione nome: `porting-<numero-issue>-<slug>`. Il prefisso `porting-` è
> quello che fa scattare le pipeline di verifica.

### 2. Cherry-pick / scrivi il fix, push

```bash
git push -u origin porting-2076-...
```

→ scatta `Build Verifier <Modulo>` per ogni modulo toccato (Contesto C). Solo compile, no push.

### 3. Apri MR `porting-2076-... → 3.0.x`

→ scatta lo stesso `Build Verifier <Modulo>` (Contesto D). Niente di più.

### 4. Review + merge su `3.0.x`

→ scatta `Build <Modulo> Release` + `Container Scanning <Modulo>` + `Trigger Restart <Modulo> Release`
(Contesto A). L'immagine SNAPSHOT viene pushata sul registry interno e
`k9-stable-2` riparte con la nuova versione. Le dep check globali (Maven/Python/Frontend)
girano una volta sola sulla pipeline parent.

### 5. Tag della release

Quando lo staging è validato, il maintainer fa:

```bash
git tag 3.0.4
git push origin 3.0.4
```

→ scatta `Build <Modulo> Release` (con tag immagine = `3.0.4`) + `Copy <Modulo> to DockerHub`
(Contesto B). L'immagine arriva su `docker.io/smclab/openk9-X:3.0.4`. **Niente restart**:
il deploy in produzione è manuale.

---

## Moduli presenti su `3.0.x`

### Backend Quarkus (8)
`datasource`, `searcher`, `ingestion`, `tenant-manager`, `file-manager`,
`resources-validator`, `tika`, `k8s-client`

### AI / Python (2)
- `rag-module` → `openk9-rag-module`
- `embedding-modules` → `openk9-openai-module` (Dockerfile.base)
  > Su `3.0.x` viene buildata **solo** l'immagine OpenAI; la sentence-transformers
  > variant non è inclusa in questa release line.

### Frontend Kaniko/Node (4 + 1)
- `search-frontend` → `openk9-search-frontend`
- `admin-ui` → `openk9-admin-ui`
- `tenant-ui` → `openk9-tenant-ui`
- `talk-to` → `openk9-talk-to`
- `openk9-chatbot` → libreria npm (no immagine Docker, **niente publish da `3.0.x`**)

### Connectors Kaniko/Python (6)
- `openk9-crawler` → `openk9-web-connector`
- `email-connector` → `openk9-email-connector`
- `database-connector` → `openk9-database-connector`
- `youtube-connector` → `openk9-youtube-connector`
- `gitlab-connector` → `openk9-gitlab-connector`
- `minio-connector` → `openk9-minio-connector`

### Moduli **NON** presenti su `3.0.x`

Esistono solo su `main`, non hanno child pipeline qui:
`api-gateway`, `agentic-rag-module`, `chunk-evaluation-module`, `docling-processor`,
`enrichers`.

---

## Esclusione cross-domain

Sui contesti **C** (push porting-*) e **D** (MR porting-*), il trigger parent filtra
per ruolo del developer. Se non sei "del dominio", la pipeline non parte:

| Tu sei | Tocchi file di | Pipeline che NON scatta |
|--------|----------------|--------------------------|
| backend (`mirko.zizzari`, `michele.bastianelli`) | frontend o AI | quelle del dominio toccato |
| frontend (`lorenzo.venneri`, `giorgio.bartolomeo`) | backend o AI | quelle del dominio toccato |
| AI (`luca.callocchia`) | backend o frontend | quelle del dominio toccato |
| **non listato** (es. `daniele.caldarini`) | qualsiasi cosa | tutte triggerano (è di fatto super-user) |

Su contesti **A** (push 3.0.x) e **B** (tag) le esclusioni **non si applicano** —
quelli sono eventi del maintainer.

> La logica è inline nelle `rules:` dei trigger parent (`backend.yaml`,
> `frontend.yaml`, `ai.yaml`). Per aggiungere/rimuovere utenti, modificare le
> condizioni `$GITLAB_USER_LOGIN != "..."` in quei file.

---

## Versioning delle immagini

### Su push `3.0.x` (Contesto A)

L'immagine è pushata sul registry interno con tag = **versione SNAPSHOT del branch**,
letta da fonti diverse a seconda del modulo:

| Modulo | Fonte | Esempio |
|--------|-------|---------|
| Backend Java | `core/pom.xml` (via `mvn help:evaluate`) | `3.0.3-SNAPSHOT` |
| AI (rag, embedding) | `python_modules_config.txt` riga `OPENK9_VERSION=` | `3.0.3-SNAPSHOT` |
| Connectors | `python_modules_config.txt` riga `OPENK9_VERSION=` | `3.0.3-SNAPSHOT` |
| Frontend | `js-packages/<modulo>/package.json` campo `version` | come definito |

> Connectors e AI condividono la stessa fonte (`python_modules_config.txt`).
> Quando il maintainer bumpa la SNAPSHOT (es. dopo `3.0.3` → passare a `3.0.4-SNAPSHOT`),
> deve aggiornare i pom Java + `python_modules_config.txt` + i `package.json` frontend.

### Su tag `3.0.N` (Contesto B)

L'immagine è pushata con tag = **`$CI_COMMIT_TAG` direttamente** (es. `3.0.4`),
**ignorando** i file di versione. Quindi per rilasciare basta `git tag 3.0.4 && git push --tags`,
senza dover allineare nulla a mano.

---

## Sicurezza & Quality

### Container Scanning (per modulo, post-build)

Job `Container Scanning <X>` in ogni child. Trivy (via `gtcs scan`) scansiona
l'immagine appena pushata sul registry interno.

- **Quando:** solo Contesto A (push `3.0.x`)
- **`allow_failure: true`** — un CVE non blocca restart
- Output: report SARIF/SBOM come artifact GitLab
- Stage: `container-scanning`, dopo `build`

### Dependency Check (globali, parent)

In `.gitlab/ci/quality.yaml` ci sono **3 job globali** (un solo job per pipeline
parent, non per modulo):

| Job | Tool | Quando scatta (`changes:`) |
|-----|------|----------------------------|
| `Maven Dependency Check` | OWASP Maven plugin | `core/**`, `vendor/**`, `pom.xml` |
| `Python Dependency Check` | `pip-audit` | `ai-packages/**`, `connectors/**` |
| `Frontend Dependency Check` | `yarn audit` | `js-packages/**` |

- **Quando:** solo Contesto A (push `3.0.x`)
- **`allow_failure: true`** — report come artifact, da consultare prima del tag

### SonarQube — non presente su `3.0.x`

Su release branch SonarQube non è eseguito: il codice portato qui è già
passato dal gate Sonar su `main`.

---

## Struttura dei file CI

```
.gitlab/
├── .gitlab-ci.yaml              # entry point: stages + include
├── .gitlab-templates.yaml       # template release: build, verifier,
│                                #   container-scanning, restart, skopeo
├── ci/
│   ├── backend.yaml             # 8 trigger backend
│   ├── frontend.yaml            # 5 trigger frontend (incl. chatbot)
│   ├── ai.yaml                  # 2 trigger AI
│   ├── common.yaml              # trigger connectors + 5 helm charts
│   ├── child-rules.yaml         # 6 rule release riusabili
│   └── quality.yaml             # 3 dep check globali
├── .gitlab-ci-datasource.yaml          # child: 1 file per modulo
├── .gitlab-ci-searcher.yaml
│   ... (16 child totali)
└── helm-charts-pipeline/
    └── .gitlab-ci-*.yaml        # child helm charts
```

### Flusso parent → child

1. Push/tag/MR su `3.0.x` → parte la **pipeline parent** (`.gitlab-ci.yaml`).
2. Parent include `backend.yaml`, `frontend.yaml`, `ai.yaml`, `common.yaml` →
   ognuno definisce dei `Trigger <X>` job che sono trigger jobs (non build).
3. Ogni `Trigger <X>` valuta `rules:` e (se matcha) lancia la **child pipeline**
   `.gitlab-ci-<modulo>.yaml` con strategy `depend` (= il parent aspetta).
4. Il child include `child-rules.yaml` e definisce i job effettivi
   (`Build <X> Release`, `Container Scanning <X>`, `Trigger Restart <X> Release`,
   `Build Verifier <X>`, `Copy <X> to DockerHub`).

### Variabili passate dal parent al child

| Variabile | Quando | Cosa fa |
|-----------|--------|---------|
| `RELEASE_MR=true` | MR `porting-* → 3.0.x` | abilita rule `child-verify-release-mr` |
| `PORTING_PUSH=true` | push diretto su `porting-*` | abilita rule `child-verify-porting-push` |

Push `3.0.x` e tag non passano variabili — le rule matchano direttamente
`$CI_COMMIT_BRANCH` e `$CI_COMMIT_TAG`.

---

## Rule riusabili (`child-rules.yaml`)

```yaml
.rules:child-build-always:
  rules:
    - when: on_success

# BUILD: push 3.0.x + tag 3.0.N
.rules:child-build-release:
  rules:
    - if: '$CI_COMMIT_BRANCH =~ /^\d+\.\d+\.x$/'
    - if: '$CI_COMMIT_TAG =~ /^\d+\.\d+\.\d+/'

# RESTART: solo push 3.0.x (tag NON auto-restartano)
.rules:child-restart-release:
  rules:
    - if: '$CI_COMMIT_BRANCH =~ /^\d+\.\d+\.x$/'

# SKOPEO a Docker Hub: solo tag 3.0.N
.rules:child-skopeo-release:
  rules:
    - if: '$CI_COMMIT_TAG =~ /^\d+\.\d+\.\d+/'

# VERIFY MR porting-* → 3.0.x
.rules:child-verify-release-mr:
  rules:
    - if: '$CI_PIPELINE_SOURCE == "parent_pipeline" && $RELEASE_MR == "true"'

# VERIFY push porting-* (no MR)
.rules:child-verify-porting-push:
  rules:
    - if: '$CI_PIPELINE_SOURCE == "parent_pipeline" && $PORTING_PUSH == "true"'
```

I job `Build Verifier <X>` usano **entrambe** le rule (`release-mr` + `porting-push`),
così è 1 solo job che copre i contesti C e D — non duplicazione.

---

## Job presenti in ogni child

### Backend Java (es. datasource)

| Job | Stage | Rule | Note |
|-----|-------|------|------|
| `Build <X> Release` | `build` | `child-build-release` | Maven build + Quarkus image push |
| `Build Verifier <X>` | `build-verifier` | `child-verify-release-mr` + `child-verify-porting-push` | Maven `package` no image |
| `Container Scanning <X>` | `container-scanning` | `child-restart-release` | Trivy scan |
| `Trigger Restart <X> Release` | `restart` | `child-restart-release` | Restart `k9-stable-2` |
| `Copy <X> to DockerHub` | `push` | `child-skopeo-release` | Skopeo registry → Docker Hub |

### Frontend (es. search-frontend)

Stessa struttura, in più:
- `Fetch version` (sempre, legge `package.json` → `version.env`)
- Build Verifier usa `node:22.14.0` + `yarn install --frozen-lockfile && lerna run build`
  (no Kaniko, evita ENOSPC sul runner)

### AI (rag-module / embedding-modules)

Stessa struttura, in più:
- `Fetch config` (sempre, legge `python_modules_config.txt` → `config.env`)
- Build Verifier = `python3 -m py_compile` su tutti i `.py`
- `embedding-modules` produce solo `openk9-openai-module` su `3.0.x` (la
  sentence-transformers variant è solo su `main`).

### Connectors

Stessa struttura, in più:
- `Fetch config` (sempre, come AI)
- 6 Build Release / Container Scanning / Skopeo, uno per connector
- Build Verifier = Kaniko `--no-push`

### `openk9-chatbot` (eccezione)

Pipeline ridotta: solo `Fetch version` + `Build Verifier OpenK9-Chatbot`.
Niente Build Release / Restart / Skopeo / Container Scanning — la libreria npm
si pubblica solo da `main`.

---

## Estensione a branch futuri (`3.1.x`, `4.0.x`, ...)

Le regex usano pattern generico `^\d+\.\d+\.x$` (branch) e `^\d+\.\d+\.\d+` (tag),
quindi qualsiasi futuro release branch `N.N.x` funziona automaticamente.

**Da modificare quando si crea un nuovo release branch:**

- `TARGET_ENV` nei `Trigger Restart <X> Release` se serve un ambiente diverso
  da `k9-stable-2` (es. `k9-stable-3` per `3.1.x`).
- `python_modules_config.txt` riga `OPENK9_VERSION=` con la nuova SNAPSHOT.
- Versioni nei `package.json` dei frontend.
- Versioni nei `pom.xml` Java.

---

## Cosa NON gira su `3.0.x`

Per chi viene dal workflow `main`, ricordati che NON ci sono:

- ❌ Feature branch `[0-9]+-...` (designated/generic logic con SNAPSHOT 998/999/996/997)
- ❌ MR generica verso `main`
- ❌ npm publish per `openk9-chatbot`
- ❌ SonarQube
- ❌ Restart automatico su tag (la prod è manuale)
- ❌ Skopeo a Docker Hub su push branch (solo SNAPSHOT, restano interni)

Se serve uno di questi flow, lavora su `main`. Su `3.0.x` la pipeline è
volutamente **minimal**: builda, scansiona, deploya in staging, su tag copia
l'artefatto pubblico.

---

## Debug: la mia pipeline è partita?

1. Pushi su `porting-2076-foo`.
2. GitLab → **CI/CD → Pipelines** → cerchi la pipeline associata al commit.
3. La parent pipeline mostra solo i `Trigger <Modulo>` (uno per modulo che hai
   toccato). Click → vedi la child pipeline con `Build Verifier <Modulo>`.
4. Se non vedi alcun trigger:
   - **Probabile causa 1:** i tuoi cambiamenti non matchano i `changes:` filter
     del modulo (es. hai toccato solo un README) → niente da buildare, è ok.
   - **Probabile causa 2:** sei in cross-domain exclusion (es. dev frontend
     che tocca file backend) → la pipeline backend non parte di proposito.
   - **Probabile causa 3:** il branch non ha prefisso `porting-` → nessun
     trigger matcha. Rinomina il branch.

### Validare le modifiche al CI in locale

```bash
# Lista tutti i job
gitlab-ci-local --list-all --file .gitlab/.gitlab-ci.yaml

# Simula uno scenario specifico (es. push 3.0.x)
gitlab-ci-local --list \
  --file .gitlab/.gitlab-ci-datasource.yaml \
  --variable CI_COMMIT_BRANCH=3.0.x \
  --variable CI_PIPELINE_SOURCE=parent_pipeline
```
