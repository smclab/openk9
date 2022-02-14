import React from "react";
import { css } from "styled-components/macro";
import { AnalysisResponseEntry, AnalysisToken } from "@openk9/rest-api";
import { myTheme } from "./myTheme";
import { TokenIcon } from "./TokenIcon";

type TokenSelectProps = {
  span: AnalysisResponseEntry;
  onSelect(token: AnalysisToken | null): void;
  selected: AnalysisToken | null;
  isOpen: boolean;
  optionIndex: number | null;
  onOptionIndexChange(optionIndex: number): void;
  isAutoSlected: boolean;
};
export function TokenSelect({
  span,
  onSelect,
  selected,
  isOpen,
  optionIndex,
  onOptionIndexChange,
  isAutoSlected,
}: TokenSelectProps) {
  const isInteractive = span.tokens.length > 0;
  const status: Status = isInteractive
    ? selected !== null
      ? isAutoSlected
        ? "auto-selected"
        : "has-selected"
      : "can-select"
    : "not-interactive";
  const entryStyle = (isSelected: boolean, isHighlighted: boolean) => css`
    padding: 8px 16px;
    :hover {
    }
    background-color: ${isHighlighted
      ? myTheme.backgroundColor2
      : myTheme.backgroundColor1};
    cursor: pointer;
    border-left: ${isSelected
      ? `8px solid var(--openk9-embeddable-search--active-color)`
      : "none"};
    padding-left: ${isSelected ? "8px" : "16px"};
  `;
  return (
    <div
      css={css`
        position: relative;
      `}
    >
      <div
        css={css`
          white-space: pre;
          ${statusStyles[status]};
        `}
      >
        {span.text}
      </div>
      {isOpen && isInteractive && (
        <div
          css={css`
            position: absolute;
            top: 100%;
            left: 0px;
            width: 600px;
            background-color: ${myTheme.backgroundColor1};
            border: 1px solid var(--openk9-embeddable-search--border-color);
            border-radius: 4px;
            z-index: 1;
          `}
        >
          <div
            onClick={() => {
              onSelect(null);
            }}
            onMouseEnter={() => {
              onOptionIndexChange(0);
            }}
            css={css`
              ${entryStyle(selected === null, optionIndex === 0)};
            `}
          >
            Deseleziona
          </div>
          {span.tokens.map((option, index) => {
            const isSelected =
              option.tokenType === selected?.tokenType &&
              option.value === selected.value;
            const isHighlighted = optionIndex === index + 1;
            return (
              <div
                key={index}
                onClick={() => {
                  onSelect(option);
                }}
                onMouseEnter={() => {
                  onOptionIndexChange(index + 1);
                }}
                css={css`
                  ${entryStyle(isSelected, isHighlighted)};
                  display: flex;
                `}
              >
                {"keywordKey" in option && (
                  <strong
                    css={css`
                      margin-right: 8px;
                    `}
                  >
                    {option.keywordName}:
                  </strong>
                )}
                <TokenIcon token={option} />
                {getTokenLabel(option)}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
}
type Status =
  | "can-select"
  | "has-selected"
  | "auto-selected"
  | "not-interactive";
const statusStyles: Record<Status, any> = {
  "can-select": css`
    color: deeppink;
  `,
  "auto-selected": css`
    color: lightseagreen;
  `,
  "has-selected": css`
    color: dodgerblue;
  `,
  "not-interactive": css`
    color: black;
  `,
};
function getTokenLabel(token: AnalysisToken) {
  switch (token.tokenType) {
    case "DATASOURCE":
      return token.value;
    case "DOCTYPE":
      return token.value;
    case "ENTITY":
      return token.entityName;
    case "TEXT":
      return token.value;
  }
}
