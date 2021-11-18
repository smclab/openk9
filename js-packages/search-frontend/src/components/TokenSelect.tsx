import React from "react";
import { css } from "styled-components/macro";
import {
  AnalysisResponseEntryDTO,
  AnalysisTokenDTO,
  TokenDTO,
} from "../utils/remote-data";
import { myTheme } from "../utils/myTheme";
import { useClickAway } from "../utils/useClickAway";
import { TokenIcon } from "./TokenIcon";

type TokenSelectProps = {
  span: AnalysisResponseEntryDTO;
  onSelect(token: TokenDTO | null): void;
  selected: TokenDTO | null;
};
export function TokenSelect({ span, onSelect, selected }: TokenSelectProps) {
  const [isOpen, setIsOpen] = React.useState(false);
  const clickAwayRef = React.useRef<HTMLDivElement | null>(null);
  useClickAway([clickAwayRef], () => setIsOpen(false));
  const isInteractive = span.tokens.length > 0;
  const status: Status = isInteractive
    ? selected !== null
      ? "has-selected"
      : "can-select"
    : "not-interactive";
  const entryStyle = css`
    padding: 8px 16px;
    :hover {
      background-color: ${myTheme.backgroundColor2};
    }
    cursor: pointer;
  `;
  return (
    <div
      css={css`
        position: relative;
        user-select: none;
        z-index: ${isInteractive ? 0 : -1};
      `}
      onClick={() => {
        if (isInteractive) {
          setIsOpen(true);
        }
      }}
      ref={clickAwayRef}
    >
      <div
        css={css`
          cursor: ${isInteractive ? "pointer" : "default"};
          white-space: pre;
          ${statusStyles[status]};
        `}
      >
        {span.text}
      </div>
      {isOpen && (
        <div
          css={css`
            position: absolute;
            top: 100%;
            left: 0px;
            width: 400px;
            background-color: ${myTheme.backgroundColor1};
            border: 1px solid ${myTheme.searchBarBorderColor};
            border-radius: 4px;
            z-index: 1;
          `}
        >
          <div
            onClick={() => {
              onSelect(null);
            }}
            css={css`
              ${entryStyle};
            `}
          >
            Deseleziona
          </div>
          {span.tokens.map((option, index) => {
            return (
              <div
                key={index}
                onClick={() => {
                  onSelect(option);
                }}
                css={css`
                  ${entryStyle};
                  display: flex;
                `}
              >
                {"keywordKey" in option && <span>{option.keywordKey}: </span>}
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
function getTokenLabel(token: AnalysisTokenDTO) {
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
