import React, {
  useEffect,
  useRef,
  useState,
  useCallback,
  useMemo,
} from "react";
import clsx from "clsx";
import { createUseStyles } from "react-jss";
import { position } from "caret-pos";

import { ThemeType } from "../theme";
import {
  Token,
  getTokenSuggestions,
  InputSuggestionToken,
  SearchQuery,
  SearchToken,
} from "@openk9/http-api";
import { InputSuggestionTokensDisplay } from "./InputSuggestionTokensDisplay";

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
  paramToken: {
    color: "white",
    backgroundColor: theme.digitalLakePrimary,
  },
  paramTokenParam: {
    marginRight: theme.spacingUnit,
    flexShrink: 0,
  },
}));

export type CommonProps = {
  suggestionsVisible?: boolean;
  suggestions: InputSuggestionToken[];
  onCloseSuggestions(): void;
  onFocusDown(): void;
  onFocus?: () => void;
  onBlur?: () => void;
};

export function AtomTokenDisplay({
  token,
  suggestionsInfo,
}: {
  token: Token;
  suggestionsInfo: [string, string][];
}) {
  const classes = useStyles();
  const cacheElement = suggestionsInfo.find((t) => t[0] === token.values[0]);
  return cacheElement ? (
    <div className={classes.token}>{cacheElement[1]}</div>
  ) : null;
}

