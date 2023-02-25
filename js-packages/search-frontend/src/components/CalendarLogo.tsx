import React from "react";

export function CalendarLogo({ size = 26 }) {
  return (
    <svg height="24" viewBox="0 0 24 24" width="24">
      <path d="M0 0h24v24H0z" fill="white" />
      <path
        d="M20 3h-1V1h-2v2H7V1H5v2H4c-1.1 0-2 .9-2 2v16c0 1.1.9 2 2 2h16c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm0 18H4V8h16v13z"
        fill="#C0272B"
      />
    </svg>
  );
}
