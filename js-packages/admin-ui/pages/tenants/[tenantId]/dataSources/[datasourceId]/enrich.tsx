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
import useSWR, { mutate } from "swr";
import { createUseStyles } from "react-jss";
import { useRouter } from "next/router";
import ClayIcon from "@clayui/icon";
import { firstOrString, noop, ThemeType } from "@openk9/search-ui-components";
import {
  changeEnrichItem,
  DataSourceInfo,
  deleteEnrichItem,
  EnrichItem,
  EnrichPipeline,
  getDataSourceInfo,
  getEnrichItem,
  getEnrichPipeline,
  getPlugins,
  PluginInfo,
  postEnrichItem,
  postEnrichPipeline,
} from "@openk9/http-api";
import { Layout } from "../../../../../components/Layout";
import { isServer, useLoginCheck, useLoginInfo } from "../../../../../state";
import { DataSourceNavBar } from "../../../../../components/DataSourceNavBar";
import { JSONView } from "../../../../../components/JSONView";
import { EditEnrichItem } from "../../../../../components/EditEnrichItem";
import { EnrichPipelineReorderStack } from "../../../../../components/EnrichItemReorderStack";
import { useToast } from "../../../../_app";
import { ConfirmationModal } from "../../../../../components/ConfirmationModal";

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
  respWrap: {
    display: "flex",
    flexWrap: "wrap",
    gap: theme.spacingUnit * 3,
    marginTop: "2em",
  },
  grow: {
    flexGrow: 1,
    maxWidth: "694px",
  },
  detailTitle: {
    display: "flex",
    alignItems: "center",
    "& h5": { flexGrow: 1 },
    "& button": { marginLeft: theme.spacingUnit },
  },
}));

function NoEnrichPipelineMessage({
  datasource,
}: {
  datasource: DataSourceInfo;
}) {
  const loginInfo = useLoginInfo();
  async function createEnrichPipeline() {
    if (!datasource) return;

    const newPipeline: Omit<EnrichPipeline, "enrichPipelineId"> = {
      active: true,
      datasourceId: datasource.datasourceId,
      name: `${datasource.datasourceId}-${datasource.name}-pipeline`,
    };
    postEnrichPipeline(newPipeline, loginInfo);
    mutate(`/api/v2/enrichPipeline`, (pipelines: EnrichPipeline[] | null) =>
      pipelines
        ? [...pipelines, { ...newPipeline, enrichPipelineId: -1 }]
        : [{ ...newPipeline, enrichPipelineId: -1 }],
    );
  }
  return (
    <>
      <h2>
        {datasource.datasourceId}: {datasource.name}
      </h2>
      <p>This datasource currently doesn&apos;t have an enrich pipeline.</p>
      <button className="btn btn-primary" onClick={createEnrichPipeline}>
        Create a new one
      </button>
    </>
  );
}

