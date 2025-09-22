import styled, { css } from "styled-components";
import React from "react";
import { useRange } from "./useRange";

export default function ListPaginations({
  itemsPerPage = 10,
  pagesToShow = 3,
}: {
  itemsPerPage?: number;
  pagesToShow?: number;
}) {
  const { range, setRange, numberOfResults, actuallyPage, setActuallyPage } =
    useRange();

  React.useEffect(() => {
    const size = itemsPerPage;
    if (range[1] !== size) {
      setRange([actuallyPage * size, size]);
    }
  }, [itemsPerPage, range, setRange, actuallyPage]);

  const size = range[1] || itemsPerPage;
  const numberOfPage = Math.max(1, Math.ceil(numberOfResults / size));

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
    setRange([next * size, size]);
  };

  const handleFirstClick = () => goToPage(0);
  const handlePrevClick = () => goToPage(actuallyPage - 1);
  const handleNextClick = () => goToPage(actuallyPage + 1);
  const handleLastClick = () => goToPage(numberOfPage - 1);

  return (
    <PaginationContainer>
      <PaginationsButton
        onClick={handleFirstClick}
        disabled={actuallyPage === 0}
        aria-label="Vai alla prima pagina"
      >
        {"<<"}
      </PaginationsButton>
      <PaginationsButton
        onClick={handlePrevClick}
        disabled={actuallyPage === 0}
        aria-label="Pagina precedente"
      >
        {"<"}
      </PaginationsButton>
      {Array.from({ length: end - start }, (_, i) => start + i).map((page) => (
        <PaginationsButton
          key={page}
          onClick={() => goToPage(page)}
          isActive={actuallyPage === page}
          aria-label={`Vai alla pagina ${page + 1}`}
        >
          {page + 1}
        </PaginationsButton>
      ))}
      <PaginationsButton
        onClick={handleNextClick}
        disabled={actuallyPage === numberOfPage - 1}
        aria-label="Pagina successiva"
      >
        {">"}
      </PaginationsButton>
      <PaginationsButton
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
