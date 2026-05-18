# pipeline-tests

Script per testare localmente le regole delle pipeline GitLab CI.

## Requisiti

- [`gitlab-ci-local`](https://github.com/firecow/gitlab-ci-local) installato e disponibile nel PATH
- Python 3

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
- Push su release branch con file unrelated → nessun trigger

### Release MR (`porting-*` → `2026.1.x`)

- MR verso `2026.1.x` con file backend/frontend/AI/enricher → tutti i trigger release del dominio scattano (simulando `CI_MERGE_REQUEST_TARGET_BRANCH_NAME=2026.1.x`)

### Release tag (`2026.1.0`)

- Tag push → ogni trigger di dominio scatta (GitLab ignora `changes:` sui tag, quindi tutti i moduli vengono ricostruiti per garantire artefatti coerenti).
