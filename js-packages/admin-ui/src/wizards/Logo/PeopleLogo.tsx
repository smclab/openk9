import React from "react";

export function PeopleLogo({ height = 150, weigth = 200, className = "" }) {
  return (
    <svg version="1.1" width="256" height="256" viewBox="0 0 256 256">
      <defs></defs>
      <g transform="translate(1.4065934065934016 1.4065934065934016) scale(2.81 2.81)">
        <path
          d="M 84.657 90 H 5.343 v -2 c 0 -21.867 17.79 -39.657 39.657 -39.657 c 21.867 0 39.657 17.79 39.657 39.657 V 90 z M 9.398 86 h 71.203 C 79.562 67.265 63.99 52.343 45 52.343 S 10.439 67.265 9.398 86 z"
          transform=" matrix(1 0 0 1 0 0) "
          stroke-linecap="round"
        />
        <path
          d="M 45 43.971 c -12.123 0 -21.985 -9.863 -21.985 -21.986 C 23.015 9.863 32.877 0 45 0 c 12.123 0 21.985 9.863 21.985 21.985 C 66.985 34.108 57.123 43.971 45 43.971 z M 45 4 c -9.917 0 -17.985 8.068 -17.985 17.985 c 0 9.917 8.068 17.986 17.985 17.986 s 17.985 -8.068 17.985 -17.986 C 62.985 12.068 54.917 4 45 4 z"
          transform=" matrix(1 0 0 1 0 0) "
          stroke-linecap="round"
        />
      </g>
    </svg>
  );
}
