import base64
import os
from io import BytesIO

from docling.document_converter import DocumentConverter
from docling_core.types.io import DocumentStream

from app.utils.fm_helper import FileManagerHelper
from app.utils.format_detect import extract_extension_base64

FILE_MANAGER_HOST = os.getenv("FILE_MANAGER_HOST", default="http://localhost:8000")
DATASOURCE_HOST = os.getenv("DATASOURCE_HOST", default="http://localhost:8001")
FMHelper = FileManagerHelper(FILE_MANAGER_HOST)


def conversion(bin, tenant):
    resource_id = bin.get("resourceId")
    resource = FMHelper.get_base64(tenant, resource_id)
    bites = BytesIO(base64.b64decode(resource))
    extension = extract_extension_base64(resource)
    source = DocumentStream(name=f"doc.{extension}", stream=bites)
    converter = DocumentConverter()
    result = converter.convert(source)
    return result
