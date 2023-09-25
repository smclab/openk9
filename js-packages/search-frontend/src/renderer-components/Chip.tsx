import React from "react";

type ChipProps = {
  title: string;
};
export function Chip({ title }: ChipProps) {
  return (
    <button
      style={{
        padding: "8px 16px",
        borderRadius: "20px",
        border: "1px solid #F9EDEE",
        background: "#F9EDEE",
      }}
      aria-label={`chip of ${title}`}
    >
      <div
        style={{
          display: "flex",
          alignItems: "baseline",
          color: "#c0272b",
        }}
      >
        {title}
      </div>
    </button>
  );
}
