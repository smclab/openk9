import React from "react";
import { css } from "styled-components/macro";

type DetailTitleProps = {
  children: React.ReactNode;
  fontSize?: string;
  fontweigth?: string;
  marginBottom?: string;
};
export function DetailTitle({
  children,
  fontSize = "1.5em",
  fontweigth = "500",
  marginBottom = "8px",
}: DetailTitleProps) {
  return (
    <div
      className="openk9-embeddable-detail-title"
      css={css`
        font-size: ${fontSize};
        font-weight: ${fontweigth};
        margin-bottom: ${marginBottom};
      `}
    >
      {children}
    </div>
  );
}
