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



import os
import sys
import tempfile
from pathlib import Path
from unittest.mock import MagicMock

MODULE_ROOT = Path(__file__).resolve().parent.parent

# The app uses flat imports (`from utils.helpers import ...`). In the source tree
# the code lives in <module>/app, while the Docker image copies app/ straight
# into the WORKDIR next to tests/: put whichever directory holds it on sys.path.
APP_DIR = MODULE_ROOT / "app" if (MODULE_ROOT / "app").is_dir() else MODULE_ROOT

if str(APP_DIR) not in sys.path:
    sys.path.insert(0, str(APP_DIR))

# server.py opens its log file at import time, and the metrics would download
# their models from Hugging Face: keep the log out of the source tree and make
# any accidental model download fail instead of reaching the network.
os.environ.setdefault("LOG_FILE", os.path.join(tempfile.mkdtemp(), "app.log"))
os.environ.setdefault("HF_HUB_OFFLINE", "1")

# Stub the external services the app modules touch at import time, once and for
# the whole session:
# - api.py registers the Phoenix tracer and helpers.py builds the Phoenix
#   clients, so both resolve to mocks and never reach a Phoenix server;
# - server.py connects to RabbitMQ and starts consuming, so a mocked connection
#   makes the import return instead of blocking forever.
# Tests replace these per test (monkeypatch) when they assert on the calls.
import phoenix.client  # noqa: E402
import phoenix.otel  # noqa: E402
import pika  # noqa: E402

phoenix.otel.register = MagicMock()
phoenix.client.Client = MagicMock
phoenix.client.AsyncClient = MagicMock
pika.BlockingConnection = MagicMock()
