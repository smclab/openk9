import React, { JSX } from "react";
import { setterConnection, useRange } from "./useRange";

type CorrectionFunction = {
  information: (
    correctedQuery: string,
    errorQuery: string,
    confirm: () => void,
    hasAutocorretion?: boolean,
    confirmedErrorQuery?: () => void,
  ) => React.ReactNode;
  setSearch: (newSearch: string) => void;
  onCorrectionCallback?: () => void;
};

export default function Correction({
  information,
  setSearch,
  onCorrectionCallback,
}: CorrectionFunction): JSX.Element | null {
  const { correction } = useRange();

  if (!correction || !correction.autocorrectionText) return null;

  const corrected = correction?.autocorrectionText ?? "";
  const original = correction?.originalText ?? "";
  const hasCorrected = correction.searchedWithCorrectedText;

  return (
    <>
      {information(
        corrected,
        original,
        () => {
          setSearch(original);
          onCorrectionCallback && onCorrectionCallback();
        },
        hasCorrected,
        () => {
          setSearch(corrected);
        },
      )}
    </>
  );
}
