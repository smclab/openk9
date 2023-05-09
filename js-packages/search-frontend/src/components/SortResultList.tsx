import { css } from "styled-components/macro";
import { UseQueryResult } from "react-query";
import React from "react";
import { SortField } from "./client";
import { useQuery } from "react-query";
import { useOpenK9Client } from "./client";

export function SortResultList({
  setSortResult,
}: {
  setSortResult: (sortResultNew: SortField) => void;
}) {
  const client = useOpenK9Client();
  const options = useQuery(["date-label-sort-options", {}], async () => {
    return await client.getLabelSort();
  });
  return (
    <span className="openk9-container-sort-result-list-component">
      <select
        className="form-control openk9-sort-result-select"
        id="regularSelectElement"
        css={css`
          border-radius: 34px;
          border: 1px solid #a292926b;
          height: 30px;
          cursor: pointer;
          :focus {
            border: 1px solid #a292926b;
            outline: none;
          }
          background: transparent;
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
          relevance
        </option>
        {options.data?.map((option) => {
          return (
            <React.Fragment key={option.id}>
              <option
                value={JSON.stringify({
                  label: option.field,
                  sort: "asc",
                })}
              >
                {option.label} asc
              </option>
              <option
                value={JSON.stringify({
                  label: option.field,
                  sort: "desc",
                })}
              >
                {option.label} desc
              </option>
            </React.Fragment>
          );
        })}
      </select>
    </span>
  );
}
