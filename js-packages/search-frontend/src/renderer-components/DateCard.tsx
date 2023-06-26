import { css } from "@emotion/react";
import {
  GenericResultItem,
  GenericResultItemFields,
} from "../components/client";
import { get } from "lodash";

type DateCard<E> = {
  result: GenericResultItem<E>;
  label?: string;
  path: string;
};

export function DateCard<E>({
  result,
  label = "Last Edit:",
  path,
}: DateCard<E>) {
  const printDate = get(result.source, path);
  const lastEdit = new Date(printDate).toLocaleString().replace(",", "");
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
