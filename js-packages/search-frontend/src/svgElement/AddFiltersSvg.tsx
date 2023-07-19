import React from "react";

export function AddFiltersSvg({ size = "18px" }: { size?: string }) {
  return (
    <svg width={size} height={size} viewBox="0 0 25 24" fill="none">
      <mask
        id="mask0_1113_7109"
        maskUnits="userSpaceOnUse"
        x="0"
        y="0"
        width="18"
        height="18"
      >
        <rect x="0.5" width="18" height="18" fill="#D9D9D9" />
      </mask>
      <g mask="url(#mask0_1113_7109)">
        <path
          d="M10.05 18L4.34998 12.3L5.77498 10.875L10.05 15.15L19.225 5.97498L20.65 7.39998L10.05 18Z"
          fill="white"
        />
      </g>
    </svg>
  );
}
