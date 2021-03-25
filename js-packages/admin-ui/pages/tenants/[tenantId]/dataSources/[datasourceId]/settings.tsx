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

import { useState } from "react";
import clsx from "clsx";
import { createUseStyles } from "react-jss";
import { useRouter } from "next/router";
import { format } from "date-fns";
import useSWR from "swr";
import ClayNavigationBar from "@clayui/navigation-bar";
import ClayIcon from "@clayui/icon";
import {
  firstOrString,
  pluginInfoLoader,
  pluginLoader,
  ThemeType,
} from "@openk9/search-ui-components";
import { Layout } from "../../../../../components/Layout";
import {
  changeDataSourceInfo,
  DataSourceInfo,
  getDataSourceInfo,
  getDriverServiceNames,
  triggerReindex,
} from "@openk9/http-api";
import { ClayTooltipProvider } from "@clayui/tooltip";
import ClayAlert from "@clayui/alert";
import { ClayToggle, ClayInput } from "@clayui/form";
import { ConfirmationModal } from "../../../../../components/ConfirmationModal";
import { CronInput, CronInputType } from "../../../../../components/CronInput";
import ClayAutocomplete from "@clayui/autocomplete";
import ClayDropDown from "@clayui/drop-down";

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
  navMenu: {
    backgroundColor: "transparent",
  },
  navActionButton: {
    marginLeft: theme.spacingUnit,
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
    marginBottom: "16px",
  },
  labelReadOnly: {
    marginLeft: "8px",
  },
  buttons: {
    display: "flex",
    justifyContent: "flex-end",
  },
  closeButton: {
    marginRight: "16px",
  },
}));

function Controls({
  setIsVisibleModal,
}: {
  setIsVisibleModal(b: boolean): void;
}) {
  const classes = useStyles();
  return (
    <ClayNavigationBar triggerLabel="Configuration" className={classes.navMenu}>
      <ClayNavigationBar.Item active>
        <a className="nav-link">Configuration</a>
      </ClayNavigationBar.Item>
      <ClayNavigationBar.Item>
        <a className="nav-link">Data Browser</a>
      </ClayNavigationBar.Item>
      <ClayNavigationBar.Item>
        <a className="nav-link">Enrich</a>
      </ClayNavigationBar.Item>
      <ClayNavigationBar.Item>
        <a className="nav-link">Schedule</a>
      </ClayNavigationBar.Item>
      <ClayNavigationBar.Item>
        <a className="nav-link">ACL</a>
      </ClayNavigationBar.Item>
      <ClayNavigationBar.Item>
        <ClayTooltipProvider>
          <div>
            <a
              className={clsx("btn btn-primary", classes.navActionButton)}
              data-tooltip-align="bottom"
              title="Reindex Data Source"
              onClick={() => setIsVisibleModal(true)}
            >
              <ClayIcon symbol="reload" />
            </a>
          </div>
        </ClayTooltipProvider>
      </ClayNavigationBar.Item>
    </ClayNavigationBar>
  );
}

