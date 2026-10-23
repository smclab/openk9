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


from app.utils.llm import get_structured_output_method


def test_ollama_uses_the_native_json_schema_field():
    # Ollama simulates the tool call instead of emitting it and answers in
    # prose: the caller gets no object back. Its native `format` field, which
    # LangChain reaches through `json_schema`, does answer.
    assert get_structured_output_method("ollama") == "json_schema"


def test_the_other_providers_stay_on_function_calling():
    # What runs in production, and the only method that carries the ge/le
    # bounds the evaluation schemas put on `vote`.
    assert get_structured_output_method("openai") == "function_calling"
    assert get_structured_output_method("watsonx") == "function_calling"
    assert get_structured_output_method("aws_bedrock") == "function_calling"


def test_an_unconfigured_provider_falls_back_to_function_calling():
    assert get_structured_output_method(None) == "function_calling"
    assert get_structured_output_method("some-future-provider") == "function_calling"
