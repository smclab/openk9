import hashlib
import logging
from urllib.parse import urlparse
import requests
import mimetypes
from ..generic.utility import clean_extraction, get_as_base64, get_favicon, get_title


logger = logging.getLogger(__name__)


def get_path(url):
    base_url = urlparse(url)
    return base_url.path
