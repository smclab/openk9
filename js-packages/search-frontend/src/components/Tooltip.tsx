import React from "react";
import { css } from "styled-components/macro";
import { myTheme } from "./myTheme";

type TooltipProps = {
  children: React.ReactNode;
  description: string;
};
export function Tooltip({ children, description }: TooltipProps) {
  const [isOpen, setIsOpen] = React.useState(false);
  return (
    <div
      css={css`
        position: relative;
      `}
      onMouseEnter={() => setIsOpen(true)}
      onMouseLeave={() => setIsOpen(false)}
    >
      {children}
      {isOpen && (
        <div
          css={css`
            position: absolute;
            right: 0px;
            padding: 8px 16px;
            background-color: ${myTheme.backgroundColor2};
            width: 200px;
            border: 1px solid var(--openk9-embeddable-search--border-color);
            border-radius: 4px;
          `}
        >
          {description}
        </div>
      )}
    </div>
  );
}
