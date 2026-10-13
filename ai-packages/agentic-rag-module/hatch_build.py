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

"""Generate the gRPC stubs when uv (re)installs the project.

uv rebuilds the project whenever a file listed in ``[tool.uv] cache-keys``
changes, so ``uv run`` keeps the stubs in sync with the .proto files. Stubs
that are already newer than their .proto are left untouched: rewriting them
would change their mtime and make uv rebuild on every run.
"""

from importlib.resources import files
from pathlib import Path

from hatchling.builders.hooks.plugin.interface import BuildHookInterface


class GrpcStubsBuildHook(BuildHookInterface):
    def initialize(self, version, build_data):
        from grpc_tools import protoc

        root = Path(self.root)
        # Well-known types (google/protobuf/*.proto) bundled with grpc_tools,
        # added the same way `python -m grpc_tools.protoc` does
        well_known_protos = files("grpc_tools") / "_proto"
        for proto in sorted(root.glob("app/**/*.proto")):
            stubs = [
                proto.with_name(proto.stem + suffix)
                for suffix in ("_pb2.py", "_pb2_grpc.py")
            ]
            if all(
                stub.exists() and stub.stat().st_mtime >= proto.stat().st_mtime
                for stub in stubs
            ):
                continue
            exit_code = protoc.main(
                [
                    "grpc_tools.protoc",
                    f"-I{root}",
                    f"-I{well_known_protos}",
                    f"--python_out={root}",
                    f"--grpc_python_out={root}",
                    str(proto),
                ]
            )
            if exit_code != 0:
                raise RuntimeError(f"protoc failed on {proto.relative_to(root)}")
