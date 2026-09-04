## Sommario e benefici

(Questa sezione descrive in breve lo scopo di questa evoluzione. 
Se non c'è un'analisi collegata, può essere utile includere le ragioni 
dietro la decisione di implementazione)

## Requisiti

(Questa sezione riguarda le API o i componenti che verranno aggiunti o modificati. 
Qui possiamo definire i contratti delle nostre API.)

## Comportamento atteso

(Cosa ci aspettiamo che accada quando le nostre API o i nostri componenti vengono usati.)

## Criteri di accettazione

(La **Definition of Done**: cosa deve essere vero perché questa issue si possa 
chiudere. Servono a tre cose — dire quando è finita, guidare l'implementazione 
in BDD/TDD, e diventare la checklist di UAT della merge request.

Due forme, e non sono interscambiabili.

**I vincoli** — un campo, un default, una chiave di configurazione, un 
contratto — si scrivono in linguaggio naturale, come elenco:

- il campo `multimodal` di `EmbeddingModel` è un booleano, default `false`
- il valore assente equivale a `false`: nessuna migrazione sui dati esistenti

**I comportamenti** — tutto quello che ha un innesco e un esito osservabile — 
si scrivono in **Gherkin**, con le keyword in inglese e non tradotte: è la 
regola di comunicazione fra chi scrive la specifica e chi la implementa, 
persona o agente che sia.

    Scenario: un riferimento immagine viene embeddato se il modello è multimodale
      Given un modello di embedding marcato come multimodale
      When si indicizza un documento che porta un riferimento a un'immagine
      Then in indice c'è un chunk per quel riferimento, con il suo vettore

Non forzare Gherkin su un vincolo: «Given the field exists, When I read it, 
Then it is nullable» non aggiunge niente e insegna a compilare a vuoto.)

## Link / Riferimenti

(Qui aggiungiamo i dettagli di implementazione e le analisi correlate che sono 
state condotte.)

## Breaking Changes

(Dobbiamo ragionare su come le nostre modifiche impattano il prodotto nel suo insieme.)

## Deprecazioni

(Come già fatto per le Breaking Changes, dobbiamo tenere traccia di cosa
questa issue rende deprecato)

/label ~enhancement 
