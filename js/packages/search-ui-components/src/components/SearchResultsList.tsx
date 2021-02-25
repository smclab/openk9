import React, { useLayoutEffect } from "react";

import {
  GenericResultItem,
  ResultRenderersType,
  SearchResult,
} from "@openk9/http-api";
import { arrOrEncapsulate } from "../utils";

function ResultDisplay<E>({
  data,
  renderers,
  onSelect,
}: {
  data: GenericResultItem<E>;
  renderers: ResultRenderersType<E>;
  onSelect(): void;
}): JSX.Element | null {
  const Renderer = arrOrEncapsulate(data.source.type as any)
    .map((k) => renderers[k])
    .filter(Boolean)[0];
  if (Renderer) {
    return <Renderer data={data} onSelect={onSelect} />;
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
}

export function SearchResultsList<E>({
  renderers,
  searchResults,
  keyboardFocusEnabled,
  onSelectResult,
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
        />
      ))}
    </div>
  );
}
