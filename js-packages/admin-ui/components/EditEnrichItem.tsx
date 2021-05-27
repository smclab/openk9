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

import React, { useLayoutEffect, useMemo, useState } from "react";
import dynamic from "next/dynamic";
import { createUseStyles } from "react-jss";
import clsx from "clsx";
import { ClayInput, ClayToggle } from "@clayui/form";
import ClayIcon from "@clayui/icon";
import ClayAutocomplete from "@clayui/autocomplete";
import ClayDropDown from "@clayui/drop-down";
import { pluginLoader, ThemeType } from "@openk9/search-ui-components";
import {
  EnrichItem,
  EnrichPlugin,
  getServices,
  PluginInfo,
} from "@openk9/http-api";
import { isServer } from "../state";
import { AutocompleteItemIcon } from "./AutocompleteItemIcon";

const DefaultSettingsEditor = dynamic(() => import("./DefaultSettingsEditor"), {
  ssr: false,
});

export const useStyles = createUseStyles((theme: ThemeType) => ({
  grow: {
    flexGrow: 1,
  },
  detailTitle: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
  },
  editElement: {
    marginBottom: theme.spacingUnit * 2,
  },
  buttons: {
    display: "flex",
    justifyContent: "flex-end",
    marginBottom: theme.spacingUnit * 2,
  },
  closeButton: {
    marginRight: theme.spacingUnit * 2,
  },
}));

export function EditEnrichItem({
  selectedEnrich,
  pluginInfos,
  editing,
  setEditing,
  onSave,
  onAbort,
}: {
  selectedEnrich: EnrichItem;
  pluginInfos: PluginInfo[];
  editing: EnrichItem;
  setEditing(
    fn: EnrichItem | null | ((ei: EnrichItem | null) => EnrichItem | null),
  ): void;
  onSave(): void;
  onAbort(): void;
}) {
  const classes = useStyles();

  const plugins = useMemo(
    () => (pluginInfos || []).map((pi) => pluginLoader.read(pi.pluginId)),
    [pluginInfos],
  );
  const pluginServices = useMemo(
    () =>
      getServices(plugins).filter((p) => p.type === "ENRICH") as EnrichPlugin[],
    [plugins],
  );
  const serviceNames = useMemo(
    () => pluginServices.map((ps) => ps.serviceName),
    [pluginServices],
  );

  const [eiSettings, setEiSettings] = useState<{ [ein: string]: string }>({});
  useLayoutEffect(() => {
    const result: { [ein: string]: string } = {};
    result[editing.serviceName] = editing.jsonConfig;

    serviceNames?.forEach((ein) => {
      const ps = pluginServices.find((ps) => ps.serviceName === ein);
      if (ps && !result[ein]) {
        result[ein] = ps.initialSettings;
      }
    });
    setEiSettings(result);
  }, [serviceNames, pluginInfos]);

  const currentPlugin = pluginServices.find(
    (ps) => ps.serviceName === editing?.serviceName,
  );

  const SettingsRenderer =
    currentPlugin?.settingsRenderer ||
    (!isServer && DefaultSettingsEditor) ||
    null;

  const [activeAutocomplete, setActiveAutocomplete] = useState(false);

  return (
    <div className={classes.grow}>
      <h5>Item Configuration: {selectedEnrich.name}</h5>
      <div className={classes.editElement}>
        <strong>Name:</strong>{" "}
        <ClayInput
          placeholder="Insert the name here"
          onChange={(e) =>
            setEditing((ei) => ({
              ...(ei as EnrichItem),
              name: e.target.value,
            }))
          }
          value={editing.name}
          type="text"
        />
      </div>
      <div className={classes.editElement}>
        <strong>Status:</strong>
        {"  "}
        <ClayToggle
          onToggle={(e) =>
            setEditing((ei) => ({ ...(ei as EnrichItem), active: e }))
          }
          toggled={editing.active}
        />
        {editing.active ? (
          <span className="label label-success">
            <span className="label-item label-item-expand">ENABLED</span>
          </span>
        ) : (
          <span className="label label-warning">
            <span className="label-item label-item-expand">DISABLED</span>
          </span>
        )}
      </div>
      <div className={classes.editElement}>
        <strong>Driver Service Name:</strong>
        <ClayAutocomplete>
          <ClayAutocomplete.Input
            onChange={(e) =>
              setEditing((ei) => ({
                ...(ei as EnrichItem),
                serviceName: e.target.value,
              }))
            }
            placeholder="Insert the service name here"
            value={editing.serviceName}
            onFocus={() => setActiveAutocomplete(true)}
            // Hack to make onClick work after blur
            onBlur={() => setTimeout(() => setActiveAutocomplete(false), 300)}
          />
          <ClayAutocomplete.DropDown active={activeAutocomplete}>
            <ClayDropDown.ItemList>
              {serviceNames &&
                serviceNames.map((ein) => {
                  const enrichPlugin = pluginServices.find(
                    (ps) => ps.serviceName === ein,
                  );
                  const displayName = enrichPlugin?.displayName;
                  const Icon = enrichPlugin?.iconRenderer;
                  return (
                    <AutocompleteItemIcon
                      key={ein}
                      icon={Icon && <Icon size={16} />}
                      match={ein + " " + displayName}
                      value={displayName || ein}
                      onClick={() =>
                        setEditing((ei) => ({
                          ...(ei as EnrichItem),
                          jsonConfig: eiSettings[ein],
                          serviceName: ein,
                        }))
                      }
                    />
                  );
                })}
            </ClayDropDown.ItemList>
          </ClayAutocomplete.DropDown>
        </ClayAutocomplete>
      </div>
      <div className={classes.editElement}>
        {SettingsRenderer && SettingsRenderer != null && (
          <SettingsRenderer
            currentSettings={editing.jsonConfig}
            setCurrentSettings={(e) => {
              setEditing((ei) => ({ ...(ei as EnrichItem), jsonConfig: e }));
              setEiSettings((cfg) => ({
                ...cfg,
                [editing.serviceName]: e,
              }));
            }}
          />
        )}
      </div>
      <div className={classes.buttons}>
        <button
          className={clsx("btn btn-secondary", classes.closeButton)}
          type="button"
          onClick={onAbort}
        >
          {editing.serviceName ? (
            "Close without Save"
          ) : (
            <>
              <span className="inline-item inline-item-before">
                <ClayIcon symbol="trash" />
              </span>
              Delete
            </>
          )}
        </button>
        <button className="btn btn-primary" type="button" onClick={onSave}>
          <span className="inline-item inline-item-before">
            <ClayIcon symbol="disk" />
          </span>
          Save
        </button>
      </div>
    </div>
  );
}
