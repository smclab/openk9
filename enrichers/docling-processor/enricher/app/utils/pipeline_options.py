import logging
from typing import Any, Callable, Dict, Type

from docling.backend.json.docling_json_backend import DoclingJSONBackend
from docling.backend.mets_gbs_backend import MetsGbsDocumentBackend
from docling.backend.webvtt_backend import WebVTTDocumentBackend
from docling.datamodel.base_models import InputFormat
from docling.document_converter import (
    AsciiDocFormatOption,
    AudioFormatOption,
    CsvFormatOption,
    ExcelFormatOption,
    FormatOption,
    HTMLFormatOption,
    ImageFormatOption,
    MarkdownFormatOption,
    PatentUsptoFormatOption,
    PdfFormatOption,
    PowerpointFormatOption,
    WordFormatOption,
    XMLJatsFormatOption,
)
from docling.pipeline.simple_pipeline import SimplePipeline
from docling.pipeline.standard_pdf_pipeline import StandardPdfPipeline

# =========================
# FORMAT FACTORIES (UNIFICATO)
# =========================

FORMAT_FACTORIES: Dict[InputFormat, Callable[[], FormatOption]] = {
    InputFormat.CSV: CsvFormatOption,
    InputFormat.XLSX: ExcelFormatOption,
    InputFormat.DOCX: WordFormatOption,
    InputFormat.PPTX: PowerpointFormatOption,
    InputFormat.MD: MarkdownFormatOption,
    InputFormat.ASCIIDOC: AsciiDocFormatOption,
    InputFormat.HTML: HTMLFormatOption,
    InputFormat.XML_USPTO: PatentUsptoFormatOption,
    InputFormat.XML_JATS: XMLJatsFormatOption,
    InputFormat.IMAGE: ImageFormatOption,
    InputFormat.PDF: PdfFormatOption,
    InputFormat.AUDIO: AudioFormatOption,
    # Formati speciali (richiedono backend/pipeline)
    InputFormat.METS_GBS: lambda: FormatOption(
        pipeline_cls=StandardPdfPipeline,
        backend=MetsGbsDocumentBackend,
    ),
    InputFormat.JSON_DOCLING: lambda: FormatOption(
        pipeline_cls=SimplePipeline,
        backend=DoclingJSONBackend,
    ),
    InputFormat.VTT: lambda: FormatOption(
        pipeline_cls=SimplePipeline,
        backend=WebVTTDocumentBackend,
    ),
}


# =========================
# UTILS
# =========================


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
        return [normalize_dict(v) for v in d]
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

    # Factory
    factory = FORMAT_FACTORIES.get(in_format)
    if factory is None:
        raise ValueError(f"Unsupported format: {in_format}")

    # Istanza opzioni
    opts = factory()

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
