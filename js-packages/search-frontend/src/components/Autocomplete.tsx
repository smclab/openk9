import { css } from "styled-components";
import React from "react";

type AutocompleteProps = {
  suggestions: Array<{ autocomplete: string }>;
  highlightIndex: number;
  applySuggestion(s: { autocomplete: string }): void;
  setIsAutocompleteOpen(open: boolean): void;
  setHighlightIndex(index: number): void;
  callbackSelect?(): void;
};
export default function Autocomplete({
  suggestions,
  highlightIndex,
  applySuggestion,
  setIsAutocompleteOpen,
  setHighlightIndex,
  callbackSelect,
}: AutocompleteProps): React.ReactNode {
  const itemRefs = React.useRef<(HTMLDivElement | null)[]>([]);

  React.useEffect(() => {
    if (highlightIndex >= 0 && itemRefs.current[highlightIndex]) {
      itemRefs.current[highlightIndex]?.scrollIntoView({
        block: "nearest",
        behavior: "smooth",
      });
    }
  }, [highlightIndex, suggestions.length]);

  return (
    <div
      className="openk9--autocomplete-suggestions-container"
      css={css`
        left: 0;
        right: 0;
        width: 100vw;
        max-width: none;
        min-width: 320px;
        top: unset;
        background: #fff;
        border: 1.5px solid #c22525;
        box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
        border-radius: 8px;
        max-height: 240px;
        overflow-y: auto;
        z-index: 1000;
        animation: fadeIn 0.18s ease;
        left: calc(50% - 50vw);
        top: 53px;

        left: 0;
        width: 100%;
        max-width: 100%;
        position: absolute;
        @keyframes fadeIn {
          from {
            opacity: 0;
            transform: translateY(-8px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }
      `}
    >
      {suggestions.map((s, index) => (
        <div
          key={index}
          ref={(el) => (itemRefs.current[index] = el)}
          className={`openk9--autocomplete-suggestion-item ${
            highlightIndex === index
              ? "openk9--autocomplete-suggestion-item-highlighted"
              : ""
          }`}
          css={css`
            padding: 12px 20px;
            cursor: pointer;
            font-size: 1rem;
            color: #222;
            background: "transparent"};
            transition: background 0.15s, color 0.15s;
            border-bottom: 1px solid #f3f3f3;
            &:last-child {
              border-bottom: none;
            }
            &:hover,
            &.openk9--autocomplete-suggestion-item-highlighted {
              color: #c22525;
              text-decoration: underline;
            }
          `}
          onMouseDown={(e) => {
            e.preventDefault();
            applySuggestion(s);
            setIsAutocompleteOpen(false);
            callbackSelect && callbackSelect();
          }}
          onMouseEnter={() => setHighlightIndex(index)}
        >
          {s.autocomplete}
        </div>
      ))}
    </div>
  );
}
