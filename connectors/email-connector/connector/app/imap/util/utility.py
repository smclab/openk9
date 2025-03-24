import requests
import logging
import email
import email.header
import email.utils
import base64

from bs4 import BeautifulSoup
from datetime import datetime
from logging.config import dictConfig
from ..util.log_config import LogConfig

dictConfig(LogConfig().dict())

logger = logging.getLogger("email-logger")


def post_message(url, payload, timeout=20):

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
    data = base64.b64encode(response).decode('utf-8')
    return data


def parse_email(fetched_msg):
    msg = email.message_from_bytes(fetched_msg[0][1])
    msg_subject, encoding = email.header.decode_header(msg['Subject'])[0]
    if isinstance(msg_subject, bytes):
        try:
            msg_subject = msg_subject.decode('utf-8')
        except UnicodeDecodeError:
            msg_subject = msg_subject.decode('latin1')

    msg_from = msg['From']
    msg_to = msg['To']
    msg_cc = msg['Cc']
    msg_id = str(msg.get('Message-ID')).strip()

    msg_date = email.utils.parsedate_tz(msg['Date'])
    local_date = email.utils.mktime_tz(msg_date)

    binaries = []
    attachments = []
    raw_body = ""
    try:
        if msg.is_multipart():
            for j, part in enumerate(msg.walk()):
                content_type = part.get_content_type()
                if content_type == 'text/html' or content_type == "text/plain":
                    charset = part.get_content_charset()
                    # decode the base64 unicode bytestring into plain text
                    raw_body = part.get_payload(decode=True).decode(encoding=charset, errors="ignore")
                    # if we've found the plain/text part, stop looping thru the parts
                elif content_type in ["image/png", "image/jpg", "image/jpeg", "application/pdf", "application/msword"]:
                    data = get_as_base64(part.get_payload(decode=True))
                    binary = {
                        "id": str(msg_id) + "_" + str(j),
                        "name": str(msg_id) + "_" + str(j),
                        "contentType": content_type,
                        "data": data
                    }
                    binaries.append(binary)

        else:
            # not multipart - i.e. plain text, no attachments
            charset = msg.get_content_charset()
            raw_body = msg.get_payload(decode=True).decode(encoding=charset, errors="ignore")
    except TypeError:
        raw_body = ""

    raw_body = raw_body.replace('"', '')

    body = BeautifulSoup(raw_body, features="html.parser").get_text()
    body = body.replace('\n', ' ').replace('\r', '').replace('\t', '').strip()

    msg_date = datetime.fromtimestamp(local_date).strftime("%a, %d %b %Y %H:%M:%S")

    raw_msg = ""
    if msg_subject is not None:
        raw_msg = raw_msg + "Subject: " + msg_subject

    msg_from_email = email.utils.parseaddr(msg_from)[1]
    msg_from_user = email.utils.parseaddr(msg_from)[0]
    if len(msg_from_user) == 0:
        msg_from_user = None

    acl_list = [msg_from_email]

    raw_msg = raw_msg + " Date: " + msg_date + " From: " + msg_from

    msg_to_email = msg_to_user = msg_cc_email = msg_cc_user = None

    if msg_to is not None:

        msg_to_email = [email.utils.parseaddr(to)[1] for to in str(msg_to).split(",")]
        msg_to_user = [email.utils.parseaddr(to)[0] if len(to) > 0 else None for to in str(msg_to).split(",")]

        raw_msg = raw_msg + " To: " + msg_to

        acl_list = acl_list + msg_to_email

    if msg_cc is not None:

        msg_cc_email = [email.utils.parseaddr(cc)[1] for cc in str(msg_cc).split(",")]
        msg_cc_user = [email.utils.parseaddr(cc)[0] if len(cc) > 0 else None for cc in str(msg_cc).split(",")]

        raw_msg = raw_msg + " CC: " + msg_cc

        acl_list = acl_list + msg_cc_email

    raw_msg = raw_msg + " " + body

    struct_msg = {'date': (local_date * 1000),
                  'from_user': msg_from_user, 'from_email': msg_from_email,
                  'to_user': msg_to_user, 'to_email': msg_to_email,
                  'cc_user': msg_cc_user, 'cc_email': msg_cc_email,
                  'subject': msg_subject,
                  'to': msg_to, 'cc': msg_cc,
                  'body': body, "htmlBody": raw_body}

    return raw_msg, struct_msg, msg_id, binaries, acl_list
