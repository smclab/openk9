import React from "react";

type DetailContainerProps = { children: React.ReactNode };
export function DetialContainer({ children }: DetailContainerProps) {
  return <div>{children}</div>;
}
