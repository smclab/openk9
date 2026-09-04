## Sommario

(Riassunto conciso del difetto incontrato)

## Passi per riprodurlo

(Come si riproduce il problema?)

## Ambiente di riferimento

(Descrivi l'ambiente in cui il difetto è stato osservato: 
versione di OpenK9, tipo di deploy (Docker Compose / Kubernetes), 
sistema operativo, browser se pertinente, 
e ogni dettaglio di configurazione rilevante)

## Qual è il comportamento attuale?

(Cosa accade a causa di questo difetto?)

## Qual è il comportamento corretto atteso?

(Cosa dovrebbe accadere invece)

## Criteri di accettazione

(La **Definition of Done** della correzione. Servono anche a guidare 
l'implementazione in BDD/TDD e a diventare la checklist di UAT della merge 
request che chiude la issue.

Il **primo scenario è la riproduzione del difetto**, scritto in modo che 
fallisca prima della correzione e passi dopo: è la sola prova che la causa è 
stata toccata e non mascherata. In **Gherkin**, con le keyword in inglese e non 
tradotte.

    Scenario: la cancellazione di un documento produce un solo esito
      Given un documento che la sorgente non ha più
      When la scheduling lo lavora
      Then il work stage riceve un solo esito, e non un fallimento seguito da un successo

Gli altri criteri seguono la stessa forma. I vincoli che non hanno un innesco — 
un valore di default, un limite, un formato — restano un elenco in linguaggio 
naturale.)

## Log e screenshot rilevanti

(Incolla i log che servono — usa i blocchi di codice (```) per formattare output
di console, log e codice, altrimenti sono illeggibili.)

## Possibili correzioni

(Se puoi, collega la riga di codice che potrebbe essere responsabile del problema)

/label ~"Bug fix"
