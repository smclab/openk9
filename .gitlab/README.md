# Pipeline CI/CD — Branch di Release (3.0.x)

Questo documento descrive il funzionamento della pipeline su branch di release (`N.N.x`).
Per la documentazione generale della pipeline su `main`, vedere `README.md` su `main`.

---

## Contesto

- `main` è il branch di sviluppo attivo.
- `3.0.x` è il branch di release per bugfix destinato a clienti su versione 3.0.x.
- I branch `porting-*` vengono creati da `3.0.x` e hanno MR verso `3.0.x`. Non vanno mai su `main`.
- `k9-stable-2` è l'unico ambiente di deploy per `3.0.x`. Non esistono ambienti personali per sviluppatori su questo branch.
- La logica utente-specifica (998/999/996-SNAPSHOT) non si applica su `3.0.x`.

---

## Comportamento pipeline per tipo di evento

| Trigger | Build | Push immagine | Restart | Verifier |
|---|---|---|---|---|
| MR `porting-*` → `3.0.x` | compile only | no | no | si |
| push/merge su `3.0.x` | build | `3.0.x-SNAPSHOT` | k9-stable-2 | no |
| tag `3.0.1`, `3.0.2`, ... | build | versione esatta | k9-stable-2 | no |

---

## Moduli presenti su 3.0.x

**Backend (Quarkus):** `datasource`, `ingestion`, `searcher`, `tenant-manager`, `file-manager`, `resources-validator`, `tika`, `k8s-client`

**AI (Kaniko/Python):** `rag-module`, `embedding-modules` (produce `openk9-sentence-transformers-module` e `openk9-openai-module`)

**Frontend (Kaniko/Node):** `search-frontend`, `admin-ui`, `tenant-ui`, `talk-to`, `openk9-chatbot` (solo npm publish)

**Connectors (Kaniko):** `openk9-crawler` (web), `email-connector`, `database-connector`, `youtube-connector`, `gitlab-connector`, `minio-connector`

---

## Moduli NON presenti su 3.0.x

Non hanno job release nella pipeline e non vengono mai triggerati su questo branch:
`api-gateway`, `agentic-rag-module`, `chunk-evaluation-module`, `docling-processor`

---

## Struttura job per ogni modulo

Ogni modulo ha tre job dedicati al release branch, in coda ai job standard:

| Job | Rule | Cosa fa |
|---|---|---|
| `Build <Module> Release` | `child-deploy-release` | build + push immagine |
| `Build Verifier <Module> Release MR` | `child-verify-release-mr` | compile only, no push |
| `Trigger Restart <Module> Release` | `child-deploy-release` | restart su `k9-stable-2` |

---

## Rule riutilizzabili (child-rules.yaml)

```yaml
.rules:child-deploy-release:
  rules:
    - if: '$CI_COMMIT_BRANCH =~ /^\d+\.\d+\.x$/'
    - if: '$CI_COMMIT_TAG =~ /^\d+\.\d+\.\d+/'

.rules:child-verify-release-mr:
  rules:
    - if: '$CI_PIPELINE_SOURCE == "parent_pipeline" && $RELEASE_MR == "true"'
```

`RELEASE_MR: "true"` viene passato dal parent trigger quando la MR ha `CI_MERGE_REQUEST_TARGET_BRANCH_NAME =~ /^\d+\.\d+\.x$/`.

---

## Versioning immagini

| Evento | Tag immagine |
|---|---|
| push su `3.0.x` | `3.0.x-SNAPSHOT` (dal nome del branch, già incluso nel pom.xml) |
| tag `3.0.1` | `3.0.1` (versione esatta dal pom.xml / package.json / config.env) |
| MR `porting-*` | nessun push |

---

## Estensione a branch futuri (3.1.x, 4.0.x, ...)

Le rule usano regex generica, quindi funzionano automaticamente per qualsiasi branch `N.N.x`.
Se un branch futuro deve puntare a un ambiente diverso da `k9-stable-2`, aggiornare
la variabile `TARGET_ENV` nei job `Trigger Restart <Module> Release` del child yaml corrispondente.
