import Button from "@clayui/button";
import { useModal } from "@clayui/core";
import ClayToolbar from "@clayui/toolbar";
import { useLocation } from "react-router-dom";
import { useToast } from "./ToastProvider";
import React from "react";
import { ClassNameButton } from "../App";
import { ContainerFluid, ContainerFluidWithoutView } from "./Form";

export function DocumentTypesSettings() {
  const showToast = useToast();
  const modal = useModal();
  const { state } = useLocation();
  const { data } = state as any;

  async function copy(text: string) {
    await navigator.clipboard.writeText(text);
    showToast({ displayType: "success", title: "copy successful", content: "" });
    modal.onClose();
  }

  return (
    <React.Fragment>
      <ClayToolbar light>
        <ContainerFluidWithoutView>
          <ClayToolbar.Nav>
            <ClayToolbar.Item expand></ClayToolbar.Item>
            <ClayToolbar.Item>
              <Button className={`${ClassNameButton} btn-sm`} onClick={() => copy(JSON.stringify(data, null, 2))} disabled={!data}>
                Copy To Clipboard
              </Button>
            </ClayToolbar.Item>
          </ClayToolbar.Nav>
        </ContainerFluidWithoutView>
      </ClayToolbar>
      <ContainerFluid>
        <textarea className="sheet form-control" style={{ height: "80vh" }} readOnly value={JSON.stringify(data, null, 2)} />
      </ContainerFluid>
    </React.Fragment>
  );
}
