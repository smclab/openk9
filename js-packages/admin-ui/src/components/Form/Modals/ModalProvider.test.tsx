// 3.0.x runs its tests on Jest (craco test): they use the Jest globals, as App.test.tsx does
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
