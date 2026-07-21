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

if str(MODULE_ROOT) not in sys.path:
    sys.path.insert(0, str(MODULE_ROOT))

# app.server reads these at import time (reading the upload config and creating
# the upload directory). Set them once for the whole session so any test module
# can `import app.server` without repeating the setup.
os.environ.setdefault("ORIGINS", "http://localhost")
os.environ.setdefault("OPENSEARCH_HOST", "http://localhost:9200")
os.environ.setdefault("UPLOAD_DIR", tempfile.mkdtemp())
os.environ.setdefault("UPLOAD_FILE_EXTENSIONS", "")
os.environ.setdefault("MAX_UPLOAD_FILE_SIZE", "10")
os.environ.setdefault("MAX_UPLOAD_FILES_NUMBER", "5")

# Stub the LLM layer (and the cloud SDKs it pulls in) so app modules import
# without cloud credentials. Applied once, session-wide, and never removed:
# per-module stubbing that mutated sys.modules mid-session leaked mocks into
# sibling test modules and made the suite order-dependent.
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
