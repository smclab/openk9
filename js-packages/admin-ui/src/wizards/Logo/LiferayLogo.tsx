import React from "react";

export function LiferayLogo({ height = 150, weigth = 200, className = "" }) {
  return (
    <svg width={height} height={weigth} className={className} viewBox="10 0 25 50">
      <g transform="translate(0 0)">
        <path d="M16.07 29.272H9.717v-6.087c0-.265 0-.265.265-.265h6.22l-.132 6.352z" fill="#1d396b" />
        <path d="M16.07 15.113v6.352H9.717v-6.352h6.352z" fill="#0b1f3b" />
        <path d="M33.14 30.728h6.352v6.352H33.14v-6.352zm-1.323 7.807v6.352h-6.352v-6.352h6.352z" fill="#1d396b" />
        <path d="M33.14 38.535h6.352v6.352H33.14v-6.352z" fill="#0b1f3b" />
        <path d="M23.877 15.113v6.352h-6.352v-6.352h6.352z" fill="#1d396b" />
        <path
          d="M23.877 22.92v6.352h-6.352V22.92h6.352zm7.94-7.807v6.352h-6.352v-6.352h6.352zM16.07 30.728v6.352H9.717v-6.352h6.352zm15.747 0v6.352h-6.352v-6.352h6.352zm7.675-7.808v6.352H33.14V22.92h6.352zM23.877 38.535v6.352h-6.352v-6.352h6.352z"
          fill="#7cb1df"
        />
      </g>
    </svg>
  );
}
