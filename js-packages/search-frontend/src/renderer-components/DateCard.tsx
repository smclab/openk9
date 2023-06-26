import { css } from "styled-components/macro";
import {
  GenericResultItem,
  GenericResultItemFields,
} from "../components/client";
import { get } from "lodash";
import React from "react";

type DateCardProps<E> = {
  result: GenericResultItem<E>;
  label?: string;
  path: string;
};

export function DateCard<E>({
  result,
  label = "Last Edit:",
  path,
}: DateCardProps<E>) {
  const printDate = get(result.source, path);
  const lastEdit = new Date(printDate).toLocaleString().replace(",", "");
  return (
    <div
      className="openk9-embeddable-more-detail-card-container"
      style={{ display: "flex", flexDirection: "row", gap: "5px" }}
    >
      <div
        className="openk9-embeddable-more-detail-card-container-last-edit"
        css={css`
          color: #71717a;
          font-style: normal;
          font-weight: 600;
          font-size: 15px;
          line-height: 19px;
        `}
      >
        {label}:
      </div>
      <div
        className="openk9-embeddable-more-detail-card-container-date"
        css={css`
          font-style: normal;
          font-weight: 600;
          font-size: 14px;
          line-height: 19px;
          color: #3f3f46;
        `}
      >
        {lastEdit}
      </div>
    </div>
  );
}
