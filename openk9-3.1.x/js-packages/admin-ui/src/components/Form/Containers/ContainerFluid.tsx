/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
import React from "react";

export function ContainerFluid({
  children,
  size = "xs",
  style,
  flexColumn = false,
}: {
  children: React.ReactNode;
  size?: "xs" | "md" | "lg";
  style?: React.CSSProperties;
  flexColumn?: boolean;
}) {
  const widthMap = {
    xs: "50%",
    md: "85%",
    lg: "100%",
  };

  const styleFlexColumn: React.CSSProperties = {
    display: "flex",
    flexDirection: "column",
    gap: "10px",
  };

  return (
    <div
      className="container-fluid container-view"
      style={{
        width: widthMap[size],
        marginLeft: "0",
        ...(flexColumn && styleFlexColumn),
        ...style,
      }}
    >
      {children}
    </div>
  );
}

