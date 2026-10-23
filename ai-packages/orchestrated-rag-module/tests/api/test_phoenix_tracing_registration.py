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


"""Registering the Phoenix tracer provider, as the server does when Arize
Phoenix is enabled, works with the installed OpenTelemetry exporters."""

from phoenix.otel import register


def test_phoenix_tracer_provider_registers():
    tracer_provider = register(
        project_name="openk9",
        endpoint="http://phoenix:6006/v1/traces",
        set_global_tracer_provider=False,
        verbose=False,
    )

    try:
        assert tracer_provider is not None
    finally:
        tracer_provider.shutdown()
