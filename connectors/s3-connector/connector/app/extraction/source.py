from urllib.parse import quote


def object_url(base_url, bucket_name, object_name):
    """
    Public URL of an object: <base>/<bucket>/<object>, without signature.
    The object name keeps its slashes, every other reserved character is
    percent-encoded.
    """
    return "%s/%s/%s" % (
        base_url.rstrip("/"), quote(bucket_name, safe=""), quote(object_name))

