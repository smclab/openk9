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


from langchain_core.runnables import RunnableLambda

from app.rag.agentic_rag import RagGraph

QUERY = "dimmi di più"
PREVIOUS_QUERY = "Che cos'è la garanzia Infortuni del Conducente?"
PREVIOUS_RESPONSE = "È una copertura assicurativa per il conducente."


def _graph(chain_output, configuration=None):
    """Build a RagGraph stub exercising the real _rewrite_query: only the
    rewriting LLM is replaced, by a runnable returning a fixed string and
    recording the prompt it read."""
    graph = RagGraph.__new__(RagGraph)
    graph.configuration = configuration or {}
    graph.sent_prompts = []

    def rewrite(prompt_value):
        graph.sent_prompts.append(prompt_value.to_string())
        return chain_output

    graph.utility_llm = RunnableLambda(rewrite)
    return graph


def _sections(prompt, headings):
    """Split the prompt at the given headings, in order, and return the text
    under each one."""
    sections = []
    for heading, next_heading in zip(headings, headings[1:]):
        body = prompt.split(heading, 1)[1].split(next_heading, 1)[0]
        sections.append(body.strip())
    return sections


def test_rewrite_returns_the_chain_output_verbatim():
    graph = _graph("Puoi fornirmi maggiori dettagli?")

    rewritten = graph._rewrite_query(QUERY, PREVIOUS_QUERY, PREVIOUS_RESPONSE)

    # A rewrite sharing no content word with the previous query used to get the
    # previous query prepended, which put the earlier subject back into the
    # retrieval query even when the turn was a topic switch. The chain output is
    # now what reaches retrieval, untouched.
    assert rewritten == "Puoi fornirmi maggiori dettagli?"
    assert PREVIOUS_QUERY not in rewritten


def test_default_prompt_puts_each_turn_in_its_own_section():
    graph = _graph("riscritta")

    graph._rewrite_query(QUERY, PREVIOUS_QUERY, PREVIOUS_RESPONSE)

    prompt = graph.sent_prompts[0]
    assert "FOLLOW-UP REWRITING GUIDELINES" in prompt
    assert _sections(
        prompt,
        [
            "**ORIGINAL QUERY:**",
            "**PREVIOUS QUERY:**",
            "**PREVIOUS RESPONSE:**",
            "Reply ONLY with the rewritten query.",
        ],
    ) == [f'"{QUERY}"', f'"{PREVIOUS_QUERY}"', PREVIOUS_RESPONSE]


def test_tenant_prompt_replaces_the_default_guidelines():
    # Braces in the tenant text are literal, not template variables.
    tenant_prompt = "Riscrivi la domanda {come richiesto} dal tenant."
    graph = _graph("riscritta", {"rephrase_prompt_template": tenant_prompt})

    graph._rewrite_query(QUERY, PREVIOUS_QUERY, PREVIOUS_RESPONSE)

    prompt = graph.sent_prompts[0]
    assert prompt.startswith(tenant_prompt)
    assert "FOLLOW-UP REWRITING GUIDELINES" not in prompt
    assert _sections(
        prompt,
        [
            "**QUERY ORIGINALE:**",
            "**QUERY PRECEDENTE:**",
            "**RISPOSTA PRECEDENTE:**",
            "Rispondi ESCLUSIVAMENTE con la query riscritta.",
        ],
    ) == [f'"{QUERY}"', f'"{PREVIOUS_QUERY}"', PREVIOUS_RESPONSE]
