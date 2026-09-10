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
from pathlib import Path

MODULE_ROOT = Path(__file__).resolve().parent.parent

if str(MODULE_ROOT) not in sys.path:
    sys.path.insert(0, str(MODULE_ROOT))

# app.server and app.utils.converter read these at import time. Set them once
# for the whole session so any test module can import them without repeating
# the setup, and so the suite never depends on the developer's own .env.
os.environ.setdefault("DATASOURCE_HOST", "http://localhost:8001")
os.environ.setdefault("MAX_CONCURRENT_CONVERSIONS", "2")
os.environ.setdefault("CALLBACK_TIMEOUT_SECONDS", "30")
os.environ.setdefault("FETCH_TIMEOUT_SECONDS", "30")
