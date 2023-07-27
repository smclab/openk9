import React from "react";

export function ArrowLeftSvg({ size = "26" }: { size?: string }) {
  return (
    <svg width={size} height={size} viewBox="0 0 16 16" fill="none">
      <mask
        id="mask0_1113_7190"
        maskUnits="userSpaceOnUse"
        x="0"
        y="0"
        width="16"
        height="16"
      >
        <rect width="16" height="16" fill="#D9D9D9" />
      </mask>
      <g mask="url(#mask0_1113_7190)">
        <path
          d="M7.99996 13.3333L2.66663 8L7.99996 2.66667L8.94996 3.6L5.21663 7.33333H13.3333V8.66667H5.21663L8.94996 12.4L7.99996 13.3333Z"
          fill="#71717A"
        />
      </g>
    </svg>
  );
}
