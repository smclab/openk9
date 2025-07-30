import React from "react";

export function ContainerFluidWithoutView({
  children,
}: {
  children: React.ReactNode;
}) {
  return <div className="container-fluid  ">{children}</div>;
}
