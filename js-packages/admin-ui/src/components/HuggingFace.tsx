import { ClaySelect } from "@clayui/form";
import React from "react";
import { Spacy } from "../wizards/Logo/Spacy";
import { Button, useModal } from "@clayui/core";
import { Trasformers } from "../wizards/Logo/Trasformers";
import { Fastai } from "../wizards/Logo/Fastai";
import { Flair } from "../wizards/Logo/Flair";
import { Stanza } from "../wizards/Logo/Stanza";
import { useNavigate } from "react-router-dom";
import { useToast } from "./ToastProvider";
import { ContainerFluid, CustomButtom, SimpleModal } from "./Form";

export function HuggingFace() {
  const [name, setName] = React.useState("");
  const [task, setTask] = React.useState("");
  const [library, setLibrary] = React.useState("");
  const [isInvalidateName, setIsInvalidateName] = React.useState(false);
  const [isInvalidateTask, setIsInvalidateTask] = React.useState(false);
  const [isInvalidateLibrary, setIsInvalidateLibrary] = React.useState(false);
  const { observer, onOpenChange, open } = useModal();
  const showToast = useToast();
  const navigate = useNavigate();
  return (
    <React.Fragment>
      {open && (
        <SimpleModal
          observer={observer}
          labelContinue={"yes"}
          labelCancel={"cancel"}
          actionContinue={() => {
            const json = JSON.parse(JSON.stringify(" name: " + name + " task: " + task + " library: " + library));
            const requestOptions = {
              method: "POST",
              headers: { "Content-Type": "application/json" },
              body: JSON.stringify({ pipelineName: task, modelName: name, tokenizerName: "string", library: library }),
            };
            fetch("/api/k8s-client/k8s/deploy-ml-model", requestOptions)
              .then((response) => response.json())
              .then((data) => {
                if (data?.status === "DANGER") {
                  showToast({ displayType: "danger", title: "error released", content: "" + data?.message });
                } else {
                  showToast({ displayType: "success", title: "successfully released", content: "" });
                }
              });
            onOpenChange(false);
            navigate(`/maching-learning/hugging-face-view`, { replace: true });
          }}
          actionCancel={() => {
            onOpenChange(false);
          }}
          description="Are you sure you want to release it?"
        />
      )}

      <ContainerFluid>
        <form
          className="sheet"
          onSubmit={(event) => {
            if (name !== "" && library !== "" && task !== "") {
              event.preventDefault();
              onOpenChange(true);
            } else {
              event.preventDefault();
              if (name === "") {
                setIsInvalidateName(true);
                setTimeout(() => {
                  setIsInvalidateName(false);
                }, 3000);
              }
              if (task === "") {
                setIsInvalidateTask(true);
                setTimeout(() => {
                  setIsInvalidateTask(false);
                }, 3000);
              }
              if (library === "") {
                setIsInvalidateLibrary(true);
                setTimeout(() => {
                  setIsInvalidateLibrary(false);
                }, 3000);
              }
            }
          }}
        >
          <div className="form-group-item">
            <label style={{ paddingTop: "18px" }}>Name</label>
            <input
              type="text"
              className="form-control"
              value={name}
              onChange={(event) => {
                setName(event.currentTarget.value);
              }}
            ></input>
            {isInvalidateName && (
              <div className="form-feedback-group has-warning">
                <div className="form-feedback-item">{"must not be empty"}</div>
              </div>
            )}
          </div>

          <div className="form-group-item">
            <label style={{ paddingTop: "18px" }}>Library:</label>
            <div>
              <Button
                displayType={null}
                className={library === "spacy" ? "btn-info" : ""}
                onClick={(event) => {
                  if (library === "spacy") {
                    setLibrary("");
                  } else {
                    setLibrary("spacy");
                  }
                }}
              >
                <Spacy />
                Spacy
              </Button>
              <Button
                displayType={null}
                className={library === "transformers" ? "btn-info" : ""}
                onClick={(event) => {
                  if (library === "transformers") {
                    setLibrary("");
                  } else {
                    setLibrary("transformers");
                  }
                }}
              >
                <Trasformers />
                <span style={{ marginLeft: "10px" }}> Transformers</span>
              </Button>
              <Button
                displayType={null}
                className={library === "flair" ? "btn-info" : ""}
                onClick={(event) => {
                  if (library === "flair") {
                    setLibrary("");
                  } else {
                    setLibrary("flair");
                  }
                }}
              >
                <Flair />
                Flair
              </Button>
              <Button
                displayType={null}
                className={library === "stanza" ? "btn-info" : ""}
                onClick={(event) => {
                  if (library === "stanza") {
                    setLibrary("");
                  } else {
                    setLibrary("stanza");
                  }
                }}
              >
                <Stanza />
                Stanza
              </Button>
              <Button
                displayType={null}
                className={library === "fastai" ? "btn-info" : ""}
                onClick={(event) => {
                  if (library === "fastai") {
                    setLibrary("");
                  } else {
                    setLibrary("fastai");
                  }
                }}
              >
                <Fastai />
                Fastai
              </Button>
            </div>
          </div>
          {isInvalidateLibrary && (
            <div className="form-feedback-group has-warning">
              <div className="form-feedback-item">{"please choose library"}</div>
            </div>
          )}
          <div className="form-group-item">
            <label style={{ paddingTop: "18px" }}>Task</label>
            <ClaySelect
              defaultValue={task}
              className={library === "task" ? "btn-info" : ""}
              onClick={(event) => {
                setTask(event.currentTarget.value);
              }}
            >
              <ClaySelect.Option label={"select task"} value={""} />
              <ClaySelect.Option label={"text classification"} value={"text-classification"} />
              <ClaySelect.Option label={"token classification"} value={"token-classification"} />
              <ClaySelect.Option label={"object detection"} value={"object-detection"} />
              <ClaySelect.Option label={"image classifcation"} value={"image-classifcation"} />
              <ClaySelect.Option label={"summarizzation"} value={"summarizzation"} />
              <ClaySelect.Option label={"translation"} value={"translation"} />
              <ClaySelect.Option label={"audio speech recognition"} value={"audio-speech-recognition"} />
            </ClaySelect>
          </div>
          {isInvalidateTask && (
            <div className="form-feedback-group has-warning">
              <div className="form-feedback-item">{"please choose task"}</div>
            </div>
          )}
          <div className="sheet-footer">
            <CustomButtom nameButton={"Deploy"} typeSelectet="submit" />
          </div>
        </form>
      </ContainerFluid>
    </React.Fragment>
  );
}
