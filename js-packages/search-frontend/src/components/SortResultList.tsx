import { css } from "styled-components/macro";
import { UseQueryResult } from "react-query";
import React from "react";
import { SortField } from "./client";

export function SortResultList({
  setSortResult,
  options,
}: {
  setSortResult: (sortResultNew: SortField) => void;
  options: UseQueryResult<
    {
      field: string;
      id: number;
      label: string;
    }[],
    unknown
  >;
}) {
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
            <React.Fragment>
              <option
                key={option.id + "asc"}
                value={JSON.stringify({
                  label: option.field,
                  sort: "asc",
                })}
              >
                {option.label} asc
              </option>
              <option
                key={option.id + "desc"}
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
