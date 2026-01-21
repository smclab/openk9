import base64
from io import BytesIO

from docling.datamodel.base_models import FormatToExtensions
from docling.datamodel.document import DocumentStream, _DocumentConversionInput

from app.utils.exceptions import FormatError


def extract_extension_base64(base64_content: str) -> str:
    """
    Starting from a base64 string, extract the file's extension using Docling internal functions.

    Args:
        base64_content: String in base64

    Returns:
        File's extension (es. 'pdf', 'docx') or raise an error if 
        -   the extension could not be found
        -   the base64 string could not be decoded
    """
    try:
        decoded_content = base64.b64decode(base64_content)
        stream = DocumentStream(name="unknown", stream=BytesIO(decoded_content))
        dci = _DocumentConversionInput(path_or_stream_iterator=[])
        detected_format = dci._guess_format(stream)
        if detected_format and detected_format in FormatToExtensions:
            return FormatToExtensions[detected_format][0]
        raise FormatError("File format could not be detected or file is corrupted")

    except Exception:
        raise FormatError("File format could not be detected or file is corrupted")
