import useGenerateResponse from "./useGenerateResponse";
import styled from "styled-components";
import React from "react";
import isEqual from "lodash/isEqual";
import { recoverySearchQueryAndSort } from "./ResultList";
import { useRange } from "./useRange";

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

  const { generateResponse, message, isChatting } = useGenerateResponse({
    initialMessages: [],
  });

  const [prevSearchQuery, setPrevSearchQuery] =
    React.useState<any[]>(searchQueryData);
  const [prevRange, setPrevRange] = React.useState<[number, number]>(range);

  React.useEffect(() => {
    if (!isEqual(searchQuery, prevSearchQuery) || !isEqual(range, prevRange)) {
      setPrevSearchQuery(searchQuery);
      setPrevRange(range);
      const clearSearchQuery = searchQuery.map(
        ({ isSearch, isTab, filter, goToSuggestion, ...rest }) => rest,
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
      {question !== "" && (
        <Container>
          <>
            <ContainerBox>
              {message?.question && <Question>Generate answer</Question>}
              {message?.answer && <Answer>{message.answer}</Answer>}
            </ContainerBox>
          </>
        </Container>
      )}
    </>
  );
}

const Container = styled.div`
  background: white;
  border-bottom-left-radius: 10px;
  border-bottom-right-radius: 10px;
  padding: 20px;
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
  white-space: pre-wrap; /* Preserva i ritorni a capo */
`;
