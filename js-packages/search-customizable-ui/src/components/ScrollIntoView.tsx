import React from "react";

export function ScrollIntoView<E extends HTMLElement>({
  enabled,
  children,
}: {
  enabled: boolean;
  children: (ref: React.MutableRefObject<E | null>) => React.ReactNode;
}) {
  const ref = React.useRef<E | null>(null);
  React.useLayoutEffect(() => {
    if (enabled) {
      ref.current?.scrollIntoView({ block: "nearest" });
    }
  }, [enabled]);
  return children(ref) as JSX.Element;
}
