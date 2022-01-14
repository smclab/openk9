import React from "react";
import { css } from "styled-components/macro";

type ResultIconProps = {
  children: React.ReactNode;
};
export function ResultIcon({ children }: ResultIconProps) {
  return (
    <div
      css={css`
        width: 30px;
        height: 30px;
        display: flex;
        align-items: center;
        justify-content: center;
      `}
    >
      {children}
    </div>
  );
}

type ResultFaviconProps = { src: string };
export function ResultFavicon({ src }: ResultFaviconProps) {
  return (
    <ResultIcon>
      <img
        src={src}
        alt=""
        css={css`
          max-height: 30px;
          max-width: 30px;
        `}
      />
    </ResultIcon>
  );
}
