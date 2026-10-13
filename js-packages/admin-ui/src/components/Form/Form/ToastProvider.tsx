import React from "react";
import { Snackbar, Alert, AlertColor } from "@mui/material";

type ToastDefinition = {
  displayType: AlertColor;
  title: string;
  content: React.ReactNode;
};

const ToastContext = React.createContext<(params: ToastDefinition) => void>(null as any);

export function useToast() {
  const showToast = React.useContext(ToastContext);
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
      {toastItems.map(({ key, displayType, title, content }) => (
        <Snackbar
          key={key}
          open
          autoHideDuration={6000}
          onClose={() => {
            setToastItems((toastItems) => toastItems.filter((item) => item.key !== key));
          }}
          anchorOrigin={{ vertical: "bottom", horizontal: "right" }}
          sx={{ paddingBottom: "50px" }}
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
    </ToastContext.Provider>
  );
}
