import pika
from config import settings

rabbit_host = settings.rabbitmq_host
rabbit_user = settings.rabbitmq_user
rabbit_pass = settings.rabbitmq_pass.get_secret_value()

RETRY_DELAY_MS = settings.rabbitmq_retry_delay_ms

MAIN_EX = "main.exchange"
RETRY_EX = "retry.exchange"
ERROR_EX = "error.exchange"

MAIN_Q = "main.queue"
RETRY_Q = "retry.queue"
ERROR_Q = "error.queue"


def setup_rabbitmq():
    credentials = pika.PlainCredentials(rabbit_user, rabbit_pass)
    conn = pika.BlockingConnection(
        pika.ConnectionParameters(host=rabbit_host, credentials=credentials)
    )
    ch = conn.channel()

    # Exchange declare
    ch.exchange_declare(exchange=MAIN_EX, exchange_type="direct", durable=True)
    ch.exchange_declare(exchange=RETRY_EX, exchange_type="direct", durable=True)
    ch.exchange_declare(exchange=ERROR_EX, exchange_type="direct", durable=True)

    # Queue declare
    ch.queue_declare(
        queue=MAIN_Q,
        durable=True,
        arguments={
            "x-dead-letter-exchange": RETRY_EX,  # When failed → retry exchange
            "x-dead-letter-routing-key": "retry",
        },
    )

    ch.queue_declare(
        queue=RETRY_Q,
        durable=True,
        arguments={
            "x-message-ttl": RETRY_DELAY_MS,  # Wait before retrying
            "x-dead-letter-exchange": MAIN_EX,  # After TTL → back to main
            "x-dead-letter-routing-key": "main",  # Recommended
        },
    )
    ch.queue_declare(queue=ERROR_Q, durable=True)

    # Queue bind
    ch.queue_bind(queue=MAIN_Q, exchange=MAIN_EX, routing_key="main")
    ch.queue_bind(queue=RETRY_Q, exchange=RETRY_EX, routing_key="retry")
    ch.queue_bind(queue=ERROR_Q, exchange=ERROR_EX, routing_key="error")

    print("Retry infrastructure created")
    conn.close()
