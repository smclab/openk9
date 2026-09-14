from typing import Any, Dict, FrozenSet

from docling.datamodel.base_models import InputFormat
from docling.datamodel.pipeline_options import (
    EasyOcrOptions,
    PdfPipelineOptions,
    PictureDescriptionApiOptions,
    PictureDescriptionVlmOptions,
)
from docling.document_converter import FormatOption, _get_default_option
from pydantic import ValidationError

from app.utils.logger import logger

# =========================
# SUPPORTED FORMATS
# =========================

# docling knows more formats than this image can convert, and they would only
# fail deep inside the backend. This set is the gate; the FormatOption itself
# comes from docling. Left out are the formats whose backend needs an install
# extra we do not ship (odfdo for OpenDocument, arelle-release for XBRL,
# whisper/librosa for audio and video) plus XML_USPTO and METS_GBS, which
# docling can only convert from a file: their detection and backend read the
# stream without rewinding it, and this enricher only ever has a stream.
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
        InputFormat.XML_JATS,
        InputFormat.XML_DOCLANG,
        InputFormat.DCLX,
        InputFormat.IMAGE,
        InputFormat.PDF,
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
    """Flatten a nested dict."""
    items = {}
    for k, v in data.items():
        new_key = f"{parent_key}{sep}{k}" if parent_key else k
        if isinstance(v, dict):
            items.update(flatten(v, new_key, sep))
        else:
            items[new_key] = v
    return items


def unflatten_dict(data: Dict[str, Any], sep: str = ".") -> Dict[str, Any]:
    """Rebuild a nested dict from flat keys."""
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

# Options whose class docling picks with a `kind` discriminator instead of
# leaving the one already in place. Updating the object docling defaults to is
# not enough for them: the default picture description is a local vision model
# and knows nothing of `url`, `headers` or `timeout`, so an enrich item asking
# for a remote endpoint needs the object built from scratch. `ocr_options`
# works the same way, but its engine is pinned in get_format_options.
OPTIONS_BY_KIND: Dict[str, Dict[str, type]] = {
    "picture_description_options": {
        "api": PictureDescriptionApiOptions,
        "vlm": PictureDescriptionVlmOptions,
    }
}


def kind_hint(key: str, kinds: Dict[str, type]) -> str:
    """
    Name the kinds that do expose the key, when some other one does.

    A key dropped because it belongs to another variant is the likeliest
    mistake here: the options in place are the default ones, and every remote
    endpoint setting lives on a class the configuration has to ask for.
    """
    if not kinds:
        return ""

    taken_by = sorted(
        kind for kind, options_cls in kinds.items() if key in options_cls.model_fields
    )
    if not taken_by:
        return ""

    return f"; {key!r} belongs to kind {' or '.join(repr(k) for k in taken_by)}"


def build_options(key: str, kinds: Dict[str, type], arguments: Dict[str, Any]) -> Any:
    """
    Build the option class the configuration selects with its `kind`.

    Returns None when the configuration cannot produce an object, so that the
    caller keeps the options docling defaults to.
    """
    kind = arguments.get("kind")
    options_cls = kinds.get(kind)

    if options_cls is None:
        logger.warning(
            f"Skipping config key '{key}': unknown kind {kind!r}, "
            f"expected one of {sorted(kinds)}"
        )
        return None

    # `kind` is a ClassVar on the docling options, not a field.
    fields = {k: v for k, v in arguments.items() if k != "kind"}

    # The docling options do not forbid extra fields, so a key that is not
    # theirs would be dropped on construction as silently as before.
    for unknown in [k for k in fields if k not in options_cls.model_fields]:
        logger.warning(
            f"Skipping config key '{key}.{unknown}': "
            f"{options_cls.__name__} has no such option"
            f"{kind_hint(unknown, kinds)}"
        )
        del fields[unknown]

    try:
        return options_cls(**fields)
    except ValidationError as e:
        logger.warning(
            f"Skipping config key '{key}': "
            f"{options_cls.__name__} rejected the configuration: {e}"
        )
        return None


def add_configs(
    opts: Any, arguments: Dict[str, Any], kinds: Dict[str, type] = None
) -> Any:
    """
    Apply configurations to an object, recursively.

    `kinds` are the classes the configuration could have selected for `opts`,
    used to tell which one takes a key these options do not have.
    """
    if opts is None:
        return None

    for key, value in arguments.items():
        if not hasattr(opts, key):
            logger.warning(
                f"Skipping config key '{key}': "
                f"{type(opts).__name__} has no such option"
                f"{kind_hint(key, kinds)}"
            )
            continue

        # Class selected by the configuration → build it
        nested_kinds = OPTIONS_BY_KIND.get(key)
        if isinstance(value, dict) and nested_kinds is not None and "kind" in value:
            built = build_options(key, nested_kinds, value)
            if built is not None:
                setattr(opts, key, built)
            continue

        current_attr = getattr(opts, key)

        # Nested dict → recurse
        if isinstance(value, dict):
            if current_attr is None:
                logger.warning(
                    f"Skipping config key '{key}': {type(opts).__name__} leaves "
                    f"it unset, there is nothing to apply the configuration to"
                )
                continue
            updated = add_configs(current_attr, value, nested_kinds)
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
    Build a configured FormatOption from flat configs.
    """

    # Format validation
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

    # No configs → return right away
    if not configs:
        return {in_format: opts}

    logger.debug(f"Raw configs: {configs}")

    # 1. Unflatten
    arguments = unflatten_dict(configs)

    # 2. Normalize (essential)
    arguments = normalize_dict(arguments)

    # 3. Extract the right sections
    pipeline_args = arguments.get("pipeline_options", {})
    backend_args = arguments.get("backend_options", {})

    # 4. Apply the pipeline configs
    if opts.pipeline_options:
        opts.pipeline_options = add_configs(opts.pipeline_options, pipeline_args)

    # 5. (optional) backend config
    if hasattr(opts, "backend_options") and backend_args:
        opts.backend_options = add_configs(opts.backend_options, backend_args)

    return {in_format: opts}
