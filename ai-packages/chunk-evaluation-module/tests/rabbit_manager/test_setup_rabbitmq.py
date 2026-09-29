#
# Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
#
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU Affero General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Affero General Public License for more details.
#
# You should have received a copy of the GNU Affero General Public License
# along with this program.  If not, see <http://www.gnu.org/licenses/>.
#


from unittest.mock import MagicMock, call

import rabbit_manager.structure as structure


def test_setup_rabbitmq_declares_main_retry_and_error_infrastructure(monkeypatch):
    connection = MagicMock()
    monkeypatch.setattr(
        structure.pika, "BlockingConnection", MagicMock(return_value=connection)
    )

    structure.setup_rabbitmq()

    channel = connection.channel.return_value
    channel.exchange_declare.assert_has_calls(
        [
            call(exchange="main.exchange", exchange_type="direct", durable=True),
            call(exchange="retry.exchange", exchange_type="direct", durable=True),
            call(exchange="error.exchange", exchange_type="direct", durable=True),
        ]
    )
    channel.queue_declare.assert_has_calls(
        [
            call(
                queue="main.queue",
                durable=True,
                arguments={
                    "x-dead-letter-exchange": "retry.exchange",
                    "x-dead-letter-routing-key": "retry",
                },
            ),
            call(
                queue="retry.queue",
                durable=True,
                arguments={
                    "x-message-ttl": int(structure.RETRY_DELAY_MS),
                    "x-dead-letter-exchange": "main.exchange",
                    "x-dead-letter-routing-key": "main",
                },
            ),
            call(queue="error.queue", durable=True),
        ]
    )
    channel.queue_bind.assert_has_calls(
        [
            call(queue="main.queue", exchange="main.exchange", routing_key="main"),
            call(queue="retry.queue", exchange="retry.exchange", routing_key="retry"),
            call(queue="error.queue", exchange="error.exchange", routing_key="error"),
        ]
    )
    connection.close.assert_called_once()
