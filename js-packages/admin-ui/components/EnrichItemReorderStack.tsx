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

import React from "react";
import { createUseStyles } from "react-jss";
import clsx from "clsx";
import ClayIcon from "@clayui/icon";
import { ParentSize } from "@vx/responsive";
import {
  DragDropContext,
  Droppable,
  Draggable,
  DropResult,
  DraggableProvided,
} from "react-beautiful-dnd";
import { pluginLoader, ThemeType } from "@openk9/search-ui-components";
import {
  EnrichItem,
  EnrichPlugin,
  PluginInfo,
  reorderEnrichItems,
} from "@openk9/http-api";
import { useLoginInfo } from "../state";
import { mutate } from "swr";

export const useStyles = createUseStyles((theme: ThemeType) => ({
  enrichStack: {
    minWidth: 250,
    maxWidth: 300,
  },
  stackColumns: {
    display: "flex",
  },
  stackVertArrow: {
    width: 24,
    flexShrink: 0,
    marginLeft: 8,
  },
  reorderColumn: {
    flexGrow: 1,
  },
  enrichItem: {
    margin: [theme.spacingUnit, 0],
    padding: [theme.spacingUnit, 4],
    border: "1px solid rgba(0,0,0,0.25)",
    borderRadius: theme.borderRadius,
    boxShadow: theme.shortBoxShadow,
    backgroundColor: "white",
    display: "flex",
    alignItems: "center",
    "& svg": { fill: "currentColor" },
  },
  handle: {
    margin: 0,
    marginRight: theme.spacingUnit / 2,
  },
  icon: {
    margin: 0,
    marginRight: theme.spacingUnit / 2,
    width: 32,
    height: "100%",
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
  },
  enrichItemInput: {
    opacity: 0.5,
    padding: [theme.spacingUnit / 1, theme.spacingUnit * 2],
    paddingLeft: 36,
    fontWeight: 600,
    textTransform: "uppercase",
    letterSpacing: "0.1ch",
    marginBottom: theme.spacingUnit * 2,
  },
  enrichItemOutputContainer: {
    marginTop: theme.spacingUnit * 2,
    position: "relative",
  },
  enrichItemOutput: {
    opacity: 0.5,
    padding: [theme.spacingUnit / 1, theme.spacingUnit * 2],
    paddingLeft: 36,
    fontWeight: 600,
    textTransform: "uppercase",
    letterSpacing: "0.1ch",
  },
  addButton: {
    fontSize: 12,
    padding: theme.spacingUnit / 2,
    position: "absolute",
    top: -theme.spacingUnit,
    right: theme.spacingUnit,

    "& *": {
      fontSize: 12,
    },
  },
  enrichItemSelected: {
    backgroundColor: theme.digitalLakePrimaryD2,
    color: "white",
  },
}));

function EnrichItemBlock({
  item,
  selected,
  onSelect,
  pluginInfos,
  draggableProvided,
}: {
  item: EnrichItem;
  selected: boolean;
  onSelect(): void;
  pluginInfos: PluginInfo[];
  draggableProvided: DraggableProvided;
}) {
  const classes = useStyles();

  const pluginInfo = (pluginInfos || []).find((p) =>
    item.serviceName.startsWith(p.bundleInfo.symbolicName),
  );
  const plugin = pluginInfo && pluginLoader.read(pluginInfo.pluginId, pluginInfo.bundleInfo.lastModified);

  const enrichPlugin = plugin?.pluginServices.find(
    (ps) => ps.type === "ENRICH" && ps.serviceName === item.serviceName,
  ) as EnrichPlugin | null;

  const displayName =
    enrichPlugin?.displayName || item.serviceName.split(".").slice(-1)[0];
  const Icon = enrichPlugin?.iconRenderer || null;

  return (
    <div
      role="button"
      className={clsx(
        classes.enrichItem,
        selected && classes.enrichItemSelected,
      )}
      onClick={onSelect}
      onKeyDown={(e) => e.key === "Enter" && onSelect()}
      tabIndex={item._position}
      style={draggableProvided.draggableProps.style}
      ref={draggableProvided.innerRef}
      {...draggableProvided.draggableProps}
      {...draggableProvided.dragHandleProps}
    >
      <ClayIcon symbol="drag" className={classes.handle} />{" "}
      {Icon && (
        <>
          <div className={classes.icon}>
            <Icon size={24} />
          </div>{" "}
        </>
      )}
      {displayName}
    </div>
  );
}

export function EnrichPipelineReorderStack({
  dsEnrichItems,
  selectedEnrichId,
  setSelectedEnrichId,
  pluginInfos,
  onAdd,
  editing,
}: {
  dsEnrichItems: EnrichItem[];
  selectedEnrichId: number | null;
  setSelectedEnrichId(v: number | null): void;
  pluginInfos: PluginInfo[];
  onAdd(): void;
  editing: boolean;
}) {
  const classes = useStyles();

  const loginInfo = useLoginInfo();

  async function dragEnd(result: DropResult) {
    if (!dsEnrichItems || !result.destination) return;

    const sorted = [...dsEnrichItems].sort((a, b) => a._position - b._position);
    const [removed] = sorted.splice(result.source.index, 1);
    if (removed) sorted.splice(result.destination.index, 0, removed);
    const reordered: EnrichItem[] = sorted.map((s, i) => ({
      ...s,
      _position: i + 1,
    }));

    mutate(`/api/v2/enrichItem`, reordered, false);

    await reorderEnrichItems(
      reordered.map((e) => e.enrichItemId).filter(Boolean),
      loginInfo,
    );
  }

  return (
    <div className={classes.enrichStack}>
      <h5>Enrich Pipeline</h5>

      <div className={classes.stackColumns}>
        <div className={classes.reorderColumn}>
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
                    .map((item, index) => (
                      <Draggable
                        key={item.enrichItemId}
                        draggableId={item.enrichItemId.toString()}
                        index={index}
                      >
                        {(draggableProvided) => (
                          <EnrichItemBlock
                            item={item}
                            selected={item.enrichItemId == selectedEnrichId}
                            onSelect={() =>
                              setSelectedEnrichId(item.enrichItemId)
                            }
                            pluginInfos={pluginInfos}
                            draggableProvided={draggableProvided}
                          />
                        )}
                      </Draggable>
                    ))}

                  {droppableProvided.placeholder}

                  <div className={classes.enrichItemOutputContainer}>
                    <div
                      className={clsx(
                        classes.enrichItem,
                        classes.enrichItemOutput,
                      )}
                    >
                      Output
                    </div>
                    <button
                      className={clsx(
                        "btn btn-sm btn-secondary",
                        classes.addButton,
                      )}
                      type="button"
                      disabled={editing}
                      onClick={onAdd}
                    >
                      <span className="inline-item inline-item-before">
                        <ClayIcon symbol="plus" />
                      </span>{" "}
                      ADD
                    </button>
                  </div>
                </div>
              )}
            </Droppable>
          </DragDropContext>
        </div>

        <div className={classes.stackVertArrow} key={dsEnrichItems.length}>
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
