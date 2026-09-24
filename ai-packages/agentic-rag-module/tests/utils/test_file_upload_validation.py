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

"""An uploaded file is validated before it is converted and indexed.

Only the configured extensions are accepted, compared as whole extensions,
and a file above the size limit is refused before it reaches the disk. Whatever the outcome, no temporary copy is left behind.
"""

import asyncio
import io
from unittest.mock import MagicMock

import pytest
from fastapi import UploadFile

from app.utils import file_upload

ALLOWED_EXTENSIONS = [".pdf", ".md", ".docx", ".xlsx", ".pptx", ".csv"]
MAX_SIZE = 1024


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


def _process(filename, upload_dir, content=b"contenuto"):
    upload = UploadFile(file=io.BytesIO(content), filename=filename)

    return asyncio.run(
        file_upload.process_file(
            file=upload,
            user_id="user-1",
            chat_id="chat-1",
            tenant_id="tenant-1",
            upload_file_extensions=ALLOWED_EXTENSIONS,
            upload_dir=str(upload_dir),
            max_upload_file_size=MAX_SIZE,
            opensearch_host="http://localhost:9200",
            grpc_datasource_host="localhost:50051",
            grpc_embedding_module_host="localhost:50053",
        )
    )


@pytest.mark.parametrize(
    "filename",
    [
        # Prefixes of allowed extensions: the match is on the whole extension.
        "legacy.xls",
        "legacy.doc",
        "legacy.ppt",
        "short.p",
        # No extension at all.
        "README",
        "script.exe",
    ],
)
def test_extension_not_configured_is_rejected(filename, tmp_path, pipeline):
    result = _process(filename, tmp_path)

    assert result == {
        "status": "error",
        "filename": filename,
        "error": "Invalid document type",
    }
    assert list(tmp_path.iterdir()) == []
    pipeline.DoclingLoader.assert_not_called()


def test_configured_extension_is_accepted(tmp_path, pipeline):
    result = _process("report.pdf", tmp_path)

    assert result == {"status": "success", "filename": "report.pdf"}
    pipeline.save_uploaded_documents.assert_called_once_with(
        "http://localhost:9200", "tenant-1", [{"vector": [0.1, 0.2, 0.3]}], 3
    )


def test_pdf_uses_the_pdf_pipeline(tmp_path, pipeline):
    _process("scan.pdf", tmp_path)

    pipeline.DocumentConverter.assert_called_once()
    assert (
        pipeline.DoclingLoader.call_args.kwargs["converter"]
        is pipeline.DocumentConverter.return_value
    )


def test_file_above_the_size_limit_is_rejected(tmp_path, pipeline):
    result = _process("big.pdf", tmp_path, content=b"x" * (MAX_SIZE + 1))

    assert result["status"] == "error"
    assert result["error"].startswith("File too large.")
    assert list(tmp_path.iterdir()) == []
    pipeline.DoclingLoader.assert_not_called()


def test_file_at_the_size_limit_is_accepted(tmp_path, pipeline):
    result = _process("exact.pdf", tmp_path, content=b"x" * MAX_SIZE)

    assert result["status"] == "success"


def test_unreadable_content_is_reported_and_removed(tmp_path, pipeline):
    pipeline.DoclingLoader.return_value.load.side_effect = RuntimeError("corrupt")

    result = _process("broken.pdf", tmp_path)

    assert result == {
        "status": "error",
        "filename": "broken.pdf",
        "error": "Failed to process file content.",
    }
    assert list(tmp_path.iterdir()) == []


def test_embedding_failure_is_reported_and_removed(tmp_path, pipeline):
    pipeline.documents_embedding.side_effect = RuntimeError("grpc down")

    result = _process("report.pdf", tmp_path)

    assert result == {
        "status": "error",
        "filename": "report.pdf",
        "error": "Failed to generate embeddings: grpc down",
    }
    assert list(tmp_path.iterdir()) == []


def test_temporary_copy_is_removed_after_success(tmp_path, pipeline):
    _process("report.pdf", tmp_path)

    assert list(tmp_path.iterdir()) == []
