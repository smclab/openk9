import React from "react";

export function Trasformers({ height = "1em", width = "1em", viewbox = "0 0 90 90", className = "" }) {
  return (
    <svg
      className="text-black inline-block text-sm"
      aria-hidden="true"
      focusable="false"
      role="img"
      preserveAspectRatio="xMidYMid meet"
      width={width}
      height={height}
      viewBox={viewbox}
    >
      <defs>
        <mask id="a" x="31.46" y="42.5" width="26" height="25" maskUnits="userSpaceOnUse">
          <path
            d="M44.46,57.29c9.83,0,13-8.76,13-13.26,0-2.34-1.57-1.61-4.09-.36-2.33,1.15-5.46,2.74-8.91,2.74-7.18,0-13-6.88-13-2.38S34.63,57.29,44.46,57.29Z"
            fill="#fff"
          ></path>
        </mask>
      </defs>
      <path d="M44.71,77.5A34.75,34.75,0,1,0,10,42.75,34.75,34.75,0,0,0,44.71,77.5Z" fill="#ffd21e"></path>
      <path
        d="M79.46,42.75A34.75,34.75,0,1,0,44.71,77.5,34.75,34.75,0,0,0,79.46,42.75ZM6,42.75A38.75,38.75,0,1,1,44.71,81.5,38.75,38.75,0,0,1,6,42.75Z"
        fill="#ff9d0b"
      ></path>
      <path d="M56,33.29c1.28.45,1.78,3.07,3.07,2.39a5,5,0,1,0-6.76-2.07C52.92,34.76,54.86,32.89,56,33.29Z" fill="#3a3b45"></path>
      <path d="M32.45,33.29c-1.28.45-1.79,3.07-3.07,2.39a5,5,0,1,1,6.76-2.07C35.53,34.76,33.58,32.89,32.45,33.29Z" fill="#3a3b45"></path>
      <path
        d="M44.46,57.29c9.83,0,13-8.76,13-13.26,0-2.34-1.57-1.61-4.09-.36-2.33,1.15-5.46,2.74-8.91,2.74-7.18,0-13-6.88-13-2.38S34.63,57.29,44.46,57.29Z"
        fill="#3a3b45"
      ></path>
      <g mask="url(#a)">
        <path
          d="M44.71,67.5a8.68,8.68,0,0,0,3-16.81l-.36-.13c-.84-.26-1.73,2.6-2.65,2.6S43,50.28,42.23,50.51a8.68,8.68,0,0,0,2.48,17Z"
          fill="#f94040"
        ></path>
      </g>
      <path d="M68.21,38A3.25,3.25,0,1,0,65,34.75,3.25,3.25,0,0,0,68.21,38Z" fill="#ff9d0b"></path>
      <path d="M21.71,38a3.25,3.25,0,1,0-3.25-3.25A3.25,3.25,0,0,0,21.71,38Z" fill="#ff9d0b"></path>
      <path
        d="M15,49A5.22,5.22,0,0,0,11,50.87a5.92,5.92,0,0,0-1.33,3.75,7.28,7.28,0,0,0-1.94-.3A5.3,5.3,0,0,0,3.74,56a5.8,5.8,0,0,0-1.59,4.79A5.58,5.58,0,0,0,2.94,63a5.42,5.42,0,0,0-1.79,2.82,5.88,5.88,0,0,0,.79,4.74c-.08.12-.15.26-.22.39a5.2,5.2,0,0,0-.14,4.63c1,2.32,3.57,4.14,8.52,6.11,3.07,1.21,5.89,2,5.91,2a44.12,44.12,0,0,0,10.93,1.59c5.86,0,10.05-1.79,12.46-5.33C43.28,74.25,42.73,69,37.71,64a27.53,27.53,0,0,1-5-7.77c-.77-2.66-2.83-5.62-6.24-5.62h0a5.07,5.07,0,0,0-.86.07,5.68,5.68,0,0,0-3.73,2.38A13.22,13.22,0,0,0,19,50.28,7.41,7.41,0,0,0,15,49Zm0,4a3.6,3.6,0,0,1,1.82.65C19,55,23.09,62.08,24.6,64.83a2.43,2.43,0,0,0,2.14,1.31c1.55,0,2.75-1.53.15-3.48-3.92-2.93-2.55-7.72-.68-8a1,1,0,0,1,.24,0c1.7,0,2.45,2.93,2.45,2.93a31.55,31.55,0,0,0,6,9.29c3.77,3.77,4,6.8,1.22,10.84-1.88,2.75-5.47,3.58-9.16,3.58A41.51,41.51,0,0,1,17,79.81c-.11,0-13.45-3.8-11.76-7a1.39,1.39,0,0,1,1.34-.76c2.38,0,6.71,3.54,8.57,3.54A.77.77,0,0,0,16,75c.79-2.84-12.06-4-11-8.16a1.31,1.31,0,0,1,1.44-1c3.14,0,10.2,5.53,11.68,5.53a.28.28,0,0,0,.24-.11c.74-1.19.33-2-4.89-5.19S4.6,61,6.68,58.7a1.3,1.3,0,0,1,1-.38c3.17,0,10.66,6.82,10.66,6.82s2,2.1,3.25,2.1a.74.74,0,0,0,.68-.38c.86-1.46-8.06-8.22-8.56-11C13.37,54,14,53,15,53Z"
        fill="#ff9d0b"
      ></path>
      <path
        d="M36.1,77.69c2.75-4,2.55-7.07-1.22-10.84a31.55,31.55,0,0,1-6-9.29s-.82-3.21-2.69-2.91-3.24,5.08.68,8-.78,4.92-2.29,2.17S19,55,16.84,53.65s-3.63-.59-3.13,2.2,9.43,9.55,8.56,11-3.93-1.72-3.93-1.72S8.77,56.43,6.68,58.7,8.27,62.87,13.49,66s5.63,4,4.89,5.19S6.1,62.7,5,66.82,16.79,72.14,16,75,6.94,69.6,5.26,72.8s11.65,7,11.76,7C21.33,80.93,32.27,83.3,36.1,77.69Z"
        fill="#ffd21e"
      ></path>
      <path
        d="M74.9,49A5.21,5.21,0,0,1,79,50.87a5.92,5.92,0,0,1,1.33,3.75,7.35,7.35,0,0,1,2-.3A5.3,5.3,0,0,1,86.19,56a5.83,5.83,0,0,1,1.59,4.79A5.75,5.75,0,0,1,87,63a5.3,5.3,0,0,1,1.79,2.82A5.88,5.88,0,0,1,88,70.55c.08.12.16.26.23.39a5.26,5.26,0,0,1,.14,4.63c-1,2.32-3.58,4.14-8.52,6.11-3.08,1.21-5.89,2-5.92,2A44.12,44.12,0,0,1,63,85.27c-5.86,0-10-1.79-12.46-5.33C46.64,74.25,47.19,69,52.22,64a27.19,27.19,0,0,0,5-7.77c.78-2.66,2.83-5.62,6.24-5.62h0a4.91,4.91,0,0,1,.86.07,5.7,5.7,0,0,1,3.74,2.38,12.79,12.79,0,0,1,2.87-2.8A7.35,7.35,0,0,1,74.9,49Zm0,4a3.63,3.63,0,0,0-1.82.65C71,55,66.83,62.08,65.32,64.83a2.42,2.42,0,0,1-2.14,1.31c-1.54,0-2.75-1.53-.14-3.48,3.91-2.93,2.54-7.72.67-8a1,1,0,0,0-.24,0c-1.7,0-2.45,2.93-2.45,2.93a31.65,31.65,0,0,1-6,9.29c-3.78,3.77-4,6.8-1.22,10.84,1.87,2.75,5.47,3.58,9.15,3.58a41.61,41.61,0,0,0,9.93-1.46c.1,0,13.45-3.8,11.76-7a1.4,1.4,0,0,0-1.34-.76c-2.38,0-6.71,3.54-8.57,3.54a.76.76,0,0,1-.83-.61c-.8-2.84,12-4,11-8.16a1.31,1.31,0,0,0-1.44-1c-3.14,0-10.2,5.53-11.68,5.53a.25.25,0,0,1-.23-.11c-.74-1.19-.34-2,4.88-5.19S85.32,61,83.24,58.7a1.29,1.29,0,0,0-1-.38c-3.18,0-10.67,6.82-10.67,6.82s-2,2.1-3.24,2.1a.74.74,0,0,1-.68-.38c-.87-1.46,8-8.22,8.55-11C76.55,54,76,53,74.9,53Z"
        fill="#ff9d0b"
      ></path>
      <path
        d="M53.83,77.69c-2.75-4-2.56-7.07,1.22-10.84a31.65,31.65,0,0,0,6-9.29s.82-3.21,2.69-2.91,3.24,5.08-.67,8,.78,4.92,2.28,2.17S71,55,73.08,53.65s3.64-.59,3.13,2.2-9.42,9.55-8.55,11,3.92-1.72,3.92-1.72,9.58-8.71,11.66-6.44-1.58,4.17-6.8,7.33-5.63,4-4.89,5.19,12.27-8.52,13.35-4.4-11.76,5.32-11,8.16,9-5.38,10.74-2.18-11.65,7-11.76,7C68.6,80.93,57.65,83.3,53.83,77.69Z"
        fill="#ffd21e"
      ></path>
    </svg>
  );
}