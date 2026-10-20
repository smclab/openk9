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
import type { QueryResult } from "@apollo/client";
import { act, render } from "@testing-library/react";
import type { TableVirtuosoProps } from "react-virtuoso";
import { describe, expect, test, vi } from "vitest";
import { Table } from "./Table";

// jsdom does not lay rows out, so Virtuoso never measures them: the test keeps the
// props the table hands to TableVirtuoso and calls them the way Virtuoso would.
const virtuoso = vi.hoisted(() => {
  const rendered: { props?: TableVirtuosoProps<unknown, unknown> } = {};
  return rendered;
});

vi.mock("react-virtuoso", async (importOriginal) => ({
  ...(await importOriginal<typeof import("react-virtuoso")>()),
  TableVirtuoso: (props: TableVirtuosoProps<unknown, unknown>) => {
    virtuoso.props = props;
    return null;
  },
}));

// the Form barrel pulls in most of the app, and these tests never show its components
vi.mock("../Form", () => ({ EmptySpace: () => null, ModalConfirm: () => null }));

const TABLE_HEIGHT = 800;
const HEADER_HEIGHT = 54;

type Row = { id: string; name: string };
type Query = { items: { edges: Array<{ node: Row }>; pageInfo: { hasNextPage: boolean; endCursor: string } } };
type Variables = { searchText?: string | null; cursor?: string | null };

function loaded(rows: number, hasNextPage: boolean): Query {
  return {
    items: {
      edges: Array.from({ length: rows }, (_, index) => ({
        node: { id: String(index + 1), name: `item-${index + 1}` },
      })),
      pageInfo: { hasNextPage, endCursor: String(rows) },
    },
  };
}

function renderTable(
  data: Query | undefined,
  {
    fetchMore,
    refetch = () => Promise.resolve({ data }),
  }: { fetchMore: () => Promise<unknown>; refetch?: () => Promise<{ data: Query | undefined } | undefined> },
) {
  const table = (data: Query | undefined) => (
    <Table
      // the table reads only data, fetchMore and refetch: the rest of the useQuery
      // result is Apollo's own machinery, which this double does not rebuild
      data={{
        queryResult: { data, fetchMore, refetch } as unknown as QueryResult<Query, Variables>,
        field: (query) => query?.items,
      }}
      columns={[]}
      rowActions={[]}
      onDelete={() => {}}
      onCreatePath=""
      edgesPath="items.edges"
      pageInfoPath="items.pageInfo"
    />
  );
  const { rerender } = render(table(data));

  const scroller = document.createElement("div");
  Object.defineProperty(scroller, "clientHeight", { value: TABLE_HEIGHT });
  // jsdom lays nothing out: the header gets the height Virtuoso reports before any row is measured
  const header = scroller.appendChild(document.createElement("table")).appendChild(document.createElement("thead"));
  header.getBoundingClientRect = () => new DOMRect(0, 0, 0, HEADER_HEIGHT);
  rendered().scrollerRef?.(scroller);

  return { rerender: (data: Query) => rerender(table(data)) };
}

function rendered() {
  if (!virtuoso.props) throw new Error("TableVirtuoso was not rendered");
  return virtuoso.props;
}

// lets the pending promises (the refetch on mount, a page load) settle
const settle = () => act(async () => {});

