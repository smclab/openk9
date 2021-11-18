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
  AnalysisTokenDTO,
} from "./utils/remote-data";
import { DetailMemo } from "./renderers/Detail";
import { myTheme } from "./utils/myTheme";
import { Results } from "./components/ResultList";
import { useClickAway } from "./utils/useClickAway";
import { TokenIcon } from "./components/TokenIcon";

export function App() {
  const [text, setText] = React.useState("");
  const [detail, setDetail] = React.useState<ResultDTO | null>(null);
  const textDebounced = useDebounce(text, 300);
  const queryAnalysis = useQueryAnalysis({
    searchText: textDebounced,
    tokens: [],
  });
  const spans = React.useMemo(
    () => calculateSpans(text, queryAnalysis.data?.analysis),
    [queryAnalysis.data?.analysis, text],
  );
  const showOverlay = text === textDebounced && queryAnalysis.data;
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
              flex-grow: 1;
              position: relative;
              display: flex;
            `}
          >
            <input
              value={text}
              onChange={(event) => {
                setText(event.currentTarget.value);
                setDetail(null);
              }}
              css={css`
                flex-grow: 1;
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
            {showOverlay && (
              <div
                css={css`
                  top: 0px;
                  left: 0px;
                  position: absolute;
                  padding: 16px;
                  display: flex;
                `}
              >
                {spans.map((span, index) => {
                  return <Select key={index} span={span} />;
                })}
              </div>
            )}
          </div>
        </div>
      }
      result={
        <Results
          displayMode={{ type: "virtual" }}
          text={textDebounced}
          onDetail={setDetail}
        />
      }
      detail={detail && <DetailMemo result={detail} />}
    />
  );
}

function calculateSpans(
  text: string,
  analysis: AnalysisResponseEntryDTO[] | undefined,
): AnalysisResponseEntryDTO[] {
  const spans: Array<AnalysisResponseEntryDTO> = [
    { text: "", start: 0, end: 0, tokens: [] },
  ];
  for (let i = 0; i < text.length; ) {
    const found = analysis?.find(({ start, end }) => i >= start && i < end);
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
}

type SelectProps = {
  span: AnalysisResponseEntryDTO;
};
function Select({ span }: SelectProps) {
  const [selected, setSelected] = React.useState<AnalysisTokenDTO | null>(null);
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
    user-select: none;
  `;
  return (
    <div
      css={css`
        position: relative;
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
              setSelected(null);
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
                  setSelected(option);
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
