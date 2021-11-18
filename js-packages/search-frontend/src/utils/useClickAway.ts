import React from "react";

export function useClickAway<E extends HTMLElement | null>(
  refs: Array<React.MutableRefObject<E>>,
  onClickAway: () => void,
) {
  React.useLayoutEffect(() => {
    const onClick = (event: MouseEvent) => {
      const isOutsideClick = !refs.some((ref) =>
        ref.current?.contains(event.target as Node),
      );
      if (isOutsideClick) {
        onClickAway();
      }
    };
    document.addEventListener("click", onClick);
    return () => document.addEventListener("click", onClick);
  }, [refs, onClickAway]);
}
