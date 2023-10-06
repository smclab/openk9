import React from "react";
import { css } from "styled-components/macro";

export function ArrowDownSvg({
  size = "26px",
  fill = "#c0272b",
}: {
  size?: string;
  fill?: string;
}) {
  return (
    <svg
      width={size}
      height={size}
      aria-hidden="true"
      viewBox="0 0 25 24"
      fill="none"
    >
      <path
        d="M20.5 12L19.09 10.59L13.5 16.17V4L11.5 4V16.17L5.91 10.59L4.5 12L12.5 20L20.5 12Z"
        className="openk9-logo-arrow-down-color"
        css={css`
          fill: #c0272b;
        `}
      />
    </svg>
  );
}
