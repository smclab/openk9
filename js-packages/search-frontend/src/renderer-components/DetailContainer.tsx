import React from "react";

type DetailContainerProps = { children: React.ReactNode };
export function DetailContainer({ children }: DetailContainerProps) {
  return <div className="openk9-embeddable-detail-container">{children}</div>;
}
