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
import types
from unittest.mock import MagicMock, patch

# app.server loads dotenv/fastapi/pydantic 
# and docling through app.utils.converter. 
# Stub those heavy modules before importing it. 
_STUBS = ["dotenv", "fastapi", "app.utils.converter"]
for name in _STUBS:
    sys.modules.setdefault(name, MagicMock())

# pydantic.BaseModel must stay subclassable (server.py: class Input(BaseModel)).
_pydantic = types.ModuleType("pydantic")
_pydantic.BaseModel = type("BaseModel", (), {})
sys.modules.setdefault("pydantic", _pydantic)

import app.server as server  # noqa: E402


def _payload():
    return {
        "resources": {
            "binaries": [
                {"id": 0, "resourceId": "r0"},
                {"id": 1, "resourceId": "r1"},
            ]
        },
        "tenantId": "t",
    }


def _result(markdown):
    result = MagicMock()
    result.document.export_to_markdown.return_value = markdown
    return result


# Run operation() over two binaries with the given error strategy; 
# the binary whose id == failing_id raises during conversion. 
# Return the JSON payload posted to the enrich callback.
def _run(strategy, failing_id):

    def conversion(binary, tenant, configs):
        if binary["id"] == failing_id:
            raise ValueError("conversion boom")
        return _result(f"md-{binary['id']}")

    with patch.object(
        server, "conversion", side_effect=conversion
    ), patch.object(server, "requests") as requests_mock:
        server.operation(_payload(), {"error_strategy": strategy}, token="tok")
    return requests_mock.post.call_args.kwargs["json"]


def test_fail_fast_returns_error():
    posted = _run("fail-fast", failing_id=1)
    assert posted == {"error": "conversion failed"}


def test_all_success_returns_binaries():
    posted = _run("fail-fast", failing_id=-1)
    assert [b["markdown"] for b in posted["binaries"]] == ["md-0", "md-1"]


def test_fail_soft_isolates_error():
    posted = _run("fail-soft", failing_id=1)
    assert posted["binaries"][0]["markdown"] == "md-0"
    assert posted["binaries"][1]["error"]
