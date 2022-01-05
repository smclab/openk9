import React from "react";
import { SearchToken } from "@openk9/rest-api";
import { OpenK9UIInteractions } from "../api";
import { useEntity } from "@openk9/search-ui-components";

const tokenStyle: React.CSSProperties = {
  marginRight: "8px",
  boxSizing: "border-box",
};

export function TokenComponent({
  token,
  onChange,
  onRemove,
  onSubmit,
  isFocused,
  onFocus,
  interactions,
}: {
  token: SearchToken;
  onChange(token: SearchToken): void;
  onRemove(): void;
  onSubmit(): void;
  isFocused: boolean;
  onFocus(): void;
  interactions: OpenK9UIInteractions;
}) {
  const inputRef = React.useRef<HTMLInputElement | null>(null);
  React.useLayoutEffect(() => {
    inputRef.current?.focus();
  }, [isFocused]);
  switch (token.tokenType) {
    case "ENTITY": {
      return <EntityTokenComponent token={token} />;
    }
    case "DATASOURCE": {
      return (
        <div style={tokenStyle}>
          <strong>datasource: </strong> {token.values[0]}
        </div>
      );
    }
    case "DOCTYPE": {
      return (
        <div style={tokenStyle}>
          <strong>doctype: </strong> {token.values[0]}
        </div>
      );
    }
    case "TEXT": {
      return (
        <div style={tokenStyle} onClick={() => onFocus()}>
          {token.keywordKey ? <strong>{token.keywordKey}: </strong> : null}
          {isFocused ? (
            <input
              ref={inputRef}
              value={token.values[0]}
              onChange={(event) => {
                onChange({ ...token, values: [event.currentTarget.value] });
                if (interactions.searchAsYouType) {
                  onSubmit();
                }
              }}
              style={{
                appearance: "none",
                outline: "none",
                border: "none",
                font: "inherit",
                padding: "0px",
              }}
              onKeyDown={(event) => {
                switch (event.key) {
                  case "Enter": {
                    event.preventDefault();
                    onSubmit();
                    break;
                  }
                  case "Backspace": {
                    if (event.currentTarget.value === "") {
                      event.preventDefault();
                      onRemove();
                    }
                    break;
                  }
                }
              }}
            />
          ) : (
            <span tabIndex={0} onFocus={() => onFocus()}>
              {token.values[0]}
            </span>
          )}
        </div>
      );
    }
    default:
      return null;
  }
}

function EntityTokenComponent({
  token,
}: {
  token: Extract<SearchToken, { tokenType: "ENTITY" }>;
}) {
  const { data: entity } = useEntity({
    type: token.entityType,
    id: token.values[0],
  });
  return (
    <span style={tokenStyle}>
      {token.keywordKey ? (
        <>
          <strong>{token.keywordKey} :</strong>{" "}
        </>
      ) : null}
      <strong>{token.entityType} </strong> {entity?.name ?? token.values[0]}
    </span>
  );
}
