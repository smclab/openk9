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
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import "overlayscrollbars/css/OverlayScrollbars.css";

type OverlayScrollbarsInstance = {
  osInstance(): { getElements(): { viewport: HTMLElement } };
};

export const CustomVirtualScrollbar = React.forwardRef<
  HTMLDivElement,
  React.ComponentPropsWithoutRef<"div">
>(({ children, className, style, ...props }, ref) => {
    const refSetter = React.useCallback(
      (scrollbarsRef: OverlayScrollbarsInstance | null) => {
        if (!scrollbarsRef) return;
        const viewport = scrollbarsRef.osInstance().getElements()
          .viewport as HTMLDivElement;
        if (typeof ref === "function") {
          ref(viewport);
        } else if (ref) {
          ref.current = viewport;
        }
      },
      [ref],
    );

    return (
      <OverlayScrollbarsComponent
        ref={refSetter}
        className={className}
        style={style}
        {...props}
      >
        {children}
      </OverlayScrollbarsComponent>
    );
  },
);

