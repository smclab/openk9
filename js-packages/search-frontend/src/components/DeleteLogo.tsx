import React from "react";

export function DeleteLogo({ widthParam = 14, heightParam = 28 }) {
  return (
    <svg
      width={widthParam}
      height={heightParam}
      viewBox="0 0 14 14"
      fill="none"
    >
      <path
        d="M14 1.41L12.59 0L7 5.59L1.41 0L0 1.41L5.59 7L0 12.59L1.41 14L7 8.41L12.59 14L14 12.59L8.41 7L14 1.41Z"
        fill="#949494"
      />
    </svg>
  );
}
