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

import React, { useState } from "react";
import dynamic from "next/dynamic";
import clsx from "clsx";
import { format } from "date-fns";
import useSWR from "swr";
import { createUseStyles } from "react-jss";
import { ClayToggle, ClayInput } from "@clayui/form";
import ClayAutocomplete from "@clayui/autocomplete";
import ClayDropDown from "@clayui/drop-down";
import ClayIcon from "@clayui/icon";
import { pluginLoader, ThemeType } from "@openk9/search-ui-components";
import {
  DataSourceInfo,
  DataSourcePlugin,
  getDriverServiceNames,
  getPlugins,
} from "@openk9/http-api";
import { CronInput, CronInputType } from "./CronInput";
import { AutocompleteItemIcon } from "./AutocompleteItemIcon";
import { isServer, useLoginInfo } from "../state";

const DefaultSettingsEditor = dynamic(() => import("./DefaultSettingsEditor"), {
  ssr: false,
});

const useStyles = createUseStyles((theme: ThemeType) => ({
  editElement: {
    marginBottom: theme.spacingUnit * 2,
  },
  labelReadOnly: {
    marginLeft: theme.spacingUnit,
  },
  buttons: {
    display: "flex",
    justifyContent: "flex-end",
  },
  closeButton: {
    marginRight: theme.spacingUnit * 2,
  },
}));

