import React from "react";
import { OverlayScrollbarsComponent } from "overlayscrollbars-react";
import "overlayscrollbars/css/OverlayScrollbars.css";

export const CustomScrollbar = React.forwardRef(
  ({ children, className, style, ...props }: any, ref: any) => {
    const refSetter = React.useCallback(
      (scrollbarsRef) => {
        if (scrollbarsRef) {
          ref.current = scrollbarsRef.osInstance().getElements().viewport;
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
