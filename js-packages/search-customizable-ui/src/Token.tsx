import React from "react";
import { doSearchEntities, EntityToken, SearchToken } from "../../http-api/src";
import { useQuery } from "react-query";

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
}: {
  token: SearchToken;
  onChange(token: SearchToken): void;
  onRemove(): void;
  onSubmit(): void;
  isFocused: boolean;
  onFocus(): void;
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
          <strong>{token.keywordKey}: </strong>{" "}
          {isFocused ? (
            <input
              ref={inputRef}
              value={token.values[0]}
              onChange={(event) => {
                onChange({ ...token, values: [event.currentTarget.value] });
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
    case "TEXT-TOKEN": {
      return <div style={tokenStyle}>{token.values[0]}</div>;
    }
    default:
      return null;
  }
}

function EntityTokenComponent({ token }: { token: EntityToken }) {
  const { data: entity } = useQuery(
    ["entity", token.entityType, token.values[0]] as const,
    async ({ queryKey }) => {
      const [, type, entityId] = queryKey;
      const found = await doSearchEntities({ type, entityId }, null);
      if (found.result.length === 1) return found.result[0];
      else throw new Error();
    },
  );
  return (
    <span style={tokenStyle}>
      <strong>{token.entityType}: </strong> {entity?.name ?? token.values[0]}
    </span>
  );
}
