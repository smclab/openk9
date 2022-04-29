import React from "react";
import { css } from "styled-components/macro";

export function HighlightedText({
  text,
  Highlight = HighLight,
}: {
  text: string;
  Highlight?: React.FC<{ children: React.ReactNode }>;
}) {
  return (
    <>
      {Array.from(
        new DOMParser().parseFromString(text, "text/html").body.childNodes,
      ).map((child, index) => {
        if (child instanceof Text) return child.textContent;
        if (child instanceof Element && child.tagName === "EM")
          return <Highlight key={index}>{child.textContent}</Highlight>;
        return null;
      })}
    </>
  );
}

function HighLight({ children }: { children?: React.ReactNode }) {
  return (
    <span
      css={css`
        color: var(--openk9-embeddable-search--primary-color);
      `}
    >
      {children}
    </span>
  );
}
