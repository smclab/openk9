import React, { JSX } from "react";
import { useRange } from "./useRange";
import { set } from "lodash";

type CorrectionFunction = {
  information: (
    correctedQuery: string,
    errorQuery: string,
    confirm: () => void,
  ) => React.ReactNode;
  setSearch: (newSearch: string) => void;
  onCorrectionCallback?: () => void; // Add this optional prop
};

export default function Correction({
  information,
  setSearch,
  onCorrectionCallback,
}: CorrectionFunction): JSX.Element | null {
  const { correction, setOverrideSearchWithCorrection } = useRange();

  if (!correction || !correction.autocorrectionText) return null;

  const corrected = correction?.autocorrectionText ?? "";
  const original = correction?.originalText ?? "";

  return (
    <>
      {information(corrected, original, () => {
        setSearch(original);
        setOverrideSearchWithCorrection(false);
        onCorrectionCallback?.(); // Call the callback when correction happens
      })}
    </>
  );
}
