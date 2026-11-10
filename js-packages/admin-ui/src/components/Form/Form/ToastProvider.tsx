import React from "react";
import { Snackbar, Alert, AlertColor, Stack } from "@mui/material";

type ToastDefinition = {
  displayType: AlertColor;
  title: string;
  content: React.ReactNode;
};

const ToastContext = React.createContext<((params: ToastDefinition) => void) | null>(null);

export function useToast() {
  const showToast = React.useContext(ToastContext);
  if (!showToast) {
    throw new Error("useToast must be used within a ToastProvider");
  }
  return showToast;
}

type ToastProviderProps = { children: React.ReactNode };
export function ToastProvider({ children }: ToastProviderProps) {
  const [toastItems, setToastItems] = React.useState<Array<{ key: number } & ToastDefinition>>([]);
  const nextKey = React.useRef(0);
  const timers = React.useRef(new Set<ReturnType<typeof setTimeout>>());

  React.useEffect(() => {
    const pending = timers.current;
    return () => pending.forEach((timer) => clearTimeout(timer));
  }, []);

  const addToast = React.useCallback(({ displayType, title, content }: ToastDefinition) => {
    const key = nextKey.current++;
    setToastItems((toastItems) => [...toastItems, { key, displayType, title, content }]);
    const timer = setTimeout(() => {
      timers.current.delete(timer);
      setToastItems((toastItems) => toastItems.filter((item) => item.key !== key));
    }, 6000);
    timers.current.add(timer);
  }, []);

  return (
    <ToastContext.Provider value={addToast}>
      {children}
      {/* one fixed column: each Snackbar is fixed on its own, so they would cover each other */}
      <Stack
        spacing={1}
        alignItems="flex-end"
        sx={{ position: "fixed", right: 24, bottom: 24, paddingBottom: "50px", zIndex: "snackbar" }}
      >
        {toastItems.map(({ key, displayType, title, content }) => (
          <Snackbar
            key={key}
            open
            autoHideDuration={6000}
            onClose={(_, reason) => {
              if (reason === "clickaway") return;
              setToastItems((toastItems) => toastItems.filter((item) => item.key !== key));
            }}
            sx={{ position: "static" }}
          >
            <Alert
              onClose={() => {
                setToastItems((toastItems) => toastItems.filter((item) => item.key !== key));
              }}
              severity={displayType}
              sx={{ width: "100%" }}
            >
              <strong>{title}</strong>
              <div>{content}</div>
            </Alert>
          </Snackbar>
        ))}
      </Stack>
    </ToastContext.Provider>
  );
}
