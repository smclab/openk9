import React from "react";

export function DataSourceIcon({ size = 24, className = "" }) {
  return (
    <svg width={size} height={size} viewBox="0 0 14 16" className={className}>
      <path
        d="M12.557,13A1.444,1.444,0,0,0,14,11.556V4.444A1.444,1.444,0,0,0,12.557,3H8.938c-.24,0-.81-.913-.979-1.259l0-.008C7.563.919,7.119,0,6.222,0h-4.8A1.474,1.474,0,0,0,0,1.519V11.556A1.444,1.444,0,0,0,1.443,13H6v1H1a1,1,0,0,0,0,2H13a1,1,0,0,0,0-2H8V13ZM1.984,1.7A.747.747,0,0,1,2,1.625H6.166a4.216,4.216,0,0,1,.5.853c.078.169.162.347.25.522H1.984ZM2,5v6H12V5Z"
        fillRule="evenodd"
        fill="currentColor"
      />
    </svg>
  );
}
