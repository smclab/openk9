(Chi legge deve capire il difetto in cinque minuti. Ogni sezione sta in un 
paragrafo o in un elenco corto. Scrivi come racconteresti il problema a un 
collega: linguaggio semplice, riferimenti al codice solo dove servono davvero. 
La issue la leggono persone e agenti; se la bozza la scrive un agente, tiene 
la voce di chi apre la issue, non la sua. Cancella le note fra parentesi.)

## Sommario

(Cosa stavi facendo, cosa è successo e cosa ti aspettavi. Poche righe: 
i dettagli stanno nello scenario qui sotto.)

## Domande aperte

(Solo finché la issue non è definita del tutto: per esempio quando la bozza 
l'apre un agente, o quando la definizione è ancora in corso. Una domanda per 
voce; se sai chi può rispondere, taggalo, e se la risposta cambia i criteri 
di accettazione, dillo. Finché resta almeno una domanda, aggiungi in fondo 
`/label ~needs-triage`. Quando tutte hanno una risposta, riporta ogni risposta 
nella sezione a cui appartiene, cancella questa sezione e togli la label.

- succede anche con il connettore minio o solo con il web crawler?
- il doppio esito arriva anche su 3.0.x? (decide su quali branch portare la 
  correzione)
)

## Ambiente

(Dove è avvenuto il difetto. Compila le voci che servono, cancella quelle che 
non c'entrano. Dalla 2026.2 in poi, se l'ambiente lo permette, allega l'export 
della configurazione del tenant (pannello di amministrazione, Import / Export, 
oppure `GET /api/datasource/v1/config/export`): chi corregge lo importa e 
parte dallo stesso punto.)

- **Versione di OpenK9:** (tag, oppure branch e commit)
- **Deploy:** (Docker Compose / Kubernetes con Helm)
- **Componenti coinvolti:** (servizi, connettori, enricher, e la loro 
  immagine se diversa da quella della versione)
- **Servizi esterni:** (versione di OpenSearch; provider e modello di LLM o 
  embedding, se pertinenti)
- **Browser:** (solo se il difetto è nel frontend)
- **Configurazione:** (export del tenant allegato, oppure i passi per 
  arrivare allo scenario)

## Come riprodurlo

(Uno scenario in Gherkin: 

- Dato: lo stato di partenza (Data, Dati o Date, se il nome lo chiede)
- Quando: i passi
- Allora: quello che dovrebbe accadere
- E: un'altra condizione o un altro esito, sotto quello di prima

Sotto, in una riga, quello che accade oggi. Scritto così fallisce prima della 
correzione e passa dopo: è il primo criterio di accettazione, e la sola prova 
che la causa è stata toccata e non mascherata.

    Scenario: la cancellazione di un documento produce un solo esito
      Dato un documento che la sorgente non ha più
      Quando la scheduling lo lavora
      Allora il work stage riceve un solo esito

    Oggi: riceve un fallimento seguito da un successo.
)

## Altri criteri di accettazione

(Lo scenario di riproduzione è il primo. 
Gli altri sono alternative ed estensioni di quello, e servono solo se 
aggiungono qualcosa perché la issue si possa chiudere.

Un'alternativa allo scenario di riproduzione:

    Scenario: un documento ancora presente nella sorgente non è cancellato
      Dato un documento che la sorgente ha ancora
      Quando la scheduling lo lavora
      Allora il documento resta in indice
      E il work stage riceve un solo esito

I vincoli senza innesco (un default, un limite, un formato) vanno in un 
elenco in linguaggio naturale. 
Ogni scenario diventa un caso di UAT della merge request che chiude la issue.)

## Log e screenshot

(Solo quelli che servono, nei blocchi di codice (```). Se sono lunghi, 
allegali.)

## Possibili correzioni

(Facoltativa. Se hai un'idea di dove sta la causa, dilla qui: è il posto 
giusto per un riferimento al codice.)

/label ~"Bug fix"
