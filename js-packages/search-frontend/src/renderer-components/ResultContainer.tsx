import React from "react";
import { css } from "styled-components/macro";

type ResultContainerProps = {
  icon?: React.ReactNode;
  children: React.ReactNode;
};
export function ResultContainer({ icon, children }: ResultContainerProps) {
  return (
    <div
      className="openk9-embeddable-result-container"
      css={css`
        display: flex;
        padding: 8px 16px;
      `}
    >
      <div
        className="openk9-embeddable-result-container-media"
        css={css`
          @media max-width: 480px {
            display: none;
          }
          width: 30px;
          margin-right: 8px;
        `}
      >
        <div
          className="openk9-embeddable-result--icon"
          css={css`
            width: 30px;
            height: 30px;
            display: flex;
            align-items: center;
            justify-content: center;
            color: var(--openk9-embeddable-search--primary-color);
          `}
        >
          {icon}
        </div>
      </div>
      <div
        className="openk9-embeddable-result--children"
        css={css`
          margin-left: 8px;
          overflow: hidden;
        `}
      >
        {children}
      </div>
    </div>
  );
}
