import React from "react";

export function TotalResults({
  totalResult
}: {
  totalResult: number | null;
}) {
  return (
    <div className="openk9-totalResults-container">{totalResult}</div>
  )
}