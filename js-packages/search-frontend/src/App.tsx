import React from "react";
import { css } from "styled-components/macro";
import "./index.css";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faSearch } from "@fortawesome/free-solid-svg-icons";
import { PageLayout } from "./components/PageLayout";
import { useDebounce } from "./utils/useDebounce";
import {
  ResultDTO,
  useQueryAnalysis,
  AnalysisResponseEntryDTO,
} from "./utils/remote-data";
import { DetailMemo } from "./renderers/Detail";
import { myTheme } from "./utils/myTheme";
import {
  FiniteResults,
  InfiniteResults,
  VirtualResults,
} from "./components/ResultList";

const resultDisplayMode:
  | { type: "finite" }
  | { type: "infinite" }
  | { type: "virtual" } = { type: "virtual" };

export function App() {
  const [text, setText] = React.useState("");
  const [detail, setDetail] = React.useState<ResultDTO | null>(null);
  const textDebounced = useDebounce(text, 300);
  const queryAnalysis = useQueryAnalysis({
    searchText: textDebounced,
    tokens: [],
  });
  const spans = React.useMemo(() => {
    const spans: Array<AnalysisResponseEntryDTO> = [
      { text: "", start: 0, end: 0, tokens: [] },
    ];
    for (let i = 0; i < text.length; ) {
      const found = queryAnalysis.data?.analysis.find(
        ({ start, end }) => i >= start && i < end,
      );
      if (found) {
        spans.push(found);
        i += found.text.length;
        spans.push({ text: "", start: i, end: i, tokens: [] });
      } else {
        const last = spans[spans.length - 1];
        last.text += text[i];
        last.end += 1;
        i += 1;
      }
    }
    return spans;
  }, [queryAnalysis.data?.analysis, text]);
  const showOverlay = text === textDebounced && queryAnalysis.data;
  const [currentChoice, setCurrentChoice] =
    React.useState<AnalysisResponseEntryDTO | null>(null);
  return (
    <PageLayout
      search={
        <div
          css={css`
            display: flex;
            border: 1px solid ${myTheme.searchBarBorderColor};
            border-radius: 4px;
            background-color: white;
            align-items: center;
          `}
        >
          <FontAwesomeIcon icon={faSearch} style={{ paddingLeft: "16px" }} />
          <div
            css={css`
              position: relative;
              flex-grow: 1;
            `}
          >
            <input
              value={text}
              onChange={(event) => {
                setText(event.currentTarget.value);
                setDetail(null);
              }}
              css={css`
                width: 100%;
                border: none;
                outline: none;
                padding: 16px;
                color: inherit;
                font-size: inherit;
                font-family: inherit;
                background-color: inherit;
              `}
              spellCheck="false"
            ></input>
            <div
              css={css`
                top: 0px;
                left: 0px;
                position: absolute;
                padding: 37px 16px 0px 16px;
                display: flex;
              `}
            >
              {showOverlay &&
                spans.map((span, index) => {
                  const isInteractive = span.tokens.length > 0;
                  return (
                    <div
                      key={index}
                      onClick={() => setCurrentChoice(span)}
                      css={css`
                        color: transparent;
                        cursor: ${isInteractive ? "pointer" : "default"};
                        white-space: pre;
                        user-select: none;
                        height: 16px;
                        overflow: hidden;
                        background-color: ${isInteractive
                          ? myTheme.redTextColor
                          : "transparent"};
                      `}
                    >
                      {span.text}
                    </div>
                  );
                })}
            </div>
            {currentChoice && (
              <div
                css={css`
                  position: absolute;
                  background-color: ${myTheme.backgroundColor2};
                  padding: 16px;
                `}
              >
                {currentChoice.tokens.map((token, index) => {
                  return (
                    <div key={index} css={css``}>
                      {token.entityName}
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </div>
      }
      result={(() => {
        switch (resultDisplayMode.type) {
          case "finite":
            return <FiniteResults text={textDebounced} onDetail={setDetail} />;
          case "infinite":
            return (
              <InfiniteResults text={textDebounced} onDetail={setDetail} />
            );
          case "virtual":
            return <VirtualResults text={textDebounced} onDetail={setDetail} />;
        }
      })()}
      detail={detail && <DetailMemo result={detail} />}
    />
  );
}
