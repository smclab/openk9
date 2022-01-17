import React from "react";
import { css } from "styled-components/macro";

type DetailHeaderImageProps = {
  src: string;
};
export function DetailHeaderImage({ src }: DetailHeaderImageProps) {
  return (
    <img
      src={src}
      alt=""
      css={css`
        max-width: 100%;
        margin-bottom: 8px;
      `}
    />
  );
}
