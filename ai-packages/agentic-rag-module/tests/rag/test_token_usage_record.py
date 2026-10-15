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


"""The token line of the end-of-request record.

Every request logs what it spent, summed over the answering model and the
utility one that routes, rewrites and resolves the language, because the
question the line answers is what the whole request cost.
"""

from types import SimpleNamespace

from app.rag.agentic_rag import _render_token_usage


def _usage(per_model):
    return SimpleNamespace(usage_metadata=per_model)


def test_a_single_model_is_summed_and_split():
    rendered = _render_token_usage(
        _usage({"gpt-4o-mini": {"input_tokens": 1200, "output_tokens": 300}})
    )

    assert "tokens_in=1200" in rendered
    assert "tokens_out=300" in rendered
    assert "gpt-4o-mini=1200/300" in rendered


def test_several_models_are_summed_together():
    # The utility model is a real part of the bill: a request that routes,
    # rewrites and resolves the language pays for all of it.
    rendered = _render_token_usage(
        _usage(
            {
                "gpt-4o-mini": {"input_tokens": 1200, "output_tokens": 300},
                "gpt-4o": {"input_tokens": 80, "output_tokens": 12},
            }
        )
    )

    assert "tokens_in=1280" in rendered
    assert "tokens_out=312" in rendered


def test_the_split_is_ordered_so_records_can_be_compared():
    rendered = _render_token_usage(
        _usage(
            {
                "zeta": {"input_tokens": 1, "output_tokens": 1},
                "alpha": {"input_tokens": 2, "output_tokens": 2},
            }
        )
    )

    assert rendered.index("alpha=") < rendered.index("zeta=")


def test_a_provider_that_reports_nothing_says_so():
    # Not every provider returns usage. Saying zero would read as a free
    # request instead of an unmeasured one.
    assert _render_token_usage(_usage({})) == "tokens=unavailable"
    assert _render_token_usage(_usage(None)) == "tokens=unavailable"
    assert _render_token_usage(SimpleNamespace()) == "tokens=unavailable"


def test_missing_counts_within_a_model_do_not_break_the_line():
    rendered = _render_token_usage(_usage({"partial": {"input_tokens": 50}}))

    assert "tokens_in=50" in rendered
    assert "tokens_out=0" in rendered
