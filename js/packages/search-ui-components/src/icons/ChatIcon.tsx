import React from "react";

export function ChatIcon({ size = 24, className = "" }) {
  return (
    <svg width={size} height={size} viewBox="0 0 16 16" className={className}>
      <path d="M12.8,2H3.2A1.2,1.2,0,0,0,2.006,3.2L2,14l2.4-2.4h8.4A1.2,1.2,0,0,0,14,10.4V3.2A1.2,1.2,0,0,0,12.8,2ZM4.4,6.2h7.2V7.4H4.4Zm4.8,3H4.4V8H9.2Zm2.4-3.6H4.4V4.4h7.2Z" />
    </svg>
  );
}
