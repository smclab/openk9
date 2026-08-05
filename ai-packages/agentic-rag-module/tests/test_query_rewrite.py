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


from langchain_core.prompts import PromptTemplate

from app.utils.query_rewrite import escape_curly_braces


def test_escape_curly_braces_doubles_braces():
    assert escape_curly_braces('a {"k": "v"} b') == 'a {{"k": "v"}} b'
    assert escape_curly_braces("no braces") == "no braces"


def test_rewrite_template_renders_with_tenant_prompt_containing_braces():
    # Mirrors _rewrite_query: the tenant prompt is concatenated into the
    # template string, so its literal braces must be escaped (issue #2186),
    # while the boilerplate placeholders stay live.
    tenant_prompt = 'Rispondi in JSON come {"query": "..."}.'
    rewrite_query_prompt = escape_curly_braces(tenant_prompt) + (
        """
        **ORIGINAL QUERY:** "{query}"
        **PREVIOUS QUERY:** "{previous_query}"
        **PREVIOUS RESPONSE:** {previous_response}
        """
    )

    template = PromptTemplate.from_template(rewrite_query_prompt)
    rendered = template.format(
        query="q",
        previous_query="pq",
        previous_response='pr with {braces}',
    )

    assert '{"query": "..."}' in rendered
    assert "pr with {braces}" in rendered


def test_analyze_template_renders_with_tenant_prompt_containing_braces():
    # Mirrors analyze_and_rewrite_query_node concatenation.
    tenant_prompt = "Classifica usando le chiavi {follow_up} e {new}."
    analyze_query_prompt = escape_curly_braces(tenant_prompt) + (
        """
        **PREVIOUS CONVERSATION:** {context}
        **CURRENT QUESTION:** {query}
        """
    )

    template = PromptTemplate.from_template(analyze_query_prompt)
    rendered = template.format(query="q", context="ctx with {braces}")

    assert "{follow_up}" in rendered
    assert "ctx with {braces}" in rendered
