/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import React from "react";
import { createUseStyles } from "react-jss";
import { ThemeType } from "@openk9/search-ui-components";

import { EmailResultItem } from "./types";

const useStyles = createUseStyles((theme: ThemeType) => ({
  body: {},
}));

export function EmailSidebar({ result }: { result: EmailResultItem }) {
  const classes = useStyles();
  return (
    <>
      <h3>{result.source.email.subject}</h3>
      <div>
        <strong>Date:</strong>{" "}
        {new Date(result.source.email.date).toLocaleString()}
      </div>
      <div>
        <strong>From:</strong> {result.source.email.from}
      </div>
      <div>
        <strong>To:</strong> {result.source.email.to}
      </div>
      {result.source.email.cc && result.source.email.cc.length > 0 && (
        <div>
          <strong>CC:</strong> {result.source.email.cc}
        </div>
      )}

      <iframe
        frameBorder="0"
        width="100%"
        height="100%"
        srcDoc={result.source.email.htmlBody}
        className={classes.body}
      />
    </>
  );
}
