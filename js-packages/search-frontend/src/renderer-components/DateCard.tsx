import { css } from "@emotion/react";

type DateCard = { date: string; label?: string };

export function DateCard({ date, label = "Last Edit:" }: DateCard) {
  const lastEdit = new Date(date).toLocaleString().replace(",", "");
  return (
    <div
      css={css`
        display: flex;
        gap: 5px;
      `}
    >
      <div
        className="openk9-embeddable-more-detail-card-container-last-edit"
        css={css`
          color: #71717a;
          font-style: normal;
          font-weight: 400;
          font-size: 14px;
          line-height: 19px;
          margin-left: 20px;
        `}
      >
        {label}
        <div
          className="openk9-embeddable-more-detail-card-container-date"
          css={css`
            font-style: normal;
            font-weight: 600;
            font-size: 14px;
            line-height: 19px;
            margin-left: 7px;
            color: #3f3f46;
          `}
        >
          {lastEdit}
        </div>
      </div>
    </div>
  );
}
