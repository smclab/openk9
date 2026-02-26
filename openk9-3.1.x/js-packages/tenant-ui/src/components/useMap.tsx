import React from "react";

function useMap<V>() {
  const [state, setState] = React.useState<Map<string, V>>(new Map());
  const get = React.useCallback(
    (key: string) => {
      return state.get(key);
    },
    [state]
  );
  const has = React.useCallback(
    (key: string) => {
      return state.has(key);
    },
    [state]
  );
  const entries = React.useMemo(() => Array.from(state.entries()), [state]);
  const keys = React.useMemo(() => Array.from(state.keys()), [state]);
  const values = React.useMemo(() => Array.from(state.values()), [state]);
  const size = state.size;
  const set = React.useCallback((key: string, value: V) => {
    setState((state) => {
      const copy = new Map(state);
      copy.set(key, value);
      return copy;
    });
  }, []);
  const rem = React.useCallback((key: string) => {
    setState((state) => {
      const copy = new Map(state);
      copy.delete(key);
      return copy;
    });
  }, []);
  return { get, set, rem, entries, keys, values, has, size };
}

export default useMap;
