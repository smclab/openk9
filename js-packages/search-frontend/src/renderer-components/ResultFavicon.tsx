import React from "react";
import { css } from "styled-components/macro";

type ResultFaviconProps = { src: string; maxHei?: string; maxWid?: string };
export function ResultFavicon({
  src,
  maxHei = "30px",
  maxWid = "30px",
}: ResultFaviconProps) {
  return (
    <img
      className="openk9-result-favicon"
      src={src}
      alt=""
      css={css`
        max-height: ${maxHei};
        max-width: ${maxWid};
      `}
    />
  );
}
