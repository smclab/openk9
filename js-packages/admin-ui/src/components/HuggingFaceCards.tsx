import ClayCard from "@clayui/card";
import ClayLayout from "@clayui/layout";
import { Link, useNavigate } from "react-router-dom";
import React from "react";
import ClayToolbar from "@clayui/toolbar";
import { ClayButtonWithIcon } from "@clayui/button";
import { EmptyPage } from "./EmptyPage";
import ClayLabel from "@clayui/label";
import { getStatusDisplayType } from "./PodsStatus";
import { Button, useModal } from "@clayui/core";
import { Trasformers } from "../wizards/Logo/Trasformers";
import { Flair } from "../wizards/Logo/Flair";
import { Spacy } from "../wizards/Logo/Spacy";
import { Stanza } from "../wizards/Logo/Stanza";
import { Fastai } from "../wizards/Logo/Fastai";
import { TextClassification } from "../wizards/Logo/TextClassification";
import { TokenClassification } from "../wizards/Logo/TokenClassification";
import { ObjectDetection } from "../wizards/Logo/ObjectDetection";
import { ImageClassification } from "../wizards/Logo/ImageClassification";
import { Summarization } from "../wizards/Logo/Summarization";
import { Translation } from "../wizards/Logo/Traslation";
import { AudioClassification } from "../wizards/Logo/AudioClassification";
import { ClassNameButton } from "../App";
import { useToast } from "./ToastProvider";
import { SimpleModal } from "./Form";

export function HuggingFaceCard() {
  const status = usePodsStatus();
  const { observer, onOpenChange, open } = useModal();
  const data = React.useMemo(() => Object.values(status).sort((a, b) => a.name.localeCompare(b.name)), [status]);
  const [name, setName] = React.useState("");
  const navigate = useNavigate();
  const showToast = useToast();
  return (
    <React.Fragment>
      {open && (
        <SimpleModal
          observer={observer}
          labelContinue={"yes"}
          labelCancel={"cancel"}
          actionContinue={() => {
            fetch(`/api/k8s-client/k8s/delete-ml-model/${name}`, { method: "DELETE" })
              .then((response) => {
                if (response.ok) {
                  showToast({ displayType: "success", title: "delete done", content: "" });
                } else {
                  showToast({ displayType: "danger", title: "Error", content: "" });
                }
              })
              .then((responseJson) => {})
              .catch((error) => {
                console.log(error);
              });
            onOpenChange(false);
          }}
          actionCancel={() => {
            onOpenChange(false);
          }}
          description="Are you sure you want to cancell it?"
        />
      )}
      <ClayToolbar light>
        <ClayLayout.ContainerFluid>
          <ClayToolbar.Nav>
            <ClayToolbar.Item expand></ClayToolbar.Item>
            <ClayToolbar.Item>
              <Link to={"configure-hugging-face"}>
                <ClayButtonWithIcon className={ClassNameButton} aria-label="" symbol="plus" small />
              </Link>
            </ClayToolbar.Item>
          </ClayToolbar.Nav>
        </ClayLayout.ContainerFluid>
      </ClayToolbar>
      {data.length === 0 && <EmptyPage message={" Empty Configuration. Create one"} link={"configure-hugging-face"} />}
      <ClayLayout.ContainerFluid view>
        <div className="row">
          {data.map(({ name, task, library, status }) => {
            return (
              <div key={name} className="col-sm-6 col-md-4 col-lg-3">
                <ClayCard>
                  <ClayCard.Body>
                    <ClayCard.Row>
                      <div className="autofit-col autofit-col-expand" style={{ marginTop: "10px" }}>
                        <section className="autofit-section">
                          <ClayCard.Description displayType="title" style={{}}>
                            {name}
                          </ClayCard.Description>
                        </section>
                      </div>
                    </ClayCard.Row>
                    <ClayCard.Row>
                      <div className="autofit-col autofit-col-expand" style={{ marginTop: "10px" }}>
                        <section className="autofit-section">
                          <div style={{ display: "flex" }}>
                            <GetIconLibrary name={library} />
                            <ClayCard.Description displayType="subtitle" style={{ marginLeft: "7px" }}>
                              {library}
                            </ClayCard.Description>
                          </div>
                        </section>
                      </div>
                    </ClayCard.Row>
                    <ClayCard.Row>
                      <div className="autofit-col autofit-col-expand" style={{ marginTop: "10px" }}>
                        <section className="autofit-section">
                          <div style={{ display: "flex" }}>
                            <GetIconTask name={task} />
                            <ClayCard.Description displayType="subtitle" style={{ marginLeft: "7px" }}>
                              {task}
                            </ClayCard.Description>
                          </div>
                        </section>
                      </div>
                    </ClayCard.Row>
                    <ClayCard.Row>
                      <div className="autofit-col autofit-col-expand" style={{ marginTop: "10px" }}>
                        <section className="autofit-section">
                          <div style={{ display: "flex" }}>
                            <ClayCard.Caption>
                              <ClayLabel displayType={getStatusDisplayType(status)}>{status}</ClayLabel>
                            </ClayCard.Caption>
                          </div>
                        </section>
                      </div>
                    </ClayCard.Row>
                    <ClayCard.Row style={{ marginTop: "12px" }}>
                      <div className="row">
                        <div className="col-sm-6">
                          <Button
                            className={ClassNameButton}
                            small
                            style={{ whiteSpace: "nowrap", textAlign: "center" }}
                            onClick={() => {
                              navigate(`/maching-learning/hugging-face-view/enrich-item/${name}`, { replace: true });
                            }}
                          >
                            create enrich item
                          </Button>
                        </div>
                        <div className="col-sm-6">
                          <Button
                            className={ClassNameButton}
                            style={{ marginLeft: "10px" }}
                            small
                            onClick={() => {
                              setName(name);
                              onOpenChange(true);
                            }}
                          >
                            remove
                          </Button>
                        </div>
                      </div>
                    </ClayCard.Row>
                  </ClayCard.Body>
                </ClayCard>
              </div>
            );
          })}
        </div>
      </ClayLayout.ContainerFluid>
    </React.Fragment>
  );
}

function usePodsStatus() {
  const [status, setStatus] = React.useState<Record<string, { name: string; task: string; library: string; status: string }>>({});

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
    const response = await fetch("/api/k8s-client/k8s/get/pods/ml");
    const data = await response.json();
    setStatus(data);
  }, []);
  useIntervalAsync(updateState, 3000);
  return status;
}

function GetIconLibrary({ name }: { name: string }) {
  switch (name) {
    case "spacy":
      return <Spacy />;
    case "transformer":
      return <Trasformers />;
    case "flair":
      return <Flair />;
    case "stanza":
      return <Stanza />;
    case "fastai":
      return <Fastai />;
    default:
      return <Trasformers />;
  }
}

function GetIconTask({ name }: { name: string }) {
  switch (name) {
    case "text-classification":
      return <TextClassification />;
    case "token-classification":
      return <TokenClassification />;
    case "object-detection":
      return <ObjectDetection />;
    case "image-classification":
      return <ImageClassification />;
    case "summarizzation":
      return <Summarization />;
    case "translation":
      return <Translation />;
    case "audio-classification":
      return <AudioClassification />;
    default:
      return <TextClassification />;
  }
}
