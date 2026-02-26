import React from "react";

type ChipProps = {
  title: string;
  customStyles?: CustomStyles;
};

type CustomStyles = {
  button?: React.CSSProperties;
  div?: React.CSSProperties;
};

export function Chip({ title, customStyles }: ChipProps) {
  const buttonStyles = {
    padding: "4px 10px",
    borderRadius: "20px",
    border: "1px solid #C0272B",
    background: "white",
    fontSize: "10px",
    fontWeight: "700",
    lineHeight: "12px",
    ...(customStyles?.button || {}),
  };

  const divStyles = {
    display: "flex",
    alignItems: "baseline",
    color: "#c0272b",
    gap: "8px",
    ...(customStyles?.div || {}),
  };

  return (
    <button style={buttonStyles} aria-label={`chip of ${title}`}>
      <div style={divStyles}>{title}</div>
    </button>
  );
}
