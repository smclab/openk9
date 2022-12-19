import React from "react";
import ClayAlert, { DisplayType } from "@clayui/alert";

type ToastDefinition = { displayType: DisplayType; title: string; content: React.ReactNode };

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
  }, []);
  return (
    <ToastContext.Provider value={addToast}>
      {children}
      <ClayAlert.ToastContainer>
        {toastItems.map(({ key, displayType, title, content }) => (
          <ClayAlert
            key={key}
            displayType={displayType}
            autoClose={5000}
            onClose={() => {
              setToastItems((toastItems) => toastItems.filter((item) => item.key !== key));
            }}
            title={title}
          >
            {content}
          </ClayAlert>
        ))}
      </ClayAlert.ToastContainer>
    </ToastContext.Provider>
  );
}
