# E2E embedding multimodale Ollama (#2670)

Verifica sullo stack locale che un modello di embedding Ollama con le
capabilities `embedding` e `vision` (EmbeddingGemma 2) indicizzi le immagini
e le pagine dei PDF, e che una ricerca con un'immagine, con un testo o con
entrambi trovi la pagina giusta. Fa anche da sentinella sulla forma di
`input` di `/api/embed` con le immagini, che Ollama non documenta.

Serve solo il repository: il corpus lo disegna `make_corpus.py`.

## Prerequisiti

- stack costruito e avviato dal checkout sotto test:

      ./k9.sh build embedding-module
      ./k9.sh up --with=file-handling --with=gen-ai

- un server Ollama recente (provato con 0.40.16) sull'host, porta 11434,
  con il modello scaricato: `ollama pull embeddinggemma-2:740m`

Il modulo di embedding raggiunge Ollama come `host.docker.internal:11434`
(`OLLAMA_URL`). Docker Desktop e Colima risolvono quel nome; con Docker
Engine su Linux no: si aggiunge al servizio `embedding-module`
`extra_hosts: ["host.docker.internal:host-gateway"]` (con Ollama in ascolto
su un indirizzo raggiungibile dal bridge, `OLLAMA_HOST=0.0.0.0`), oppure si
esporta `OLLAMA_URL` con un indirizzo che il container raggiunge. Lo script
interroga Ollama anche dall'host, su `OLLAMA_HOST_URL`
(default `http://localhost:11434`).

## Cosa fa

1. controlla lo stack e che Ollama dichiari la capability `vision`
2. disegna `corpus/` con Pillow del modulo di embedding:
   - `bilge.jpg`: pagina "Bilge pump diagram" (scafo, pozzetto, pompa, mandata)
   - `cabin.jpg`: pagina "Cabin interior layout" (pianta degli interni)
   - `manual.pdf`: tre pagine, impianto elettrico, sentina, interni
   - `query.jpg`: ritaglio ruotato e scalato dello schema della sentina
3. sentinella: chiede a Ollama un vettore per `{"text", "image"}`; deve
   essere uno solo e diverso da quello della sola immagine (coseno < 0.99)
4. tenant `e2eollama` da zero, modello di embedding Ollama registrato e
   abilitato via GraphQL, datasource con il connettore finto, data index knn
   su `document.content`
5. il connettore manda tre documenti, ognuno con un testo breve e un binario
   inline (le due pagine e il PDF), poi il LAST
6. verifica

## Asserzioni

| Gruppo | Cosa prova |
|---|---|
| IMMAGINE (x2) | un chunk-immagine per binario, `image/jpeg`, vettore di 768 dimensioni, accanto al chunk di testo |
| PDF | un chunk per pagina, pagine 1, 2, 3, vettori di 768 dimensioni |
| RICERCA immagine | il ritaglio come query KNN: il primo chunk-immagine e' lo schema della sentina (l'immagine o la pagina 2 del PDF), e `page-bilge` viene prima di `page-cabin` |
| RICERCA testo | "bilge pump diagram": `page-bilge` prima di `page-cabin` (testo e immagini nello stesso spazio) |
| RICERCA testo+immagine | la query mista ha risposta (un solo vettore, `embed_mixed`) e il primo e' lo schema della sentina |
| LOG | il modulo di embedding ha servito EmbedContent con `client=ollama-multimodal` |

Le ricerche passano dal searcher tramite il gateway (`https://e2eollama.openk9.localhost`),
sul Default Bucket del tenant portato a KNN.

Riferimento misurato con Ollama 0.40.16: ritaglio contro pagina della
sentina 0.90, contro gli interni 0.66; "bilge pump diagram" 0.79 contro 0.56.

## Esecuzione

    ./run.sh

Exit 0 se tutto passa. Con PASS il tenant, gli indici, il connettore e
`corpus/` vengono rimossi; con FAIL restano per il debug.
