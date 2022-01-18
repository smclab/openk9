import React from "react";
import ReactDOM from "react-dom";
import { Main } from "./Main";
import { QueryClient, QueryClientProvider } from "react-query";

const queryClient = new QueryClient();

function Entry() {
  const [targets, setTargets] = React.useState<TargetElements>(targetElements);
  React.useLayoutEffect(() => {
    updateTargetElements = setTargets;
  }, []);
  return (
    <QueryClientProvider client={queryClient}>
      <Main targetElements={targets} />;
    </QueryClientProvider>
  );
}

export type TargetElements = {
  search: Element | null;
  tabs: Element | null;
  results: Element | null;
  details: Element | null;
  login: Element | null;
};

const targetElements: TargetElements = {
  search: null,
  tabs: null,
  results: null,
  details: null,
  login: null,
};

let updateTargetElements = (targetElements: TargetElements) => {};

export const OpenK9: TargetElements = {
  set search(element: Element | null) {
    targetElements.search = element;
    updateTargetElements({ ...targetElements });
  },
  set tabs(element: Element | null) {
    targetElements.tabs = element;
    updateTargetElements({ ...targetElements });
  },
  set results(element: Element | null) {
    targetElements.results = element;
    updateTargetElements({ ...targetElements });
  },
  set details(element: Element | null) {
    targetElements.details = element;
    updateTargetElements({ ...targetElements });
  },
  set login(element: Element | null) {
    targetElements.login = element;
    updateTargetElements({ ...targetElements });
  },
};

window.OpenK9rootElement =
  window.OpenK9rootElement ?? document.createDocumentFragment();

ReactDOM.render(
  <React.StrictMode>
    <Entry />
  </React.StrictMode>,
  window.OpenK9rootElement,
);

window.OpenK9 = OpenK9;
declare global {
  interface Window {
    OpenK9rootElement: Element;
    OpenK9: typeof OpenK9;
  }
}
