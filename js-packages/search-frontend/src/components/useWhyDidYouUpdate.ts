import React from "react";

export function useWhyDidYouUpdate<Props extends Record<string, any>>(
  label: string,
  props: Props,
) {
  const previousProps = React.useRef<Props>();
  React.useEffect(() => {
    if (previousProps.current) {
      const allKeys = Object.keys({
        ...previousProps.current,
        ...props,
      }) as Array<keyof Props>;
      const changesObj: {
        [K in keyof Props]?: { from: Props[K]; to: Props[K] };
      } = {};
      for (const key of allKeys) {
        if (previousProps.current[key] !== props[key]) {
          changesObj[key] = {
            from: previousProps.current[key],
            to: props[key],
          };
        }
      }
      if (Object.keys(changesObj).length) {
        console.log("[why-did-you-update]", label, changesObj);
      }
    }
    previousProps.current = props;
  });
}
