import logging
from email.errors import HeaderParseError
from email.header import decode_header, make_header
from typing import Any, Mapping, Optional
from urllib.parse import quote

logger = logging.getLogger("status-logger")

USER_METADATA_PREFIX = "x-amz-meta-"


def object_url(base_url, bucket_name, object_name):
    """
    Public URL of an object: <base>/<bucket>/<object>, without signature.
    The object name keeps its slashes, every other reserved character is
    percent-encoded.
    """
    return "%s/%s/%s" % (
        base_url.rstrip("/"), quote(bucket_name, safe=""), quote(object_name))


def user_metadata(
    headers: Mapping[str, str], object_name: str
) -> Optional[dict[str, Any]]:
    """
    Metadata of a binary from the user metadata of its object, or None if
    the object has none. The x-amz-meta- prefix is removed, the key is
    lowercased and its dots become underscores; RFC 2047 encoded values
    are decoded. page must be an integer and generated true or false: a
    value of the wrong type is dropped and logged.
    """
    metadata = {}

    for header, value in headers.items():
        key = header.lower()
        if not key.startswith(USER_METADATA_PREFIX):
            continue

        key = key[len(USER_METADATA_PREFIX):].replace(".", "_")
        value = _decode(value)

        if key == "page":
            try:
                value = int(value)
            except ValueError:
                logger.warning(
                    "dropped metadata page=%r of %s: not an integer",
                    value, object_name,
                )
                continue
        elif key == "generated":
            if value not in ("true", "false"):
                logger.warning(
                    "dropped metadata generated=%r of %s: not true or false",
                    value, object_name,
                )
                continue
            value = value == "true"

        metadata[key] = value

    return metadata or None


def _decode(value: str) -> str:
    # S3 clients send non-ASCII values as RFC 2047 encoded words; a value
    # that cannot be decoded is kept as it is
    try:
        return str(make_header(decode_header(value)))
    except (LookupError, UnicodeDecodeError, HeaderParseError):
        return value
