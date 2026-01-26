import base64
import os
from io import BytesIO

from docling.document_converter import DocumentConverter
from docling_core.types.io import DocumentStream

from app.utils.fm_helper import FileManagerHelper
from app.utils.format_detect import extract_extension_base64
from app.utils.logger import logger

FILE_MANAGER_HOST = os.getenv("FILE_MANAGER_HOST", default="http://localhost:8000")
DATASOURCE_HOST = os.getenv("DATASOURCE_HOST", default="http://localhost:8001")
FMHelper = FileManagerHelper(FILE_MANAGER_HOST)


def conversion(bin, tenant):
    """
    Converts a binary resource into a document object using a base64-encoded source.

    This function retrieves a base64-encoded resource associated with the given
    tenant and resource identifier, decodes it into a binary stream, determines
    the document extension, and converts it into an internal document representation
    using the configured document converter.

    Args:
        bin (dict): A dictionary representing a binary resource. It must contain
            the key `"resourceId"` identifying the resource to be retrieved.
        tenant (str): The tenant identifier used to resolve the resource context.

    Returns:
        Any: The result of the document conversion process. The returned object
        is expected to expose a `document` attribute supporting export operations
        (e.g. `export_to_markdown()`).

    """
    resource_id = bin.get("resourceId")
    resource = FMHelper.get_base64(tenant, resource_id)
    bites = BytesIO(base64.b64decode(resource))
    extension = extract_extension_base64(resource)
    source = DocumentStream(name=f"doc.{extension}", stream=bites)
    converter = DocumentConverter()
    result = converter.convert(source)
    return result
