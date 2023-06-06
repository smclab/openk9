import Button from "@clayui/button";
import { useModal } from "@clayui/core";
import ClayToolbar from "@clayui/toolbar";
import { useQuery } from "@tanstack/react-query";
import { useParams } from "react-router-dom";
import { useRestClient } from "./queryClient";
import { useToast } from "./ToastProvider";
import React from "react";
import { ClassNameButton } from "../App";
import { ContainerFluid } from "./Form";

export function DocumentTypeMappings() {
  const restClient = useRestClient();
  const { documentTypeId } = useParams();
  const showToast = useToast();
  const modal = useModal();

  async function copy(text: string) {
    await navigator.clipboard.writeText(text);
    showToast({ displayType: "success", title: "copy successful", content: "" });
    modal.onClose();
  }

  const documentTypeMappingsQuery = useQuery([documentTypeId], async () => {
    return await restClient.dataIndexResource.postApiDatasourceV1DataIndexGetMappingsFromDocTypes({ docTypeIds: [Number(documentTypeId)] });
  });
  return (
    <React.Fragment>
      <ClayToolbar light>
        <ContainerFluid>
          <ClayToolbar.Nav>
            <ClayToolbar.Item expand></ClayToolbar.Item>
            <ClayToolbar.Item>
              <Button
                className={`${ClassNameButton} btn-sm`}
                onClick={() => copy(JSON.stringify(documentTypeMappingsQuery.data, null, 2))}
                disabled={!documentTypeMappingsQuery.data}
              >
                Copy To Clipboard
              </Button>
            </ClayToolbar.Item>
          </ClayToolbar.Nav>
        </ContainerFluid>
      </ClayToolbar>
      <ContainerFluid>
        <textarea
          className="sheet form-control"
          style={{ height: "80vh" }}
          readOnly
          value={JSON.stringify(documentTypeMappingsQuery.data, null, 2)}
        />
      </ContainerFluid>
    </React.Fragment>
  );
}
