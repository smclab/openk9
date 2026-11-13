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
import { useOverlayScrollbars } from "overlayscrollbars-react";
import "overlayscrollbars/overlayscrollbars.css";

// Virtuoso scrolls its own Scroller element: it becomes the OverlayScrollbars
// viewport, inside a host element that carries the custom scrollbars.
export const CustomVirtualScrollbar = React.forwardRef<
  HTMLDivElement,
  React.ComponentPropsWithoutRef<"div">
>(({ children, ...props }, ref) => {
  const hostRef = React.useRef<HTMLDivElement>(null);
  const [viewport, setViewport] = React.useState<HTMLDivElement | null>(null);
  const [initialize, osInstance] = useOverlayScrollbars();

  React.useEffect(() => {
    if (hostRef.current && viewport) {
      initialize({ target: hostRef.current, elements: { viewport } });
    }
    return () => osInstance()?.destroy();
  }, [viewport, initialize, osInstance]);

  const refSetter = React.useCallback(
    (element: HTMLDivElement | null) => {
      setViewport(element);
      if (typeof ref === "function") {
        ref(element);
      } else if (ref) {
        ref.current = element;
      }
    },
    [ref],
  );

  return (
    <div
      data-overlayscrollbars-initialize=""
      ref={hostRef}
      style={{ height: "100%" }}
    >
      <div ref={refSetter} {...props}>
        {children}
      </div>
    </div>
  );
});
