import React from "react";
import { css } from "@emotion/react";

interface WrapperContainerProps {
  children: React.ReactNode;
  gap?: string;
}

export function WrapperContainer({
  children,
  gap = "5px",
}: WrapperContainerProps) {
  return (
    <div
      className="openk9-filters-horizontal-container"
      css={css`
        display: flex;
        flex-direction: column;
        gap: ${gap};
      `}
    >
      {children}
    </div>
  );
}
