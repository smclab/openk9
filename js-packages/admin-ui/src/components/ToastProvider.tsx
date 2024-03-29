import React from "react";
import "@clayui/css/lib/css/atlas.css";

type ToastDefinition = { displayType: string; title: string; content: React.ReactNode };

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
    setTimeout(function () {
      setToastItems((toastItems) => toastItems.filter((item) => item.key !== key));
    }, 5000);
  }, []);

  return (
    <ToastContext.Provider value={addToast}>
      {children}
      <div className="toast-container alert-notifications" style={{ position: "fixed", bottom: "20px", right: "20px", zIndex: "1" }}>
        {toastItems.map(({ key, displayType, title, content }) => {
          const classNames = `alert alert-dismissible alert-${displayType}`;
          return (
            <div className={classNames} role={displayType} style={{ minWidth: "250px" }} key={key}>
              <div role="alert" className="alert-autofit-row autofit-row">
                <div className="autofit-col autofit-col-expand">
                  <div className="autofit-section">
                    <strong className="lead">{title}</strong>
                    {content}
                  </div>
                </div>
              </div>
              <button
                aria-label="Close"
                className="close"
                data-dismiss="alert"
                type="button"
                onClick={() => {
                  setToastItems((toastItems) => toastItems.filter((item) => item.key !== key));
                }}
              >
                X
              </button>
            </div>
          );
        })}
      </div>
    </ToastContext.Provider>
  );
}
