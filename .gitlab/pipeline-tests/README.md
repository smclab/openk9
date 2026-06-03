# pipeline-tests

Script per testare localmente le regole delle pipeline GitLab CI.

Due suite complementari:

| script | livello | cosa verifica |
|--------|---------|---------------|
| `test-pipeline-rules.py` | **parent** (`.gitlab-ci.yaml`) | quali trigger di dominio partono, per utente/branch/file modificati |
| `test-child-rules.py` | **child** (`.gitlab-ci-<modulo>.yaml`) | quali job partono dentro il modulo: main vs branch release vs tag `v` (refactor #2132) |

## Requisiti

- [`gitlab-ci-local`](https://github.com/firecow/gitlab-ci-local) installato e disponibile nel PATH
- Python 3
- Eseguire dalla root del repo

## Come funziona

Per ogni combinazione di utente / tipo di branch / dominio modificato, lo script:

1. Crea file temporanei nelle cartelle del dominio target
2. Esegue un commit locale (mai pushato)
3. Lancia `gitlab-ci-local --list` che legge il diff reale per valutare le regole `changes`
4. Annulla il commit e rimuove i file

In questo modo si verifica quali trigger si attivano e quali restano silenziosi, riproducendo fedelmente il comportamento di GitLab CI.

## Utilizzo

```bash
# tutti i test
python3 .gitlab/pipeline-tests/test-pipeline-rules.py

# output verboso
python3 .gitlab/pipeline-tests/test-pipeline-rules.py -v

# filtra per utente (es. solo i test release)
python3 .gitlab/pipeline-tests/test-pipeline-rules.py -u dmytro

# filtra per utente con output verboso
python3 .gitlab/pipeline-tests/test-pipeline-rules.py -u mirko -v
```

## Cosa viene testato

### Branch feature / main / MR standard

- **Backend designated** (mirko, michele): backend fires su file backend, niente su frontend/AI
- **Frontend designated** (lorenzo, giorgio): frontend fires su file frontend, niente su backend/AI
- **AI designated** (luca): AI fires su file AI, niente su backend/frontend
- **Generic** (utenti non designati): fires solo sul dominio dei file modificati, niente altrove

### Release branch (`2026.1.x`)

Si testa il prossimo branch di release CalVer (`2026.1.x`) che verrà staccato da `main`.

La release line legacy `3.0.x` non è testata qui: vive su un branch separato con file pipeline propri, e va testata da quel branch.

- Push su release branch con file backend → tutti i trigger backend scattano (incluso `Trigger K8S-Client`)
- Push su release branch con file frontend → tutti i trigger frontend scattano (incluso `Trigger OpenK9-Chatbot`)
- Push su release branch con file AI → tutti i trigger AI scattano (rag, agentic-rag, embedding, chunk-evaluation)
- Push su release branch con file enricher → `Trigger Docling Processor` scatta
- Push su release branch con file connector → `Trigger Connectors Build` scatta (#2133, prima silenzioso)
- Push su release branch con file unrelated → nessun trigger

### Release MR (`porting-*` → `2026.1.x`)

- MR verso `2026.1.x` con file backend/frontend/AI/enricher → tutti i trigger release del dominio scattano (simulando `CI_MERGE_REQUEST_TARGET_BRANCH_NAME=2026.1.x`)
- MR verso `2026.1.x` con file connector → `Trigger Connectors Build` scatta (#2133)

### Release tag (`2026.1.0`)

- Tag push → ogni trigger di dominio scatta (GitLab ignora `changes:` sui tag, quindi tutti i moduli vengono ricostruiti per garantire artefatti coerenti).

---

## test-child-rules.py — regole dei job dentro al modulo (#2132)

```bash
python3 .gitlab/pipeline-tests/test-child-rules.py            # tutti
python3 .gitlab/pipeline-tests/test-child-rules.py -v         # verboso (mostra i job)
python3 .gitlab/pipeline-tests/test-child-rules.py -k datasource   # filtra per file
```

A differenza della suite parent, **non serve un commit git**: le regole dei job
child non usano `changes:`, quindi basta impostare le variabili predefinite
(`CI_COMMIT_BRANCH` / `CI_COMMIT_TAG`) e leggere `gitlab-ci-local --list` sul
singolo file child.

Verifica i tre contesti del refactor #2132 su backend (datasource), AI
(embedding, rag), frontend (admin-ui) e connettori:

- **push su `main`** → `Build image` + `Container Scanning` + restart dev; **nessun** job Release.
- **push su `2026.1.x`** → `Build Release` + `Restart Release` (un solo ambiente); **nessun** job main, **nessun** Copy a Docker Hub.
- **tag `v2026.1.0`** → `Build Release` + `Copy to DockerHub`; **nessun** job main, **nessun** restart.
- **tag `v2026.10.3`** (minor/patch a più cifre) → la regex `^v\d+\.\d+\.\d+$` matcha comunque.
- **tag senza `v`** (`2026.1.0`) → i Copy a Docker Hub **non** scattano (solo immagini pulite e taggate `v` finiscono su Docker Hub). NB: per i connettori i `Build`/`Scan` scattano comunque (rule permissiva `$CI_COMMIT_TAG`), ma **non** i Copy.
- **MR release** (`RELEASE_MR=true`) → solo `Build Verifier Release MR`.
- **connettori su release branch / porting MR** (#2133) → `Fetch config` scatta, `Copy to DockerHub` resta gated al tag `v` (i singoli Build sono coperti dalla suite parent perché dipendono da `changes:`).

### Limiti noti (scenari NON riproducibili in locale)

- **Tag + `changes:` insieme**: `gitlab-ci-local` non simula in modo affidabile un
  tag con i filtri `changes:` del parent. Per questo la suite child lancia i file
  child **isolati** (dove `changes:` non c'è) e la suite parent salta il caso tag
  con `changes:` (vedi commento "tag: skipped" nel codice).
- **Espansione delle child pipeline dal parent**: `gitlab-ci-local` non espande le
  child lanciate via `trigger: include:`. Le due cose vanno quindi testate
  separatamente — parent e child — non in un unico run end-to-end.
- **Valore della versione pubblicata** (es. `:2026.1.0` vs `:2026.1.0-SNAPSHOT`,
  strip della `v`): dipende dallo **script** Kaniko/Maven a runtime (legge pom /
  `package.json` / `python_modules_config.txt`), non dalle regole dei job. Questi
  test verificano *quali job partono*, non *quale tag immagine* producono: quella
  parte è coperta dall'analisi statica dello script, non da `gitlab-ci-local`.
