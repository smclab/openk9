import React from "react";
import { css } from "styled-components/macro";

type ResultFaviconProps = { src: string };
export function ResultFavicon({ src }: ResultFaviconProps) {
  return (
    <img
      src={src}
      alt=""
      css={css`
        max-height: 30px;
        max-width: 30px;
      `}
    />
  );
}
