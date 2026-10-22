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
import { cleanup, fireEvent, render, screen } from "@testing-library/react";
import { afterEach, describe, expect, test, vi } from "vitest";
import { App } from "./App";
import type { ConfigurationUpdateFunction } from "./embeddable/entry";
import i18n from "./i18n";

// the page only hands its slots to the widget: the widget itself is left out
const { updateConfiguration } = vi.hoisted(() => ({
  updateConfiguration: vi.fn<ConfigurationUpdateFunction>(),
}));

vi.mock("./embeddable/entry", () => ({
  OpenK9: class {
    updateConfiguration = updateConfiguration;
    client = { getLanguageDefault: () => Promise.resolve({ value: "en" }) };
    useCopilotChat = () => ({
      messages: [],
      isChatting: false,
      send: vi.fn(),
      reset: vi.fn(),
    });
    addEventListener() {}
    removeEventListener() {}
  },
}));

afterEach(() => {
  cleanup();
  updateConfiguration.mockClear();
});

// the texts come from the i18n instance of the package
// (typed as possibly null by i18next: the keys of these tests always exist)
function text(key: string): string {
  return i18n.t(key) ?? key;
}

// the correction slot as the page last handed it to the widget
function correctionSlot() {
  const slots = updateConfiguration.mock.calls.flatMap(([update]) =>
    typeof update !== "function" && update.correction !== undefined
      ? [update.correction]
      : [],
  );
  const slot = slots.at(-1);
  if (!slot) throw new Error("the page registers no correction slot");
  return slot;
}

describe("the autocorrection message", () => {
  test("has a slot in the page", () => {
    render(<App />);

    expect(correctionSlot().element).toBeInstanceOf(HTMLElement);
  });

  test("offers the corrected term when the search used the typed one", () => {
    render(<App />);
    const searchTyped = vi.fn();
    const searchCorrected = vi.fn();
    render(
      <>
        {correctionSlot().information(
          "informazione",
          "infromazione",
          searchTyped,
          false,
          searchCorrected,
        )}
      </>,
    );

    expect(screen.getByText(text("your-search-did-you-mean"))).toBeTruthy();
    fireEvent.click(screen.getByRole("button", { name: "informazione" }));
    expect(searchCorrected).toHaveBeenCalledTimes(1);
    expect(searchTyped).not.toHaveBeenCalled();
  });

  test("offers the typed term when the search used the corrected one", () => {
    render(<App />);
    const searchTyped = vi.fn();
    const searchCorrected = vi.fn();
    render(
      <>
        {correctionSlot().information(
          "informazione",
          "infromazione",
          searchTyped,
          true,
          searchCorrected,
        )}
      </>,
    );

    expect(
      screen.getByText(text("search-done-with"), { exact: false }),
    ).toBeTruthy();
    expect(screen.getByText("“informazione”")).toBeTruthy();
    fireEvent.click(screen.getByRole("button", { name: "infromazione" }));
    expect(searchTyped).toHaveBeenCalledTimes(1);
    expect(searchCorrected).not.toHaveBeenCalled();
  });
});
