import logging
from typing import Any, Dict, FrozenSet

from docling.datamodel.base_models import InputFormat
from docling.datamodel.pipeline_options import EasyOcrOptions, PdfPipelineOptions
from docling.document_converter import FormatOption, _get_default_option

# =========================
# SUPPORTED FORMATS
# =========================

# docling knows more formats than this image can convert: some backends need
# an install extra we do not ship (odfdo for OpenDocument, arelle-release for
# XBRL, whisper/librosa for audio and video) and would only fail deep inside
# the backend. This set is the gate; the FormatOption itself comes from
# docling.
SUPPORTED_FORMATS: FrozenSet[InputFormat] = frozenset(
    {
        InputFormat.CSV,
        InputFormat.XLSX,
        InputFormat.XLS,
        InputFormat.DOCX,
        InputFormat.DOC,
        InputFormat.PPTX,
        InputFormat.PPT,
        InputFormat.MD,
        InputFormat.ASCIIDOC,
        InputFormat.HTML,
        InputFormat.XML_USPTO,
        InputFormat.XML_JATS,
        InputFormat.XML_DOCLANG,
        InputFormat.DCLX,
        InputFormat.IMAGE,
        InputFormat.PDF,
        InputFormat.METS_GBS,
        InputFormat.JSON_DOCLING,
        InputFormat.VTT,
        InputFormat.LATEX,
        InputFormat.EMAIL,
        InputFormat.EPUB,
        InputFormat.BOXNOTE,
        InputFormat.IWORK_PAGES,
        InputFormat.EBCDIC,
    }
)


# =========================
# UTILS
# =========================
def fix_nested_lists(value):
    if isinstance(value, list) and len(value) == 1 and isinstance(value[0], list):
        return fix_nested_lists(value[0])
    return value


def normalize_value(value):
    if value == "":
        return None

    if isinstance(value, str):
        # bool
        if value.lower() in ["true", "false"]:
            return value.lower() == "true"

        # int
        if value.isdigit():
            return int(value)

        # float
        try:
            return float(value)
        except ValueError:
            pass

    return value


def normalize_dict(d):
    if isinstance(d, dict):
        return {k: normalize_dict(v) for k, v in d.items()}
    elif isinstance(d, list):
        return fix_nested_lists([normalize_dict(v) for v in d])
    else:
        return normalize_value(d)


def flatten(
    data: Dict[str, Any], parent_key: str = "", sep: str = "."
) -> Dict[str, Any]:
    """Flatten dict annidato."""
    items = {}
    for k, v in data.items():
        new_key = f"{parent_key}{sep}{k}" if parent_key else k
        if isinstance(v, dict):
            items.update(flatten(v, new_key, sep))
        else:
            items[new_key] = v
    return items


def unflatten_dict(data: Dict[str, Any], sep: str = ".") -> Dict[str, Any]:
    """Ricostruisce dict annidato da chiavi flat."""
    result: Dict[str, Any] = {}

    for key, value in data.items():
        parts = key.split(sep)
        current = result

        for part in parts[:-1]:
            if part not in current:
                current[part] = {}
            elif not isinstance(current[part], dict):
                raise ValueError(f"Conflict at key: {key}")
            current = current[part]

        current[parts[-1]] = value

    return result


# =========================
# CONFIG MERGE
# =========================


def add_configs(opts: Any, arguments: Dict[str, Any]) -> Any:
    """
    Applica ricorsivamente configurazioni a un oggetto.
    """
    if opts is None:
        return None

    for key, value in arguments.items():
        if not hasattr(opts, key):
            logging.debug(f"Skipping unknown config key: {key}")
            continue

        current_attr = getattr(opts, key)

        # Caso nested dict → ricorsione
        if isinstance(value, dict) and current_attr is not None:
            updated = add_configs(current_attr, value)
            setattr(opts, key, updated)
        else:
            setattr(opts, key, value)

    return opts


# =========================
# MAIN API
# =========================


def get_format_options(
    configs: Dict[str, Any],
    format: InputFormat | str,
) -> Dict[InputFormat, FormatOption]:
    """
    Crea FormatOption configurato a partire da config flat.
    """

    # Validazione formato
    try:
        in_format = InputFormat(format)
    except Exception as e:
        raise ValueError(f"Invalid format: {format}") from e

    if in_format not in SUPPORTED_FORMATS:
        raise ValueError(f"Unsupported format: {in_format}")

    # Options instance: backend and pipeline are docling's own defaults
    opts = _get_default_option(in_format)

    # docling defaults the PDF pipeline's OCR to OcrAutoOptions, which picks an
    # engine by probing the environment and forwards only `mode` to it,
    # dropping `lang`. Pin EasyOCR so the enrich item's ocr_options keep having
    # an effect.
    if isinstance(opts.pipeline_options, PdfPipelineOptions):
        opts.pipeline_options.ocr_options = EasyOcrOptions()

    # Se non ci sono config → ritorna subito
    if not configs:
        return {in_format: opts}

    logging.debug(f"Raw configs: {configs}")

    # 1. Unflatten
    arguments = unflatten_dict(configs)

    # 2. Normalize (🔥 fondamentale)
    arguments = normalize_dict(arguments)

    # 3. Estrai sezioni corrette
    pipeline_args = arguments.get("pipeline_options", {})
    backend_args = arguments.get("backend_options", {})

    # 4. Applica config pipeline
    if opts.pipeline_options:
        opts.pipeline_options = add_configs(opts.pipeline_options, pipeline_args)

    # 5. (opzionale) backend config
    if hasattr(opts, "backend_options") and backend_args:
        opts.backend_options = add_configs(opts.backend_options, backend_args)

    return {in_format: opts}


# =========================
# EXPORT SCHEMAS
# =========================


def collect_format_options_schemas(
    format_options: Dict[InputFormat, FormatOption],
) -> Dict[str, Any]:
    """
    Estrae schema delle pipeline options.
    """
    result = {}

    for fmt, opt in format_options.items():
        if hasattr(opt, "pipeline_options") and opt.pipeline_options:
            result[fmt.value] = opt.pipeline_options.model_dump()
        else:
            result[fmt.value] = None

    return result
