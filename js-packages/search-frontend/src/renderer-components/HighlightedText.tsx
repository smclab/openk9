import React from "react";
import { css } from "styled-components/macro";
import { myTheme } from "../utils/myTheme";

export function HighlightedText({
  text,
  Highlight = HighLight,
}: {
  text: string;
  Highlight?: React.FC<{}>;
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
        color: ${myTheme.redTextColor};
      `}
    >
      {children}
    </span>
  );
}
