import React from "react";
import { css } from "styled-components/macro";
import { Virtuoso } from "react-virtuoso";
import { ResultDTO, useInfiniteResults } from "../utils/remote-data";
import { ResultMemo, Result } from "../renderers/Result";

type ResultsProps = {
  text: string;
  onDetail(result: ResultDTO): void;
  displayMode: { type: "finite" } | { type: "infinite" } | { type: "virtual" };
};
export function Results({ displayMode, onDetail, text }: ResultsProps) {
  switch (displayMode.type) {
    case "finite":
      return <FiniteResults text={text} onDetail={onDetail} />;
    case "infinite":
      return <InfiniteResults text={text} onDetail={onDetail} />;
    case "virtual":
      return <VirtualResults text={text} onDetail={onDetail} />;
  }
}

type ResultCountProps = {
  children: number | undefined;
};
function ResultCount({ children }: ResultCountProps) {
  return (
    <div
      css={css`
        padding: 8px 16px;
      `}
    >
      {children} results
    </div>
  );
}
type ResulListProps = {
  text: string;
  onDetail(result: ResultDTO | null): void;
};
type FiniteResultsProps = ResulListProps & {};
export function FiniteResults({ text, onDetail }: FiniteResultsProps) {
  const results = useInfiniteResults(text);
  return (
    <div>
      <ResultCount>{results.data?.pages[0].total}</ResultCount>
      {results.data?.pages[0].result.map((result, index) => {
        return <ResultMemo key={index} result={result} onDetail={onDetail} />;
      })}
    </div>
  );
}
type InfiniteResultsProps = ResulListProps & {};
export function InfiniteResults({ text, onDetail }: InfiniteResultsProps) {
  const results = useInfiniteResults(text);
  return (
    <div>
      <ResultCount>{results.data?.pages[0].total}</ResultCount>
      {results.data?.pages.map((page, pageIndex) => {
        return (
          <React.Fragment key={pageIndex}>
            {page.result.map((result, resultIndex) => {
              return (
                <ResultMemo
                  key={resultIndex}
                  result={result}
                  onDetail={onDetail}
                />
              );
            })}
          </React.Fragment>
        );
      })}
      {results.hasNextPage && (
        <button
          onClick={() => {
            results.fetchNextPage();
          }}
        >
          load more
        </button>
      )}
    </div>
  );
}
type VirtualResultsProps = ResulListProps & {};
export function VirtualResults({ text, onDetail }: VirtualResultsProps) {
  const results = useInfiniteResults(text);
  const resultsFlat = results.data?.pages.flatMap((page) => page.result);
  return (
    <div
      css={css`
        display: flex;
        flex-direction: column;
        height: 100%;
      `}
    >
      <ResultCount>{results.data?.pages[0].total}</ResultCount>
      <Virtuoso
        style={{ flexGrow: 1 }}
        totalCount={resultsFlat?.length ?? 0}
        itemContent={(index) => {
          const result = resultsFlat?.[index];
          if (result) {
            return <Result result={result} onDetail={onDetail} />;
          }
          return null;
        }}
        endReached={() => {
          if (results.hasNextPage) {
            results.fetchNextPage();
          }
        }}
      />
    </div>
  );
}
