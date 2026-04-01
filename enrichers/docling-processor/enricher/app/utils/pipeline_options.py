import logging
from typing import Dict, Type

from docling.backend.json.docling_json_backend import DoclingJSONBackend
from docling.backend.mets_gbs_backend import MetsGbsDocumentBackend
from docling.backend.webvtt_backend import WebVTTDocumentBackend
from docling.datamodel import asr_model_specs

# from docling.datamodel.backend_options import HTMLBackendOptions
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

# html_backend_options = HTMLBackendOptions(
#     source_uri="SOURCE",
# )

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


def add_configs(
    opts,
    arguments,
    obj_configs=[
        "accelerator_options",
        "picture_description_options",
        "ocr_options",
        "layout_options",
        "asr_options",
    ],
):

    for arg in arguments:
        if arg in obj_configs:
            try:
                if args := arguments.get("accelerator_options"):
                    opts.accelerator_options = add_configs(
                        opts.accelerator_options, args, obj_configs=[]
                    )
                if args := arguments.get("picture_description_options"):
                    opts.picture_description_options = add_configs(
                        opts.picture_description_options, args, obj_configs=[]
                    )
                if args := arguments.get("ocr_options"):
                    opts.ocr_options = add_configs(
                        opts.ocr_options, args, obj_configs=[]
                    )
                if args := arguments.get("layout_options"):
                    opts.layout_options = add_configs(
                        opts.acceleratolayout_optionsr_options, args, obj_configs=[]
                    )
                if args := arguments.get("asr_options"):
                    opts.asr_options = add_configs(
                        opts.asr_options, args, obj_configs=[]
                    )
                # logging.info("has", arg)
            except:
                pass
                # logging.info("non c'è", arg)

        else:
            if hasattr(opts, arg):
                # logging.info("has", arg)
                setattr(opts, arg, arguments[arg])
            else:
                pass
                # logging.info("NOT PRESENT", arg)

    return opts


def get_format_options(configs, format):

    logging.info(f"Format: {format}")
    arguments = unflatten_dict(configs)
    opts_cls = FORMAT_TO_OPTIONS_CLS.get(format)
    opts = opts_cls()
    # logging.info(opts)
    pipeline_options = add_configs(opts.pipeline_options, arguments)
    opts.pipeline_options = pipeline_options
    in_form = InputFormat(format)
    format_options = {
        in_form: opts,
    }
    # logging.info(format_options)
    return format_options
