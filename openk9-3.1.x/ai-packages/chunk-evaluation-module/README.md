## Title and Short Description

Asynchronous Chunk Evaluation Service with RabbitMQ and Phoenix

A containerized, asynchronous evaluation service that processes document chunks via RabbitMQ, computes quality metrics, stores results in daily Phoenix datasets, and periodically runs automated experiments.
## Description
This module implements an event-driven evaluation pipeline designed to assess the quality of chunked documents. Incoming messages are consumed from RabbitMQ, evaluated through multiple custom metrics, and stored in Phoenix datasets that are organized on a daily basis.

The system is designed to run as a long-lived service and includes:

- RabbitMQ-based messaging layer with:
    - main processing queue
    - delayed retry queue (TTL-based)
    - error queue for unrecoverable messages
- Robust retry logic, with configurable delay and maximum retry attempts
- A scoring pipeline built on top of chonkie and custom metrics:
    - Semantic coherence
    - Redundancy / bloat
    - Layout fidelity
- Daily dataset lifecycle management, automatically creating or updating datasets
- Scheduled experiment execution using Phoenix, triggered at fixed time intervals
- Full Docker-based deployment, suitable for both local development and OpenK9 environments

## Quickstart

### OpenK9 Setup
In an OpenK9 environment, RabbitMQ and Phoenix are typically already available as managed services.
Ensure the following prerequisites are met:
- RabbitMQ reachable from the evaluator service
- Phoenix service accessible via environment configuration
- Proper credentials set via environment variables

Required environment variables:
```bash
RABBITMQ_HOST=rabbitmq
RABBITMQ_USER=<user>
RABBITMQ_PASS=<password>

RABBITMQ_MAX_RETRIES=3
RABBITMQ_RETRY_DELAY_MS=5000
MIN_TIME_DELAY_MINUTES=5
```
Once deployed, the evaluator service will:
1. Initialize RabbitMQ exchanges and queues
2. Start consuming messages from main.queue
3. Automatically retry or route failed messages to the error queue
4. Periodically run Phoenix experiments on accumulated data

### Local Setup
The recommended way to run the project locally is via Docker Compose.

Docker Compose Services
```
version: "3.9"

services:
  rabbitmq:
    image: rabbitmq:4.1.0-management
    container_name: rabbitmq
    hostname: rabbitmq
    environment:
      RABBITMQ_DEFAULT_USER: <user>
      RABBITMQ_DEFAULT_PASS: <password>
    ports:
      - 5672:5672
      - 15672:15672
    healthcheck:
      test: "bash -c ':> /dev/tcp/127.0.0.1/15672' || exit 1"
      interval: 10s
      timeout: 10s
      retries: 5

  evaluator:
    build: .
    container_name: evaluator
    depends_on:
      rabbitmq:
        condition: service_healthy
    env_file: .env
    restart: always
    volumes:
      - ./dati:/app/dati
```
Steps
1. Create the .env file
```
RABBITMQ_HOST=rabbitmq
RABBITMQ_USER=user
RABBITMQ_PASS=pass
RABBITMQ_MAX_RETRIES=3
RABBITMQ_RETRY_DELAY_MS=5000
MIN_TIME_DELAY_MINUTES=5
```
2. Build and start services
```
docker compose up --build
```
3. Verify RabbitMQ
    - Management UI: http://localhost:15672
    - Default credentials as defined in docker-compose.yml
4. Evaluator behavior
Once started, the evaluator container will log:
```
Retry infrastructure created
[*] In attesa di messaggi. Premere CTRL+C per uscire.
```

At this point, it is ready to consume messages.

To see how to send messages, see here: [LINK PER EMBEDDING MODULE]

## API Reference

#### Message Payload Format
```
{
  "chunks": [
    {
      "<chunk_id>": {
        "text": "chunk text",
        "embedding": [0.1, 0.2, 0.3]
      }
    }
  ],
  "text": "full document text"
}
```

## Configuration
| Variable                  | Description                                                                      |
| ------------------------- | ---------------------------------------------------------------------------------|
| `RABBITMQ_HOST`           | RabbitMQ hostname or service name                                                |
| `RABBITMQ_USER`           | RabbitMQ username                                                                |
| `RABBITMQ_PASS`           | RabbitMQ password                                                                |
| `RABBITMQ_MAX_RETRIES`    | Maximum number of processing retries before sending a message to the error queue |
| `RABBITMQ_RETRY_DELAY_MS` | Delay (in milliseconds) before retrying a failed message                         |




## License

Copyright (c) the respective contributors, as shown by the AUTHORS file.
This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published
by the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.
This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
GNU Affero General Public License for more details.
You should have received a copy of the GNU Affero General Public License
along with this program. If not, see http://www.gnu.org/licenses/.