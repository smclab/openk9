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


import utils.helpers as helpers


def test_score_builds_chunks_and_collects_every_metric(monkeypatch):
    received = {}

    def fake_scr(chunks):
        received["scr"] = chunks
        return {"forward": 1.0, "backward": 1.0}

    def fake_rb(chunks, doc):
        received["rb"] = (chunks, doc)
        return {"redundancy": 0.0}

    def fake_lf(chunks, doc):
        received["lf"] = (chunks, doc)
        return {"coverage": 1.0}

    monkeypatch.setattr(helpers, "calculate_scr", fake_scr)
    monkeypatch.setattr(helpers, "calculate_rb", fake_rb)
    monkeypatch.setattr(helpers, "calculate_lf", fake_lf)

    result = helpers.score(
        {
            "chunks": [
                {"chunk-1": {"text": "primo", "embedding": [0.1, 0.2]}},
                {"chunk-2": {"text": "secondo", "embedding": [0.3, 0.4]}},
            ],
            "text": "primo secondo",
        }
    )

    assert result == {
        "semantic_choerence": {"forward": 1.0, "backward": 1.0},
        "redundancy_bloat": {"redundancy": 0.0},
        "layout_fidelity": {"coverage": 1.0},
    }
    chunks = received["scr"]
    assert [c.text for c in chunks] == ["primo", "secondo"]
    assert list(chunks[0].embedding) == [0.1, 0.2]
    assert received["rb"] == (chunks, "primo secondo")
    assert received["lf"] == (chunks, "primo secondo")
