import React from "react";

type DetailContainerProps = { children: React.ReactNode };
export function DetailContainer({ children }: DetailContainerProps) {
  return <div>{children}</div>;
}
