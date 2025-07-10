import React from "react";

export function ContainerFluidTable({
  children,
}: {
  children: React.ReactNode;
}) {
  return <div className="container-fluid container-view ">{children}</div>;
}
