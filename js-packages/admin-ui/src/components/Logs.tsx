import React from "react";
import ClayLayout from "@clayui/layout";
import { useParams } from "react-router-dom";
import { TableVirtuoso } from "react-virtuoso";

export function Logs() {
  const { podName } = useParams();
  if (!podName) throw new Error();
  const logs = useLogs(podName);
  const [scrollType, setScrollType] = React.useState<"auto" | "smooth">("auto");
  React.useEffect(() => {
    const timeoutId = setTimeout(() => {
      setScrollType("smooth");
    }, 2000);
    return () => clearTimeout(timeoutId);
  }, []);
  return (
    <ClayLayout.ContainerFluid view>
      <div className="text-light bg-dark text-3 text-monospace">
        <TableVirtuoso
          style={{ height: "80vh" }}
          data={logs}
          followOutput={(isAtBottom) => {
            if (isAtBottom) {
              return scrollType;
            } else {
              return false;
            }
          }}
          itemContent={(index, logLine) => {
            return (
              <React.Fragment>
                <td className="text-muted c-pl-sm-3 align-top text-right">{index + 1}</td>
                <td className="c-px-sm-3" style={{ whiteSpace: "pre-wrap", wordBreak: "break-all" }}>
                  {(() => {
                    try {
                      return JSON.stringify(JSON.parse(logLine), null, 2);
                    } catch (error) {
                      return logLine || "\n";
                    }
                  })()}
                </td>
              </React.Fragment>
            );
          }}
        />
      </div>
    </ClayLayout.ContainerFluid>
  );
}

function useLogs(podName: string) {
  const [logs, setLogs] = React.useState<Array<string>>([]);
  React.useEffect(() => {
    const eventSource = new EventSource(`/k8s/log/sse/${podName}?tail=200`);
    eventSource.addEventListener("message", (event) => {
      setLogs((logs) => [...logs, event.data]);
    });
    return () => {
      eventSource.close();
    };
  }, [podName]);
  return logs;
}
