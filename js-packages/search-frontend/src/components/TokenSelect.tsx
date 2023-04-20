import React from "react";
import { css } from "styled-components/macro";
import { AnalysisResponseEntry, AnalysisToken } from "./client";
import { DeleteLogo } from "./DeleteLogo";
import { TokenIcon } from "./TokenIcon";
import { SelectionsAction } from "./useSelections";

type TokenSelectProps = {
  span: AnalysisResponseEntry;
  onSelect(token: AnalysisToken | null): void;
  onSelectText(token: AnalysisToken | null): void;
  selected: AnalysisToken | null;
  isOpen: boolean;
  selectionsDispatch: (action: SelectionsAction) => void;
  optionIndex: number | null;
  onOptionIndexChange(optionIndex: number): void;
  isAutoSlected: boolean;
  setOpenedDropdown: React.Dispatch<
    React.SetStateAction<{
      textPosition: number;
      optionPosition: number;
    } | null>
  >;
};
export function TokenSelect({
  span,
  onSelect,
  selected,
  isOpen,
  optionIndex,
  onOptionIndexChange,
  isAutoSlected,
  setOpenedDropdown,
  onSelectText,
  selectionsDispatch,
}: TokenSelectProps) {
  const isInteractive = span.tokens.length > 0;
  const [subtitle, setSubtitle] = React.useState(false);
  const status: Status = isInteractive
    ? selected !== null
      ? isAutoSlected
        ? "auto-selected"
        : "has-selected"
      : "can-select"
    : "not-interactive";
  const entryStyle = (isSelected: boolean, isHighlighted: boolean) => css`
    padding: 8px 16px;
    border-bottom: 1px solid #b09c9c12;
    :hover {
    }
    background-color: ${isHighlighted
      ? " #eeeeee"
      : "var(--openk9-embeddable-search--primary-background-color)"};
    cursor: pointer;
    border-left: ${isSelected
      ? `8px solid var(--openk9-embeddable-search--active-color)`
      : "none"};
    padding-left: ${isSelected ? "8px" : "16px"};
  `;
  const deseleziona = (isSelected: boolean) => css`
    padding: 8px 16px;
    :hover {
    }
    background-color: ${"var(--openk9-embeddable-search--secondary-background-color)"};
    cursor: ${!isSelected ? "" : "not-allowed"};
  `;
  return (
    <div
      className="openk9-token-select-container"
      css={css`
        position: relative;
      `}
    >
      <div
        className="openk9-token-select"
        css={css`
          white-space: pre;
          ${statusStyles[status]};
          border-radius: 4px;
          border-top: ${isOpen
            ? "2px solid var(--openk9-embeddable-search--active-color)"
            : ""};
          margin-top: ${isOpen ? "-2px" : ""};
        `}
      >
        {span.text}
      </div>
      {isOpen && isInteractive && (
        <div
          className="openk9-token-select-is-open"
          css={css`
            position: absolute;
            top: 100%;
            left: 0px;
            width: 330px;
            background-color: var(
              --openk9-embeddable-search--primary-background-color
            );
            border: 1px solid var(--openk9-embeddable-search--border-color);
            border-radius: 4px;
            background-color: var(
              --openk9-embeddable-search--secondary-background-color
            );
            margin-bottom: 1px solid red;

            z-index: 2; /* workaround for scrollbar overaly problem */
          `}
        >
          <div
            onMouseEnter={() => {
              onOptionIndexChange(0);
            }}
            css={css`
              ${deseleziona(selected === null)};
            `}
          >
            <div
              className="openk9-token-select-uncheck"
              css={css`
                display: flex;
                justify-content: space-between;
                align-items: baseline;
                color: var(--openk9-embeddable-search--secondary-text-color);
                text-decoration: ${selected && subtitle ? "underline" : ""};
                cursor: ${selected && subtitle ? "pointer" : ""};
              `}
            >
              <div
                onClick={() => {
                  if (selected) onSelect(null);
                }}
                onMouseEnter={() => {
                  setSubtitle(true);
                }}
                onMouseLeave={() => {
                  setSubtitle(false);
                }}
              >
                Uncheck
              </div>
              <div
                style={{ cursor: "pointer" }}
                onClick={() => {
                  setOpenedDropdown(null);
                }}
              >
                <DeleteLogo widthParam={10} heightParam={10} />
              </div>
            </div>
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
                  if (option.tokenType === "AUTOCOMPLETE") {
                    onSelectText(option);
                  } else {
                    onSelect(option);
                  }
                }}
                onMouseEnter={() => {
                  onOptionIndexChange(index + 1);
                }}
                className="openk9-token-select-container-highlighted"
                css={css`
                  ${entryStyle(isSelected, isHighlighted)};
                  display: flex;
                  justify-content: space-between;
                  align-items: baseline;
                `}
              >
                {"keywordName" in option && (
                  <strong
                    className="openk9-token-select-highlighted"
                    css={css`
                      margin-right: 8px;
                    `}
                  >
                    {option.keywordName}:
                  </strong>
                )}
                <FactoryTokenType option={option} />
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
    color: var(--openk9-embeddable-search--primary-color);
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

function FactoryTokenType({
  option,
}: {
  option: AnalysisToken & {
    score: number;
  };
}) {
  switch (option.tokenType) {
    case "AUTOCOMPLETE":
      return (
        <React.Fragment>
          <div
            className="openk9-token-select-factory-autocomplete"
            css={css`
              display: flex;
              font-family: "Helvetica";
              font-style: normal;
              font-weight: 400;
              font-size: 15px;
              line-height: 17px;
            `}
          >
            {option.value}
          </div>
        </React.Fragment>
      );
      break;
    case "AUTOCORRECT":
      return (
        <span className="openk9-token-select-factory-autocorrect">
          Did you mean? <strong>{option.value}</strong>
        </span>
      );
    default:
      return (
        <React.Fragment>
          <div
            className="openk9-token-select-factory-create"
            css={css`
              display: flex;
              font-family: "Helvetica";
              font-style: normal;
              font-weight: 400;
              font-size: 15px;
              line-height: 17px;
            `}
          >
            {getTokenLabel(option)}
          </div>
          <div
            className="openk9-token-select-factory-container-option-label"
            style={{
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
              padding: "4px 8px",
              gap: "4px",
              height: "15px",
              background: "#FFFFFF",
              border:
                "1px solid var(--openk9-embeddable-search--secondary-active-color)",
              borderRadius: "20px",
              marginLeft: "10px",
            }}
          >
            <p
              className="openk9-token-select-factory-option-label"
              css={css`
                color: var(--openk9-embeddable-search--secondary-active-color);
                margin-bottom: 13px;
                font-size: 12px;
              `}
            >
              {option.label}
            </p>
          </div>
        </React.Fragment>
      );
      break;
  }
}
