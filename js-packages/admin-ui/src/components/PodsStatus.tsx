import React from "react";
import { Link } from "react-router-dom";
import { ContainerFluid } from "./Form";

export function PodsStatus() {
  const status = usePodsStatus();
  const data = React.useMemo(() => Object.values(status).sort((a, b) => a.serviceName.localeCompare(b.serviceName)), [status]);
  return (
    <ContainerFluid>
      <div className="row">
        {data.map(({ podName, serviceName, status }) => {
          return (
            <div key={serviceName} className="col-sm-6 col-md-4 col-lg-3">
              <div className="card">
                <div className="card-body">
                  <div className="card-row">
                    <div className="autofit-col autofit-col-expand">
                      <section className="autofit-section">
                        <div className="card-description card-title title">{serviceName}</div>
                        <div className="card-caption">
                          <span className={`label label-${getStatusDisplayType(status)}`}>{status}</span>
                        </div>
                      </section>
                    </div>
                  </div>
                  <Link to={`/logs/${podName}`} className="card-link">
                    Logs
                  </Link>
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </ContainerFluid>
  );
}

export function getStatusDisplayType(status: string) {
  switch (status) {
    case "Succeded":
    case "Running":
      return "success";
    case "Pending":
      return "warning";
    case "Failed":
    case "Evicted":
      return "danger";
    case "Terminating":
      return "danger";
    default:
      throw new Error();
  }
}

function usePodsStatus() {
  const [status, setStatus] = React.useState<Record<string, { serviceName: string; status: string; podName: string }>>({});

  const useIntervalAsync = (fn: () => Promise<unknown>, ms: number) => {
    const timeout = React.useRef<number>();
    const mountedRef = React.useRef(false);
    const run = React.useCallback(async () => {
      await fn();
      if (mountedRef.current) {
        timeout.current = window.setTimeout(run, ms);
      }
    }, [fn, ms]);
    React.useEffect(() => {
      mountedRef.current = true;
      run();
      return () => {
        mountedRef.current = false;
        window.clearTimeout(timeout.current);
      };
    }, [run]);
  };
  const updateState = React.useCallback(async () => {
    const response = await fetch("/api/k8s-client/k8s/get/pods");
    const data = await response.json();
    setStatus(data);
  }, []);
  useIntervalAsync(updateState, 3000);
  return status;
}