function ParamTokenDisplay({
  token,
  onTokenChange,
  onTokenDelete,
  suggestionsInfo,
  ...rest
}: {
  token: SearchToken;
  onTokenChange(token: SearchToken): void;
  onTokenDelete(): void;
  suggestionsInfo: [string, string][];
  selectedSuggestion: string | null;
  setSelectedSuggestion: React.Dispatch<React.SetStateAction<string | null>>;
} & CommonProps &
  React.HTMLAttributes<HTMLInputElement>) {
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
  small,
  className,
  inputContRef,
  autoFocus,
  suggestionsInfo,
  suggestionsVisible,
  suggestions,
  selectedSuggestion,
  setSelectedSuggestion,
  onCloseSuggestions,
  onFocusDown,
  noParams,
  ...rest
}: {
  token: SearchToken;
  onTokenChange(token: SearchToken): void;
  onTokenDelete(): void;
  onPrevTokenDelete(): void;
  noParams?: boolean;
  small?: boolean;
  autoFocus?: boolean;
  inputContRef?: React.MutableRefObject<HTMLInputElement | null>;
  suggestionsInfo: [string, string][];
  selectedSuggestion: string | null;
  setSelectedSuggestion: React.Dispatch<React.SetStateAction<string | null>>;
} & CommonProps &
  React.HTMLAttributes<HTMLInputElement>) {
  const classes = useStyles();
  const inputRef = useRef<HTMLInputElement | null>(null);

  const filteredSuggestions = useMemo(
    () =>
      noParams ? suggestions.filter((s) => s.kind !== "PARAM") : suggestions,
    [suggestions, noParams],
  );

  const handleWritingQueryChange = useCallback(
    (e: React.ChangeEvent<HTMLInputElement>) => {
      if (e.target.value.length === 0) {
        if (token.keywordKey) {
          onTokenChange({
            ...token,
            tokenType: "TEXT" as const,
            values: [],
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
    [token, onTokenDelete, onTokenChange],
  );

  const handleAutoComplete = useCallback(
    (id?: string) => {
      const toAdd = filteredSuggestions.find(
        (s) => s.id === (id || selectedSuggestion),
      );
      if (toAdd && toAdd.kind === "ENTITY") {
        const tok: SearchToken = {
          tokenType: "ENTITY" as const,
          keywordKey: token.keywordKey,
          entityType: toAdd.type,
          values: [toAdd.id],
        };
        onTokenChange(tok);
      } else if (toAdd && toAdd.kind === "PARAM") {
        const tok: SearchToken = {
          tokenType: "TEXT" as const,
          keywordKey: toAdd.id,
          values: [""],
        };
        onTokenChange(tok);
      } else if (toAdd && toAdd.kind === "TOKEN") {
        const tok: SearchToken = {
          tokenType: toAdd.outputTokenType || ("TEXT" as any),
          keywordKey: toAdd.outputKeywordKey as any,
          values: [toAdd.id as any],
        };
        onTokenChange(tok);
      }
    },
    [filteredSuggestions, selectedSuggestion, onTokenChange, token],
  );

  const handleBackspace = useCallback(
    (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (token.values.length === 0 || token.values[0] === "") {
        e.preventDefault();
        onTokenDelete();
      } else if (
        inputRef.current &&
        inputRef.current.selectionStart === inputRef.current.selectionEnd &&
        inputRef.current.selectionStart === 0
      ) {
        e.preventDefault();
        onPrevTokenDelete();
      }
    },
    [token, onTokenDelete, onPrevTokenDelete],
  );

  const nextSuggestion = useCallback(() => {
    setSelectedSuggestion((selectedSuggestion) => {
      const index = filteredSuggestions.findIndex(
        (s) => s.id === selectedSuggestion,
      );
      if (index !== -1) {
        return filteredSuggestions[
          Math.min(filteredSuggestions.length - 1, index + 1)
        ].id;
      } else {
        return filteredSuggestions[1]
          ? filteredSuggestions[1].id
          : filteredSuggestions[0]
          ? filteredSuggestions[0].id
          : null;
      }
    });
  }, [filteredSuggestions, setSelectedSuggestion]);
  const prevSuggestion = useCallback(() => {
    setSelectedSuggestion((selectedSuggestion) => {
      const index = filteredSuggestions.findIndex(
        (s) => s.id === selectedSuggestion,
      );
      if (index !== -1) {
        return filteredSuggestions[Math.max(0, index - 1)].id;
      } else {
        return selectedSuggestion;
      }
    });
  }, [filteredSuggestions, setSelectedSuggestion]);

  const handleInputFieldKeyDown = useCallback(
    (e: React.KeyboardEvent<HTMLInputElement>) => {
      if (e.key === "Enter" && filteredSuggestions.length > 0) {
        e.preventDefault();
        handleAutoComplete();
      } else if (e.key === "Backspace") {
        handleBackspace(e);
      } else if (e.key === "ArrowDown") {
        e.preventDefault();
        if (filteredSuggestions.length > 0) {
          nextSuggestion();
        } else {
          onFocusDown();
        }
      } else if (e.key === "ArrowUp") {
        e.preventDefault();
        prevSuggestion();
      } else if (e.key === "Escape") {
        e.preventDefault();
        onCloseSuggestions();
      }
    },
    [
      filteredSuggestions,
      handleAutoComplete,
      handleBackspace,
      nextSuggestion,
      onFocusDown,
      prevSuggestion,
      onCloseSuggestions,
    ],
  );

  useEffect(() => {
    if (autoFocus && inputRef.current) {
      inputRef.current.focus();
    }
  }, [autoFocus]);

  return (
    <>
      <input
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

      {filteredSuggestions.length > 0 && !token.keywordKey && (
        <InputSuggestionTokensDisplay
          visible={suggestionsVisible}
          suggestions={filteredSuggestions}
          x={
            (inputRef.current &&
              inputRef.current.offsetLeft -
                (inputContRef?.current?.scrollLeft || 0) +
                position(inputRef.current)?.left) ||
            0
          }
          y={
            ((inputRef.current && position(inputRef.current)?.top) || 0) +
            (inputContRef?.current?.height || 32)
          }
          selected={selectedSuggestion}
          onAdd={handleAutoComplete}
        />
      )}
    </>
  );
}

function SingleToken({
  token,
  onTokenChange,
  onTokenDelete,
  onPrevTokenDelete,
  ...rest
}: {
  token: SearchToken;
  onTokenChange(token: SearchToken): void;
  onTokenDelete(): void;
  onPrevTokenDelete(): void;
  small?: boolean;
  noParams?: boolean;
  autoFocus?: boolean;
  inputContRef?: React.MutableRefObject<HTMLInputElement | null>;
  suggestionsInfo: [string, string][];
  selectedSuggestion: string | null;
  setSelectedSuggestion: React.Dispatch<React.SetStateAction<string | null>>;
} & CommonProps &
  React.HTMLAttributes<HTMLInputElement>) {
  return token.keywordKey ? (
    <ParamTokenDisplay
      {...rest}
      token={token}
      onTokenChange={onTokenChange}
      onTokenDelete={onTokenDelete}
    />
  ) : token.tokenType === "ENTITY" ? (
    <AtomTokenDisplay token={token} {...rest} />
  ) : (
    <SingleInput
      token={token}
      onTokenChange={onTokenChange}
      onTokenDelete={onTokenDelete}
      onPrevTokenDelete={onPrevTokenDelete}
      {...rest}
    />
  );
}

export function SearchQueryField({
  searchQuery,
  onSearchQueryChange,
  suggestions,
  onFocus,
  focusToken,
  onFocusToken,
  suggestionsVisible,
  ...rest
}: {
  searchQuery: SearchQuery;
  onSearchQueryChange(searchQuery: SearchQuery): void;
  focusToken: number | null;
  onFocusToken(token: number | null): void;
} & CommonProps &
  React.HTMLAttributes<HTMLInputElement>) {
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

  const [suggestionsInfo, setSuggestionsInfo] = useState<[string, string][]>(
    [],
  );
  useEffect(() => {
    Promise.all(
      searchQuery.map(async (token) => {
        if (token.tokenType !== "TEXT" || token.keywordKey) {
          const suggestions = await getTokenSuggestions(token);
          const pairs = suggestions.map(
            (s) => [s.id, s.displayDescription] as [string, string],
          );
          return pairs;
        } else {
          return [];
        }
      }),
    ).then((s) => {
      setSuggestionsInfo(s.flat().filter(Boolean) as [string, string][]);
    });
  }, [searchQuery]);

  const [selectedSuggestion, setSelectedSuggestion] = useState<string | null>(
    null,
  );
  const calcSelectedSuggestion =
    (suggestions.find((s) => s.id === selectedSuggestion) &&
      selectedSuggestion) ||
    (suggestions[0] && suggestions[0].id);

  const isEmptyOrLastTokenIsNotText =
    searchQuery.length === 0 ||
    !(searchQuery[searchQuery.length - 1]?.tokenType === "TEXT");

  return (
    <div style={{ position: "relative" }}>
      <div ref={inputContRef} className={classes.inputCont}>
        {[...otherTokens, ...textTokens].map(
          ([tok, i], j, arr) =>
            tok && (
              <SingleToken
                key={i}
                token={tok}
                onTokenChange={(ntok) => handleTokenChange(ntok, i)}
                onTokenDelete={() => handleTokenDelete(i)}
                onPrevTokenDelete={() => handleTokenDelete(i - 1)}
                inputContRef={inputContRef}
                suggestions={suggestions}
                suggestionsInfo={suggestionsInfo}
                selectedSuggestion={calcSelectedSuggestion}
                setSelectedSuggestion={setSelectedSuggestion}
                suggestionsVisible={suggestionsVisible && focusToken === i}
                {...rest}
                autoFocus={j === arr.length - 1 && isEmptyOrLastTokenIsNotText}
                onFocus={() => {
                  onFocusToken(i);
                  onFocus && onFocus();
                }}
              />
            ),
        )}
      </div>
    </div>
  );
}
