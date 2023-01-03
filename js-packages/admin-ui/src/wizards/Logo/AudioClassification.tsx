import React from "react";

export function AudioClassification({ height = 150, weigth = 200, className = "" }) {
  return (
    <svg
      className=""
      aria-hidden="true"
      focusable="false"
      role="img"
      width="1em"
      height="1em"
      preserveAspectRatio="xMidYMid meet"
      viewBox="0 0 32 32"
    >
      <path
        d="M25 4H10a2.002 2.002 0 0 0-2 2v14.556A3.955 3.955 0 0 0 6 20a4 4 0 1 0 4 4V12h15v8.556A3.954 3.954 0 0 0 23 20a4 4 0 1 0 4 4V6a2.002 2.002 0 0 0-2-2zM6 26a2 2 0 1 1 2-2a2.002 2.002 0 0 1-2 2zm17 0a2 2 0 1 1 2-2a2.003 2.003 0 0 1-2 2zM10 6h15v4H10z"
        fill="currentColor"
      ></path>
    </svg>
  );
}
