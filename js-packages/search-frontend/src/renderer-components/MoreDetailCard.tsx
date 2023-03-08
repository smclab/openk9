import React from "react";
import { css } from "styled-components/macro";
import { truncatedLineStyle } from "./truncatedLineStyle";

type MoreDetailCars = { icon: React.ReactNode; date?: string };
export function MoreDetailCard({ icon, date }: MoreDetailCars) {
  return (
    <div>
      <div
        css={css`
          margin-top: 11px;
          color: #c0272b;
          display: flex;
          align-items: center;
          justify-content: flex-start;
        `}
      >
        {icon}
        <span
          css={css`
            color: #71717a;
            font-style: normal;
            font-weight: 400;
            font-size: 10px;
            line-height: 14px;
            margin-left: 5px;
          `}
        >
          PDF
        </span>
        <span
          css={css`
            color: #71717a;
            font-style: normal;
            font-weight: 400;
            font-size: 14px;
            line-height: 19px;
            margin-left: 20px;
          `}
        >
          Last Edit:
          <span
            css={css`
              font-style: normal;
              font-weight: 600;
              font-size: 14px;
              line-height: 19px;
              margin-left: 7px;
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
