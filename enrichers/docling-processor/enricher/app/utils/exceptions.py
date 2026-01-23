import base64


class FormatError(Exception):
    pass


def handle_exception(e: Exception):
    if isinstance(e, (base64.binascii.Error, ValueError)):
        print(f"base64 error: {e}")

    elif isinstance(e, AttributeError):
        print(f"export error: {e}")

    elif isinstance(e, FormatError):
        print(f"format error: {e}")

    else:
        print(f"generic error: {e}")
