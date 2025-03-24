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

import imaplib


class ImapClient:
    imap = None

    def __init__(self, server, port, recipient, password, use_ssl=True):
        # check for required param
        if not recipient:
            raise ValueError('You must provide a recipient email address')
        self.server = server
        self.port = port
        self.recipient = recipient
        self.password = password
        self.use_ssl = use_ssl
        self.recipient_folder = ''
        # instantiate our IMAP client object
        try:
            if self.use_ssl:
                self.imap = imaplib.IMAP4_SSL(self.server, self.port)
            else:
                self.imap = imaplib.IMAP4(self.server, self.port)
        except OSError as err:
            raise err

    def login(self):
        try:
            self.imap.login(self.recipient, self.password)
        except (imaplib.IMAP4_SSL.error, imaplib.IMAP4.error) as err:
            raise err
    
    def close(self):
        self.imap.close()

    def logout(self):
        self.imap.logout()

    def select_folder(self, folder=None):
        """
        Select the IMAP folder to read messages from. By default
        the class will read from the INBOX folder
        """
        self.recipient_folder = folder
        if self.recipient_folder is not None:
            resp, _ = self.imap.select(self.recipient_folder)
        else:
            resp, _ = self.imap.select()

        return resp

    def search(self, query):

        mbox_response, msgnums = self.imap.search(None, query)
        return mbox_response, msgnums

    def get_message(self, num):

        retval, rawmsg = self.imap.fetch(num, '(RFC822)')
        return retval, rawmsg
