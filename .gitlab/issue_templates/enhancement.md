(Chi legge deve capire in dieci minuti cosa si vuole e come si saprà che è 
fatto. Ogni sezione sta in un paragrafo o in un elenco corto. Linguaggio 
semplice; riferimenti al codice solo dove servono davvero, per esempio nel 
contratto di una API. La issue la leggono persone e agenti; se la bozza la 
scrive un agente, tiene la voce di chi apre la issue, non la sua. Cancella le 
note fra parentesi.)

## Sommario e benefici

(Cosa si vuole ottenere e per chi. Se c'è un'analisi collegata, linkala invece 
di riassumerla; se non c'è, due righe sul perché di questa scelta.)

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

    Scenario: un riferimento immagine viene embeddato se il modello è multimodale
      Given un modello di embedding marcato come multimodale
      When si indicizza un documento che porta un riferimento a un'immagine
      Then in indice c'è un chunk per quel riferimento, con il suo vettore

Non forzare Gherkin su un vincolo: quelli stanno nei Requisiti.)

## Link / Riferimenti

(Analisi, discussioni, issue collegate. Dalla 2026.2 in poi, se aiuta a 
partire dallo stesso punto, l'export della configurazione del tenant 
(pannello di amministrazione, Import / Export).)

## Breaking Changes

(Cosa smette di funzionare come prima per chi aggiorna. "Nessuna" è una 
risposta.)

## Deprecazioni

(Cosa questa issue rende deprecato, e cosa lo sostituisce.)

/label ~enhancement 
