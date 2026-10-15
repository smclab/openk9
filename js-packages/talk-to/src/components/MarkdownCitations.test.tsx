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

/**
 * The marker the answer writes, as the reader sees it: a link to the source
 * it points at, next to the sentence it belongs to.
 */
import { render, screen } from "@testing-library/react";
import Markdown from "react-markdown";
import { describe, expect, it } from "vitest";

import {
  type CitedSource,
  remarkCitationMarkers,
} from "../../../shared/inline-citations/inlineCitations";
import { markdownComponentsWithCitations } from "./MarkdownRenderer";

const SOURCES: CitedSource[] = [
  { ordinal: 1, title: "Primo documento", url: "https://smc.it/uno" },
  { ordinal: 2, title: "Secondo documento", url: "https://smc.it/due" },
];

function renderAnswer(answer: string, sources = SOURCES) {
  return render(
    <Markdown
      remarkPlugins={[remarkCitationMarkers]}
      components={markdownComponentsWithCitations(sources)}
    >
      {answer}
    </Markdown>,
  );
}

describe("marcatori di citazione resi nella risposta", () => {
  it("rende il marcatore come link alla fonte", () => {
    renderAnswer("SMC opera dal 1995 [2].");

    const link = screen.getByRole("link", { name: "[2]" });
    expect(link).toHaveAttribute("href", "https://smc.it/due");
    expect(link).toHaveAttribute("title", "Secondo documento");
  });

  it("apre la fonte in una scheda nuova senza perdere la risposta", () => {
    renderAnswer("Una frase [1].");

    const link = screen.getByRole("link", { name: "[1]" });
    expect(link).toHaveAttribute("target", "_blank");
    expect(link).toHaveAttribute("rel", "noreferrer");
  });

  it("lascia il numero visibile se nessuna fonte lo risolve", () => {
    // Il server non sopprime il marcatore proprio perche' degradi in
    // qualcosa di leggibile: nasconderlo toglierebbe l'informazione.
    renderAnswer("Una frase [9].", SOURCES);

    expect(screen.queryByRole("link", { name: "[9]" })).toBeNull();
    expect(screen.getByText(/\[?9\]?/)).toBeTruthy();
  });

  it("non rende link una fonte senza url", () => {
    renderAnswer("Una frase [1].", [{ ordinal: 1, title: "Senza url" }]);

    expect(screen.queryByRole("link")).toBeNull();
  });

  it("rende due note per due frasi dalla stessa fonte", () => {
    renderAnswer("Prima [2]. Seconda [2].");

    expect(screen.getAllByRole("link", { name: "[2]" })).toHaveLength(2);
  });

  it("non tocca un indice dentro un blocco di codice", () => {
    renderAnswer("Esempio:\n\n```python\nvalori[2] = 7\n```\n");

    expect(screen.queryByRole("link")).toBeNull();
    expect(screen.getByText(/valori\[2\] = 7/)).toBeTruthy();
  });

  it("rende la risposta senza marcatori come prima", () => {
    renderAnswer("Nessun marcatore qui.");

    expect(screen.getByText("Nessun marcatore qui.")).toBeTruthy();
    expect(screen.queryByRole("link")).toBeNull();
  });
});