export function EditDataSource<T extends Partial<DataSourceInfo> | null>({
  editingDataSource,
  onChange,
  onAbort,
  onSave,
}: {
  editingDataSource: DataSourceInfo;
  onChange: React.Dispatch<React.SetStateAction<T>>;
  onAbort(): void;
  onSave(): void;
}) {
  const classes = useStyles();

  const loginInfo = useLoginInfo();

  // TODO: check API to use
  // Show also icon in the dropdown list?
  const { data: driverServiceNames } = useSWR(
    `/api/v1/driver-service-names`,
    () => getDriverServiceNames(loginInfo),
  );

  const { data: pluginInfos } = useSWR(`/api/v1/plugin`, () =>
    getPlugins(loginInfo),
  );
  const plugins = (pluginInfos || []).map(
    (pi) =>
      [
        pi.pluginId,
        pi.bundleInfo.symbolicName,
        pluginLoader.read(pi.pluginId),
      ] as const,
  );

  const currentPluginInfo = (pluginInfos || []).find((p) =>
    editingDataSource.driverServiceName.startsWith(p.bundleInfo.symbolicName),
  );
  const currentPlugin =
    currentPluginInfo &&
    plugins.find(([id]) => id === currentPluginInfo.pluginId);

  const dataSourcePlugin = (
    currentPlugin && currentPlugin[2]
  )?.pluginServices.find(
    (ps) =>
      ps.type === "DATASOURCE" &&
      ps.driverServiceName === editingDataSource.driverServiceName,
  ) as DataSourcePlugin | null;

  const SettingsRenderer =
    dataSourcePlugin?.settingsRenderer ||
    (!isServer && DefaultSettingsEditor) ||
    null;

  const [
    minutesValue,
    hoursValue,
    daysOfMonthValue,
    monthValue,
    daysOfWeekValue,
    yearValue,
  ] = editingDataSource && editingDataSource.scheduling.split(" ");

  const schedulingValue: CronInputType = {
    minutesValue,
    hoursValue,
    daysOfMonthValue,
    monthValue,
    daysOfWeekValue,
    yearValue,
  };

  const [activeAutocomplete, setActiveAutocomplete] = useState(false);

  return (
    <>
      <div className={classes.editElement}>
        <strong>Name:</strong>{" "}
        <ClayInput
          placeholder="Insert the name here"
          onChange={(e) => onChange((ds) => ({ ...ds, name: e.target.value }))}
          value={editingDataSource.name}
          type="text"
        />
      </div>
      <div className={classes.editElement}>
        <strong>Status:</strong>
        {"  "}
        <ClayToggle
          onToggle={(e) => onChange((ds) => ({ ...ds, active: e }))}
          toggled={editingDataSource.active}
        />
        {editingDataSource.active ? (
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
        <strong>Description:</strong>
        <ClayInput
          placeholder="Insert the description here"
          onChange={(e) =>
            onChange((ds) => ({ ...ds, description: e.target.value }))
          }
          value={editingDataSource.description}
          type="text"
        />
      </div>
      <div className={classes.editElement}>
        <strong>Driver Service Name:</strong>
        <ClayAutocomplete>
          <ClayAutocomplete.Input
            onChange={(e) =>
              onChange((ds) => ({ ...ds, driverServiceName: e.target.value }))
            }
            placeholder="Insert the driver service name here"
            value={editingDataSource.driverServiceName}
            onFocus={() => setActiveAutocomplete(true)}
            // Hack to make onClick work after blur
            onBlur={() => setTimeout(() => setActiveAutocomplete(false), 300)}
          />
          <ClayAutocomplete.DropDown active={activeAutocomplete}>
            <ClayDropDown.ItemList>
              {driverServiceNames &&
                driverServiceNames.map((dsn) => {
                  const pluginRecord = plugins.find(([, bi]) =>
                    dsn.startsWith(bi),
                  );
                  const plugin = pluginRecord && pluginRecord[2];
                  const dataSourcePlugin = plugin?.pluginServices.find(
                    (ps) =>
                      ps.type === "DATASOURCE" &&
                      ps.driverServiceName ===
                        editingDataSource.driverServiceName,
                  ) as DataSourcePlugin | null;
                  const displayName = dataSourcePlugin?.displayName;
                  const Icon = dataSourcePlugin?.iconRenderer;
                  return (
                    <AutocompleteItemIcon
                      key={dsn}
                      icon={Icon && <Icon size={16} />}
                      match={
                        editingDataSource.driverServiceName + " " + displayName
                      }
                      value={displayName || dsn}
                      onClick={() =>
                        onChange((ds) => ({ ...ds, driverServiceName: dsn }))
                      }
                    />
                  );
                })}
            </ClayDropDown.ItemList>
          </ClayAutocomplete.DropDown>
        </ClayAutocomplete>
      </div>
      <div className={classes.editElement}>
        <strong>Tenant Id:</strong> {editingDataSource.tenantId}
        <span className={clsx("label label-info", classes.labelReadOnly)}>
          <span className="label-item label-item-expand">READ ONLY</span>
        </span>
      </div>
      <div className={classes.editElement}>
        <strong>Last Ingestion Date:</strong>{" "}
        {format(editingDataSource.lastIngestionDate, "dd/MM/yyyy, HH:mm")}
        <span className={clsx("label label-info", classes.labelReadOnly)}>
          <span className="label-item label-item-expand">READ ONLY</span>
        </span>
      </div>
      <div className={classes.editElement}>
        <strong>Scheduling:</strong>
        <CronInput
          schedulingValue={schedulingValue}
          setSchedulingValue={(e) =>
            onChange((ds) => ({
              ...ds,
              scheduling: [
                e.minutesValue,
                e.hoursValue,
                e.daysOfMonthValue,
                e.monthValue,
                e.daysOfWeekValue,
                e.yearValue,
              ].join(" "),
            }))
          }
        />
      </div>
      <div className={classes.editElement}>
        {SettingsRenderer && SettingsRenderer != null && (
          <SettingsRenderer
            currentSettings={editingDataSource.jsonConfig}
            setCurrentSettings={(e) =>
              onChange((ds) => ({ ...ds, jsonConfig: e }))
            }
          />
        )}
      </div>
      <div className={classes.buttons}>
        <button
          className={clsx("btn btn-secondary", classes.closeButton)}
          type="button"
          onClick={onAbort}
        >
          Close without Save
        </button>
        <button className="btn btn-primary" type="button" onClick={onSave}>
          <span className="inline-item inline-item-before">
            <ClayIcon symbol="disk" />
          </span>
          Save
        </button>
      </div>
    </>
  );
}
