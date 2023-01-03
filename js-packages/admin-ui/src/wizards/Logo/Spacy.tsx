import React from "react";

export function Spacy({ height = 150, weigth = 200, className = "" }) {
  return (
    <svg
      className="text-black inline-block  text-sm"
      aria-hidden="true"
      focusable="false"
      role="img"
      preserveAspectRatio="xMidYMid meet"
      width="2em"
      height="2em"
      viewBox="1 -18 60 80"
    >
      <path
        d="M23.53,10.89c-3.67-.39-4-5.31-8.59-4.93-2.32,0-4.35,1-4.35,3,0,3.19,4.83,3.48,7.82,4.25,5,1.54,9.86,2.51,9.86,8,0,6.86-5.41,9.27-12.56,9.27-6,0-12-2.13-12-7.63a2.87,2.87,0,0,1,2.9-2.71,2.81,2.81,0,0,1,3,2C11,24.61,12.52,26,16.39,26c2.41,0,4.92-1,4.92-3,0-3-3.09-3.68-6.28-4.35C9.33,17,4.51,16.11,3.93,9.92,3.35-.8,25.47-1.18,26.82,8.19A3.1,3.1,0,0,1,23.53,10.89Z"
        fill="#09a3d5"
      ></path>
    </svg>
  );
}
