import React from "react";

export function DropBoxLogo({ height = 150, weigth = 200, className = "" }) {
  return (
    <svg width={height} height={weigth} className={className} viewBox="-80 0 400 150">
      <g transform="translate(0 0)">
        <path id="polygon116" className="cls-1" fill="#0061ff" d="M58.86 75l58.87-37.5L58.86 0 0 37.5z" />
        <path id="polygon118" className="cls-1" fill="#0061ff" d="M176.59 75l58.86-37.5L176.59 0l-58.86 37.5z" />
        <path id="polygon120" className="cls-1" fill="#0061ff" d="M117.73 112.5L58.86 75 0 112.5 58.86 150z" />
        <path id="polygon122" className="cls-1" fill="#0061ff" d="M176.59 150l58.86-37.5L176.59 75l-58.86 37.5z" />
        <path id="polygon124" className="cls-1" fill="#0061ff" d="M176.59 162.5L117.73 125l-58.87 37.5 58.87 37.5z" />
      </g>
    </svg>
  );
}
