/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import React, { useEffect, useRef, useCallback } from "react";
import clsx from "clsx";
import { createUseStyles } from "react-jss";

import { ThemeType } from "../theme";
import { Token, SearchQuery, SearchToken } from "@openk9/http-api";

const useStyles = createUseStyles((theme: ThemeType) => ({
  inputCont: {
    padding: 0,
    display: "flex",
    alignItems: "center",
    overflowX: "auto",
  },
  inputFieldSmall: {
    minHeight: 0,
  },
  token: {
    display: "flex",
    alignItems: "center",
    flexShrink: 0,
    color: theme.digitalLakePrimary,
    border: `1px solid ${theme.digitalLakePrimary}`,
    backgroundColor: "white",
    padding: [theme.spacingUnit * 0.5, theme.spacingUnit],
    borderRadius: theme.borderRadius,
    "& + &": {
      marginLeft: theme.spacingUnit,
    },
  },
  selectedToken: {
    color: "white",
    backgroundColor: theme.digitalLakePrimaryD1,
  },
  paramToken: {
    color: "white",
    backgroundColor: theme.digitalLakePrimary,
  },
  paramTokenParam: {
    marginRight: theme.spacingUnit,
    flexShrink: 0,
  },
}));

export function AtomTokenDisplay({
  token,
  suggestionsInfo,
  focused,
  tabIndex,
  onNextTokenFocus,
  onPrevTokenFocus,
  onTokenDelete,
}: {
  token: Token;
  suggestionsInfo: [number | string, string][];
  focused: boolean;
  tabIndex: number;
  onNextTokenFocus(): void;
  onPrevTokenFocus(): void;
  onTokenDelete(): void;
}) {
  const classes = useStyles();
  const ref = useRef<HTMLDivElement | null>(null);
  const cacheElement = suggestionsInfo.find((t) => t[0] === token.values[0]);

  useEffect(() => {
    if (ref.current) {
      if (focused) ref.current.focus();
      else ref.current.blur();
    }
  }, [focused]);

  const handleKeyDown = useCallback(
    (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (e.key === "Backspace") {
        onTokenDelete();
      }

      if (e.key === "ArrowLeft") {
        e.preventDefault();
        onPrevTokenFocus();
      } else if (e.key === "ArrowRight") {
        e.preventDefault();
        onNextTokenFocus();
      }
    },
    [token, onTokenDelete],
  );

  return (
    <div
      className={clsx(classes.token, focused && classes.selectedToken)}
      ref={ref}
      onKeyDown={handleKeyDown}
      tabIndex={tabIndex}
    >
      {cacheElement ? cacheElement[1] : token.values[0]}
    </div>
  );
}

export function ParamTokenDisplay({
  token,
  onTokenChange,
  onTokenDelete,
  suggestionsInfo,
  ...rest
}: {
  token: SearchToken;
  onTokenChange(token: SearchToken): void;
  onTokenDelete(): void;
  onNextTokenFocus(): void;
  onPrevTokenFocus(): void;
  suggestionsInfo: [number | string, string][];
  focused: boolean;
  tabIndex: number;
} & React.HTMLAttributes<HTMLInputElement>) {
  const classes = useStyles();
  const cacheElement = suggestionsInfo.find((t) => t[0] === token.keywordKey);

  return (
    <div className={clsx(classes.token, classes.paramToken)}>
      <div className={classes.paramTokenParam}>
        {cacheElement ? cacheElement[1] : token.keywordKey}:
      </div>
      {token.tokenType === "TEXT" ? (
        <SingleToken
          token={{ ...token, keywordKey: undefined }}
          outerKeywordKey={token.keywordKey}
          onTokenChange={(ntok) =>
            onTokenChange({ ...ntok, keywordKey: token.keywordKey })
          }
          onTokenDelete={onTokenDelete}
          onPrevTokenDelete={onTokenDelete}
          {...rest}
          small
          autoFocus
          suggestionsInfo={suggestionsInfo}
          noParams
        />
      ) : (
        <AtomTokenDisplay
          onTokenDelete={onTokenDelete}
          token={token}
          {...rest}
          suggestionsInfo={suggestionsInfo}
        />
      )}
    </div>
  );
}

