import React from "react";
import { render, screen } from "@testing-library/react";
import App from "./App";

test("renders openk9", () => {
  render(<App />);
  const element = screen.getByText(/OpenK9/i);
  expect(element).toBeInTheDocument();
});
