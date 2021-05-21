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

import React, { forwardRef, HTMLAttributes, Suspense, useState } from "react";
import clsx from "clsx";
import useSWR from "swr";
import { createUseStyles } from "react-jss";
import { useRouter } from "next/router";
import ClayIcon from "@clayui/icon";
import { ParentSize } from "@vx/responsive";
import {
  DragDropContext,
  Droppable,
  Draggable,
  DropResult,
  DraggableProvidedDragHandleProps,
  DraggableProvidedDraggableProps,
} from "react-beautiful-dnd";
import {
  firstOrString,
  pluginLoader,
  ThemeType,
} from "@openk9/search-ui-components";
import {
  DataSourceInfo,
  EnrichItem,
  EnrichPipeline,
  EnrichPlugin,
  getDataSourceInfo,
  getEnrichItem,
  getEnrichPipeline,
  getPlugins,
  PluginInfo,
  postEnrichPipeline,
  reorderEnrichItems,
} from "@openk9/http-api";
import { Layout } from "../../../../../components/Layout";
import { isServer, useLoginCheck, useLoginInfo } from "../../../../../state";
import { DataSourceNavBar } from "../../../../../components/DataSourceNavBar";

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
  enrichStack: {
    minWidth: 200,
    maxWidth: 300,
  },
  stackColumns: {
    display: "flex",
  },
  stackVertArrow: {
    width: 24,
    flexShrink: 0,
    marginLeft: 4,
  },
  stackContainer: {
    flexGrow: 1,
  },
  enrichItem: {
    margin: [theme.spacingUnit, 0],
    padding: [theme.spacingUnit, theme.spacingUnit * 2],
    border: "1px solid rgba(0,0,0,0.25)",
    borderRadius: theme.borderRadius,
    boxShadow: theme.shortBoxShadow,
    backgroundColor: "white",
  },
  enrichItemInput: {
    opacity: 0.5,
    padding: [theme.spacingUnit / 1, theme.spacingUnit * 2],
    fontWeight: 600,
    textTransform: "uppercase",
    letterSpacing: "0.1ch",
  },
  enrichItemOutput: {
    opacity: 0.5,
    padding: [theme.spacingUnit / 1, theme.spacingUnit * 2],
    fontWeight: 600,
    textTransform: "uppercase",
    letterSpacing: "0.1ch",
  },
  enrichItemSelected: {
    backgroundColor: theme.digitalLakePrimaryD2,
    color: "white",
  },
  json: {
    marginTop: "0.2rem",
    backgroundColor: theme.digitalLakeMainL2,
    color: "white",
    padding: theme.spacingUnit * 2,
    borderRadius: theme.borderRadius,
  },
}));

