import useGenerateResponse from "./useGenerateResponse";
import styled, { keyframes } from "styled-components";
import React from "react";
import isEqual from "lodash/isEqual";
import { recoverySearchQueryAndSort } from "./ResultList";
import { useRange } from "./useRange";
import Markdown from "react-markdown";

export default function GenerateResponse({
  question,
  searchQuery,
  language,
  sortAfterKey,
}: {
  question: string;
  searchQuery: any[];
  language: string;
  sortAfterKey: string;
}) {
  const { searchQueryData, sortData } = recoverySearchQueryAndSort(searchQuery);
  const { range } = useRange();
  //da rimuovere quando passiamo alla search con il pulsante
  const [loadingSearch, setLoadingSearch] = React.useState(false);

  const { generateResponse, message, isChatting, isLoading } =
    useGenerateResponse({
      initialMessages: [],
      setIsRequestLoading: setLoadingSearch,
    });

  const [prevSearchQuery, setPrevSearchQuery] =
    React.useState<any[]>(searchQueryData);
  const [prevRange, setPrevRange] = React.useState<[number, number]>(range);

  React.useEffect(() => {
    if (!isEqual(searchQuery, prevSearchQuery) || !isEqual(range, prevRange)) {
      setLoadingSearch(true);
      setPrevSearchQuery(searchQuery);
      setPrevRange(range);
      const clearSearchQuery = searchQuery.map(
        ({ isSearch, isTab, filter, goToSuggestion, count, ...rest }) => rest,
      );
      generateResponse(
        question,
        clearSearchQuery,
        language,
        sortAfterKey,
        sortData,
        prevRange,
      );
    }
  }, [
    searchQuery,
    prevSearchQuery,
    generateResponse,
    question,
    language,
    sortAfterKey,
    sortData,
    prevRange,
  ]);

  return (
    <>
      {question && (
        <Container>
          <ContainerBox>
            <Question>Generate answer</Question>
            {isChatting || loadingSearch ? (
              <SmallLoader />
            ) : (
              message?.answer && (
                <Answer>
                  <Markdown>{message.answer}</Markdown>
                </Answer>
              )
            )}
          </ContainerBox>
        </Container>
      )}
    </>
  );
}
const Container = styled.div`
  background: white;
  border-bottom-right-radius: 10px;
  border-bottom-left-radius: 10px;
  padding: 16px;
`;

const ContainerBox = styled.div`
  padding: 5px;
`;

const Question = styled.div`
  font-size: 1.2rem;
  font-weight: bold;
  color: #333;
  margin-bottom: 10px;
`;

const Answer = styled.div`
  font-size: 1rem;
  color: #555;
`;

const spVortex = keyframes`
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(359deg);
  }
`;

const SmallLoader = styled.div`
  width: 1rem;
  height: 1rem;
  clear: both;
  margin: 1rem left;
  border: 2px black solid;
  border-radius: 100%;
  overflow: hidden;
  position: relative;

  &:before,
  &:after {
    content: "";
    border-radius: 50%;
    position: absolute;
    width: inherit;
    height: inherit;
    animation: ${spVortex} 2s infinite linear;
  }

  &:before {
    border-top: 0.5rem black solid;
    top: -0.1875rem;
    left: calc(-50% - 0.1875rem);
    transform-origin: right center;
  }

  &:after {
    border-bottom: 0.5rem black solid;
    top: 0.1875rem;
    right: calc(-50% - 0.1875rem);
    transform-origin: left center;
  }
`;
