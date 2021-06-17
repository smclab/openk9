import React from "react";
import ReactDOM from "react-dom";

import { Brand } from "@openk9/search-ui-components";

function App() {
  return (
    <>
      <Brand />
      It Works!
    </>
  );
}

export function initOpenK9(element: Element) {
  ReactDOM.render(<App />, element);
}

const openK9API = {
  initOpenK9,
};

declare global {
  interface Window {
    OpenK9: typeof openK9API;
  }
}

window.OpenK9 = openK9API;
export default openK9API;
