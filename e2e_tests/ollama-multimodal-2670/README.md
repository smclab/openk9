# E2E embedding multimodale Ollama (#2670)

Verifica sullo stack locale che un modello di embedding Ollama con le
capabilities `embedding` e `vision` (EmbeddingGemma 2) indicizzi le immagini
e le pagine dei PDF, e che una ricerca con un'immagine, con un testo o con
entrambi trovi la pagina giusta. Fa anche da sentinella sulla forma di
`input` di `/api/embed` con le immagini, che Ollama non documenta.

## Prerequisiti

- stack costruito e avviato dal checkout sotto test:

      ./k9.sh build embedding-module
      ./k9.sh up --with=file-handling --with=gen-ai

- un server Ollama recente (provato con 0.40.16) sull'host, porta 11434,
  raggiungibile dai container come `host.docker.internal:11434`, con il
  modello scaricato: `ollama pull embeddinggemma-2:740m`
- le immagini di prova nella cartella `CORPUS_DIR` (da esportare, vedi `config.env`): tre pagine
  rasterizzate di un manuale (JPEG) e il ritaglio fotografato di una di
  esse. Non sono nel repository; con un altro corpus basta cambiare i
  percorsi relativi in `config.env`.

## Cosa fa

1. controlla lo stack e che Ollama dichiari la capability `vision`
2. prepara `corpus/`: le due immagini e un PDF di tre pagine (pagine 17, 19
   e 20), costruito con Pillow del modulo di embedding
3. tenant `e2eollama` da zero, modello di embedding Ollama registrato e
   abilitato via GraphQL, datasource con il connettore finto, data index knn
   su `document.content`
4. il connettore manda tre documenti, ognuno con un testo breve e un binario
   inline: la pagina 19 (schema dell'impianto di sentina), la pagina 6 e il
   PDF; poi il LAST
5. verifica

## Asserzioni

| Gruppo | Cosa prova |
|---|---|
| IMMAGINE (x2) | un chunk-immagine per binario, `image/jpeg`, vettore di 768 dimensioni, accanto al chunk di testo |
| PDF | un chunk per pagina, pagine 1, 2, 3, vettori di 768 dimensioni |
| RICERCA immagine | il ritaglio della pagina 19 come query KNN: il primo chunk-immagine e' la pagina 19 (l'immagine o la pagina 2 del PDF), e la pagina 19 viene prima della 6 |
| RICERCA testo | "schema dell'impianto di sentina": l'immagine della pagina 19 prima di quella della pagina 6 (testo e immagini nello stesso spazio) |
| RICERCA testo+immagine | la query mista ha risposta (un solo vettore, `embed_mixed`) e il primo e' la pagina 19 |
| LOG | il modulo di embedding ha servito EmbedContent con `client=ollama-multimodal` |

Le ricerche passano dal searcher tramite il gateway (`https://e2eollama.openk9.localhost`),
sul Default Bucket del tenant portato a KNN.

## Esecuzione

    ./run.sh

Exit 0 se tutto passa. Con PASS il tenant, gli indici, il connettore e
`corpus/` vengono rimossi; con FAIL restano per il debug.
