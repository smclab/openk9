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
import { fireEvent, render, screen } from "@testing-library/react";
import { ModalProvider, useModal } from "./ModalProvider";

type Opener = { label: string; modals: Array<Parameters<ReturnType<typeof useModal>>[0]> };

function OpenModals({ label, modals }: Opener) {
  const showModal = useModal();
  return <button onClick={() => modals.forEach(showModal)}>{label}</button>;
}

// fake timers freeze the clock, so the modals are opened in the same millisecond
beforeEach(() => {
  jest.useFakeTimers({ now: new Date("2026-10-13T10:00:00Z") });
});

afterEach(() => {
  jest.useRealTimers();
});

describe("the modals", () => {
  test("run the callback when the button is pressed", () => {
    const callback = jest.fn();
    render(
      <ModalProvider>
        <OpenModals label="open" modals={[{ displayType: "success", title: "saved", callback }]} />
      </ModalProvider>,
    );
    fireEvent.click(screen.getByText("open"));

    fireEvent.click(screen.getByText("Close"));

    expect(callback).toHaveBeenCalledTimes(1);
  });

  test("opened together stay distinct: closing one leaves the other open", () => {
    render(
      <ModalProvider>
        <OpenModals
          label="open"
          modals={[
            { displayType: "info", title: "first" },
            { displayType: "info", title: "second" },
          ]}
        />
      </ModalProvider>,
    );
    fireEvent.click(screen.getByText("open"));

    fireEvent.click(screen.getAllByText("Close")[0]);

    expect(screen.queryByText("first")).not.toBeInTheDocument();
    expect(screen.getByText("second")).toBeInTheDocument();
  });
});
