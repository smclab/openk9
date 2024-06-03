import React from "react";

export function SearchSvg({
  size = "18",
  label,
}: {
  size?: string;
  label?: string;
}) {
  return (
    <svg
      width={size}
      height={size}
      aria-label={label || ""}
      aria-hidden="true"
      viewBox="0 0 17 18"
      fill="none"
      className="openk9-search-logo"
    >
      <path
        opacity="0.7"
        fillRule="evenodd"
        clipRule="evenodd"
        className="openk9-search-logo-color"
        d="M2.31129 3.01417C-0.653565 5.93904 -0.653565 10.6812 2.31129 13.6061C5.13437 16.3911 9.62761 16.5242 12.6104 14.0056L15.5701 16.9253L15.5701 16.9253C16.3516 17.6961 17.5237 16.5398 16.7423 15.7689L13.7468 12.8139C15.9909 9.87938 15.758 5.68762 13.048 3.01417C10.0831 0.0892974 5.27614 0.0892974 2.31129 3.01417ZM3.48343 4.1705C5.80093 1.88425 9.55834 1.88425 11.8758 4.1705C14.1933 6.45676 14.1933 10.1635 11.8758 12.4498C9.55834 14.736 5.80093 14.736 3.48343 12.4498C1.16593 10.1635 1.16593 6.45676 3.48343 4.1705Z"
        fill="#C0272B"
      />
    </svg>
  );
}
