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


def escape_curly_braces(text):
    """Escape literal curly braces so dynamic content is safe inside a
    ``PromptTemplate``.

    Tenant-configured prompts may contain literal ``{``/``}`` (e.g. JSON
    examples). Doubling them prevents ``PromptTemplate.from_template`` from
    interpreting them as template variables.
    """
    return text.replace("{", "{{").replace("}", "}}")
