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
// 2026.1.x runs no Vitest: these tests use the Jest globals, as App.test.tsx does
import { act, fireEvent, render, screen } from "@testing-library/react";
import { ToastProvider, useToast } from "./ToastProvider";

function TwoToasts() {
  const toast = useToast();
  return (
    <button
      onClick={() => {
        toast({ displayType: "info", title: "first", content: "" });
        toast({ displayType: "info", title: "second", content: "" });
      }}
    >
      show
    </button>
  );
}

// fake timers freeze the clock, so both toasts are emitted in the same millisecond
beforeEach(() => {
  jest.useFakeTimers({ now: new Date("2026-10-13T10:00:00Z") });
});

afterEach(() => {
  jest.restoreAllMocks();
  jest.useRealTimers();
});

describe("two toasts emitted together", () => {
  test("stay visible for their whole duration", () => {
    render(
      <ToastProvider>
        <TwoToasts />
      </ToastProvider>,
    );
    fireEvent.click(screen.getByText("show"));

    act(() => jest.advanceTimersByTime(5999));
    expect(screen.getByText("first")).toBeInTheDocument();
    expect(screen.getByText("second")).toBeInTheDocument();

    act(() => jest.advanceTimersByTime(1));
    expect(screen.queryByText("first")).not.toBeInTheDocument();
    expect(screen.queryByText("second")).not.toBeInTheDocument();
  });

  test("closing one leaves the other open", () => {
    render(
      <ToastProvider>
        <TwoToasts />
      </ToastProvider>,
    );
    fireEvent.click(screen.getByText("show"));

    fireEvent.click(screen.getAllByRole("button", { name: "Close" })[0]);

    expect(screen.queryByText("first")).not.toBeInTheDocument();
    expect(screen.getByText("second")).toBeInTheDocument();
  });
});

test("the close timers are cleared when the provider unmounts", () => {
  const setTimeoutSpy = jest.spyOn(globalThis, "setTimeout");
  const clearTimeoutSpy = jest.spyOn(globalThis, "clearTimeout");
  const { unmount } = render(
    <ToastProvider>
      <TwoToasts />
    </ToastProvider>,
  );
  fireEvent.click(screen.getByText("show"));
  // MUI transitions leave timers of their own: only the 6 second close timers matter here
  const closeTimers = setTimeoutSpy.mock.calls.flatMap(([, delay], i) =>
    delay === 6000 ? [setTimeoutSpy.mock.results[i].value] : [],
  );

  unmount();

  expect(closeTimers.length).toBeGreaterThanOrEqual(2);
  for (const timer of closeTimers) {
    expect(clearTimeoutSpy).toHaveBeenCalledWith(timer);
  }
});
