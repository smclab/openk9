import { Link, useNavigate } from "react-router-dom";
import React from "react";
import ClayToolbar from "@clayui/toolbar";
import { ClayButtonWithIcon } from "@clayui/button";
import { EmptyPage } from "./EmptyPage";
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
import { ContainerFluid, ContainerFluidWithoutView, SimpleModal } from "./Form";
import { keycloak } from "./authentication";
import { useMutation, useQuery } from "@tanstack/react-query";
import { useRestClient } from "./queryClient";
import { MlPodResponse } from "../openapi-generated";

export function HuggingFaceCard() {
  const status = usePodsStatus();
  const { observer, onOpenChange, open } = useModal();
  const data = React.useMemo(
    () =>
      Object.values(status)
        .filter((a) => a && a.name)
        .sort((a, b) => {
          if (a.name && b.name) {
            return a.name.localeCompare(b.name);
          }
          return 0;
        }),
    [status]
  );

  const [name, setName] = React.useState("");
  const navigate = useNavigate();
  const showToast = useToast();
  const restClient = useRestClient();
  const delateMutation = useMutation(
    async () => {
      return await restClient.mlk8SResource.deleteApiK8SClientK8SDeleteMlModel(name);
    },
    {
      onSuccess: (data) => {
        showToast({ displayType: "info", title: "Data Index created", content: data.message });
      },
      onError: (error) => {
        showToast({ displayType: "info", title: "Error", content: "error" });
      },
    }
  );

  return (
    <React.Fragment>
      {open && (
        <SimpleModal
          observer={observer}
          labelContinue={"yes"}
          labelCancel={"cancel"}
          actionContinue={() => {
            delateMutation.mutate();
            onOpenChange(false);
          }}
          actionCancel={() => {
            onOpenChange(false);
          }}
          description="Are you sure you want to cancell it?"
        />
      )}
      <ClayToolbar light>
        <ContainerFluidWithoutView>
          <ClayToolbar.Nav>
            <ClayToolbar.Item expand></ClayToolbar.Item>
            <ClayToolbar.Item>
              <Link to={"configure-hugging-face"}>
                <ClayButtonWithIcon className={ClassNameButton} aria-label="" symbol="plus" small />
              </Link>
            </ClayToolbar.Item>
          </ClayToolbar.Nav>
        </ContainerFluidWithoutView>
      </ClayToolbar>
      {data.length === 0 && <EmptyPage message={" Empty Configuration. Create one"} link={"configure-hugging-face"} />}
      <ContainerFluid>
        <div className="row">
          {data.map(({ name, task, library, status }) => {
            return (
              <div key={name} className="col-sm-6 col-md-4 col-lg-3">
                <div className="card">
                  <div className="card-body">
                    <div className="card-row">
                      <div className="autofit-col autofit-col-expand" style={{ marginTop: "10px" }}>
                        <section className="autofit-section">
                          <div className="card-description card-title title">{name}</div>
                        </section>
                      </div>
                    </div>
                    <div className="card-row">
                      <div className="autofit-col autofit-col-expand" style={{ marginTop: "10px" }}>
                        <section className="autofit-section">
                          <div style={{ display: "flex" }}>
                            <GetIconLibrary name={library ?? ""} />
                            <div className="card-description card-subtitle subtitle" style={{ marginLeft: "7px" }}>
                              {library}
                            </div>
                          </div>
                        </section>
                      </div>
                    </div>
                    <div className="card-row">
                      <div className="autofit-col autofit-col-expand" style={{ marginTop: "10px" }}>
                        <section className="autofit-section">
                          <div style={{ display: "flex" }}>
                            <GetIconTask name={task ?? ""} />
                            <div className="card-description card-subtitle subtitle" style={{ marginLeft: "7px" }}>
                              {task}
                            </div>
                          </div>
                        </section>
                      </div>
                    </div>
                    <div className="card-row">
                      <div className="autofit-col autofit-col-expand" style={{ marginTop: "10px" }}>
                        <section className="autofit-section">
                          <div style={{ display: "flex" }}>
                            <div className="card-caption ">
                              <span className={`label label-${getStatusDisplayType(status ?? "")}`}>{status}</span>
                            </div>
                          </div>
                        </section>
                      </div>
                    </div>
                    <div className="card-row" style={{ marginTop: "12px" }}>
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
                              setName(name ?? "");
                              onOpenChange(true);
                            }}
                          >
                            remove
                          </Button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>
      </ContainerFluid>
    </React.Fragment>
  );
}

function usePodsStatus() {
  const [status, setStatus] = React.useState<Array<MlPodResponse>>([]);
  const restClient = useRestClient();

  const updateState = async () => {
    try {
      const response = await restClient.mlk8SResource.getApiK8SClientK8SGetPodsMl();
      setStatus(response);
    } catch (error) {
      console.error("Errore nell'ottenere lo stato dei pod:", error);
    }
  };

  React.useEffect(() => {
    const intervalId = setInterval(updateState, 3000);
    return () => clearInterval(intervalId);
  }, []);

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
