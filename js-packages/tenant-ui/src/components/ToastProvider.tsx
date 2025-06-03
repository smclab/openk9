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
  const [toastItems, setToastItems] = React.useState<Array<{ key: string } & ToastDefinition>>([]);

  const addToast = React.useCallback(({ displayType, title, content }: ToastDefinition) => {
    const key = new Date().toJSON();
    setToastItems((toastItems) => [...toastItems, { key, displayType, title, content }]);
    setTimeout(() => {
      setToastItems((toastItems) => toastItems.filter((item) => item.key !== key));
    }, 6000);
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
