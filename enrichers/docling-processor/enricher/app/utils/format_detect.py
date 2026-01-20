import base64
from io import BytesIO

from docling.datamodel.base_models import FormatToExtensions
from docling.datamodel.document import DocumentStream, _DocumentConversionInput

from app.utils.exceptions import FormatError


def extract_extension_base64(base64_content: str) -> str:
    """
    Estrae l'estensione del file da una stringa base64 usando le funzionalità di Docling.

    Args:
        base64_content: Stringa codificata in base64

    Returns:
        Estensione del file (es. 'pdf', 'docx') o stringa vuota se non rilevato
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