function EnrichItemShow({
  selectedEnrich,
  setSelectedEnrichId,
  pluginInfos,
  editing,
  setEditing,
}: {
  selectedEnrich: EnrichItem;
  setSelectedEnrichId(id: number | null): void;
  pluginInfos: PluginInfo[];
  editing: EnrichItem | null;
  setEditing(
    fn: EnrichItem | null | ((ei: EnrichItem | null) => EnrichItem | null),
  ): void;
}) {
  const classes = useStyles();

  const { pushToast } = useToast();
  const loginInfo = useLoginInfo();

  async function handleSave() {
    if (editing) {
      const prev = selectedEnrich;
      const newEnrichItem: Partial<EnrichItem> = {};

      Object.keys(prev).forEach((key) => {
        const k = key as keyof EnrichItem;
        if (prev[k] !== editing[k]) {
          (newEnrichItem[k] as EnrichItem[typeof k]) = editing[k];
        }
      });

      if (Object.entries(newEnrichItem).length !== 0) {
        const saved = await changeEnrichItem(
          prev.enrichItemId,
          newEnrichItem,
          loginInfo,
        );
        pushToast(`The datasource has been updated`);
        mutate(`/api/v2/enrichItem`, (eis: EnrichItem[]) => [
          ...eis.filter((ei) => ei.enrichItemId !== saved.enrichItemId),
          saved,
        ]);

        setEditing(null);
      }
    }
  }

  const [deleteModalV, setDeleteModalV] = useState(false);
  async function doDelete() {
    await deleteEnrichItem(selectedEnrich.enrichItemId, loginInfo);
    pushToast(`Enrich Item Deleted`);
    setSelectedEnrichId(null);
    mutate(`/api/v2/enrichItem`, (eis: EnrichItem[]) =>
      eis.filter((ei) => ei.enrichItemId !== selectedEnrich.enrichItemId),
    );
  }

  async function handleAbort() {
    if (!editing || editing?.serviceName) {
      setEditing(null);
    } else {
      await deleteEnrichItem(editing.enrichItemId, loginInfo);
      setSelectedEnrichId(null);
      setEditing(null);
      mutate(`/api/v2/enrichItem`, (eis: EnrichItem[]) =>
        eis.filter((ei) => ei.enrichItemId !== editing.enrichItemId),
      );
    }
  }

  return editing ? (
    <EditEnrichItem
      selectedEnrich={selectedEnrich}
      pluginInfos={pluginInfos}
      editing={editing}
      setEditing={setEditing}
      onSave={handleSave}
      onAbort={handleAbort}
    />
  ) : (
    <div className={classes.grow}>
      <div className={classes.detailTitle}>
        <h5>Item Configuration: {selectedEnrich.name}</h5>
        <button
          className="btn btn-secondary"
          type="button"
          onClick={() => setDeleteModalV(true)}
        >
          <span className="inline-item inline-item-before">
            <ClayIcon symbol="trash" />
          </span>
          Delete
        </button>
        <button
          className="btn btn-secondary"
          type="button"
          onClick={() => setEditing(selectedEnrich)}
        >
          <span className="inline-item inline-item-before">
            <ClayIcon symbol="pencil" />
          </span>
          Edit
        </button>
      </div>
      <div>
        <strong>Status:</strong>{" "}
        {selectedEnrich.active ? (
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
        <strong>Position:</strong> {selectedEnrich._position}
      </div>
      <div>
        <strong>Service Name:</strong> {selectedEnrich.serviceName}
      </div>
      <div>
        <strong>JSON Configuration</strong>
        <JSONView jsonString={selectedEnrich.jsonConfig} />
      </div>

      {deleteModalV && (
        <ConfirmationModal
          title="Delete"
          message="Are you sure you want to delete this enrich item?"
          abortText="Abort"
          confirmText="Delete"
          onCloseModal={() => setDeleteModalV(false)}
          onConfirmModal={doDelete}
        />
      )}
    </div>
  );
}

function Inner({ datasourceId }: { datasourceId: number }) {
  const classes = useStyles();

  const loginInfo = useLoginInfo();

  const { data: datasource } = useSWR(
    `/api/v2/datasource/${datasourceId}`,
    () => getDataSourceInfo(datasourceId, loginInfo),
  );

  const { data: pluginInfos } = useSWR(`/api/v1/plugin`, () =>
    getPlugins(loginInfo),
  );

  const { data: enrichPipelines } = useSWR(`/api/v2/enrichPipeline`, () =>
    getEnrichPipeline(loginInfo),
  );

  const { data: enrichItem } = useSWR(`/api/v2/enrichItem`, () =>
    getEnrichItem(loginInfo),
  );

  const [selectedEnrichId, setSelectedEnrichId] = useState<number | null>(null);

  const [editing, setEditing] = useState<EnrichItem | null>(null);

  if (!datasource || !pluginInfos || !enrichItem) {
    return <span className="loading-animation" />;
  }

  const dsEnrichPipeline =
    enrichPipelines &&
    enrichPipelines.filter((e) => e.datasourceId === datasourceId)[0];

  if (!dsEnrichPipeline) {
    return <NoEnrichPipelineMessage datasource={datasource} />;
  }

  const dsEnrichItems =
    (dsEnrichPipeline &&
      enrichItem.filter(
        (e) => e.enrichPipelineId === dsEnrichPipeline.enrichPipelineId,
      )) ||
    [];

  const selectedEnrich = dsEnrichItems.find(
    (ei) => ei.enrichItemId === selectedEnrichId,
  );

  async function handleAdd() {
    if (!dsEnrichPipeline) return;

    const newPosition = dsEnrichItems[dsEnrichItems.length - 1]
      ? dsEnrichItems[dsEnrichItems.length - 1]._position + 1
      : 0;

    const emptyEnrichItem = {
      active: false,
      enrichPipelineId: dsEnrichPipeline?.enrichPipelineId,
      jsonConfig: "{}",
      name: "New Enrich Pipeline",
      serviceName: "",
      _position: newPosition,
    };

    const result = await postEnrichItem(emptyEnrichItem, loginInfo);
    setSelectedEnrichId(result.enrichItemId);
    setEditing(result);
    mutate(`/api/v2/enrichItem`);
  }

  return (
    <div>
      <h2>
        {datasource.datasourceId}: {datasource.name}
      </h2>

      <div className={classes.respWrap}>
        {!isServer && (
          <Suspense fallback={<span className="loading-animation" />}>
            <EnrichPipelineReorderStack
              dsEnrichItems={dsEnrichItems}
              selectedEnrichId={selectedEnrichId}
              setSelectedEnrichId={editing ? noop : setSelectedEnrichId}
              pluginInfos={pluginInfos}
              onAdd={handleAdd}
              editing={Boolean(editing)}
            />
          </Suspense>
        )}

        {selectedEnrich && !isServer ? (
          <Suspense fallback={<span className="loading-animation" />}>
            <EnrichItemShow
              selectedEnrich={selectedEnrich}
              pluginInfos={pluginInfos}
              editing={editing}
              setEditing={setEditing}
              setSelectedEnrichId={setSelectedEnrichId}
            />
          </Suspense>
        ) : (
          <div>Select an enrich item to configure.</div>
        )}
      </div>
    </div>
  );
}

function DSEnrich() {
  const classes = useStyles();

  const { query } = useRouter();
  const tenantId = query.tenantId && firstOrString(query.tenantId);
  const datasourceId = query.datasourceId && firstOrString(query.datasourceId);

  const { loginValid } = useLoginCheck();
  if (!loginValid) return <span className="loading-animation" />;

  if (!tenantId || !datasourceId) return null;

  return (
    <>
      <Layout
        breadcrumbsPath={[
          { label: "Tenants", path: "/tenants" },
          { label: tenantId },
          { label: "DataSources", path: `/tenants/${tenantId}/dataSources` },
          { label: datasourceId },
          { label: "Enrich", path: `/tenants/${tenantId}/dataSources/enrich` },
        ]}
        breadcrumbsControls={
          <DataSourceNavBar
            tenantId={parseInt(tenantId)}
            datasourceId={parseInt(datasourceId)}
          />
        }
      >
        <div className={classes.root}>
          <Inner datasourceId={parseInt(datasourceId)} />
        </div>
      </Layout>
    </>
  );
}

export default DSEnrich;
