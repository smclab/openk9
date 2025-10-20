import os
import uuid

import aiofiles
from docling.datamodel.pipeline_options import PdfPipelineOptions
from docling.document_converter import (
    DocumentConverter,
    InputFormat,
    PdfFormatOption,
)
from fastapi import UploadFile
from langchain_docling import DoclingLoader
from langchain_docling.loader import ExportType

from app.external_services.grpc.grpc_client import (
    get_embedding_model_configuration,
)
from app.utils.chat_history import save_uploaded_documents
from app.utils.embedding import documents_embedding
from app.utils.logger import logger


async def process_file(
    file: UploadFile,
    user_id: str,
    chat_id: str,
    realm_name: str,
    virtual_host: str,
    upload_file_extensions: list,
    upload_dir: str,
    max_upload_file_size: int,
    opensearch_host: str,
    grpc_datasource_host: str,
    grpc_embedding_module_host: str,
):
    """
    Process an uploaded file through validation, conversion, embedding, and storage.

    This function handles the complete pipeline for processing user-uploaded files:

    1. Validates file type and size
    2. Securely saves the file to disk
    3. Converts document content to markdown using Docling
    4. Generates embeddings via gRPC services
    5. Stores embedded documents in OpenSearch
    6. Cleans up temporary files

    :param file: The uploaded file object from FastAPI
    :type file: UploadFile
    :param user_id: Unique identifier for the user
    :type user_id: str
    :param chat_id: Unique identifier for the chat session
    :type chat_id: str
    :param realm_name: Name of the realm/namespace for data isolation
    :type realm_name: str
    :param virtual_host: Virtual host identifier for multi-tenancy
    :type virtual_host: str
    :param upload_file_extensions: List of allowed file extensions (e.g., ['.pdf', '.docx'])
    :type upload_file_extensions: list
    :param upload_dir: Directory path where temporary files are stored
    :type upload_dir: str
    :param max_upload_file_size: Maximum allowed file size
    :type max_upload_file_size: int
    :param opensearch_host: Host address for OpenSearch connection
    :type opensearch_host: str
    :param grpc_datasource_host: Host address for gRPC datasource service
    :type grpc_datasource_host: str
    :param grpc_embedding_module_host: Host address for gRPC embedding service
    :type grpc_embedding_module_host: str

    :return: Dictionary containing processing status and metadata
    :rtype: dict

    :raises Exception: Various exceptions during file processing, embedding, or storage

    **Example Return Values:**

    Success case:

    .. code-block:: python

        {
            "status": "success",
            "filename": "document.pdf"
        }

    Error cases:

    .. code-block:: python

        {
            "status": "error",
            "filename": "document.pdf",
            "error": "Invalid document type"
        }

        {
            "status": "error",
            "filename": "document.pdf",
            "error": "File too large. Max size is 10.00 MB"
        }

        {
            "status": "error",
            "filename": "document.pdf",
            "error": "Failed to process file content."
        }

    **Processing Steps:**

    1. **Validation**: Checks file extension and size limits
    2. **Storage**: Saves file securely with UUID-based naming
    3. **Conversion**: Uses DoclingLoader to convert document to markdown
    4. **Embedding**: Generates embeddings via gRPC services
    5. **Indexing**: Stores embedded documents in OpenSearch
    6. **Cleanup**: Removes temporary files regardless of success/error
    """
    unique_id = uuid.uuid4()
    filename, file_extension = os.path.splitext(file.filename or "unnamed")

    if file_extension not in upload_file_extensions:
        logger.error(f"File {filename}: invalid document type")
        return {
            "status": "error",
            "filename": file.filename,
            "error": "Invalid document type",
        }

    renamed_uploaded_file = f"{upload_dir}/{unique_id}{file_extension}"

    try:
        content = await file.read()
        if len(content) > max_upload_file_size:
            logger.error(f"File {filename} too large")
            return {
                "status": "error",
                "filename": file.filename,
                "error": f"File too large. Max size is {max_upload_file_size / (1024 * 1024):.2f} MB",
            }

        async with aiofiles.open(renamed_uploaded_file, "wb") as buffer:
            await buffer.write(content)

    except Exception as e:
        logger.error(f"Failed to save file {filename}: {str(e)}")
        return {"status": "error", "filename": file.filename, "error": str(e)}

    try:
        converter = None
        export_type = ExportType.MARKDOWN

        if file_extension == ".pdf":
            pipeline_options = PdfPipelineOptions(do_ocr=False)
            converter = DocumentConverter(
                format_options={
                    InputFormat.PDF: PdfFormatOption(pipeline_options=pipeline_options),
                },
            )

        loader = DoclingLoader(
            converter=converter,
            export_type=export_type,
            file_path=renamed_uploaded_file,
        )
        docs = loader.load()
    except Exception as e:
        os.remove(renamed_uploaded_file)
        logger.error(f"Failed to load file {filename}: {str(e)}")
        return {
            "status": "error",
            "filename": file.filename,
            "error": "Failed to process file content.",
        }

    try:
        embedding_model_configuration = get_embedding_model_configuration(
            grpc_host=grpc_datasource_host, virtual_host=virtual_host
        )
        vector_size = embedding_model_configuration.get("vector_size")

        for doc in docs:
            document = {
                "document_id": unique_id,
                "filename": filename,
                "file_extension": file_extension,
                "user_id": user_id,
                "chat_id": chat_id,
                "text": doc.page_content,
            }
            embedded_documents = documents_embedding(
                grpc_host_embedding=grpc_embedding_module_host,
                embedding_model_configuration=embedding_model_configuration,
                document=document,
            )
            save_uploaded_documents(
                opensearch_host, realm_name, embedded_documents, vector_size
            )

        return {"status": "success", "filename": file.filename}

    except Exception as e:
        logger.error(f"Failed to generate embeddings for {filename}: {str(e)}")
        return {
            "status": "error",
            "filename": file.filename,
            "error": f"Failed to generate embeddings: {str(e)}",
        }
    finally:
        if os.path.exists(renamed_uploaded_file):
            os.remove(renamed_uploaded_file)
