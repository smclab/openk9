## Sommario
(Descrivi in breve cosa fa questa merge request e perché. 
Collega la issue correlata, se esiste.
)

## Comportamento atteso
(Cosa ci aspettiamo che accada come conseguenza di queste modifiche. 
Chi rivede deve poter verificare le modifiche contro questa descrizione.
)

## Su cosa concentrarsi
(Aiuta chi rivede a mettere a fuoco. 
Quali convenzioni potresti aver mancato? 
Su cosa hai dei dubbi? 
Elenca le preoccupazioni specifiche: 
 - "Non sono sicuro che la gestione dello stato in React sia idiomatica" 
 - "Controllare che la struttura dei values Helm vada bene per la nostra pipeline" 
 - "Dubbi sull'annotazione usata qui" 
**Stai fra i tre e i cinque punti**: i posti dove un occhio umano vale più di 
un test. Tutto il resto va in un'affermazione verificabile, qui sotto.
)

## Dipendenze
(Elenca le merge request che devono essere mergiate prima di questa, 
o che dipendono da questa.
Esempio: "Questa MR dipende da !142 (modifiche di autenticazione sul frontend), 
che va mergiata prima nel branch della feature".
)

## UAT 
(Una checklist che possa eseguire qualcuno che non è l'autore, sullo stack 
locale o su un ambiente di riferimento. Tre parti:

1. **Ambiente**: come tirare su quello che serve, e quali credenziali o servizi 
   esterni richiedono i casi.
2. **Configurazione**: la ricetta minima per arrivare al punto in cui i casi si 
   possono eseguire. Chi rivede e non ha mai configurato questa parte del 
   prodotto deve potercela fare. Includi le trappole che hai incontrato.
3. **Test checklist**: una casella per caso, ognuna con *cosa fare*, *cosa DEVE 
   succedere* e *dove si osserva*.

Ogni voce dice quale criterio di accettazione della issue verifica. Un criterio 
senza voce è un buco: o non è stato verificato, o era scritto in modo non 
verificabile — e in entrambi i casi va detto.

**Non** elencare qui i test unitari: si verificano rifacendo la build, ed 
elencarli non dà niente a chi rivede.

Esempio di una voce:

- [ ] **Riprocessare non duplica.** Ricarica lo stesso file e reindicizza. 
  Atteso: lo stesso numero di chunk di prima, e il file testimone intatto. 
  Dove si osserva: aggregazione per `contentId` sull'indice, prima e dopo.

Dichiarare l'attesa *prima* di eseguire è quello che rende il caso capace di 
fallire. Un caso senza attesa dichiarata è una dimostrazione, non una verifica.
)
