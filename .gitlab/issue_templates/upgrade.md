(Chi legge deve capire in cinque minuti cosa si aggiorna e perché. Ogni 
sezione sta in un paragrafo o in un elenco corto, con linguaggio semplice. 
Cancella le note fra parentesi.)

## Sommario

(Questa sezione descrive in breve lo scopo di questo aggiornamento. 
Possiamo spiegare perché serve aggiornare la libreria o la dipendenza 
in questione.)

## Domande aperte

(Solo finché la issue non è definita del tutto: per esempio quando la bozza 
l'apre un agente, o quando la definizione è ancora in corso. Una domanda per 
voce; se sai chi può rispondere, taggalo, e se la risposta cambia i criteri 
di accettazione, dillo. Finché resta almeno una domanda, aggiungi in fondo 
`/label ~needs-triage`. Quando tutte hanno una risposta, riporta ogni risposta 
nella sezione a cui appartiene, cancella questa sezione e togli la label. 
Poi stima la issue (vedi la nota in fondo).

- la nuova versione richiede Java 21 anche per i connettori?
- serve un periodo in cui convivono le due versioni? (cambia le Azioni)
)

## Componenti

(Le librerie o dipendenze che si aggiornano e i moduli che l'aggiornamento 
tocca con ogni probabilità. Una riga per dipendenza.)

| Dipendenza | Da | A | Moduli toccati |
|------------|----|---|----------------|
| (es. Quarkus) | (3.15) | (3.20) | (datasource, searcher) |

## Azioni

(Possiamo aggiungere una breve analisi dei passi necessari per fare 
l'aggiornamento. Possiamo anche descrivere le soluzioni trovate per mitigare 
i problemi di compatibilità.)

## Criteri di accettazione

(La **Definition of Done** dell'aggiornamento. Qui di solito sono osservazioni 
e non scenari, quindi un elenco in linguaggio naturale va bene — non forzare 
Gherkin dove non serve:

- lo stack si avvia e tutti i servizi diventano healthy
- nei log non compaiono warning di deprecazione della libreria aggiornata
- il contratto delle API esposte non cambia)

## Breaking Changes

(Dopo aver condotto l'analisi, dobbiamo documentare tutte le rotture di 
compatibilità che questo aggiornamento introduce.)

(Stima: il tempo per chiudere la issue secondo i criteri di accettazione, 
con l'agente che scrive il codice. 
Si stima solo una issue senza Domande aperte. 
Aggiungi in fondo `/estimate <tempo>` (es. `/estimate 1d 2h`), oppure 
mettilo nel commento che toglie `needs-triage`.)

/label ~upgrade