describe("the table loads the next page", () => {
  test("when its rows are shorter than the table", async () => {
    const fetchMore = vi.fn(() => new Promise(() => {}));
    renderTable(loaded(10, true), { fetchMore });
    await settle();

    rendered().totalListHeightChanged?.(400);

    expect(fetchMore).toHaveBeenCalledOnce();
    expect(fetchMore).toHaveBeenCalledWith(expect.objectContaining({ variables: { first: 20, after: "10" } }));
  });

  test("only once while that page is loading", async () => {
    const fetchMore = vi.fn(() => new Promise(() => {}));
    renderTable(loaded(10, true), { fetchMore });
    await settle();

    rendered().totalListHeightChanged?.(400);
    rendered().endReached?.(9);

    expect(fetchMore).toHaveBeenCalledOnce();
  });

  test("page after page, until the rows fill the table", async () => {
    const fetchMore = vi.fn(() => Promise.resolve());
    const { rerender } = renderTable(loaded(10, true), { fetchMore });
    await settle();

    rendered().totalListHeightChanged?.(400);
    await settle();
    rerender(loaded(30, true));
    rendered().totalListHeightChanged?.(700);
    await settle();
    rerender(loaded(50, true));
    rendered().totalListHeightChanged?.(1000);

    expect(fetchMore).toHaveBeenCalledTimes(2);
    expect(fetchMore).toHaveBeenLastCalledWith(expect.objectContaining({ variables: { first: 20, after: "30" } }));
  });

  test("only after the refetch, after the list it returns", async () => {
    let finishRefetch: (result: { data: Query }) => void = () => {};
    const refetch = () => new Promise<{ data: Query }>((resolve) => (finishRefetch = resolve));
    const fetchMore = vi.fn(() => new Promise(() => {}));
    // the list an earlier visit left in the cache, while the refetch brings it back to page one
    const { rerender } = renderTable(loaded(30, true), { fetchMore, refetch });

    rendered().totalListHeightChanged?.(2664);
    rendered().endReached?.(29);
    rerender(loaded(10, true)); // page one can show up before the refetch promise settles
    rendered().totalListHeightChanged?.(400);
    expect(fetchMore).not.toHaveBeenCalled();

    await act(async () => finishRefetch({ data: loaded(10, true) }));

    expect(fetchMore).toHaveBeenCalledOnce();
    expect(fetchMore).toHaveBeenCalledWith(expect.objectContaining({ variables: { first: 20, after: "10" } }));
  });

  test("on the first visit, once the rows of the refetched list are measured", async () => {
    let finishRefetch: (result: { data: Query }) => void = () => {};
    const refetch = () => new Promise<{ data: Query }>((resolve) => (finishRefetch = resolve));
    const fetchMore = vi.fn(() => new Promise(() => {}));
    // nothing in the cache yet: the table mounts empty
    const { rerender } = renderTable(undefined, { fetchMore, refetch });

    rendered().totalListHeightChanged?.(HEADER_HEIGHT); // the empty list
    await act(async () => finishRefetch({ data: loaded(10, true) }));
    rerender(loaded(10, true));
    rendered().totalListHeightChanged?.(HEADER_HEIGHT); // page one is there, but none of its rows is measured yet
    expect(fetchMore).not.toHaveBeenCalled();

    rendered().totalListHeightChanged?.(400);

    expect(fetchMore).toHaveBeenCalledOnce();
    expect(fetchMore).toHaveBeenCalledWith(expect.objectContaining({ variables: { first: 20, after: "10" } }));
  });

  test("when the end of the list was reached during the refetch", async () => {
    let finishRefetch: (result: { data: Query }) => void = () => {};
    const refetch = () => new Promise<{ data: Query }>((resolve) => (finishRefetch = resolve));
    const fetchMore = vi.fn(() => new Promise(() => {}));
    // scrolled to the bottom of a long list, the user searches: page one fills the table
    // and shows its last row before the refetch promise settles
    const { rerender } = renderTable(loaded(60, true), { fetchMore, refetch });

    rendered().totalListHeightChanged?.(5000);
    rerender(loaded(20, true));
    rendered().totalListHeightChanged?.(1700);
    rendered().endReached?.(19);
    expect(fetchMore).not.toHaveBeenCalled();

    await act(async () => finishRefetch({ data: loaded(20, true) }));

    expect(fetchMore).toHaveBeenCalledOnce();
    expect(fetchMore).toHaveBeenCalledWith(expect.objectContaining({ variables: { first: 20, after: "20" } }));
  });

  test("after a refetch that a newer one replaced", async () => {
    const fetchMore = vi.fn(() => new Promise(() => {}));
    // Apollo resolves a replaced refetch with nothing
    renderTable(loaded(10, true), { fetchMore, refetch: () => Promise.resolve(undefined) });
    await settle();

    rendered().totalListHeightChanged?.(400);

    expect(fetchMore).toHaveBeenCalledOnce();
  });
});

describe("the table does not load the next page", () => {
  test("before its rows are measured", async () => {
    const fetchMore = vi.fn(() => new Promise(() => {}));
    renderTable(loaded(10, true), { fetchMore });

    await settle();

    expect(fetchMore).not.toHaveBeenCalled();
  });

  test("when its rows already fill the table", async () => {
    const fetchMore = vi.fn(() => new Promise(() => {}));
    renderTable(loaded(10, true), { fetchMore });
    await settle();

    rendered().totalListHeightChanged?.(1000);

    expect(fetchMore).not.toHaveBeenCalled();
  });

  test("when there are no more pages", async () => {
    const fetchMore = vi.fn(() => new Promise(() => {}));
    renderTable(loaded(10, false), { fetchMore });
    await settle();

    rendered().totalListHeightChanged?.(400);

    expect(fetchMore).not.toHaveBeenCalled();
  });
});
