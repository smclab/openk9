/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import React, { Suspense, useState } from "react";
import { createUseStyles } from "react-jss";
import clsx from "clsx";
import { useRouter } from "next/router";
import useSWR, { mutate } from "swr";
import { ClayInput } from "@clayui/form";
import { Tenant } from "@openk9/rest-api";
import { Layout } from "../../../components/Layout";
import { isServer } from "../../../state";
import { useToast } from "../../_app";
import { JSONView } from "../../../components/JSONView";
import { client } from "../../../components/client";
import { ThemeType } from "../../../components/theme";
import { firstOrString } from "../../../components/utils";
import DefaultSettingsEditor from "../../../components/DefaultSettingsEditor";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    margin: [theme.spacingUnit * 2, "auto"],
    backgroundColor: "white",
    boxShadow: theme.baseBoxShadow,
    width: "100%",
    maxWidth: 1000,
    borderRadius: theme.borderRadius,
    overflow: "auto",
    padding: theme.spacingUnit * 2,
  },
  actions: {
    display: "flex",
    justifyContent: "flex-end",
  },
  settingHeader: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
  },
  dataList: {
    "& div": {
      marginBottom: "0.2rem",
    },
    marginBottom: "1rem",
  },
  editElement: {
    marginBottom: "16px",
  },
  buttons: {
    display: "flex",
    justifyContent: "flex-end",
  },
  closeButton: {
    marginRight: "16px",
  },
}));

function EditInside({
  data,
  setIsEditMode,
}: {
  data: Tenant;
  setIsEditMode(mode: boolean): void;
}) {
  const classes = useStyles();
  const { pushToast } = useToast();

  const [tenant, setTenant] = useState({ ...data });

  const handleChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value;
    const id = e.target.id;
    setTenant((cs) => ({
      ...cs,
      [id]: value,
    }));
  };

  const handleSave = async () => {
    await client.putTenant(tenant);
    pushToast("Success");
    setIsEditMode(false);
    mutate(`/api/v2/tenant/${tenant.tenantId}`);
  };

  if (!data) {
    return <span className="loading-animation" />;
  }

  return (
    <>
      <div className={classes.editElement}>
        <strong>Name:</strong>{" "}
        <ClayInput
          id="name"
          placeholder="Insert here the name"
          onChange={(event) => handleChange(event)}
          value={tenant.name}
          type="text"
        />
      </div>
      <div className={classes.editElement}>
        <strong>Description:</strong>
        <ClayInput
          id="virtualHost"
          placeholder="Insert here the VirtualHost"
          onChange={(event) => handleChange(event)}
          value={tenant.virtualHost}
          type="text"
        />
      </div>
      <div className={classes.editElement}>
        <strong>JSON Configuration</strong>
        <DefaultSettingsEditor
          currentSettings={tenant.jsonConfig}
          setCurrentSettings={(jsonConfig) =>
            setTenant((cs) => ({
              ...cs,
              jsonConfig,
            }))
          }
        />
      </div>
      <div className={classes.buttons}>
        <button
          className={clsx("btn btn-secondary", classes.closeButton)}
          type="button"
          onClick={() => setIsEditMode(false)}
        >
          Close without Save
        </button>
        <button
          className="btn btn-primary"
          type="button"
          onClick={() => handleSave()}
        >
          Save
        </button>
      </div>
    </>
  );
}

function Inside({ tenantId }: { tenantId: number }) {
  const classes = useStyles();

  const { data } = useSWR(`/api/v2/tenant/${tenantId}`, () =>
    client.getTenant(tenantId),
  );
  const [isEditMode, setIsEditMode] = useState(false);

  if (!data) {
    return <span className="loading-animation" />;
  }

  return (
    <>
      {isEditMode ? (
        <EditInside data={data} setIsEditMode={setIsEditMode} />
      ) : (
        <>
          <div className={classes.settingHeader}>
            <h2>{data.name}</h2>
            <button
              className="btn btn-primary"
              type="button"
              onClick={() => setIsEditMode(true)}
            >
              Edit Settings
            </button>
          </div>
          <div className={classes.dataList}>
            <div>
              <strong>Virtual Host:</strong> {data.virtualHost}
            </div>
          </div>

          <h5>Tenant Configuration</h5>
          <JSONView jsonString={data.jsonConfig} />
        </>
      )}
    </>
  );
}

function TenantSettings() {
  const classes = useStyles();

  const { query } = useRouter();
  const tenantId = query.tenantId && firstOrString(query.tenantId);

  if (!tenantId) return null;

  return (
    <>
      <Layout
        breadcrumbsPath={[
          { label: "Tenants", path: "/tenants" },
          { label: tenantId },
          { label: "Tenant Settings", path: `/tenants/${tenantId}/settings` },
        ]}
      >
        <div className={classes.root}>
          {!isServer && (
            <Suspense fallback={<span className="loading-animation" />}>
              <Inside tenantId={parseInt(tenantId)} />
            </Suspense>
          )}
        </div>
      </Layout>
    </>
  );
}

export default TenantSettings;
