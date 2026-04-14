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

### Release branch (`3.0.x`, `3.1.x`, ...)

- Push su release branch con file backend → i 9 trigger backend release scattano; `Trigger K8S-Client` resta silenzioso (non presente in `3.0.x`)
- Push su release branch con file frontend → i 4 trigger frontend release scattano; `Trigger OpenK9-Chatbot` resta silenzioso
- Push su release branch con file AI → solo `Trigger Rag Module` scatta (agentic-rag, embedding, chunk-evaluation non in `3.0.x`)
- Push su release branch con file unrelated → nessun trigger

### Release MR (`porting-*` → release branch)

- MR verso release branch con file backend/frontend/AI → i trigger release del dominio scattano (simulando `CI_MERGE_REQUEST_TARGET_BRANCH_NAME=3.0.x`)

> **Nota:** i tag release (`3.0.2`, ecc.) non sono testati automaticamente — `gitlab-ci-local` non valuta `changes:` in modo affidabile con `CI_COMMIT_TAG`.

## Moduli presenti in `3.0.x`

| Dominio | Moduli inclusi | Moduli esclusi |
|---------|---------------|----------------|
| Backend | datasource, searcher, ingestion, file-manager, tenant-manager, api-gateway, tika, entity-manager, resources-validator | k8s-client |
| Frontend | search-frontend, admin-ui, tenant-ui, talk-to | chatbot |
| AI | rag-module | agentic-rag, embedding, chunk-evaluation |
