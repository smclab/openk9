import React from "react";
import { css } from "styled-components";
import { truncatedLineStyle } from "./truncatedLineStyle";

type MoreDetailCars = { icon: React.ReactNode; date?: string };
export function MoreDetailCard({ icon, date }: MoreDetailCars) {
  return (
    <div>
      <div
        className="openk9-embeddable-more-detail-card-icon"
        css={css`
          margin-top: var(--openk9-embeddable-search--spacing-md, 12px);
          color: var(
            --openk9-embeddable-search--secondary-active-color,
            #c0272b
          );
          display: flex;
          align-items: center;
          justify-content: flex-start;
        `}
      >
        {icon}
        <span
          className="openk9-embeddable-more-detail-card-container-title"
          css={css`
            color: #71717a;
            font-style: normal;
            font-weight: var(
              --openk9-embeddable-search--font-weight-regular,
              400
            );
            font-size: var(--openk9-embeddable-search--font-size-xs, 12px);
            line-height: 14px;
            margin-left: var(--openk9-embeddable-search--spacing-xs, 4px);
          `}
        >
          PDF
        </span>
        <span
          className="openk9-embeddable-more-detail-card-container-last-edit"
          css={css`
            color: #71717a;
            font-style: normal;
            font-weight: var(
              --openk9-embeddable-search--font-weight-regular,
              400
            );
            font-size: var(--openk9-embeddable-search--font-size-sm, 14px);
            line-height: 19px;
            margin-left: var(--openk9-embeddable-search--spacing-xl, 20px);
          `}
        >
          Last Edit:
          <span
            className="openk9-embeddable-more-detail-card-container-date"
            css={css`
              font-style: normal;
              font-weight: var(
                --openk9-embeddable-search--font-weight-semibold,
                600
              );
              font-size: var(--openk9-embeddable-search--font-size-sm, 14px);
              line-height: 19px;
              margin-left: var(--openk9-embeddable-search--spacing-sm, 8px);
              color: #3f3f46;
            `}
          >
            {date}
          </span>
        </span>
      </div>
    </div>
  );
}
