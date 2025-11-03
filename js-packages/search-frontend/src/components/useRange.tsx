import React from "react";
import { AutocorrectionType } from "./client";

type Range = [number, number];

interface RangeContextType {
  range: Range;
  setRange: (r: Range) => void;
  numberOfResults: number;
  setNumberOfResults: (n: number) => void;
  actuallyPage: number;
  setActuallyPage: (p: number) => void;
  resetPage: (pageSize?: number) => void;
  correction: AutocorrectionType | undefined | null;
  setCorrection: (c: AutocorrectionType | undefined | null) => void;
  overrideSearchWithCorrection: RangeContextProviderProps;
  setOverrideSearchWithCorrection: setterConnection;
}
export type RangeContextProviderProps = {
  isAutocorrection: boolean | null | undefined;
  renderingCorrection: boolean;
};
const RangeContext = React.createContext<RangeContextType | undefined>(
  undefined,
);

export type setterConnection = React.Dispatch<
  React.SetStateAction<{
    isAutocorrection: boolean | undefined | null;
    renderingCorrection: boolean;
  }>
>;

export const RangeProvider: React.FC<{
  children: React.ReactNode;
  defaultPageSize?: number;
}> = ({ children, defaultPageSize = 10 }) => {
  const [range, setRange] = React.useState<Range>([0, defaultPageSize]);
  const [numberOfResults, setNumberOfResults] = React.useState(0);
  const [actuallyPage, setActuallyPage] = React.useState(0);
  const [correction, setCorrection] = React.useState<
    AutocorrectionType | undefined | null
  >(null);
  const [overrideSearchWithCorrection, setOverrideSearchWithCorrection] =
    React.useState<{
      isAutocorrection: boolean | undefined | null;
      renderingCorrection: boolean;
    }>({ isAutocorrection: null, renderingCorrection: false });
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
        correction,
        setCorrection,
        overrideSearchWithCorrection,
        setOverrideSearchWithCorrection,
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
