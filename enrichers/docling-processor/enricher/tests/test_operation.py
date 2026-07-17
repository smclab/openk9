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

import importlib.abc
import importlib.machinery
import sys
import types
from unittest.mock import MagicMock, patch

# ---------------------------------------------------------------------------
# Import shims. app.server pulls in fastapi, pydantic, dotenv and the heavy
# docling stack. None of those are needed to exercise operation(): its only
# real collaborators (conversion, requests.post) are patched per test. We stub
# the third-party modules so this test runs without installing them.
# ---------------------------------------------------------------------------


def _missing(name):
    try:
        __import__(name)
        return False
    except Exception:
        return True


class _StubLoader(importlib.abc.Loader):
    def create_module(self, spec):
        module = types.ModuleType(spec.name)
        module.__getattr__ = lambda name: MagicMock()
        return module

    def exec_module(self, module):
        pass


class _StubFinder(importlib.abc.MetaPathFinder):
    def __init__(self, roots):
        self._roots = roots

    def find_spec(self, fullname, path=None, target=None):
        if fullname.split(".")[0] in self._roots:
            return importlib.machinery.ModuleSpec(
                fullname, _StubLoader(), is_package=True
            )
        return None


def _install_import_shims():
    docling_roots = [n for n in ("docling", "docling_core") if _missing(n)]
    if docling_roots:
        sys.meta_path.insert(0, _StubFinder(docling_roots))

    if _missing("dotenv"):
        mod = types.ModuleType("dotenv")
        mod.load_dotenv = lambda *a, **k: None
        sys.modules["dotenv"] = mod

    if _missing("pydantic"):
        mod = types.ModuleType("pydantic")

        class BaseModel:
            pass

        mod.BaseModel = BaseModel
        sys.modules["pydantic"] = mod

    if _missing("fastapi"):
        mod = types.ModuleType("fastapi")

        class FastAPI:
            def post(self, *a, **k):
                return lambda fn: fn

            def get(self, *a, **k):
                return lambda fn: fn

        mod.FastAPI = FastAPI
        sys.modules["fastapi"] = mod


_install_import_shims()

from app import server  # noqa: E402


def _payload(n_binaries):
    return {
        "resources": {
            "binaries": [
                {"id": i, "url": f"http://example.test/{i}"}
                for i in range(n_binaries)
            ]
        },
        "tenantId": "tenant-x",
    }


def _ok_conversion(markdown):
    result = MagicMock()
    result.document.export_to_markdown.return_value = markdown
    return result


def _run_operation(configs, conversion_side_effect, n_binaries=2):
    with patch.object(
        server, "conversion", side_effect=conversion_side_effect
    ), patch.object(server, "requests") as requests_mock:
        server.operation(_payload(n_binaries), configs, token="callback-token")
    assert requests_mock.post.called, "callback POST was not invoked"
    return requests_mock.post.call_args.kwargs["json"]


def test_multibinary_fail_fast_returns_error_not_binaries():
    """A fail-fast conversion error must reach the callback as an error."""

    def side_effect(bin, tenant, configs):
        if bin["id"] == 1:
            raise ValueError("conversion boom")
        return _ok_conversion("md-0")

    posted = _run_operation({"error_strategy": "fail-fast"}, side_effect)

    assert posted == {"error": "conversion failed"}


def test_multibinary_default_strategy_returns_error():
    """Default strategy (else branch) also returns the error, not binaries."""

    def side_effect(bin, tenant, configs):
        if bin["id"] == 1:
            raise ValueError("conversion boom")
        return _ok_conversion("md-0")

    # no error_strategy -> operation() defaults to "fail_fast" -> else branch
    posted = _run_operation({}, side_effect)

    assert posted == {"error": "conversion failed"}


def test_multibinary_all_success_returns_binaries():
    """No error: the multi-binary contract stays {'binaries': [...]}."""

    def side_effect(bin, tenant, configs):
        return _ok_conversion(f"md-{bin['id']}")

    posted = _run_operation({"error_strategy": "fail-fast"}, side_effect)

    assert "error" not in posted
    assert [b["markdown"] for b in posted["binaries"]] == ["md-0", "md-1"]


def test_multibinary_fail_soft_isolates_error():
    """fail-soft keeps returning binaries with a per-binary error entry."""

    def side_effect(bin, tenant, configs):
        if bin["id"] == 1:
            raise ValueError("conversion boom")
        return _ok_conversion("md-0")

    posted = _run_operation({"error_strategy": "fail-soft"}, side_effect)

    assert "error" not in posted
    assert posted["binaries"][0]["markdown"] == "md-0"
    assert posted["binaries"][1]["error"]
