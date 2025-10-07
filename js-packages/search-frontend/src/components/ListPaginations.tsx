import styled, { css } from "styled-components";
import React from "react";
import { useRange } from "./useRange";
import { SelectionsAction, SelectionsState } from "./useSelections";

export default function ListPaginations({
  itemsPerPage = 10,
  pagesToShow = 3,
  state,
  dispatch,
  extraClass,
}: {
  itemsPerPage?: number;
  pagesToShow?: number;
  state: SelectionsState;
  dispatch: React.Dispatch<SelectionsAction>;
  extraClass?:
    | { buttonPagination?: string; activeButtonPagination?: string }
    | null
    | undefined;
}) {
  const { numberOfResults, actuallyPage, setActuallyPage } = useRange();

  React.useEffect(() => {
    const size = itemsPerPage;
    if (state.range[1] !== size) {
      dispatch({ type: "set-page-size", pageSize: size });
      setActuallyPage(0);
    }
  }, [itemsPerPage, state.range, dispatch, setActuallyPage]);

  const size = state.range[1] || itemsPerPage;
  const numberOfPage = Math.max(1, Math.ceil(numberOfResults / size));
  const derivedPage = Math.floor((state.range[0] || 0) / size);

  React.useEffect(() => {
    if (derivedPage !== actuallyPage) setActuallyPage(derivedPage);
  }, [derivedPage, actuallyPage, setActuallyPage]);

  const getPageRange = () => {
    let start = Math.max(0, actuallyPage - Math.floor(pagesToShow / 2));
    let end = start + pagesToShow;
    if (end > numberOfPage) {
      end = numberOfPage;
      start = Math.max(0, end - pagesToShow);
    }
    return { start, end };
  };

  const { start, end } = getPageRange();

  const goToPage = (page: number) => {
    const next = Math.max(0, Math.min(numberOfPage - 1, page));
    setActuallyPage(next);
    dispatch({ type: "set-range", range: [next * size, size] });
  };

  const handleFirstClick = () => goToPage(0);
  const handlePrevClick = () => goToPage(actuallyPage - 1);
  const handleNextClick = () => goToPage(actuallyPage + 1);
  const handleLastClick = () => goToPage(numberOfPage - 1);

  return (
    <PaginationContainer className="openk9-pagination-container">
      <PaginationsButton
        className={`openk9-pagination-arrow ${
          extraClass?.buttonPagination ?? ""
        }`}
        onClick={handleFirstClick}
        disabled={actuallyPage === 0}
        aria-label="Vai alla prima pagina"
      >
        {"<<"}
      </PaginationsButton>
      <PaginationsButton
        className={`openk9-pagination-arrow ${
          extraClass?.buttonPagination ?? ""
        }`}
        onClick={handlePrevClick}
        disabled={actuallyPage === 0}
        aria-label="Pagina precedente"
      >
        {"<"}
      </PaginationsButton>
      {Array.from({ length: end - start }, (_, i) => start + i).map((page) => (
        <PaginationsButton
          className={`openk9-pagination-number ${
            extraClass?.buttonPagination ?? ""
          } ${
            page === actuallyPage
              ? (extraClass?.activeButtonPagination ?? "") +
                " active-btn-k9-paginations"
              : ""
          }`}
          key={page}
          onClick={() => goToPage(page)}
          isActive={actuallyPage === page}
          aria-label={`Vai alla pagina ${page + 1}`}
        >
          {page + 1}
        </PaginationsButton>
      ))}
      <PaginationsButton
        className={`openk9-pagination-arrow ${
          extraClass?.buttonPagination ?? ""
        }`}
        onClick={handleNextClick}
        disabled={actuallyPage === numberOfPage - 1}
        aria-label="Pagina successiva"
      >
        {">"}
      </PaginationsButton>
      <PaginationsButton
        className={`openk9-pagination-arrow ${
          extraClass?.buttonPagination ?? ""
        }`}
        onClick={handleLastClick}
        disabled={actuallyPage === numberOfPage - 1}
        aria-label="Vai all'ultima pagina"
      >
        {">>"}
      </PaginationsButton>
    </PaginationContainer>
  );
}

const PaginationContainer = styled.div`
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 16px;
  gap: 8px;
`;

const PaginationsButton = styled.button<{ isActive?: boolean }>`
  ${({ isActive = false }) => css`
    padding: 6px 14px;
    background: ${isActive ? "#c83939" : "#fff"};
    border-radius: 8px;
    border: 2px solid #c83939};
    font-size: 16px;
    font-weight: ${isActive ? "bold" : "normal"};
    cursor: pointer;
    color: ${isActive ? "#fff" : "#c83939"};
    box-shadow: ${isActive ? "0 2px 8px rgba(200,57,57,0.15)" : "none"};
    transition: background 0.2s, color 0.2s;
    outline: none;
    &:hover:not(:disabled) {
      background: #c83939;
      color: #fff;
    }
    &:disabled {
      opacity: 0.5;
      cursor: not-allowed;
    }
  `}
`;
