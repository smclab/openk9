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
import { useRouter } from "next/router";
import { format } from "date-fns";
import useSWR from "swr";
import ClayIcon from "@clayui/icon";
import { DataSourceInfo } from "@openk9/rest-api";
import { DataSourceNavBar } from "../../../../../components/DataSourceNavBar";
import { EditDataSource } from "../../../../../components/EditDataSource";
import { Layout } from "../../../../../components/Layout";
import { isServer } from "../../../../../state";
import { useToast } from "../../../../_app";
import { JSONView } from "../../../../../components/JSONView";
import { DSItemsCountShow } from "../../../../../components/DSItemsCountShow";
import { client } from "../../../../../components/client";
import { ThemeType } from "../../../../../components/theme";
import { firstOrString } from "../../../../../components/utils";

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
  dataList: {
    "& div": {
      marginBottom: "0.2rem",
    },
    marginBottom: "1rem",
  },
  settingHeader: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
  },
}));

function Inner({
  datasourceId,
  onSaveDataSource,
}: {
  datasourceId: number;
  onSaveDataSource(
    datasourceId: number,
    prevDataSource: DataSourceInfo,
    editingDataSource: DataSourceInfo,
  ): Promise<DataSourceInfo>;
}) {
  const classes = useStyles();

  const { data: datasource, mutate } = useSWR(
    `/api/v2/datasource/${datasourceId}`,
    () => client.getDataSourceInfo(datasourceId),
  );

  const [editingDataSource, setEditingDataSource] =
    useState<DataSourceInfo | null>(null);

  if (!datasource) {
    return <span className="loading-animation" />;
  }

  async function handleSave() {
    if (editingDataSource && datasource) {
      mutate(editingDataSource);
      mutate(
        await onSaveDataSource(datasourceId, datasource, editingDataSource),
      );
      setEditingDataSource(null);
    }
  }
  if (editingDataSource && !isServer) {
    return (
      <Suspense fallback={<span className="loading-animation" />}>
        <EditDataSource
          editingDataSource={editingDataSource}
          onChange={setEditingDataSource}
          onAbort={() => setEditingDataSource(null)}
          onSave={handleSave}
        />
      </Suspense>
    );
  }

  return (
    <>
      <div className={classes.settingHeader}>
        <h2>
          {datasource.datasourceId}: {datasource.name}
        </h2>
        <button
          className="btn btn-secondary"
          type="button"
          onClick={() => setEditingDataSource(datasource)}
        >
          <span className="inline-item inline-item-before">
            <ClayIcon symbol="pencil" />
          </span>
          Edit
        </button>
      </div>
      <div className={classes.dataList}>
        <div>
          <strong>Status:</strong>{" "}
          {datasource.active ? (
            <span className="label label-success">
              <span className="label-item label-item-expand">ENABLED</span>
            </span>
          ) : (
            <span className="label label-warning">
              <span className="label-item label-item-expand">DISABLED</span>
            </span>
          )}
        </div>
        <div>
          <strong>Items in Index:</strong>{" "}
          <DSItemsCountShow datasourceId={datasourceId} /> items
        </div>
        <div>
          <strong>Description:</strong> {datasource.description}
        </div>
        <div>
          <strong>Driver Service Name:</strong> {datasource.driverServiceName}
        </div>
        <div>
          <strong>Tenant Id:</strong> {datasource.tenantId}
        </div>
        <div>
          <strong>Last Ingestion Date:</strong>{" "}
          {(() => {
            try {
              return format(
                new Date(datasource.lastIngestionDate),
                "dd/MM/yyyy, HH:mm",
              );
            } catch (error) {
              return datasource.lastIngestionDate;
            }
          })()}
        </div>
        <div>
          <strong>Scheduling:</strong> {datasource.scheduling}
        </div>
      </div>

      <h5>JSON Configuration</h5>
      <JSONView jsonString={datasource.jsonConfig} />
    </>
  );
}

function DSSettings() {
  const classes = useStyles();

  const { query } = useRouter();
  const tenantId = query.tenantId && firstOrString(query.tenantId);
  const datasourceId = query.datasourceId && firstOrString(query.datasourceId);
  const datasourceInt = parseInt(datasourceId || "NaN");

  const { pushToast } = useToast();

  if (isNaN(datasourceInt) || !tenantId || !datasourceId) {
    return null;
  }

  async function saveDataSource(
    datasourceId: number,
    prevDataSource: DataSourceInfo,
    editedDatasource: DataSourceInfo,
  ) {
    const newDatasource: Partial<DataSourceInfo> = {};

    Object.keys(prevDataSource).forEach((key) => {
      const k = key as keyof DataSourceInfo;
      if (prevDataSource[k] !== editedDatasource[k]) {
        (newDatasource[k] as DataSourceInfo[typeof k]) = editedDatasource[k];
      }
    });

    if (Object.entries(newDatasource).length !== 0) {
      const saved = await client.changeDataSourceInfo(
        datasourceId,
        newDatasource,
      );
      pushToast(`The datasource has been updated`);
      return saved;
    }

    return prevDataSource;
  }

  return (
    <>
      <Layout
        breadcrumbsPath={[
          { label: "Tenants", path: "/tenants" },
          { label: tenantId },
          { label: "DataSources", path: `/tenants/${tenantId}/dataSources` },
          { label: datasourceId },
          { label: "Settings", path: `/tenants/${tenantId}/dataSources` },
        ]}
        breadcrumbsControls={
          <DataSourceNavBar
            tenantId={parseInt(tenantId)}
            datasourceId={datasourceInt}
          />
        }
      >
        <div className={classes.root}>
          <Inner
            datasourceId={datasourceInt}
            onSaveDataSource={saveDataSource}
          />
        </div>
      </Layout>
    </>
  );
}

export default DSSettings;
