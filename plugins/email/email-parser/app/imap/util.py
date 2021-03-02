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

import email
import email.header
import email.utils

from bs4 import BeautifulSoup
from datetime import datetime


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
    msg_id = msg.get('Message-ID')

    msg_date = email.utils.parsedate_tz(msg['Date'])
    local_date = email.utils.mktime_tz(msg_date)

    raw_body = ""
    try:
        if msg.is_multipart():
            for part in msg.walk():
                content_type = part.get_content_type()
                disp = str(part.get('Content-Disposition'))
                # look for plain text parts, but skip attachments
                if content_type == 'text/html' and 'attachment' not in disp:
                    charset = part.get_content_charset()
                    # decode the base64 unicode bytestring into plain text
                    raw_body = part.get_payload(decode=True).decode(encoding=charset, errors="ignore")
                    # if we've found the plain/text part, stop looping thru the parts
                    break
        else:
            # not multipart - i.e. plain text, no attachments
            charset = msg.get_content_charset()
            raw_body = msg.get_payload(decode=True).decode(encoding=charset, errors="ignore")
    except TypeError:
        raw_body = ""

    raw_body = raw_body.replace('"', '')

    body = BeautifulSoup(raw_body, features="html.parser").get_text()
    body = body.replace('\n', ' ').replace('\r', '').replace('\t', '').strip()

    struct_msg = {'date': (local_date*1000), 'from': msg_from, 'subject': msg_subject, 'to': msg_to, 'cc': msg_cc,
                  'body': body, "htmlBody": raw_body}
    
    msg_date = datetime.fromtimestamp(local_date).strftime("%a, %d %b %Y %H:%M:%S")

    raw_msg = ""
    if msg_subject is not None:
        raw_msg = raw_msg + "Subject: " + msg_subject
    raw_msg = raw_msg + " Date: " + msg_date + " From: " + msg_from
    if msg_to is not None:
        raw_msg = raw_msg + " To: " + msg_to
    if msg_cc is not None:
        raw_msg = raw_msg + " CC: " + msg_cc
    raw_msg = raw_msg + " " + body

    return raw_msg, struct_msg, msg_id
