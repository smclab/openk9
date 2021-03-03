# Ingestion

## ingestion-service
    
    ingestion-service si occupa di instaurare una connessione con rabbitmq.
    per impostare la connessione modificare le configuration osgi:
    
    default:
    
        RabbitMQActivator.cfg
        uri=amqp://test:test@localhost:5672

## ingestion-queue

avrà a disposizione 2 servizi OSGi `it.rios.projectq.ingestion.api.BundleReceiver`, `it.rios.projectq.ingestion.api.BundleSender`
con le seguenti configurazione definite nel bnd.bnd

    Rabbit-Exchange: projectq.topic
    Rabbit-Routing-Key: projectq
    Rabbit-Queue: data-ingestion