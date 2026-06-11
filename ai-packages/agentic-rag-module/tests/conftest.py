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


import sys
from pathlib import Path
from unittest.mock import MagicMock

MODULE_ROOT = Path(__file__).resolve().parent.parent

if str(MODULE_ROOT) not in sys.path:
    sys.path.insert(0, str(MODULE_ROOT))

# Stub heavy third-party dependencies so that app modules can be imported
# without installing the full requirements (and without cloud credentials).
_STUBBED_MODULES = [
    "langchain_aws",
    "langchain_classic",
    "langchain_classic.chains",
    "langchain_google_community",
    "langchain_google_community.model_armor",
    "app.utils.llm",
]

for module_name in _STUBBED_MODULES:
    sys.modules.setdefault(module_name, MagicMock())
