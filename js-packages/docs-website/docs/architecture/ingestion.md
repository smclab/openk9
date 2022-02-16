---
id: ingestion
title: Ingestion
slug: /ingestion

---


Ingestion component handles data ingestion logic. It accepts data from external data parser through specific rest api
and send data to queue, only after doing some operations on them.

![img](../../static/img/ingestion.png)

### Ingestion logic

Defines the operations to be done on the data, based on some logic. In particular it discards data
that exceed a certain size.

### Queue Adapter

Adapter to send data on queuing system. It is developed for the message broker used. Openk9 uses RabbitMQ
as message broker. See [client documentation](https://www.rabbitmq.com/clients.html) to realize adapter.

### Rest Api

See more on [Openk9 Api Documentation](/docs/api/ingestion-api)
