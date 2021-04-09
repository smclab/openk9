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
import clsx from "clsx";
import { createUseStyles } from "react-jss";
import { useRouter } from "next/router";
import { format } from "date-fns";
import useSWR, { mutate } from "swr";
import ClayAlert from "@clayui/alert";
import { ClayToggle, ClayInput } from "@clayui/form";
import ClayAutocomplete from "@clayui/autocomplete";
import ClayDropDown from "@clayui/drop-down";
import ClayIcon from "@clayui/icon";

import {
  firstOrString,
  pluginInfoLoader,
  pluginLoader,
  ThemeType,
} from "@openk9/search-ui-components";
import {
  changeDataSourceInfo,
  DataSourceInfo,
  getDataSourceInfo,
  getDriverServiceNames,
  triggerReindex,
} from "@openk9/http-api";

import { ConfirmationModal } from "../../../../../components/ConfirmationModal";
import { CronInput, CronInputType } from "../../../../../components/CronInput";
import { Layout } from "../../../../../components/Layout";
import { isServer } from "../../../../../state";
import { DataSourceNavBar } from "../../../../../components/DataSourceNavBar";
import { AutocompleteItemIcon } from "../../../../../components/AutocompleteItemIcon";

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
  json: {
    marginTop: "0.2rem",
    backgroundColor: theme.digitalLakeMainL2,
    color: "white",
    padding: theme.spacingUnit * 2,
    borderRadius: theme.borderRadius,
  },
  alert: {
    "& .alert-autofit-row": {
      alignItems: "center",
    },
  },
  settingHeader: {
    display: "flex",
    justifyContent: "space-between",
    alignItems: "center",
  },
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
  acLine: {
    display: "flex",
    alignItems: "center",
    paddingLeft: theme.spacingUnit,
  },
}));

function EditInner({
  editingDataSource,
  onChange,
  onAbort,
  onSave,
}: {
  editingDataSource: DataSourceInfo;
  onChange: React.Dispatch<React.SetStateAction<DataSourceInfo>>;
  onAbort(): void;
  onSave(): void;
}) {
  const classes = useStyles();

  // TODO: check API to use
  // Show also icon in the dropdown list?
  const { data: driverServiceNames } = useSWR(
    `/api/v1/driver-service-names`,
    getDriverServiceNames,
  );

  const pluginInfos = pluginInfoLoader.read();
  const plugins = pluginInfos.map(
    (pi) =>
      [
        pi.pluginId,
        pi.bundleInfo.symbolicName,
        pluginLoader.read(pi.pluginId),
      ] as const,
  );

  const currentPluginInfo = pluginInfos.find((p) =>
    editingDataSource.driverServiceName.startsWith(p.bundleInfo.symbolicName),
  );
  const currentPlugin =
    currentPluginInfo &&
    plugins.find(([id]) => id === currentPluginInfo.pluginId)[2];
  const SettingsRenderer =
    (currentPlugin &&
      currentPlugin?.dataSourceAdminInterfacePath?.settingsRenderer) ||
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
                  const displayName = plugin?.displayName;
                  const Icon =
                    plugin?.dataSourceAdminInterfacePath?.iconRenderer;
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

function Inner({
  tenantId,
  datasourceId,
  onSaveDataSource,
}: {
  tenantId: number;
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
    () => !isNaN(datasourceId) && getDataSourceInfo(datasourceId),
  );

  const [
    editingDataSource,
    setEditingDataSource,
  ] = useState<DataSourceInfo | null>(null);

  if (!datasource) {
    return <span className="loading-animation" />;
  }

  async function handleSave() {
    mutate(editingDataSource);
    mutate(await onSaveDataSource(datasourceId, datasource, editingDataSource));
    setEditingDataSource(null);
  }

  if (editingDataSource && !isServer) {
    return (
      <Suspense fallback={<span className="loading-animation" />}>
        <EditInner
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
          {format(datasource.lastIngestionDate, "dd/MM/yyyy, HH:mm")}
        </div>
        <div>
          <strong>Scheduling:</strong> {datasource.scheduling}
        </div>
      </div>

      <h5>JSON Configuration</h5>
      <pre className={classes.json}>
        {JSON.stringify(JSON.parse(datasource.jsonConfig), null, 4)}
      </pre>
    </>
  );
}

function DSSettings() {
  const classes = useStyles();

  const { query } = useRouter();
  const tenantId = query.tenantId && firstOrString(query.tenantId);
  const datasourceId = query.datasourceId && firstOrString(query.datasourceId);
  const [isVisibleModal, setIsVisibleModal] = useState(false);

  function onPerformAction(label: string) {
    setToastItems((tt) => [...tt, { label, key: Math.random().toFixed(5) }]);
  }

  async function reindex(ids: number) {
    const resp = await triggerReindex([ids]);
    console.log(resp);
    onPerformAction(`Reindex requested for 1 item`);
  }

  async function saveDataSource(
    datasourceId: number,
    prevDataSource: DataSourceInfo,
    editedDatasource: DataSourceInfo,
  ) {
    const newDatasource: Partial<DataSourceInfo> = {};

    Object.keys(prevDataSource).forEach((key) => {
      if (prevDataSource[key] !== editedDatasource[key]) {
        newDatasource[key] = editedDatasource[key];
      }
    });

    if (Object.entries(newDatasource).length !== 0) {
      const saved = await changeDataSourceInfo(datasourceId, newDatasource);
      onPerformAction(`The datasource has been updated`);
      return saved;
    }

    return prevDataSource;
  }

  const [toastItems, setToastItems] = useState<
    { label: string; key: string }[]
  >([]);

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
            onReindex={() => setIsVisibleModal(true)}
            tenantId={parseInt(tenantId)}
            datasourceId={parseInt(datasourceId)}
          />
        }
      >
        <div className={classes.root}>
          <Inner
            tenantId={parseInt(tenantId)}
            datasourceId={parseInt(datasourceId)}
            onSaveDataSource={saveDataSource}
          />
        </div>
      </Layout>

      <ClayAlert.ToastContainer>
        {toastItems.map((value) => (
          <ClayAlert
            displayType="success"
            className={classes.alert}
            autoClose={5000}
            key={value.key}
            onClose={() => {
              setToastItems((prevItems) =>
                prevItems.filter((item) => item.key !== value.key),
              );
            }}
          >
            {value.label}
          </ClayAlert>
        ))}
      </ClayAlert.ToastContainer>

      {isVisibleModal && (
        <ConfirmationModal
          title="Reindex"
          message="Are you sure you want to reindex this data source? This will delete the previously indexed data and may take a long time."
          abortText="Abort"
          confirmText="Reindex"
          onCloseModal={() => setIsVisibleModal(false)}
          onConfirmModal={() => reindex(Number(datasourceId))}
        />
      )}
    </>
  );
}

export default DSSettings;
