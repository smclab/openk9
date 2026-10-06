from urllib.parse import quote


def object_url(base_url, bucket_name, object_name):
    """
    Public URL of an object: <base>/<bucket>/<object>, without signature.
    The object name keeps its slashes, every other reserved character is
    percent-encoded.
    """
    return "%s/%s/%s" % (
        base_url.rstrip("/"), quote(bucket_name, safe=""), quote(object_name))


def text_content(content_type, data):
    """
    Text of a textual object, to be sent as rawContent; an empty string for
    the other formats, whose text is extracted by the enrichers.
    """
    if not content_type or not content_type.split(";")[0].strip().lower().startswith("text/"):
        return ""

    try:
        return data.decode("utf-8")
    except UnicodeDecodeError:
        return ""
