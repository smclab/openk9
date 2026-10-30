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

"""Field examples are declared the pydantic 2 way and still reach the schemas.

An ``example=`` keyword on ``Field`` is deprecated and goes away with
pydantic 3; ``examples=[...]`` replaces it. The examples still show up in
the OpenAPI document and in the structured output schema the judge reads.

The deprecation fires when a model class is defined, that is on the first
import, so the check runs in a fresh interpreter rather than by reloading
modules the rest of the suite already holds.
"""

import subprocess
import sys
import tempfile
from pathlib import Path

import pytest

from app import server
from app.rag import agentic_rag, evaluations

MODULE_ROOT = Path(__file__).resolve().parent.parent.parent

PROBE = f"""
import sys
sys.path.insert(0, {str(MODULE_ROOT)!r})
import app.models.models
import app.rag.agentic_rag
import app.rag.evaluations
"""


def test_models_import_without_deprecated_field_keywords():
    # The module's .env is kept out by running from an empty directory.
    with tempfile.TemporaryDirectory() as empty_directory:
        completed = subprocess.run(
            [
                sys.executable,
                "-W",
                "error:Using extra keyword arguments on `Field`",
                "-c",
                PROBE,
            ],
            capture_output=True,
            check=False,
            text=True,
            cwd=empty_directory,
            env={
                "PATH": "/usr/bin:/bin",
                "HOME": str(Path.home()),
                "ORIGINS": "http://localhost",
            },
        )

    assert completed.returncode == 0, completed.stderr


@pytest.mark.parametrize(
    ("schema", "field", "examples"),
    [
        ("Media", "contentType", ["image/png"]),
        ("SearchToken", "filter", [False]),
        ("SearchQuery", "searchText", ["What is OpenK9?"]),
        ("SearchQuery", "datasourceIds", [[1, 2]]),
        ("SearchQueryChat", "chatId", ["chat_abc123def456"]),
        ("UserChats", "paginationSize", [10]),
        ("ChatMessage", "newTitle", ["Project Discussion"]),
        ("Evaluation", "start_time", ["2025-12-17"]),
    ],
)
def test_request_body_fields_keep_their_example_in_openapi(schema, field, examples):
    properties = server.app.openapi()["components"]["schemas"][schema]["properties"]

    assert properties[field]["examples"] == examples


def test_header_fields_keep_their_example_in_openapi():
    headers = {
        parameter["name"]: parameter
        for path in server.app.openapi()["paths"].values()
        for operation in path.values()
        for parameter in operation.get("parameters", [])
        if parameter["in"] == "header"
    }

    assert headers["x-tenant-id"]["schema"]["examples"] == ["tenant-12345"]


@pytest.mark.parametrize("module", [agentic_rag, evaluations])
def test_retriever_judgment_schema_keeps_the_chunk_id_example(module):
    schema = module.RetrieverEvaluationResponse.model_json_schema()

    assert schema["properties"]["chunk_id"] == {
        "description": "Unique identifier chunk",
        "examples": ["9460035979983399"],
        "title": "Chunk Id",
        "type": "string",
    }
