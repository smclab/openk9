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


"""Tests for which loggers LOGGING_LEVEL governs.

Left on the root logger it would govern the whole process: asking for the
detail of the pipeline would also turn on the HTTP and SSE layers, which
print the body of the request sent to the model and every chunk of the
answer, and drown the pipeline records in an order of magnitude more lines.

The configuration is applied once, when the module is first imported, so
each case is measured in a fresh interpreter rather than by reloading:
``logging.basicConfig`` is a no-op once the root logger has a handler, and
a reload would silently assert nothing.
"""

import json
import subprocess
import sys
from pathlib import Path

import pytest

MODULE_ROOT = Path(__file__).resolve().parent.parent.parent

DEPENDENCY = "sse_starlette.sse"
APPLICATION = "app.rag.agentic_rag"

# Reports the level each logger ends up with, from a process that has just
# configured the logging the way the service does at startup.
PROBE = f"""
import json, logging, sys
sys.path.insert(0, {str(MODULE_ROOT)!r})
import app.utils.logger  # noqa: F401  (configures the logging on import)

print(json.dumps({{
    name: logging.getLevelName(logging.getLogger(name).getEffectiveLevel())
    for name in ({APPLICATION!r}, {DEPENDENCY!r})
}}))
"""


def _levels(**environment):
    """Effective level of each logger in a process configured with the given
    environment."""
    completed = subprocess.run(
        [sys.executable, "-c", PROBE],
        capture_output=True,
        text=True,
        check=True,
        cwd=MODULE_ROOT,
        env={"PATH": "/usr/bin:/bin", "HOME": str(Path.home()), **environment},
    )
    return json.loads(completed.stdout)


@pytest.fixture(scope="module")
def pipeline_debug():
    return _levels(LOGGING_LEVEL="DEBUG", DEPENDENCIES_LOGGING_LEVEL="INFO")


def test_debug_of_the_pipeline_leaves_the_dependencies_alone(pipeline_debug):
    assert pipeline_debug[APPLICATION] == "DEBUG"
    assert pipeline_debug[DEPENDENCY] == "INFO"


def test_dependencies_keep_reporting_what_matters(pipeline_debug):
    # Quietening them must not hide an OpenSearch or provider failure.
    assert pipeline_debug[DEPENDENCY] != "CRITICAL"


def test_the_dependencies_can_still_be_turned_on():
    levels = _levels(LOGGING_LEVEL="DEBUG", DEPENDENCIES_LOGGING_LEVEL="DEBUG")

    assert levels[DEPENDENCY] == "DEBUG"


def test_both_sides_default_to_info():
    levels = _levels()

    assert levels[APPLICATION] == "INFO"
    assert levels[DEPENDENCY] == "INFO"


def test_raising_the_application_level_does_not_raise_the_others():
    levels = _levels(LOGGING_LEVEL="WARNING")

    assert levels[APPLICATION] == "WARNING"
    assert levels[DEPENDENCY] == "INFO"
