import React from "react";

export function useDebounce<T>(value: T, delay: number) {
  const [debouncedValue, setDebouncedValue] = React.useState(value);

  React.useEffect(() => {
    const timeout = setTimeout(() => {
      setDebouncedValue(value);
    }, delay);

    return function cleanup() {
      clearTimeout(timeout);
    };
  }, [value, delay]);

  return debouncedValue;
}
