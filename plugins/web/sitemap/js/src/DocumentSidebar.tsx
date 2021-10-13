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

import { DocumentResultItem } from "./types";

const useStyles = createUseStyles((theme: ThemeType) => ({
  break: {
    overflow: "hidden",
    maxWidth: "100%",
    textOverflow: "ellipsis",
    whiteSpace: "nowrap",
  },
  previews: {
    margin: [theme.spacingUnit * 2, 0],
  },
}));

export function DocumentSidebar({ result }: { result: DocumentResultItem }) {
  const classes = useStyles();

  return (
    <>
      <h3>{result.source.document.title}</h3>
      <div className={classes.break}>
        <strong>File Type:</strong> {result.source.document.contentType}
      </div>
      <div className={classes.break}>
        <strong>Document Type:</strong> {result.source.document.documentType}
      </div>
      <div className={classes.break}>
        <strong>Path:</strong> {result.source.file.path}
      </div>
      <div className={classes.break}>
        <strong>Last Edit:</strong>{" "}
        {new Date(result.source.file.lastModifiedDate).toLocaleString()}
      </div>
      <div className={classes.break}>
        <strong>URL:</strong>{" "}
        <a href={result.source.document.url} target="_blank">
          {result.source.document.url}
        </a>
      </div>
      <div>
        <strong>Content:</strong> {result.source.document.content}
      </div>
    </>
  );
}
