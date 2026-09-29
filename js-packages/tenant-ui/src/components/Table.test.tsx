import { afterEach, beforeEach, expect, test, vi, type MockInstance } from "vitest";
import React from "react";
import { render, screen } from "@testing-library/react";
import { MemoryRouter } from "react-router-dom";
import { Table } from "./Table";

type Row = { id?: string | null; name?: string | null };
type Query = { items: { edges: Array<{ node: Row }> } };

// jsdom has no ResizeObserver, needed by react-virtuoso
class ResizeObserverStub {
  observe() {}
  unobserve() {}
  disconnect() {}
}

let consoleError: MockInstance<typeof console.error>;

beforeEach(() => {
  vi.stubGlobal("ResizeObserver", ResizeObserverStub);
  consoleError = vi.spyOn(console, "error").mockImplementation(() => {});
});

afterEach(() => {
  consoleError.mockRestore();
  vi.unstubAllGlobals();
});

function renderTable(rows: Row[]) {
  const queryResult = {
    data: { items: { edges: rows.map((node) => ({ node })) } },
    fetchMore: vi.fn(),
    refetch: vi.fn(),
  } as any;
  return render(
    <MemoryRouter>
      <Table<Query, Row, { searchText?: string | null; cursor?: string | null }>
        data={{ queryResult, field: (data) => data?.items }}
        columns={[{ header: "Name", content: (row) => row?.name }]}
        onCreatePath="/"
        onDelete={() => {}}
        rowActions={[{ label: "View", action: () => {} }]}
      />
    </MemoryRouter>
  );
}

// React's invalid nesting errors, worded differently in React 18 and 19
function nestingErrors() {
  return consoleError.mock.calls
    .map((args) => args.map(String).join(" "))
    .filter((message) => /validateDOMNesting|cannot (appear as|be) a child of/.test(message));
}

test("header cells sit in a row of the table head", () => {
  renderTable([{ id: "1", name: "first" }]);
  const header = screen.getByText("Actions").closest("th");
  expect(header?.parentElement?.tagName).toBe("TR");
  expect(header?.parentElement?.parentElement?.tagName).toBe("THEAD");
  expect(nestingErrors()).toEqual([]);
});

test("the empty placeholder row sits in a table body", () => {
  renderTable([]);
  const cell = screen.getByText("No entities").closest("td");
  expect(cell?.parentElement?.tagName).toBe("TR");
  expect(cell?.parentElement?.parentElement?.tagName).toBe("TBODY");
  expect(nestingErrors()).toEqual([]);
});
