"""
Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Affero General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.
"""

import requests
import logging
from urllib.parse import urlparse
from bs4 import BeautifulSoup
import base64
import re

logger = logging.getLogger(__name__)


def get_title(response, title_tag):
    title = response.css(title_tag).get()
    if title is not None:
        title = title.strip()
    else:
        title = "Unknown title"
    return title


def get_content(response, max_length, body_tag=None):

    if body_tag is None:
        body = response.body
    else:
        body = response.css(body_tag).get()

    try:
        soup = BeautifulSoup(body, 'lxml').get_text()
    except TypeError:
        return None

    if body is not None:
        content = soup.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ').strip()
        content = re.sub(' +', ' ', content)
        if max_length is not None:
            content = content[:max_length]
    else:
        content = None

    return content


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
        return None
    if not icon_link["href"].startswith("http"):
        base_url = urlparse(domain)
        href = 'http://' + base_url.netloc + icon_link["href"]
    else:
        href = icon_link["href"]
    return href


def post_message(url, payload, timeout=30):
    try:
        r = requests.post(url, json=payload, timeout=timeout)
        if r.status_code == 200:
            return
        else:
            r.raise_for_status()
    except requests.RequestException as e:
        logger.error(str(e) + " during request at url: " + str(url))
        raise e


def get_as_base64(response):
    data = base64.b64encode(response.content).decode('utf-8')
    return data


def str_to_bool(s):
    if s.capitalize() == 'True':
        return True
    elif s.capitalize() == 'False':
        return False
    else:
        raise ValueError("full parameter must be True or False")
