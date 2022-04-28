import React from "react";

export function useClientSideOnly() {
  const [state, setState] = React.useState(false);
  React.useEffect(() => {
    setState(true);
  }, []);
  return state;
}
