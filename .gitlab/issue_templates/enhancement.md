(Chi legge deve capire in dieci minuti cosa si vuole e come si saprà che è 
fatto. Ogni sezione sta in un paragrafo o in un elenco corto. Linguaggio 
semplice; riferimenti al codice solo dove servono davvero, per esempio nel 
contratto di una API. La issue la leggono persone e agenti; se la bozza la 
scrive un agente, tiene la voce di chi apre la issue, non la sua. Cancella le 
note fra parentesi.)

## Sommario e benefici

(Cosa si vuole ottenere e per chi. Se c'è un'analisi collegata, linkala invece 
di riassumerla; se non c'è, due righe sul perché di questa scelta.)

## Domande aperte

(Solo finché la issue non è definita del tutto: per esempio quando la bozza 
l'apre un agente, o quando la definizione è ancora in corso. Una domanda per 
voce; se sai chi può rispondere, taggalo, e se la risposta cambia i criteri 
di accettazione, dillo. Finché resta almeno una domanda, aggiungi in fondo 
`/label ~needs-triage`. Quando tutte hanno una risposta, riporta ogni risposta 
nella sezione a cui appartiene, cancella questa sezione e togli la label.

- il flag `multimodal` si imposta per modello o per tenant? (cambia i Requisiti)
- serve esporlo anche nel pannello di amministrazione, o basta l'API?
)

## Requisiti

(I vincoli: le API o i componenti che cambiano, i contratti, i default. 
In linguaggio naturale, come elenco:

- il campo `multimodal` di `EmbeddingModel` è un booleano, default `false`
- il valore assente equivale a `false`: nessuna migrazione sui dati esistenti
)

## Criteri di accettazione

(La **Definition of Done**: i comportamenti, cioè tutto quello che ha un 
innesco e un esito osservabile. In **Gherkin**, con le keyword in inglese e 
non tradotte: è la regola condivisa fra chi scrive la specifica e chi la 
implementa, persona o agente che sia. Diventano i casi di UAT della merge 
request, uno per scenario.

    Scenario: un riferimento immagine è embeddato se il modello è multimodale
      Given un modello di embedding marcato come multimodale
      When si indicizza un documento che porta un riferimento a un'immagine
      Then in indice c'è un chunk per quel riferimento, con il suo vettore

Non forzare Gherkin su un vincolo: quelli stanno nei Requisiti.)

## Link / Riferimenti

(Compila le voci che servono, cancella quelle che non c'entrano.)

- **Analisi:** (JEP, ADR o documento di design)
- **Issue collegate:** (quelle da cui dipende, che blocca, o che sono simili)
- **Discussioni:** (thread, riunioni, decisioni prese altrove)
- **Export del tenant:** (dalla 2026.2 in poi, se aiuta a partire dallo 
  stesso punto: pannello di amministrazione, Import / Export)

## Breaking Changes

(Cosa smette di funzionare come prima per chi aggiorna. "Nessuna" è una 
risposta.)

## Deprecazioni

(Cosa questa issue rende deprecato, e cosa lo sostituisce.)

/label ~enhancement 
