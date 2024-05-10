import React from "react";

export function TotalResults({
  totalResult,
  saveTotalResultState,
}: {
  totalResult: number | null;
  saveTotalResultState?: React.Dispatch<React.SetStateAction<number | null>>;
}) {
  if (saveTotalResultState && totalResult!==null) {
    saveTotalResultState(totalResult);
  } else {
    if (saveTotalResultState) {
      saveTotalResultState(0);
    }
  }

  return <div className="openk9-totalResults-container">{totalResult || 0}</div>;
}
