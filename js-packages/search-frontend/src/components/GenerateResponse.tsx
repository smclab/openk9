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
import isEqual from "lodash/isEqual";
import React from "react";
import Markdown from "react-markdown";
import styled, { keyframes } from "styled-components";
import { css } from "styled-components";
import { DeleteLogo } from "./DeleteLogo";
import { recoverySearchQueryAndSort } from "./ResultList";
import useGenerateResponse, { Message } from "./useGenerateResponse";
import { useRange } from "./useRange";

type Props = {
  question: string;
  searchQuery: any[];
  language: string;
  sortAfterKey: string;
};

export default function GenerateResponse({
  question,
  searchQuery,
  language,
  sortAfterKey,
}: Props) {
  const { searchQueryData, sortData } = recoverySearchQueryAndSort(searchQuery);
  const { range } = useRange();
  const [loadingSearch, setLoadingSearch] = React.useState<boolean>(false);

  const { generateResponse, message, isChatting, cancelAllResponses } =
    useGenerateResponse({
      initialMessages: [] as Message[],
      setIsRequestLoading: setLoadingSearch,
    });

  const [prevSearchQuery, setPrevSearchQuery] =
    React.useState<any[]>(searchQueryData);
  const [prevRange, setPrevRange] = React.useState<[number, number]>(
    range as [number, number],
  );

  React.useEffect(() => {
    if (!isEqual(searchQuery, prevSearchQuery) || !isEqual(range, prevRange)) {
      setLoadingSearch(true);
      setPrevSearchQuery(searchQuery);
      setPrevRange(range as [number, number]);

      const clearSearchQuery = searchQuery.map(
        ({
          search: _s,
          isTab: _t,
          filter: _f,
          goToSuggestion: _g,
          count: _c,
          ...rest
        }: any) => rest,
      );

      cancelAllResponses();
      generateResponse(
        question,
        clearSearchQuery,
        language,
        sortAfterKey,
        sortData,
        range as [number, number],
      );
    }
  }, [
    searchQuery,
    range,
    generateResponse,
    question,
    language,
    sortAfterKey,
    sortData,
    prevSearchQuery,
    prevRange,
    cancelAllResponses,
  ]);

  return (
    <>
      {question && (
        <Container>
          <ContainerBox>
            <Question>Generate answer</Question>

            {message?.status === "ERROR" ? (
              <div
                css={css`
                  display: flex;
                  align-items: stretch;
                  border-radius: 10px;
                  overflow: hidden;
                  width: 100%;
                `}
              >
                <div
                  css={css`
                    padding: 12px;
                    background: var(--openk9-embeddable-search--primary-color);
                    border: 2px solid
                      var(--openk9-embeddable-search--primary-color);
                    display: flex;
                    justify-content: center;
                    align-items: center;
                    border-top-left-radius: 10px;
                    border-bottom-left-radius: 10px;
                    min-width: 48px;
                  `}
                >
                  <DeleteLogo colorSvg={"white"} />
                </div>
                <div
                  css={css`
                    padding: 12px;
                    border: 2px solid
                      var(--openk9-embeddable-search--primary-color);
                    border-left: none;
                    width: 100%;
                    display: flex;
                    align-items: center;
                    font-size: 0.95rem;
                    color: var(--openk9-embeddable-search--primary-color);
                    font-weight: 700;
                    background-color: #ffffff;
                    border-top-right-radius: 10px;
                    border-bottom-right-radius: 10px;
                  `}
                >
                  {message.answer}
                </div>
              </div>
            ) : (
              <>
                {(isChatting || loadingSearch) && <SmallLoader />}
                {(message?.answer || isChatting) && (
                  <Answer>
                    <Markdown>{message?.answer ?? ""}</Markdown>
                  </Answer>
                )}
              </>
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
  margin-top: 8px;
  white-space: pre-wrap;
`;

const spVortex = keyframes`
  from { transform: rotate(0deg); }
  to { transform: rotate(359deg); }
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

