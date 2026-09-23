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

## Come riprodurlo

(Uno scenario in Gherkin, con le keyword in inglese e non tradotte: 
Given lo stato di partenza, When i passi, Then quello che dovrebbe accadere. 
Sotto, in una riga, quello che accade oggi. Scritto così fallisce prima della 
correzione e passa dopo: è il primo criterio di accettazione, e la sola prova 
che la causa è stata toccata e non mascherata.

    Scenario: la cancellazione di un documento produce un solo esito
      Given un documento che la sorgente non ha più
      When la scheduling lo lavora
      Then il work stage riceve un solo esito

    Oggi: riceve un fallimento seguito da un successo.
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

## Criteri di accettazione

(Lo scenario di riproduzione è il primo. Aggiungi solo quello che serve in più 
perché la issue si possa chiudere: altri scenari in Gherkin per i casi vicini 
che non devono rompersi, e i vincoli senza innesco (un default, un limite, un 
formato) come elenco in linguaggio naturale. Diventano i casi di UAT della 
merge request che chiude la issue.)

## Log e screenshot

(Solo quelli che servono, nei blocchi di codice (```). Se sono lunghi, 
allegali.)

## Possibili correzioni

(Facoltativa. Se hai un'idea di dove sta la causa, dilla qui: è il posto 
giusto per un riferimento al codice.)

/label ~"Bug fix"
