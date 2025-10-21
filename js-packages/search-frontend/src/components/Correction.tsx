import React, { JSX } from "react";
import { useRange } from "./useRange";

type CorrectionFunction = {
  information: (
    correctedQuery: string,
    errorQuery: string,
    confirm: () => void,
  ) => React.ReactNode;
  setSearch: (newSearch: string) => void;
};

export default function Correction({
  information,
  setSearch,
}: CorrectionFunction): JSX.Element | null {
  const { correction } = useRange();

  if (!correction || !correction.autocorrectionText) return null;

  const corrected = correction?.autocorrectionText ?? "";
  const original = correction?.originalText ?? "";

  return (
    <>
      {information(corrected, original, () => {
        setSearch(corrected);
      })}
    </>
  );
}
