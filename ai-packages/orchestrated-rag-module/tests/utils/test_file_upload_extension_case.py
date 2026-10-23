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

"""Upload extensions are compared regardless of case.

Files named in upper case, common from Windows and scanners, used to be
refused. Only the comparison ignores case: the extension indexed with the
document, which becomes part of the source title in the chat, is kept as
uploaded.
"""

import asyncio
import io
from unittest.mock import MagicMock

import pytest
from fastapi import UploadFile

from app.utils import file_upload

ALLOWED_EXTENSIONS = [".pdf", ".md", ".docx", ".xlsx", ".pptx", ".csv"]


@pytest.fixture
def pipeline(monkeypatch):
    """Replace conversion, embedding and indexing with recording mocks."""
    mocks = MagicMock()
    mocks.DoclingLoader.return_value.load.return_value = [
        MagicMock(page_content="un gatto e un topo")
    ]
    mocks.get_embedding_model_configuration.return_value = {"vector_size": 3}
    mocks.documents_embedding.return_value = [{"vector": [0.1, 0.2, 0.3]}]

    for name in (
        "DocumentConverter",
        "DoclingLoader",
        "get_embedding_model_configuration",
        "documents_embedding",
        "save_uploaded_documents",
    ):
        monkeypatch.setattr(file_upload, name, getattr(mocks, name))

    return mocks


def _process(filename, upload_dir, upload_file_extensions=ALLOWED_EXTENSIONS):
    upload = UploadFile(file=io.BytesIO(b"contenuto"), filename=filename)

    return asyncio.run(
        file_upload.process_file(
            file=upload,
            user_id="user-1",
            chat_id="chat-1",
            tenant_id="tenant-1",
            upload_file_extensions=upload_file_extensions,
            upload_dir=str(upload_dir),
            max_upload_file_size=1024,
            opensearch_host="http://localhost:9200",
            grpc_datasource_host="localhost:50051",
            grpc_embedding_module_host="localhost:50053",
        )
    )


@pytest.mark.parametrize("filename", ["REPORT.PDF", "Notes.Md", "slides.PPTX"])
def test_upper_case_extension_is_accepted(filename, tmp_path, pipeline):
    result = _process(filename, tmp_path)

    assert result == {"status": "success", "filename": filename}


def test_upper_case_configured_extension_matches(tmp_path, pipeline):
    result = _process("report.pdf", tmp_path, upload_file_extensions=[".PDF"])

    assert result == {"status": "success", "filename": "report.pdf"}


def test_upper_case_pdf_uses_the_pdf_pipeline(tmp_path, pipeline):
    _process("SCAN.PDF", tmp_path)

    pipeline.DocumentConverter.assert_called_once()
    assert (
        pipeline.DoclingLoader.call_args.kwargs["converter"]
        is pipeline.DocumentConverter.return_value
    )


def test_indexed_extension_keeps_its_original_case(tmp_path, pipeline):
    _process("SCAN.PDF", tmp_path)

    document = pipeline.documents_embedding.call_args.kwargs["document"]
    assert document["filename"] == "SCAN"
    assert document["file_extension"] == ".PDF"
