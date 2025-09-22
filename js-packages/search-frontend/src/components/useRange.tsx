import React from "react";

type Range = [number, number];

interface RangeContextType {
  range: Range;
  setRange: (r: Range) => void;
  numberOfResults: number;
  setNumberOfResults: (n: number) => void;
  actuallyPage: number;
  setActuallyPage: (p: number) => void;
  resetPage: (pageSize?: number) => void;
}

const RangeContext = React.createContext<RangeContextType | undefined>(
  undefined,
);

export const RangeProvider: React.FC<{
  children: React.ReactNode;
  defaultPageSize?: number;
}> = ({ children, defaultPageSize = 10 }) => {
  const [range, setRange] = React.useState<Range>([0, defaultPageSize]);
  const [numberOfResults, setNumberOfResults] = React.useState(0);
  const [actuallyPage, setActuallyPage] = React.useState(0);

  const resetPage = React.useCallback(
    (pageSize?: number) => {
      const size = pageSize ?? range[1] ?? defaultPageSize;
      setActuallyPage(0);
      setRange([0, size]);
    },
    [range[1], defaultPageSize],
  );

  return (
    <RangeContext.Provider
      value={{
        range,
        setRange,
        numberOfResults,
        setNumberOfResults,
        actuallyPage,
        setActuallyPage,
        resetPage,
      }}
    >
      {children}
    </RangeContext.Provider>
  );
};

export const useRange = () => {
  const ctx = React.useContext(RangeContext);
  if (!ctx) throw new Error("useRange must be used within RangeProvider");
  return ctx;
};
