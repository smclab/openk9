import React from "react";

export function TenantsIcon({ size = 24, className = "" }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" className={className}>
      <path
        d="M4,16l9-3,3-9L7,7Z"
        transform="translate(2 2)"
        fill="currentColor"
      />
      <path
        d="M12,0A12,12,0,0,1,24,12,12,12,0,0,1,12,24,12,12,0,0,1,0,12,12,12,0,0,1,12,0ZM3,12a9,9,0,1,0,9-9A9.008,9.008,0,0,0,3,12Z"
        fillRule="evenodd"
        fill="currentColor"
      />
    </svg>
  );
}
