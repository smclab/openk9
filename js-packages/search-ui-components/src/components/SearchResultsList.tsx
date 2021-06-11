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

import React, { useLayoutEffect } from "react";

import {
  GenericResultItem,
  ResultRendererProps,
  SearchResult,
} from "@openk9/http-api";
import { arrOrEncapsulate, ResultRenderersType } from "../utils";

function ResultDisplay<E>({
  data,
  renderers,
  onSelect,
  otherProps,
}: {
  data: GenericResultItem<E>;
  renderers: ResultRenderersType<E>;
  onSelect(): void;
  otherProps: Omit<ResultRendererProps<E>, "data" | "onSelect">;
}): JSX.Element | null {
  const Renderer = arrOrEncapsulate(data.source.type as any)
    .map((k) => renderers[k])
    .filter(Boolean)[0];
  if (Renderer) {
    return <Renderer data={data} onSelect={onSelect} {...otherProps} />;
  } else {
    console.warn("No renderer for", data.source.type);
    return null;
  }
}

interface Props<E> {
  renderers: ResultRenderersType<E>;
  searchResults: SearchResult<E>["result"];
  keyboardFocusEnabled?: boolean;
  onSelectResult(id: string | null): void;
  otherProps: Omit<ResultRendererProps<E>, "data" | "onSelect">;
}

export function SearchResultsList<E>({
  renderers,
  searchResults,
  keyboardFocusEnabled,
  onSelectResult,
  otherProps,
  ...rest
}: Props<E> & React.HTMLAttributes<HTMLDivElement>) {
  useLayoutEffect(() => {
    function onKeyDown(e: KeyboardEvent) {
      const focused = document.activeElement;

      if (keyboardFocusEnabled && e.key === "ArrowDown") {
        e.preventDefault();
        if (focused && !focused.classList.contains("resultLink")) {
          const first = document.querySelector(".resultLink");
          if (first) (first as any).focus();
        } else if (focused) {
          const next = focused.nextSibling;
          if (next) (next as any).focus();
        }
      } else if (keyboardFocusEnabled && e.key === "ArrowUp") {
        e.preventDefault();
        if (focused) {
          const inputField = document.querySelector(".firstFocusInput");
          const prevResult =
            focused.previousSibling &&
            (focused.previousSibling as any).classList.contains("resultLink") &&
            focused.previousSibling;
          const prev = prevResult || inputField;
          if (prev) (prev as any).focus();
          if (prev === inputField && document.scrollingElement)
            document.scrollingElement.scrollTop = 0;
        }
      }
    }

    document.addEventListener("keydown", onKeyDown);
    return () => document.removeEventListener("keydown", onKeyDown);
  }, [keyboardFocusEnabled]);

  return (
    <div {...rest}>
      {searchResults.map((result, i) => (
        <ResultDisplay
          data={result}
          renderers={renderers}
          key={result.source.id + "-" + i}
          onSelect={() => onSelectResult(result.source.id)}
          otherProps={otherProps}
        />
      ))}
    </div>
  );
}
