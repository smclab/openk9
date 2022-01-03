import React from "react";
import { SuggestionResult } from "@openk9/rest-api";
import { OpenK9UITemplates } from "../api";
import { EmbedElement } from "./EmbedElement";
import { ScrollIntoView } from "./ScrollIntoView";

type SuggestionPageProps = {
  suggestions: SuggestionResult[];
  templates: OpenK9UITemplates;
  onAdd(suggestions: SuggestionResult): void;
  onSelect(suggestion: SuggestionResult): void;
  selected: SuggestionResult | null;
};
function SuggestionPage({
  suggestions,
  templates,
  onAdd,
  onSelect,
  selected,
}: SuggestionPageProps) {
  return (
    <React.Fragment>
      {suggestions.map((suggestion, index) => {
        const customizedItem = templates.suggestionItem?.({
          suggestion,
          select: () => {
            onAdd(suggestion);
          },
        });
        const isHighlighted = selected === suggestion;
        if (customizedItem) {
          return (
            <React.Fragment key={index}>
              <EmbedElement element={customizedItem} />
            </React.Fragment>
          );
        }
        return (
          <ScrollIntoView<HTMLDivElement> key={index} enabled={isHighlighted}>
            {(ref) => (
              <div
                ref={ref}
                style={{
                  padding: "8px 16px",
                  backgroundColor: isHighlighted ? "lightgray" : "",
                  cursor: "pointer",
                }}
                onMouseEnter={() => {
                  onSelect(suggestion);
                }}
                onClick={() => {
                  onAdd(suggestion);
                }}
              >
                {(() => {
                  switch (suggestion.tokenType) {
                    case "DATASOURCE": {
                      return (
                        <>
                          <strong>datasource: </strong> {suggestion.value}
                        </>
                      );
                    }
                    case "DOCTYPE": {
                      return (
                        <>
                          <strong>doctype: </strong> {suggestion.value}
                        </>
                      );
                    }
                    case "ENTITY": {
                      return (
                        <>
                          {suggestion.keywordKey ? (
                            <>
                              <strong>{suggestion.keywordKey}: </strong>
                            </>
                          ) : null}
                          <strong>{suggestion.entityType}</strong>{" "}
                          {suggestion.entityValue}
                        </>
                      );
                    }
                    case "TEXT": {
                      return (
                        <>
                          {suggestion.keywordKey ? (
                            <>
                              <strong>{suggestion.keywordKey}: </strong>
                            </>
                          ) : null}
                          {suggestion.value}
                        </>
                      );
                    }
                    default: {
                      throw new Error();
                    }
                  }
                })()}
              </div>
            )}
          </ScrollIntoView>
        );
      })}
    </React.Fragment>
  );
}
export const SuggestionPageMemo = React.memo(SuggestionPage);
