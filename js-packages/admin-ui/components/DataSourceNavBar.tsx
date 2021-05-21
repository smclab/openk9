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

import { useState, forwardRef } from "react";
import clsx from "clsx";
import Link from "next/link";
import { useRouter } from "next/router";
import { createUseStyles } from "react-jss";
import ClayNavigationBar from "@clayui/navigation-bar";
import ClayIcon from "@clayui/icon";
import { ClayTooltipProvider } from "@clayui/tooltip";
import ClayDropDown from "@clayui/drop-down";
import { ThemeType } from "@openk9/search-ui-components";
import {
  deleteDataSource,
  getSchedulerItems,
  triggerReindex,
  triggerScheduler,
} from "@openk9/http-api";
import { ConfirmationModal } from "./ConfirmationModal";
import { useLoginInfo } from "../state";
import { useToast } from "../pages/_app";

const useStyles = createUseStyles((theme: ThemeType) => ({
  navMenu: {
    backgroundColor: "transparent",
  },
  navActionButton: {
    marginLeft: theme.spacingUnit,
  },
}));

// Warning silencer to fix ClayUI dropdown
const DumbFragment = forwardRef(
  ({ children }, ref) => (void ref, (<>{children}</>)),
);

export function DataSourceNavBar({
  tenantId,
  datasourceId,
}: {
  tenantId: number;
  datasourceId: number;
}) {
  const classes = useStyles();

  const { route, push } = useRouter();

  const [scheduleModalV, setScheduleModalV] = useState(false);
  const [reindexModalV, setReindexModalV] = useState(false);
  const [deleteModalV, setDeleteModalV] = useState(false);
  const loginInfo = useLoginInfo();
  const { pushToast } = useToast();

  async function schedule(ids: number[]) {
    const schedulerItems = await getSchedulerItems(loginInfo);
    const schedulerItemsToRestart = schedulerItems
      .filter((job) => ids.includes(job.datasourceId))
      .map((job) => job.jobName);
    const resp = await triggerScheduler(schedulerItemsToRestart, loginInfo);
    console.log(resp);
    pushToast(`Reindex requested for 1 item`);
  }

  async function reindex(datasourceId: number) {
    const resp = await triggerReindex([datasourceId], loginInfo);
    console.log(resp);
    pushToast(`Full reindex requested for 1 item`);
  }

  async function doDelete(datasourceId: number) {
    const resp = await deleteDataSource(datasourceId, loginInfo);
    console.log(resp);
    pushToast(`DataSource Deleted`);
    push(`/tenants/${tenantId}/dataSources`);
  }

  const [dropdownOpen, setDropdownOpen] = useState(false);

  return (
    <>
      <ClayNavigationBar
        triggerLabel="Configuration"
        className={classes.navMenu}
      >
        <ClayNavigationBar.Item>
          <Link
            href={`/tenants/${tenantId}/dataSources/${datasourceId}/settings`}
            passHref
          >
            <a
              className={clsx(
                "nav-link",
                route.endsWith("/settings") && "active",
              )}
            >
              Configuration
            </a>
          </Link>
        </ClayNavigationBar.Item>
        <ClayNavigationBar.Item>
          <Link
            href={`/tenants/${tenantId}/dataSources/${datasourceId}/dataBrowser`}
            passHref
          >
            <a
              className={clsx(
                "nav-link",
                route.endsWith("/dataBrowser") && "active",
              )}
            >
              Data Browser
            </a>
          </Link>
        </ClayNavigationBar.Item>
        <ClayNavigationBar.Item>
          <Link
            href={`/tenants/${tenantId}/dataSources/${datasourceId}/enrich`}
            passHref
          >
            <a
              className={clsx(
                "nav-link",
                route.endsWith("/enrich") && "active",
              )}
            >
              Enrich
            </a>
          </Link>
        </ClayNavigationBar.Item>
        <ClayNavigationBar.Item>
          <ClayTooltipProvider>
            <div className={clsx(classes.navActionButton, "btn-group")}>
              <button
                className="btn btn-primary"
                data-tooltip-align="bottom"
                title="Trigger Scheduler"
                onClick={() => setScheduleModalV(true)}
              >
                <ClayIcon symbol="reload" />
              </button>
              <ClayDropDown
                trigger={
                  <button
                    aria-expanded={dropdownOpen}
                    aria-haspopup={true}
                    className="btn btn-primary btn-monospaced dropdown-toggle"
                    type="button"
                  >
                    <ClayIcon symbol="caret-bottom" />
                  </button>
                }
                active={dropdownOpen}
                onActiveChange={setDropdownOpen}
                containerElement={DumbFragment}
              >
                <ClayDropDown.ItemList>
                  <ClayDropDown.Item onClick={() => setScheduleModalV(true)}>
                    Trigger Scheduler
                  </ClayDropDown.Item>
                  <ClayDropDown.Item onClick={() => setReindexModalV(true)}>
                    Full Reindex
                  </ClayDropDown.Item>
                  <ClayDropDown.Divider />
                  <ClayDropDown.Item onClick={() => setDeleteModalV(true)}>
                    Delete DataSource
                  </ClayDropDown.Item>
                </ClayDropDown.ItemList>
              </ClayDropDown>
            </div>
          </ClayTooltipProvider>
        </ClayNavigationBar.Item>
      </ClayNavigationBar>

      {scheduleModalV && (
        <ConfirmationModal
          title="Trigger Scheduler"
          message="Are you sure you want to trigger the scheduler for this data source? This will look for new data and may take some time."
          abortText="Abort"
          confirmText="Trigger"
          onCloseModal={() => setScheduleModalV(false)}
          onConfirmModal={() => schedule([Number(datasourceId)])}
        />
      )}
      {reindexModalV && (
        <ConfirmationModal
          title="Full Reindex"
          message="Are you sure you want to reindex this data source? This will delete ALL the previously indexed data and may take a long time."
          abortText="Abort"
          confirmText="Reindex"
          onCloseModal={() => setReindexModalV(false)}
          onConfirmModal={() => reindex(Number(datasourceId))}
        />
      )}
      {deleteModalV && (
        <ConfirmationModal
          title="Delete"
          message="Are you sure you want to delete this data source? This will delete ALL the previously indexed data."
          abortText="Abort"
          confirmText="Delete"
          onCloseModal={() => setDeleteModalV(false)}
          onConfirmModal={() => doDelete(Number(datasourceId))}
        />
      )}
    </>
  );
}