function SingleInput({
  token,
  onTokenChange,
  onTokenDelete,
  onPrevTokenDelete,
  onNextTokenFocus,
  onPrevTokenFocus,
  small,
  className,
  inputContRef,
  suggestionsInfo,
  noParams,
  outerKeywordKey,
  focused,
  tabIndex,
  ...rest
}: {
  token: SearchToken;
  onTokenChange(token: SearchToken): void;
  onTokenDelete(): void;
  onPrevTokenDelete(): void;
  onNextTokenFocus(): void;
  onPrevTokenFocus(): void;
  noParams?: boolean;
  small?: boolean;
  inputContRef?: React.MutableRefObject<HTMLInputElement | null>;
  suggestionsInfo: [number | string, string][];
  outerKeywordKey?: string;
  focused: boolean;
  tabIndex: number;
} & React.HTMLAttributes<HTMLInputElement>) {
  const classes = useStyles();
  const inputRef = useRef<HTMLInputElement | null>(null);

  const handleWritingQueryChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      if (e.target.value.length === 0) {
        if (outerKeywordKey) {
          onTokenChange({
            ...token,
            tokenType: "TEXT" as const,
            values: [""],
          });
        } else {
          onTokenDelete();
        }
      } else {
        onTokenChange({
          ...token,
          tokenType: "TEXT" as const,
          values: [e.target.value],
        });
      }
    },
    [token, onTokenDelete, onTokenChange, outerKeywordKey],
  );

  const handleInputFieldKeyDown = useCallback(
    (e: React.KeyboardEvent<HTMLInputElement>) => {
      const cursorPos =
        inputRef.current &&
        inputRef.current.selectionStart === inputRef.current.selectionEnd &&
        inputRef.current.selectionStart;
      const inputLimit = inputRef.current && inputRef.current.value.length;

      if (e.key === "Backspace") {
        if (token.values.length === 0 || token.values[0] === "") {
          e.preventDefault();
          onTokenDelete();
        } else if (cursorPos === 0) {
          e.preventDefault();
          onPrevTokenDelete();
        }
      }

      if (e.key === "ArrowLeft" && cursorPos === 0) {
        e.preventDefault();
        onPrevTokenFocus();
      } else if (e.key === "ArrowRight" && cursorPos === inputLimit) {
        e.preventDefault();
        onNextTokenFocus();
      }
    },
    [token, onTokenDelete, onPrevTokenDelete],
  );

  useEffect(() => {
    if (inputRef.current) {
      if (focused) inputRef.current.focus();
      else inputRef.current.blur();
    }
  }, [focused]);

  return (
    <>
      <input
        tabIndex={tabIndex}
        aria-label="Search for"
        placeholder="Search..."
        type="text"
        value={token.values[0] || ""}
        onChange={handleWritingQueryChange}
        onKeyDown={handleInputFieldKeyDown}
        ref={inputRef}
        className={clsx(className, small && classes.inputFieldSmall)}
        {...rest}
      />
    </>
  );
}

function SingleToken({
  token,
  ...rest
}: {
  token: SearchToken;
  onTokenChange(token: SearchToken): void;
  onTokenDelete(): void;
  onPrevTokenDelete(): void;
  onNextTokenFocus(): void;
  onPrevTokenFocus(): void;
  small?: boolean;
  noParams?: boolean;
  autoFocus?: boolean;
  inputContRef?: React.MutableRefObject<HTMLInputElement | null>;
  suggestionsInfo: [number | string, string][];
  outerKeywordKey?: string;
  focused: boolean;
  tabIndex: number;
} & React.HTMLAttributes<HTMLInputElement>) {
  return token.keywordKey ? (
    <ParamTokenDisplay {...rest} token={token} />
  ) : token.tokenType === "ENTITY" ? (
    <AtomTokenDisplay token={token} {...rest} />
  ) : (
    <SingleInput token={token} {...rest} />
  );
}

export function SearchQueryField({
  searchQuery,
  onSearchQueryChange,
  onFocus,
  focusToken,
  onFocusToken,
  suggestionsInfo,
  ...rest
}: {
  searchQuery: SearchQuery;
  onSearchQueryChange(searchQuery: SearchQuery): void;
  focusToken: number | null;
  onFocusToken(token: number | null): void;
  suggestionsInfo: [number | string, string][];
} & React.HTMLAttributes<HTMLInputElement>) {
  const classes = useStyles();

  const inputContRef = useRef<HTMLInputElement | null>(null);
  useEffect(() => {
    if (inputContRef.current) {
      // Scroll to the end at every write
      inputContRef.current.scrollLeft = 999999;
    }
  });

  const hasWriteableField =
    searchQuery.length > 0 &&
    searchQuery.some((t) => t.tokenType === "TEXT" && !t.keywordKey);
  const tokens = [
    ...searchQuery,
    !hasWriteableField && {
      tokenType: "TEXT" as const,
      values: [""],
    },
  ];

  const indexedTokens = tokens.map<[SearchToken | false, number]>((t, i) => [
    t,
    i,
  ]);
  const textTokens = indexedTokens.filter(([t]) => t && t.tokenType === "TEXT");
  const otherTokens = indexedTokens.filter(
    ([t]) => t && t.tokenType !== "TEXT",
  );

  function handleTokenChange(ntok: SearchToken, i: number) {
    onSearchQueryChange(
      i >= searchQuery.length
        ? [...searchQuery, ntok]
        : searchQuery.map((t, j) => (i === j ? ntok : t)),
    );
  }

  function handleTokenDelete(i: number) {
    if (i >= searchQuery.length) {
      onSearchQueryChange(searchQuery.slice(0, -1));
    } else {
      onSearchQueryChange(searchQuery.filter((t, j) => i !== j));
    }
  }

  return (
    <div style={{ position: "relative" }}>
      <div ref={inputContRef} className={classes.inputCont}>
        {[...otherTokens, ...textTokens].map(
          ([tok, i], j, arr) =>
            tok && (
              <SingleToken
                key={i}
                tabIndex={i}
                token={tok}
                onTokenChange={(ntok) => handleTokenChange(ntok, i)}
                onTokenDelete={() => handleTokenDelete(i)}
                onPrevTokenDelete={() => handleTokenDelete(i - 1)}
                inputContRef={inputContRef}
                suggestionsInfo={suggestionsInfo}
                {...rest}
                onFocus={() => onFocusToken(i)}
                focused={focusToken === i}
                onNextTokenFocus={() =>
                  onFocusToken(Math.min((focusToken || 0) + 1, arr.length - 1))
                }
                onPrevTokenFocus={() => onFocusToken((focusToken || 1) - 1)}
              />
            ),
        )}
      </div>
    </div>
  );
}
