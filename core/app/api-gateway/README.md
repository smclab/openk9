# API Gateway

L'obiettivo è sviluppare un Api Gateway base che possa delegare le richieste
verso i servizi core Datasource e Searcher, ma anche verso Rag Module.

Il componente dovrà, in diversi step implementativi:

1. gestire il routing del traffico degli utenti (admin, search)
    - [x] richieste verso datasource, searcher, rag-module
    - [x] il routing non deve essere gestito da application properties ma programmaticamente,
      propedeutico per la gestione della sicurezza tramite configurazione tenant su database.

2. risolvere i tenantId internamente

    - [x] integrare database (h2, HA postgres)
    - [x] tabella mapping (virtualHost, tenantId)
    - [x] filtro per mapping
    - [ ] gestione tenant di default (h2 single-tenant)
    - [x] pub/sub tenant-manager -> api-gateway per aggiornare il mapping

3. configurazione security event-driven

    - [x] il tenant-manager, oltre a mandare un evento della creazione di un nuovo tenant,
      invia anche la configurazione del tenant, che include le configurazioni della security:
        - admin datasource: basic/oauth2
        - searcher/current-bucket: apikey/oauth2
        - rag/current-bucket: apikey/oauth2
    - [x] il gateway riceve questi eventi e configura il suo database e i suoi tenant di conseguenza.

4. security OAuth2

    - [x] esporre un endpoint per inviare le configurazioni del client OAuth2
    - [x] verificare il JWT token come resource server OAuth2
    - [ ] rimappare i claims del JWT in un oggetto o headers con le informazione dell'utente:
      username, email, roles
    - [x] proteggere le rotte di datasource, searcher, rag-module
    - [x] rimuovere il legame con keycloak e sicurezza da servizi

5. security via apiKey

    - [x] searcher e rag-module possono essere protetti o no
    - [x] generare apiKey per searcher e rag-module se non protetti via OAuth2
    - [ ] la apikey andrà a finire di sicuro nel gateway, non mi è chiaro da chi viene generata
      (tenant-manager? datasource admin?)

6. routing traffico ingestion

    - [ ] richieste verso ingestion (connettori esterni), datasource callback (enrich-item esterni)
    - [ ] autenticazione tramite api-key


backlog idee:

- piuttosto che rimappare i claim in un oggetto o headers particolari, provare a ricreare un nuovo
jwt che non verrà verificato dai servizi, ma che conterrà le informazioni per 
autenticare e autorizzare la richiesta, che sia oauth2 o apikey.

jwt oauth2 -> jwt openk9
apikey -> jwt openk9

i servizi downstream lavorano sui jwt openk9, che sono considerati fidati.
(considerare se applicare anche una firma con un certificato condiviso tra gateway e servizi, per 
maggiore sicurezza)

- oltre a mappare rotta e tipo di autenticazione, bisogna mappare anche i claim che servono per
autorizzare le richieste. In base ai ruoli che si aspettano i servizi downstream.


