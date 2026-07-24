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

"""Shared test bootstrap.

Puts the module root on sys.path (so ``import app`` resolves regardless
of the working directory) and generates the gRPC stubs from
embedding.proto when they are missing or stale — in the image they are
generated at Docker build time. Generation is skipped when grpc_tools is
not installed, so the pure-Python suites (embedding/, utils/) still run;
the gRPC suite (server/) fails explicitly on the missing stub instead.
"""

import importlib.util
import pathlib
import subprocess
import sys

ROOT = pathlib.Path(__file__).resolve().parents[1]
PROTO = ROOT / "app/external_services/grpc/embedding/embedding.proto"
PB2 = ROOT / "app/external_services/grpc/embedding/embedding_pb2.py"

if str(ROOT) not in sys.path:
    sys.path.insert(0, str(ROOT))

_stale = not PB2.exists() or PB2.stat().st_mtime < PROTO.stat().st_mtime

if _stale and importlib.util.find_spec("grpc_tools") is not None:
    subprocess.check_call(
        [
            sys.executable,
            "-m",
            "grpc_tools.protoc",
            "-I.",
            "--python_out=.",
            "--grpc_python_out=.",
            str(PROTO.relative_to(ROOT)),
        ],
        cwd=ROOT,
    )
