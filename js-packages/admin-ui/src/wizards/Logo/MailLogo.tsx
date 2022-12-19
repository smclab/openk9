import React from "react";

export function EmailLogo({ height = 250, weigth = 200, className = "" }) {
  return (
    <svg width="87.3" height="78" version="1.1" viewBox="0 0 100.3 68">
      <g transform="translate(-308.35,-38)">
        <polygon points="256.2,217.405 256.38,217.405 435,380.27 435,54.73" fill="black" />
        <polygon points="435,54.73 0,54.73 217.5,252.5  " fill="blue" />
        <polygon points="256.2,217.405 217.5,252.5 178.71,217.405 0,380.27 435,380.27 256.38,217.405  " fill="black" />
        <polygon points="0,54.73 0,380.27 178.71,217.405  " fill="black" />
      </g>
    </svg>
  );
}
