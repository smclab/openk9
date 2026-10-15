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
import { render, screen, waitFor, within } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
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

function renderAnswer(
  answer: string,
  sources = SOURCES,
  quotes?: Map<number, string>,
) {
  return render(
    <Markdown
      remarkPlugins={[remarkCitationMarkers]}
      components={markdownComponentsWithCitations(sources, quotes)}
    >
      {answer}
    </Markdown>,
  );
}

describe("la pastiglia che cita la fonte", () => {
  it("mostra il titolo della fonte, non il numero", () => {
    // Su un corpus a dominio unico il nome del sito non distinguerebbe
    // nulla: e' il titolo del documento a dire quale fonte e'.
    renderAnswer("SMC opera dal 1995 [2].");

    expect(screen.getByText("Secondo documento")).toBeTruthy();
    expect(screen.queryByText("[2]")).toBeNull();
  });

  it("accorcia un titolo troppo lungo per stare in riga", () => {
    renderAnswer("Una frase [1].", [
      {
        ordinal: 1,
        title: "Un titolo molto lungo che non sta dentro una riga di prosa",
        url: "https://smc.it/uno",
      },
    ]);

    const chip = screen.getByText(/^Un titolo molto lungo/);
    expect(chip.textContent?.endsWith("…")).toBe(true);
    expect((chip.textContent ?? "").length).toBeLessThanOrEqual(28);
  });

  it("riunisce in una pastiglia sola le fonti della stessa frase", () => {
    // "[1][2]" e' una citazione con due fonti, non due citazioni.
    renderAnswer("Una frase [1][2].");

    expect(screen.getByText("Primo documento")).toBeTruthy();
    expect(screen.getByText("+1")).toBeTruthy();
    expect(screen.queryByText("Secondo documento")).toBeNull();
  });

  it("apre il pannello col dominio e il link al passaggio del mouse", async () => {
    const user = userEvent.setup();
    renderAnswer("Una frase [1].");

    expect(screen.queryByRole("tooltip")).toBeNull();
    await user.hover(screen.getByText("Primo documento"));

    const panel = screen.getByRole("tooltip");
    expect(within(panel).getByText("smc.it")).toBeTruthy();
    const link = within(panel).getByRole("link", { name: "Primo documento" });
    expect(link).toHaveAttribute("href", "https://smc.it/uno");
    expect(link).toHaveAttribute("target", "_blank");
    expect(link).toHaveAttribute("rel", "noreferrer");
  });

  it("mostra nel pannello la citazione testuale quando c'e'", async () => {
    const user = userEvent.setup();
    renderAnswer("Una frase [1].", SOURCES, new Map([[1, "un passaggio copiato"]]));

    await user.hover(screen.getByText("Primo documento"));

    expect(within(screen.getByRole("tooltip")).getByText(/un passaggio copiato/)).toBeTruthy();
  });

  it("mostra il pannello anche senza citazione testuale", async () => {
    // Circa un quarto delle fonti non ne ha una: resta il titolo.
    const user = userEvent.setup();
    renderAnswer("Una frase [1].");

    await user.hover(screen.getByText("Primo documento"));

    expect(within(screen.getByRole("tooltip")).getByRole("link")).toBeTruthy();
  });

  it("elenca tutte le fonti raggruppate nel pannello", async () => {
    const user = userEvent.setup();
    renderAnswer("Una frase [1][2].");

    await user.hover(screen.getByText("Primo documento"));

    const panel = screen.getByRole("tooltip");
    expect(within(panel).getByRole("link", { name: "Primo documento" })).toBeTruthy();
    expect(within(panel).getByRole("link", { name: "Secondo documento" })).toBeTruthy();
  });

  it("il pannello resta aperto quando il puntatore ci si sposta sopra", async () => {
    // Il difetto: fra pastiglia e pannello c'era un margine che non
    // apparteneva a nessuno dei due, e attraversarlo chiudeva tutto proprio
    // mentre si andava a cliccare il link.
    const user = userEvent.setup();
    renderAnswer("Una frase [1].");

    await user.hover(screen.getByText("Primo documento"));
    const panel = screen.getByRole("tooltip");
    await user.hover(within(panel).getByRole("link"));

    expect(screen.getByRole("tooltip")).toBeTruthy();
  });

  it("il pannello si chiude lasciando sia pastiglia sia pannello", async () => {
    const user = userEvent.setup();
    renderAnswer("Una frase [1]. Altro testo.");

    await user.hover(screen.getByText("Primo documento"));
    expect(screen.getByRole("tooltip")).toBeTruthy();

    await user.unhover(screen.getByRole("tooltip"));
    await waitFor(() => expect(screen.queryByRole("tooltip")).toBeNull());
  });

  it("lascia il numero visibile se nessuna fonte lo risolve", () => {
    // Il server non sopprime il marcatore proprio perche' degradi in
    // qualcosa di leggibile.
    renderAnswer("Una frase [9].");

    expect(screen.getByText("[9]")).toBeTruthy();
  });

  it("non tocca un indice dentro un blocco di codice", () => {
    renderAnswer("Esempio:\n\n```python\nvalori[2] = 7\n```\n");

    expect(screen.queryByText("Secondo documento")).toBeNull();
    expect(screen.getByText(/valori\[2\] = 7/)).toBeTruthy();
  });

  it("rende la risposta senza marcatori come prima", () => {
    renderAnswer("Nessun marcatore qui.");

    expect(screen.getByText("Nessun marcatore qui.")).toBeTruthy();
    expect(screen.queryByRole("link")).toBeNull();
  });
});
