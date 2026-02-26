from bs4 import BeautifulSoup
from urllib.parse import urlparse
import base64
import logging
import re
import requests
from scrapy import Item, Spider
import scrapy

logger = logging.getLogger(__name__)

# region DOM handling


def get_title(response, title_tag):
    title = response.css(title_tag).get()
    if title is not None:
        title = title.strip()
    else:
        title = None
    return title


def get_content(response, max_length=None, body_tag=None, excluded_body_tags=None):
    content = ''

    if body_tag is None:
        body = response.body
    else:
        body = response.css(body_tag).get()

    try:
        soupObj = BeautifulSoup(body, 'lxml')
        # Tags removing
        if excluded_body_tags is not None:
            for tag_name in excluded_body_tags:
                for tag in soupObj.select(tag_name):
                    tag.decompose()
        soup = soupObj.get_text(separator=' ')

    except TypeError:
        return content

    if body is not None:
        content = soup.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').strip()
        content = re.sub(' +', ' ', content)
        if max_length is not None:
            content = content[:max_length]

    return content


def extract_text(element, max_length=None):

    content = ''

    try:
        soup = BeautifulSoup(element, 'lxml').get_text(separator=u' ')
    except TypeError:
        return content

    if element is not None:
        content = soup.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').strip().lower()
        content = re.sub(' +', ' ', content)
        if max_length is not None:
            content = content[:max_length]

    return content


def get_favicon(domain):
    if 'http' not in domain:
        domain = 'http://' + domain
    page = requests.get(domain, verify=False)
    soup = BeautifulSoup(page.text, features="lxml")
    icon_link = soup.find("link", rel=["Shortcut Icon", "shortcut icon", "icon"])
    if icon_link is None:
        return None
    href = icon_link.get("href")
    if not href.startswith("http"):
        base_url = urlparse(domain)
        href = f"{base_url.scheme}://{base_url.netloc}{href}"
    return href

# endregion

# region Formatting


def get_as_base64(response):
    data = base64.b64encode(response).decode('utf-8')
    return data


def str_to_bool(s):
    if s.capitalize() == 'True':
        return True
    elif s.capitalize() == 'False':
        return False
    else:
        raise ValueError("full parameter must be True or False")


def clean_extraction(text: str, lowercase: bool = False) -> str | None:
    """ Given a string, it returns a string cleaned from \\n \\t, spaces etc..
    -   lowercase: True = lowercase the string"""
    try:
        if lowercase:
            return text.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').strip().lower()
        else:
            return text.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').strip()
    except:
        return None

# endregion

# region HTTP


def extract_domain(url):
    root_addr = ""
    try:
        parsed_url = urlparse(url)
        root_addr = "http://" + parsed_url.netloc
    except Exception as e:
        logger.info("extract_domain: %s", e)

    return root_addr


def post_message(url, payload, timeout=30):
    """ Pass the body as json instead of data"""
    try:
        r = requests.post(url, json=payload, timeout=timeout)
        if r.status_code == 200:
            return
        else:
            r.raise_for_status()
    except requests.RequestException as e:
        logger.error(str(e) + " during request at url: " + str(url))
        raise e


def generate_item(fields):
    item = Item()
    for field in fields:
        item.fields[field] = scrapy.Field()
    return item

# endregion
