from typing import Dict, Type

from docling.backend.json.docling_json_backend import DoclingJSONBackend
from docling.backend.mets_gbs_backend import MetsGbsDocumentBackend
from docling.backend.webvtt_backend import WebVTTDocumentBackend
from docling.datamodel import asr_model_specs
from docling.datamodel.backend_options import HTMLBackendOptions
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

html_backend_options = HTMLBackendOptions(
    source_uri="SOURCE",
)

all_formats = {
    InputFormat.CSV: CsvFormatOption(),
    InputFormat.XLSX: ExcelFormatOption(),
    InputFormat.DOCX: WordFormatOption(),
    InputFormat.PPTX: PowerpointFormatOption(),
    InputFormat.MD: MarkdownFormatOption(),
    InputFormat.ASCIIDOC: AsciiDocFormatOption(),
    InputFormat.HTML: HTMLFormatOption(),
    InputFormat.XML_USPTO: PatentUsptoFormatOption(),
    InputFormat.XML_JATS: XMLJatsFormatOption(),
    InputFormat.METS_GBS: FormatOption(
        pipeline_cls=StandardPdfPipeline, backend=MetsGbsDocumentBackend
    ),
    InputFormat.IMAGE: ImageFormatOption(),
    InputFormat.PDF: PdfFormatOption(),
    InputFormat.JSON_DOCLING: FormatOption(
        pipeline_cls=SimplePipeline, backend=DoclingJSONBackend
    ),
    InputFormat.AUDIO: AudioFormatOption(),
    InputFormat.VTT: FormatOption(
        pipeline_cls=SimplePipeline, backend=WebVTTDocumentBackend
    ),
}

FORMAT_TO_OPTIONS_CLS: Dict[InputFormat, Type] = {
    InputFormat.CSV: CsvFormatOption,
    InputFormat.XLSX: ExcelFormatOption,
    InputFormat.DOCX: WordFormatOption,
    InputFormat.PPTX: PowerpointFormatOption,
    InputFormat.MD: MarkdownFormatOption,
    InputFormat.ASCIIDOC: AsciiDocFormatOption,
    InputFormat.HTML: HTMLFormatOption,
    InputFormat.XML_USPTO: PatentUsptoFormatOption,
    InputFormat.XML_JATS: XMLJatsFormatOption,
    InputFormat.METS_GBS: FormatOption,  # Rimosso il costruttore con parametri
    InputFormat.IMAGE: ImageFormatOption,
    InputFormat.PDF: PdfFormatOption,
    InputFormat.JSON_DOCLING: FormatOption,  # Rimosso il costruttore con parametri
    InputFormat.AUDIO: AudioFormatOption,
    InputFormat.VTT: FormatOption,  # Rimosso il costruttore con parametri
}


def collect_format_options_schemas(format_options: dict[InputFormat, FormatOption]):
    result = {}
    for fmt, opt in format_options.items():
        result[fmt.value] = (
            opt.pipeline_options.model_dump() if opt.pipeline_options else None
        )
    return result


def flatten(to_flatten, separator="."):
    found = True
    current_data = to_flatten

    while found:
        found = False
        new_flattened = {}
        for key, val in current_data.items():
            if isinstance(val, dict):
                for sub_key, sub_val in val.items():
                    new_flattened[f"{key}{separator}{sub_key}"] = sub_val
                found = True
            else:
                new_flattened[key] = val

        current_data = new_flattened

    return current_data


def unflatten_dict(flat_dict, separator="."):
    unflattened = {}

    for key, value in flat_dict.items():
        parts = key.split(separator)
        current = unflattened

        for part in parts[:-1]:
            if part not in current:
                current[part] = {}
            current = current[part]

        current[parts[-1]] = value

    return unflattened


def get_format_options(configs, format):

    options = collect_format_options_schemas(all_formats)
    signature = options[format]
    flat_signature = flatten(signature)
    flat_arguments = {
        hit: configs[hit] for hit in configs if (hit in flat_signature.keys())
    }
    arguments = unflatten_dict(flat_arguments)

    tipo = "pdf"
    opts_cls = FORMAT_TO_OPTIONS_CLS.get(tipo)
    in_form = InputFormat(tipo)
    format_options = {
        in_form: opts_cls(**arguments),
    }
    return format_options
