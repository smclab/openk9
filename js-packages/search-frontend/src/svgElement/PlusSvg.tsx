import React from "react";

export function PlusSvg({ size = 26 }) {
  return (
    <svg
      width={size}
      height={size}
      fontWeight="200"
      viewBox="0 0 14 14"
      fill="none"
    >
      <path d="M14 8H8V14H6V8H0V6H6V0H8V6H14V8Z" fill="#C0272B" />
    </svg>
  );
}
