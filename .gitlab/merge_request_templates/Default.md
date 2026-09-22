(La descrizione la leggono in due: il reviewer e l'agente che lo accompagna 
nella review. Non ripete la issue: chi legge la apre da qui. Dice quello che 
la issue non può dire, e poi guida la UAT passo per passo. Cancella le note 
fra parentesi.)

## Sommario

(Poche righe: `Closes #<iid>` e quello che la issue non dice. La scelta di 
implementazione e il perché; cosa è cambiato strada facendo rispetto alla 
issue e cosa resta fuori; le rotture di comportamento che la issue non 
prevedeva, da mettere nelle note di rilascio. Se parte del codice è stata 
prodotta o suggerita da un coding agent, dillo qui.)

## Su cosa concentrarsi

(Fra tre e cinque punti: i posti dove un occhio umano vale più di un test. 
Convenzioni che potresti aver mancato, scelte su cui hai dubbi, codice che i 
test non coprono. Tutto il resto va in un caso di UAT.)

## Dipendenze

(Merge request da mergiare prima di questa o che dipendono da questa, e i 
porting sulle altre linee. Esempio: "dipende da !142, da mergiare prima". 
"Nessuna" è una risposta.)

## UAT (guidata dall'agente)

(La UAT la esegue un agente leggendo questa sezione, con il reviewer accanto: 
l'agente lancia i comandi e legge gli esiti, il reviewer fa i passi nel 
pannello e decide. I casi derivano dagli scenari Gherkin della issue, uno per 
scenario e con lo stesso titolo. Un criterio della issue senza caso è un buco: 
o non è stato verificato, o non era verificabile, e in entrambi i casi va 
detto. Non elencare i test unitari: si verificano rifacendo la build.)

### Per il reviewer

(Cosa serve (Docker, `glab` autenticato, credenziali esterne se ce ne sono), 
quanto dura, e il prompt da incollare nell'agente aperto sul checkout di 
questo branch:

> Esegui la UAT della MR !<iid> di openk9/openk9. Leggi la descrizione con 
> `glab mr view <iid> -R openk9/openk9` e segui la sezione UAT nell'ordine, 
> un caso alla volta. Io faccio i passi marcati [reviewer] quando me lo chiedi.
)

### Per l'agente

(Le regole del gioco. Adattale se un caso lo richiede, non toglierle.

- Esegui tu i comandi. I passi marcati **[reviewer]** li fa il reviewer nel 
  pannello: chiediglieli e aspetta. I comandi marcati **[con ok]** scrivono 
  dati: mostrali e attendi un ok prima di lanciarli.
- Per ogni caso riporta osservato accanto ad atteso e il verdetto 
  **PASS**/**FAIL**. Se un comando non risponde come atteso mostra l'output 
  e chiedi: non dedurre. Un FAIL non blocca i casi successivi.
- Sei anche un secondo paio d'occhi. Segnala al reviewer, con il testo del 
  thread pronto da aprire sulla MR, quando trovi: un esito inatteso in un 
  caso; un criterio della issue che nessun caso copre, o un caso che non 
  verifica il suo Then; una differenza fra quello che la issue chiede e 
  quello che il codice fa; un modo per migliorare la modifica proposta. 
  Il reviewer decide cosa aprire.
- Alla fine consegna il report descritto sotto.
)

### Ambiente e configurazione

(Come tirare su quello che serve (`./k9.sh up ...`, build delle immagini 
toccate) e come arrivare al punto in cui i casi si possono eseguire. Chi non 
ha mai configurato questa parte del prodotto deve potercela fare: metti le 
trappole che hai incontrato.

Dalla 2026.2 in poi la strada più corta è l'export della configurazione del 
tenant: se la issue lo porta usa quello, altrimenti allegalo qui. Si importa 
dal pannello di amministrazione (Import / Export) o con 
`POST /api/datasource/v1/config/import?mode=OVERWRITE&dryRun=false`.

Allega anche quello che hai usato tu per eseguire i casi: script di helper, 
dati di prova, file di esempio. Più file, uno zip. Chi rivede deve poter 
rifare esattamente i tuoi passaggi.)

### Casi

(Uno per scenario Gherkin della issue, stesso titolo, nell'ordine in cui vanno 
eseguiti; poi i casi in più emersi in sviluppo, marcati come tali. Per ogni 
caso: cosa fare, cosa DEVE succedere, dove si osserva. Dichiarare l'attesa 
prima di eseguire è quello che rende il caso capace di fallire.

**C1 — Riprocessare non duplica.**
- Ricarica lo stesso file e reindicizza.
- Atteso: lo stesso numero di chunk di prima, e il file testimone intatto. 
  Dove si osserva: aggregazione per `contentId` sull'indice, prima e dopo.
)

### Report

(L'agente consegna una tabella, una riga per caso, con il verdetto e i valori 
osservati; sotto, i FAIL con l'output grezzo e le segnalazioni fatte durante 
la UAT, ognuna con il thread aperto o la ragione per cui non lo è. Il reviewer 
la incolla come commento sulla MR e spunta qui:

- [ ] C1 — Riprocessare non duplica
)
