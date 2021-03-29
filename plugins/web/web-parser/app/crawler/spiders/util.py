import requests
from urllib.parse import urlparse
from bs4 import BeautifulSoup


def str_to_bool(s):
    if s.capitalize() == 'True':
        return True
    elif s.capitalize() == 'False':
        return False
    else:
        raise ValueError("full parameter must be True or False")


def get_favicon(domain):

        if 'http' not in domain:
            domain = 'http://' + domain
        page = requests.get(domain)
        soup = BeautifulSoup(page.text, features="lxml")
        icon_link = soup.find("link", rel="Shortcut Icon")
        if icon_link is None:
            icon_link = soup.find("link", rel="shortcut icon")
        if icon_link is None:
            icon_link = soup.find("link", rel="icon")
        if icon_link is None:
            base_url = urlparse(domain)
            return get_favicon(base_url.netloc)
        if not icon_link["href"].startswith("http"):
            base_url = urlparse(domain)
            href = 'https://' + base_url.netloc + icon_link["href"]
        else:
            href = icon_link["href"]
        return href


def map_type(content_type, type_mapping):
    try:
        document_type = type_mapping[content_type]
        return document_type
    except KeyError:
        return None


def get_path(url):
    base_url = urlparse(url)
    return base_url.path