function NoEnrichPipelineMessage({
  datasource,
  mutateEnrichPipeline,
}: {
  datasource: DataSourceInfo;
  mutateEnrichPipeline: (
    data: (d: EnrichPipeline[] | undefined) => EnrichPipeline[],
  ) => void;
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
    mutateEnrichPipeline((pipelines) =>
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
      <p>This datasource currently doesn't have an enrich pipeline.</p>
      <button className="btn btn-primary" onClick={createEnrichPipeline}>
        Create a new one
      </button>
    </>
  );
}

const EnrichItemBlock = forwardRef<
  HTMLDivElement,
  {
    item: EnrichItem;
    selected: boolean;
    onSelect(): void;
    pluginInfos: PluginInfo[];
  } & DraggableProvidedDraggableProps &
    (DraggableProvidedDragHandleProps | {}) &
    HTMLAttributes<HTMLDivElement>
>(function EnrichItemBlock(
  { item, selected, onSelect, pluginInfos, ...rest },
  ref,
) {
  const classes = useStyles();

  const pluginInfo = (pluginInfos || []).find((p) =>
    item.serviceName.startsWith(p.bundleInfo.symbolicName),
  );
  const plugin = pluginInfo && pluginLoader.read(pluginInfo.pluginId);

  const enrichPlugin = plugin?.pluginServices.find(
    (ps) => ps.type === "ENRICH" && ps.serviceName === item.serviceName,
  ) as EnrichPlugin | null;

  const displayName =
    enrichPlugin?.displayName || item.serviceName.split(".").slice(-1)[0];
  const Icon = enrichPlugin?.iconRenderer || (() => null);

  return (
    <div
      className={clsx(
        classes.enrichItem,
        selected && classes.enrichItemSelected,
      )}
      onClick={onSelect}
      {...rest}
      ref={ref}
    >
      <ClayIcon symbol="drag" /> <Icon size={32} /> {displayName}
    </div>
  );
});

function EnrichPipelineReorderStack({
  dsEnrichItems,
  selectedEnrichId,
  setSelectedEnrichId,
  pluginInfos,
  mutateEnrichItems,
}: {
  dsEnrichItems: EnrichItem[];
  selectedEnrichId: number | null;
  setSelectedEnrichId(v: number | null): void;
  pluginInfos: PluginInfo[];
  mutateEnrichItems: (data?: EnrichItem[]) => void;
}) {
  const classes = useStyles();

  const loginInfo = useLoginInfo();

  async function dragEnd(result: DropResult) {
    if (!dsEnrichItems || !result.destination) return dsEnrichItems;

    const sorted = [...dsEnrichItems].sort((a, b) => a._position - b._position);
    const [removed] = sorted.splice(result.source.index, 1);
    sorted.splice(result.destination.index, 0, removed);
    const reordered: EnrichItem[] = sorted.map((s, i) => ({
      ...s,
      _position: i,
    }));

    await reorderEnrichItems(
      reordered.map((e) => e.enrichItemId),
      loginInfo,
    );

    mutateEnrichItems(reordered);
  }

  return (
    <div className={classes.enrichStack}>
      <h5>Enrich Pipeline</h5>

      <div className={classes.stackColumns}>
        <div className={classes.stackContainer}>
          <DragDropContext onDragEnd={dragEnd}>
            <Droppable droppableId="droppable">
              {(droppableProvided) => (
                <div ref={droppableProvided.innerRef}>
                  <div
                    className={clsx(
                      classes.enrichItem,
                      classes.enrichItemInput,
                    )}
                  >
                    Input
                  </div>
                  {dsEnrichItems
                    ?.sort((a, b) => a._position - b._position)
                    .map((item) => (
                      <Draggable
                        key={item.enrichItemId}
                        draggableId={item.enrichItemId.toString()}
                        index={item._position}
                      >
                        {(draggableProvided) => (
                          <EnrichItemBlock
                            item={item}
                            selected={item.enrichItemId == selectedEnrichId}
                            onSelect={() =>
                              setSelectedEnrichId(item.enrichItemId)
                            }
                            pluginInfos={pluginInfos}
                            style={draggableProvided.draggableProps.style}
                            ref={draggableProvided.innerRef}
                            {...draggableProvided.draggableProps}
                            {...draggableProvided.dragHandleProps}
                          />
                        )}
                      </Draggable>
                    ))}
                  {droppableProvided.placeholder}
                  <div
                    className={clsx(
                      classes.enrichItem,
                      classes.enrichItemOutput,
                    )}
                  >
                    Output
                  </div>
                </div>
              )}
            </Droppable>
          </DragDropContext>
        </div>

        <div className={classes.stackVertArrow}>
          <ParentSize>
            {({ width, height }) => (
              <svg width={width} height={height}>
                <defs>
                  <marker
                    id="arrowhead"
                    markerWidth={12}
                    markerHeight={8}
                    refX={12 / 2}
                    refY={8}
                  >
                    <polygon points={`0,0 ${12},${0} ${12 / 2},${8}`} />
                  </marker>
                </defs>
                <line
                  x1={12 / 2}
                  y1={8}
                  x2={12 / 2}
                  y2={height - 8}
                  stroke="black"
                  opacity={0.5}
                  strokeDasharray="4"
                  markerEnd="url(#arrowhead)"
                />
                <text
                  x={-height / 2}
                  y={width - 5}
                  fill="black"
                  transform="rotate(-90,0,0)"
                  fontSize={12}
                  textAnchor="middle"
                  opacity={0.5}
                  letterSpacing={0.8}
                  fontWeight={600}
                >
                  DATA
                </text>
              </svg>
            )}
          </ParentSize>
        </div>
      </div>
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

  const {
    data: enrichPipelines,
    mutate: mutateEnrichPipeline,
  } = useSWR(`/api/v2/enrichPipeline`, () => getEnrichPipeline(loginInfo));

  const {
    data: enrichItem,
    mutate: mutateEnrichItems,
  } = useSWR(`/api/v2/enrichItem`, () => getEnrichItem(loginInfo));

  const [selectedEnrichId, setSelectedEnrichId] = useState<number | null>(null);

  if (!datasource || !pluginInfos || !enrichItem) {
    return <span className="loading-animation" />;
  }

  const dsEnrichPipeline =
    enrichPipelines &&
    enrichPipelines.filter((e) => e.datasourceId === datasourceId)[0];

  if (!dsEnrichPipeline) {
    return (
      <NoEnrichPipelineMessage
        datasource={datasource}
        mutateEnrichPipeline={mutateEnrichPipeline}
      />
    );
  }

  const dsEnrichItems =
    dsEnrichPipeline &&
    enrichItem.filter(
      (e) => e.enrichPipelineId === dsEnrichPipeline.enrichPipelineId,
    );

  const selectedEnrich = dsEnrichItems.find(
    (ei) => ei.enrichItemId === selectedEnrichId,
  );

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
              setSelectedEnrichId={setSelectedEnrichId}
              pluginInfos={pluginInfos}
              mutateEnrichItems={mutateEnrichItems}
            />
          </Suspense>
        )}

        {selectedEnrich ? (
          <div>
            <h5>Item Configuration: {selectedEnrich.name}</h5>
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
              <pre className={classes.json}>
                {JSON.stringify(JSON.parse(selectedEnrich.jsonConfig), null, 4)}
              </pre>
            </div>
          </div>
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
