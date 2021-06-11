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

import { GenericResultItem, SidebarRendererProps } from "@openk9/http-api";
import { ThemeType } from "../theme";
import { arrOrEncapsulate, SidebarRenderersType } from "../utils";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    position: "sticky",
    top: 48,
    minWidth: 300,
    width: "100%",
    maxWidth: 500,
    height: "calc(100vh - 48px)",
    overflow: "auto",
    padding: theme.spacingUnit * 2,
    backgroundColor: theme.digitalLakeBackgroundL1,
    flexShrink: 0,
  },
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

function SidebarContentDispatch<E>({
  result,
  renderers,
  otherProps,
}: {
  result: GenericResultItem<E>;
  renderers: SidebarRenderersType<E>;
  otherProps: Omit<SidebarRendererProps<E>, "result">;
}): JSX.Element | null {
  const Renderer = arrOrEncapsulate(result.source.type as any)
    .map((k) => renderers[k])
    .filter(Boolean)[0];
  if (Renderer) {
    return <Renderer result={result} {...otherProps} />;
  } else {
    console.warn("No sidebar renderer for", result.source.type);
    return null;
  }
}

export function ResultSidebar<E>({
  result,
  renderers,
  otherProps,
}: {
  result: GenericResultItem<E> | null;
  renderers: SidebarRenderersType<E>;
  otherProps: Omit<SidebarRendererProps<E>, "result">;
}) {
  const classes = useStyles();

  return (
    <div className={classes.root}>
      {result && (
        <SidebarContentDispatch
          result={result}
          renderers={renderers}
          otherProps={otherProps}
        />
      )}
    </div>
  );
}
