import { css } from "styled-components/macro";
import { UseQueryResult } from "react-query";
import React from "react";
import { useQuery } from "react-query";
import { SortField, useOpenK9Client } from "../components/client";
import { useTranslation } from "react-i18next";

export function SortResultList({
  setSortResult,
  background = "white",
  minHeight = "40px",
  color = "#7e7e7e",
  relevance = "relevance",
}: {
  setSortResult: (sortResultNew: SortField) => void;
  background?: string;
  minHeight?: string;
  color?: string;
  relevance?: string;
}) {
  const client = useOpenK9Client();
  const options = useQuery(["date-label-sort-options", {}], async () => {
    return await client.getLabelSort();
  });
  const { t } = useTranslation();
  return (
    <span className="openk9-container-sort-result-list-component">
      <select
        className="form-control openk9-sort-result-select"
        id="regularSelectElement"
        css={css`
          border-radius: 34px;
          border: 1px solid #a292926b;
          height: 30px;
          width: 100%;
          padding-inline: 10px;
          cursor: pointer;
          :focus {
            border: 1px solid #a292926b;
            outline: none;
          }
          background: ${background};
          min-height: ${minHeight};
          color: ${color};
        `}
        onChange={(event) => {
          if (JSON.parse(event.currentTarget.value)?.label === "relevance") {
            setSortResult({});
          } else {
            setSortResult({
              [JSON.parse(event.currentTarget.value)?.label]: {
                sort: JSON.parse(event.currentTarget.value)?.sort,
                missing: "_last",
              },
            });
          }
        }}
      >
        <option
          value={JSON.stringify({
            label: "relevance",
          })}
        >
          {relevance === "relevance" ? t("relevance") : relevance}
        </option>
        {options.data?.map((option, index: number) => {
          return (
            <React.Fragment key={"fragment " + index}>
              <option
                key={option.id + "asc"}
                value={JSON.stringify({
                  label: option.field,
                  sort: "asc",
                })}
              >
                {option.label} {t("asc")}
              </option>
              <option
                key={option.id + "desc"}
                value={JSON.stringify({
                  label: option.field,
                  sort: "desc",
                })}
              >
                {option.label} {t("desc")}
              </option>
            </React.Fragment>
          );
        })}
      </select>
    </span>
  );
}
