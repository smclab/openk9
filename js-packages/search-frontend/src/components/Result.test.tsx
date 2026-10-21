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
import React from "react";
import { afterEach, describe, expect, test, vi } from "vitest";
import i18n from "../i18n";
import type { TemplatesProps } from "../embeddable/entry";
import { GenericResultItem, OpenK9ClientContext } from "./client";
import { visibleFields } from "./ObscuredResult";
import { ResultMemo } from "./Result";

type Client = NonNullable<React.ContextType<typeof OpenK9ClientContext>>;

// as the searcher sends it to a user without the ACL: web.title in the clear
const obscured = {
  source: {
    id: "uat2489acl-fin",
    documentTypes: ["web"],
    web: { title: "Piano riservato" },
    obscuredFields: [
      "acl",
      "contentId",
      "web.content",
      "web.favicon",
      "web.url",
    ],
  },
  highlight: {},
} as unknown as GenericResultItem<unknown>;

const readable = {
  source: {
    id: "uat2489acl-pub",
    documentTypes: ["web"],
    web: {
      title: "Rapporto pubblico",
      content: "budget annuale",
      url: "https://example.org/acl/pub",
      favicon: "",
    },
  },
  highlight: {},
} as unknown as GenericResultItem<unknown>;

const customTemplate: TemplatesProps[number] = {
  source: "web",
  Template: () => <p>custom template</p>,
  TemplateDetail: () => null,
};

function renderResult(
  result: GenericResultItem<unknown>,
  authInit: Promise<boolean> | null,
  templateCustom?: TemplatesProps,
  isMobile = false,
) {
  const client = { authInit, authenticate: vi.fn() } as unknown as Client;
  const onDetail = vi.fn();
  const view = render(
    <OpenK9ClientContext.Provider value={client}>
      <ResultMemo
        renderers={{ resultRenderers: {}, detailRenderers: {} }}
        result={result}
        onDetail={onDetail}
        isMobile={isMobile}
        setDetailMobile={vi.fn()}
        overChangeCard={true}
        templateCustom={templateCustom}
      />
    </OpenK9ClientContext.Provider>,
  );
  return { ...view, client, onDetail };
}

function card(container: HTMLElement) {
  const element = container.querySelector(
    ".openk9-embeddable-search--result-container",
  );
  if (!element) throw new Error("the result card is not rendered");
  return element;
}

afterEach(cleanup);

// the texts come from the i18n instance of the package
// (typed as possibly null by i18next: the keys of these tests always exist)
function text(key: string): string {
  return i18n.t(key) ?? key;
}

describe("an obscured result", () => {
  test("asks an anonymous user to log in, with the login of the header", async () => {
    const { client } = renderResult(obscured, Promise.resolve(false));

    expect(await screen.findByText(text("obscured-result-login"))).toBeTruthy();
    fireEvent.click(screen.getByRole("button", { name: text("login") }));
    expect(client.authenticate).toHaveBeenCalledWith({});
  });

  test("tells a logged in user it cannot be read, without a login", async () => {
    renderResult(obscured, Promise.resolve(true));

    expect(await screen.findByText(text("obscured-result-hint"))).toBeTruthy();
    expect(screen.queryByRole("button", { name: text("login") })).toBeNull();
  });

  test("offers no login when the embedder handles it (no OAuth2)", () => {
    renderResult(obscured, null);

    expect(screen.getByText(text("obscured-result-hint"))).toBeTruthy();
    expect(screen.queryByRole("button", { name: text("login") })).toBeNull();
  });

  test("shows its visible fields as rows, without a link nor a preview", () => {
    const { container, onDetail } = renderResult(obscured, null);

    expect(screen.getByText(text("obscured-result"))).toBeTruthy();
    expect(screen.getByText(text("obscured-result-chip"))).toBeTruthy();
    expect(screen.getByText("web")).toBeTruthy();
    expect(screen.getByText("Title")).toBeTruthy();
    expect(screen.getByText("Piano riservato")).toBeTruthy();
    expect(container.querySelector("a[href]")).toBeNull();
    fireEvent.mouseEnter(card(container));
    expect(onDetail).not.toHaveBeenCalled();
  });

  test("replaces the builtin template, even without the fields it reads", () => {
    // the pdf template reads file.lastModifiedDate, which this result does not have
    renderResult(
      {
        source: {
          id: "doc-pdf",
          documentTypes: ["pdf"],
          obscuredFields: ["file.lastModifiedDate", "document.url"],
        },
        highlight: {},
      } as unknown as GenericResultItem<unknown>,
      null,
    );

    expect(screen.getByText(text("obscured-result"))).toBeTruthy();
    expect(screen.getByText("pdf")).toBeTruthy();
  });

  test("hands it unchanged to the custom template, without the card nor a preview", () => {
    const { container, onDetail } = renderResult(obscured, null, [
      customTemplate,
    ]);

    expect(screen.getByText("custom template")).toBeTruthy();
    expect(screen.queryByText(text("obscured-result"))).toBeNull();
    fireEvent.mouseEnter(card(container));
    expect(onDetail).not.toHaveBeenCalled();
  });

  test("has no preview button on mobile, with a custom template too", () => {
    renderResult(obscured, null, [customTemplate], true);

    expect(screen.getByText("custom template")).toBeTruthy();
    expect(screen.queryByRole("button", { name: text("preview") })).toBeNull();
  });
});

describe("the visible fields of an obscured result", () => {
  test("are its fields but the reserved ones, one row per i18n parent", () => {
    const fields = visibleFields(
      {
        id: "doc-1",
        documentTypes: ["document"],
        obscuredFields: ["document.content"],
        document: {
          title: { i18n: { it: "Titolo" }, base: "Title" },
          lastModifiedDate: 1757548800000,
          tags: ["a", "b"],
        },
      },
      "it",
    );

    expect(fields).toEqual([
      { label: "Title", value: "Titolo" },
      {
        label: "Last modified date",
        value: new Date(1757548800000).toLocaleDateString("it"),
      },
      { label: "Tags", value: "a, b" },
    ]);
  });
});

describe("a readable result", () => {
  test("keeps its link and opens the preview, without the obscured parts", () => {
    const { container, onDetail } = renderResult(readable, null);

    expect(
      container.querySelector('a[href="https://example.org/acl/pub"]'),
    ).not.toBeNull();
    expect(screen.queryByText(text("obscured-result"))).toBeNull();
    fireEvent.mouseEnter(card(container));
    expect(onDetail).toHaveBeenCalledWith(readable);
  });
});
