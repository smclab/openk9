import base64


class FormatError(Exception):
    pass


def handle_exception(e: Exception):
    if isinstance(e, (base64.binascii.Error, ValueError)):
        return f"base64 error: {e}"

    elif isinstance(e, AttributeError):
        return f"export error: {e}"

    elif isinstance(e, FormatError):
        return f"format error: {e}"

    else:
        return f"generic error: {e}"
