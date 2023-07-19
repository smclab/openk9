import React from "react";

export function TrashSvg({ size = "26px" }: { size?: string }) {
  return (
    <svg width={size} height={size} viewBox="0 0 17 18" fill="none">
      <path
        d="M5.9 13.5L8.5 10.9L11.1 13.5L12.5 12.1L9.9 9.5L12.5 6.9L11.1 5.5L8.5 8.1L5.9 5.5L4.5 6.9L7.1 9.5L4.5 12.1L5.9 13.5ZM3.5 18C2.95 18 2.47917 17.8042 2.0875 17.4125C1.69583 17.0208 1.5 16.55 1.5 16V3H0.5V1H5.5V0H11.5V1H16.5V3H15.5V16C15.5 16.55 15.3042 17.0208 14.9125 17.4125C14.5208 17.8042 14.05 18 13.5 18H3.5ZM13.5 3H3.5V16H13.5V3Z"
        fill="#C0272B"
      />
    </svg>
  );
}
