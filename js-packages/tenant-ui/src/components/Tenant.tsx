import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import ClayForm from "@clayui/form";
import { TextInputWithoutChange } from "./Form";
import ClayLayout from "@clayui/layout";
import { useTenantQuery } from "../graphql-generated";
import ClayToolbar from "@clayui/toolbar";
import { ClayButtonWithIcon } from "@clayui/button";

const TenantQuery = gql`
  query Tenant($id: ID!) {
    tenant(id: $id) {
      id
      realmName
      schemaName
      modifiedDate
      virtualHost
      clientSecret
      createDate
    }
  }
`;

export function Tenant() {
  const { tenantId = "new" } = useParams();
  const navigate = useNavigate();
  const tenantQuery = useTenantQuery({
    variables: { id: tenantId as string },
    skip: !tenantId || tenantId === "new",
  });

  return (
    <React.Fragment>
      <ClayToolbar light>
        <ClayLayout.ContainerFluid>
          <ClayToolbar.Nav>
            <ClayToolbar.Item>
              <ClayButtonWithIcon
                symbol="angle-left"
                small
                onClick={() => {
                  navigate(`/tenants/`, { replace: true });
                }}
              />
            </ClayToolbar.Item>
          </ClayToolbar.Nav>
        </ClayLayout.ContainerFluid>
      </ClayToolbar>

      <ClayLayout.ContainerFluid view>
        <ClayForm className="sheet">
          <TextInputWithoutChange label="Name" id={tenantId} value={tenantQuery.data?.tenant?.virtualHost || ""} readOnly={true} />
          <TextInputWithoutChange label="Real Name" id={tenantId} value={tenantQuery.data?.tenant?.realmName || ""} readOnly={true} />
          <TextInputWithoutChange label="Schema Name" id={tenantId} value={tenantQuery.data?.tenant?.schemaName || ""} readOnly={true} />
          <TextInputWithoutChange
            label="Client Secret"
            id={tenantId}
            value={tenantQuery.data?.tenant?.clientSecret || ""}
            readOnly={true}
          />
          <TextInputWithoutChange label="Create Date" id={tenantId} value={tenantQuery.data?.tenant?.createDate || ""} readOnly={true} />
          <TextInputWithoutChange label="Modify Date" id={tenantId} value={tenantQuery.data?.tenant?.modifiedDate || ""} readOnly={true} />
        </ClayForm>
      </ClayLayout.ContainerFluid>
    </React.Fragment>
  );
}
