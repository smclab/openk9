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

# filtra per utente
python3 .gitlab/pipeline-tests/test-pipeline-rules.py -u mirko
```

## Cosa viene testato

- **Backend designated** (mirko, michele): backend fires su file backend, niente su frontend/AI
- **Frontend designated** (lorenzo, giorgio): frontend fires su file frontend, niente su backend/AI
- **AI designated** (luca): AI fires su file AI, niente su backend/frontend
- **Generic** (utenti non designati): fires solo sul dominio dei file modificati, niente altrove
- Ogni dominio viene coperto creando file in tutte le cartelle rilevanti per attivare tutti i trigger