function EditInner({
  datasourceId,
  setIsEditMode,
  onPerformAction,
}: {
  datasourceId: number;
  setIsEditMode(b: boolean): void;
  onPerformAction(l: string): void;
}) {
  const classes = useStyles();

  const { data: datasource } = useSWR(
    `/api/v2/datasource/${datasourceId}`,
    () => getDataSourceInfo(datasourceId),
  );

  // TODO: check API to use
  // Show also icon in the dropdown list?
  const { data: enableDriverServiceName } = useSWR(
    `/api/v1/driver-service-names`,
    getDriverServiceNames,
  );

  const pluginInfos = pluginInfoLoader.read();
  const pluginInfo = pluginInfos.find(
    (p) =>
      datasource &&
      datasource.driverServiceName.startsWith(p.bundleInfo.symbolicName),
  );
  const plugin = pluginInfo && pluginLoader.read(pluginInfo.pluginId);

  const SettingsRenderer =
    plugin?.dataSourceAdminInterfacePath?.settingsRenderer;

  const [isDataSourceEnabled, setIsDataSourceEnabled] = useState(
    datasource.active || false,
  );
  const [description, setDescription] = useState(datasource.description || "");
  const [name, setName] = useState(datasource.name || "");
  const [driverServiceName, setDriverServiceName] = useState(
    datasource && datasource.driverServiceName,
  );
  const [json, setJson] = useState(datasource.jsonConfig || "");

  var schedulingArray = datasource && datasource.scheduling.split(" ");

  const [schedulingValue, setSchedulingValue] = useState<CronInputType>({
    minutesValue:
      schedulingArray && schedulingArray.length > 0 ? schedulingArray[0] : "",
    hoursValue:
      schedulingArray && schedulingArray.length > 0 ? schedulingArray[1] : "",
    daysOfMonthValue:
      schedulingArray && schedulingArray.length > 0 ? schedulingArray[2] : "",
    monthValue:
      schedulingArray && schedulingArray.length > 0 ? schedulingArray[3] : "",
    daysOfWeekValue:
      schedulingArray && schedulingArray.length > 0 ? schedulingArray[4] : "",
    yearValue:
      schedulingArray && schedulingArray.length > 0 ? schedulingArray[5] : "",
  });

  function handleChangeInputDescription(event) {
    setDescription(event.target.value);
  }

  function handleChangeInputName(event) {
    setName(event.target.value);
  }

  function handleChangeInputDriverServiceName(event) {
    setDriverServiceName(event.target.value);
  }

  const activeAutocomplete =
    driverServiceName !== datasource.driverServiceName &&
    driverServiceName.length > 0 &&
    !(enableDriverServiceName.indexOf(driverServiceName) > -1);

  async function changeDatasource(datasourceId: number, datasource: any) {
    var newDatasource = {};
    var scheduling =
      schedulingValue.minutesValue +
      " " +
      schedulingValue.hoursValue +
      " " +
      schedulingValue.daysOfMonthValue +
      " " +
      schedulingValue.monthValue +
      " " +
      schedulingValue.daysOfWeekValue +
      " " +
      schedulingValue.yearValue;
    {
      datasource.active !== isDataSourceEnabled &&
        (datasource["active"] = isDataSourceEnabled);
    }
    {
      datasource.description !== description &&
        (newDatasource["description"] = description);
    }
    {
      datasource.name !== name && (newDatasource["name"] = name);
    }
    {
      datasource.driverServiceName !== driverServiceName &&
        (newDatasource["driverServiceName"] = driverServiceName);
    }
    {
      datasource.jsonConfig !== json && (newDatasource["jsonConfig"] = json);
    }
    {
      datasource.scheduling !== scheduling &&
        (newDatasource["scheduling"] = scheduling);
    }

    const resp = await changeDataSourceInfo(datasourceId, newDatasource);
    onPerformAction(`The datasource is updated.`);
  }

  return (
    <>
      <div className={classes.editElement}>
        <strong>Name:</strong>{" "}
        <ClayInput
          id="dataSourceName"
          placeholder="Insert here the name"
          onChange={(event) => handleChangeInputName(event)}
          value={name}
          type="text"
        />
      </div>
      <div className={classes.editElement}>
        <strong>Status:</strong>
        {"  "}
        <ClayToggle
          onToggle={setIsDataSourceEnabled}
          toggled={isDataSourceEnabled}
        />
      </div>
      <div className={classes.editElement}>
        <strong>Description:</strong>
        <ClayInput
          id="dataSourceDescription"
          placeholder="Insert here the description"
          onChange={(event) => handleChangeInputDescription(event)}
          value={description}
          type="text"
        />
      </div>
      <div className={classes.editElement}>
        <strong>Driver Service Name:</strong>
        <ClayAutocomplete>
          <ClayAutocomplete.Input
            id="dataSourceDriverServiceName"
            onChange={(event) => handleChangeInputDriverServiceName(event)}
            placeholder="Insert here the driver service name"
            value={driverServiceName}
          />
          <ClayAutocomplete.DropDown active={activeAutocomplete}>
            <ClayDropDown.ItemList>
              {enableDriverServiceName &&
                enableDriverServiceName.length !== 0 &&
                enableDriverServiceName.map((dsn, id) => (
                  <ClayAutocomplete.Item
                    key={id}
                    match={driverServiceName}
                    value={dsn}
                    onClick={() => setDriverServiceName(dsn)}
                  />
                ))}
            </ClayDropDown.ItemList>
          </ClayAutocomplete.DropDown>
        </ClayAutocomplete>
      </div>
      <div className={classes.editElement}>
        <strong>Tenant Id:</strong> {datasource.tenantId}
        <span className={clsx("label label-info", classes.labelReadOnly)}>
          <span className="label-item label-item-expand">READ ONLY</span>
        </span>
      </div>
      <div className={classes.editElement}>
        <strong>Last Ingestion Date:</strong>{" "}
        {format(datasource.lastIngestionDate, "dd/MM/yyyy, HH:mm")}
        <span className={clsx("label label-info", classes.labelReadOnly)}>
          <span className="label-item label-item-expand">READ ONLY</span>
        </span>
      </div>
      <div className={classes.editElement}>
        <strong>Scheduling:</strong>
        <CronInput
          schedulingValue={schedulingValue}
          setSchedulingValue={setSchedulingValue}
        />
      </div>
      <div className={classes.editElement}>
        {SettingsRenderer && (
          <SettingsRenderer settings={json} setSettings={setJson} />
        )}
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
          onClick={() => {
            changeDatasource(Number(datasource.datasourceId), datasource);
            setIsEditMode(false);
          }}
        >
          Save Settings
        </button>
      </div>
    </>
  );
}

function Inner({
  tenantId,
  datasourceId,
  setIsEditMode,
}: {
  tenantId: number;
  datasourceId: number;
  setIsEditMode(l: boolean): void;
}) {
  const classes = useStyles();

  const { data: datasource } = useSWR(
    `/api/v2/datasource/${datasourceId}`,
    () => getDataSourceInfo(datasourceId),
  );

  if (!datasource) {
    return <span className="loading-animation" />;
  }

  return (
    <>
      <div className={classes.settingHeader}>
        <h2>
          {datasource.datasourceId}: {datasource.name}
        </h2>
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

  const [isEditMode, setIsEditMode] = useState(false);

  function onPerformAction(label: string) {
    setToastItems((tt) => [...tt, { label, key: Math.random().toFixed(5) }]);
  }

  async function reindex(ids: number) {
    const resp = await triggerReindex([ids]);
    console.log(resp);
    onPerformAction(`Reindex requested for 1 item.`);
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
        breadcrumbsControls={<Controls setIsVisibleModal={setIsVisibleModal} />}
      >
        <div className={classes.root}>
          {isEditMode ? (
            <EditInner
              datasourceId={parseInt(datasourceId)}
              setIsEditMode={setIsEditMode}
              onPerformAction={onPerformAction}
            />
          ) : (
            <Inner
              tenantId={parseInt(tenantId)}
              datasourceId={parseInt(datasourceId)}
              setIsEditMode={setIsEditMode}
            />
          )}
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
          title={"Confirmation reindex"}
          message={
            "Are you sure you want to reindex the data sources selected?"
          }
          onCloseModal={() => setIsVisibleModal(false)}
          onConfirmModal={() => reindex(Number(datasourceId))}
        />
      )}
    </>
  );
}

export default DSSettings;
