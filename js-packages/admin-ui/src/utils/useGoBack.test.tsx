/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import { BrowserRouter, Link, Route, Routes, useNavigate } from "react-router-dom";
import { describe, expect, test } from "vitest";
import { useGoBack } from "./useGoBack";

function Form() {
  const goBack = useGoBack("/list");
  const navigate = useNavigate();
  return (
    <>
      <button onClick={() => navigate("/form/step")}>step</button>
      <button onClick={goBack}>back</button>
    </>
  );
}

function renderAt(path: string) {
  // a null state makes React Router start the session again from entry 0
  window.history.replaceState(null, "", path);
  render(
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Link to="/form">open</Link>} />
        <Route path="/list" element={<p>list</p>} />
        <Route path="/form/*" element={<Form />} />
      </Routes>
    </BrowserRouter>,
  );
}

describe("useGoBack", () => {
  test("goes back past the form's own entries to the page that opened it", async () => {
    renderAt("/");
    fireEvent.click(screen.getByText("open"));
    fireEvent.click(screen.getByText("step"));
    fireEvent.click(screen.getByText("back"));

    await waitFor(() => expect(window.location.pathname).toBe("/"));
    expect(screen.getByText("open")).toBeInTheDocument();
  });

  test("replaces a form opened from the URL with the list", () => {
    renderAt("/form");
    const length = window.history.length;
    fireEvent.click(screen.getByText("back"));

    expect(window.location.pathname).toBe("/list");
    expect(window.history.length).toBe(length);
  });
});
