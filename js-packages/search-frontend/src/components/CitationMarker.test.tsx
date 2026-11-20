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
import {
  act,
  cleanup,
  fireEvent,
  render,
  screen,
  within,
} from "@testing-library/react";
import { afterEach, describe, expect, test } from "vitest";
import type { CitedSource } from "../../../shared/inline-citations/inlineCitations";
import { CitedAnswer } from "./CitationMarker";

const SOURCES: CitedSource[] = [
  { ordinal: 1, title: "Primo documento", url: "https://smc.it/uno" },
];

afterEach(cleanup);

describe("CitedAnswer", () => {
  test("un nuovo token non chiude il pannello aperto", () => {
    // Prima i components erano ricreati a ogni render: lo `span` nuovo
    // rimontava il chip e il pannello si chiudeva al token successivo.
    const { rerender } = render(
      <CitedAnswer answer="Una frase [1]." citedSources={SOURCES} />,
    );
    fireEvent.mouseEnter(
      screen.getByRole("button", { name: "Primo documento" }),
    );
    expect(screen.getByRole("group")).toBeTruthy();

    rerender(
      <CitedAnswer answer="Una frase [1]. Un'altra" citedSources={SOURCES} />,
    );

    expect(screen.getByRole("group")).toBeTruthy();
  });

  test("ricarica dalla history: usa le fonti salvate", () => {
    // Senza l'evento SOURCES restano le `sources` con ordinal, titolo e url.
    render(
      <CitedAnswer
        answer="Una frase [1]."
        sources={[
          {
            ordinal: 1,
            title: "Primo documento",
            url: "https://smc.it/uno",
            citations: [{ quote: "un passaggio" }],
          },
        ]}
      />,
    );

    fireEvent.mouseEnter(
      screen.getByRole("button", { name: "Primo documento" }),
    );
    const panel = screen.getByRole("group");
    expect(within(panel).getByRole("link").getAttribute("href")).toBe(
      "https://smc.it/uno",
    );
    expect(within(panel).getByText(/un passaggio/)).toBeTruthy();
  });

  test("un url con protocollo non ammesso mostra solo il titolo", () => {
    render(
      <CitedAnswer
        answer="Una frase [1]."
        citedSources={[
          { ordinal: 1, title: "Primo documento", url: "javascript:alert(1)" },
        ]}
      />,
    );

    fireEvent.mouseEnter(
      screen.getByRole("button", { name: "Primo documento" }),
    );

    expect(
      within(screen.getByRole("group")).queryByRole("link"),
    ).toBeNull();
  });

  test("un numero senza fonte resta testo", () => {
    render(<CitedAnswer answer="Una frase [9]." citedSources={SOURCES} />);

    expect(screen.getByText("[9]")).toBeTruthy();
    expect(screen.queryByRole("button")).toBeNull();
  });

  test("Esc chiude il pannello e riporta il focus sul chip", () => {
    render(<CitedAnswer answer="Una frase [1]." citedSources={SOURCES} />);

    const chip = screen.getByRole("button", { name: "Primo documento" });
    act(() => chip.focus());
    const link = within(screen.getByRole("group")).getByRole("link");
    act(() => link.focus());
    fireEvent.keyDown(link, { key: "Escape" });

    expect(screen.queryByRole("group")).toBeNull();
    expect(document.activeElement).toBe(chip);
  });
});
