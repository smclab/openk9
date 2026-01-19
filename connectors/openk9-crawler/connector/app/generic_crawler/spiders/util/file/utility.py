import hashlib
import logging
from urllib.parse import urlparse
import requests
import mimetypes
from ..generic.utility import clean_extraction, get_as_base64, get_favicon, get_title


logger = logging.getLogger(__name__)


def extension_from_mimetype(mimetype, do_use_default_mimetype_map, mimetype_map=None):
    """
    Returns the file extension for a given MIME type.

    Args:
        mimetype (str): The MIME type string (e.g., 'image/jpeg').
        do_use_default_mimetype_map (bool): Tells if ti should use `default_map`
        mimetype_map (dict, optional): A custom MIME to extension mapping.

    Returns:
        str or None: The corresponding file extension (e.g., 'jpg'), or None if not found.
    """
    # Default MIME type to extension map (partial, extend as needed)
    default_map = {
        'image/jpeg': 'jpg',
        'image/png': 'png',
        'image/gif': 'gif',
        'application/pdf': 'pdf',
        'text/plain': 'txt',
        'application/msword': 'doc',
        'application/vnd.openxmlformats-officedocument.wordprocessingml.document': 'docx',
        'application/vnd.ms-excel': 'xls',
        'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet': 'xlsx',
        'application/zip': 'zip',
        'application/json': 'json',
        'application/vnd.ms-powerpoint': 'ppt',
        'application/vnd.openxmlformats-officedocument.presentationml.presentation': 'pptx',
        'application/epub+zip': 'epub',
        'application/xml': 'xml',
        'application/pkcs7-mime': 'p7m',
        'audio/mpeg': 'mp3'
    }

    # sets lookup as default_map if do_use_default_mimetype_map
    lookup = default_map if do_use_default_mimetype_map else {}

    # Adds custom map if provided
    if mimetype_map:
        lookup.update(mimetype_map)

    return lookup.get(mimetype.lower())


def get_path(url):
    base_url = urlparse(url)
    return base_url.path
