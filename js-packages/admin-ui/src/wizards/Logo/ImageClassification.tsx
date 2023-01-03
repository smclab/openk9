import React from "react";

export function ImageClassification({ height = 150, weigth = 200, className = "" }) {
  return (
    <svg
      className=""
      aria-hidden="true"
      fill="currentColor"
      focusable="false"
      role="img"
      width="1em"
      height="1em"
      preserveAspectRatio="xMidYMid meet"
      viewBox="0 0 32 32"
    >
      <polygon points="4 20 4 22 8.586 22 2 28.586 3.414 30 10 23.414 10 28 12 28 12 20 4 20"></polygon>
      <path d="M19,14a3,3,0,1,0-3-3A3,3,0,0,0,19,14Zm0-4a1,1,0,1,1-1,1A1,1,0,0,1,19,10Z"></path>
      <path d="M26,4H6A2,2,0,0,0,4,6V16H6V6H26V21.17l-3.59-3.59a2,2,0,0,0-2.82,0L18,19.17,11.8308,13l-1.4151,1.4155L14,18l2.59,2.59a2,2,0,0,0,2.82,0L21,19l5,5v2H16v2H26a2,2,0,0,0,2-2V6A2,2,0,0,0,26,4Z"></path>
    </svg>
  );
}
