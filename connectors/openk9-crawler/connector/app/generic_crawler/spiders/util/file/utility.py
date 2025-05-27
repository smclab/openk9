import hashlib
import logging
from urllib.parse import urlparse
import requests
import mimetypes
from ..generic.utility import clean_extraction, get_as_base64, get_favicon, get_title


logger = logging.getLogger(__name__)


def extension_from_mimetype(mimetype, mimetype_map=None):
    """
    Returns the file extension for a given MIME type.

    Args:
        mimetype (str): The MIME type string (e.g., 'image/jpeg').
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
    }

    # Use custom map if provided
    lookup = mimetype_map if mimetype_map else default_map

    return lookup.get(mimetype.lower())


def get_path(url):
    base_url = urlparse(url)
    return base_url.path
