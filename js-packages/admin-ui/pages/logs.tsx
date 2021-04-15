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

import { createUseStyles } from "react-jss";
import ClayIcon from "@clayui/icon";
import Link from "next/link";
import useSWR from "swr";
import { ThemeType } from "@openk9/search-ui-components";
import { getContainerStatus } from "@openk9/http-api";
import { Layout } from "../components/Layout";
import { useLoginCheck, useLoginInfo } from "../state";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    margin: [theme.spacingUnit * 2, "auto"],
    backgroundColor: "white",
    boxShadow: theme.baseBoxShadow,
    width: "100%",
    maxWidth: 1000,
    borderRadius: theme.borderRadius,
    overflow: "auto",

    "& thead": {
      position: "sticky",
      top: 0,
      borderTopLeftRadius: theme.borderRadius,
      borderTopRightRadius: theme.borderRadius,
      zIndex: 1000,
    },

    "& td": {
      whiteSpace: "normal",
    },
  },
  actions: {
    display: "flex",
    justifyContent: "flex-end",
  },
}));

function TBody() {
  const classes = useStyles();

  const loginInfo = useLoginInfo();

  const { data } = useSWR(`/logs/status`, () => getContainerStatus(loginInfo));

  if (!data) {
    return <span className="loading-animation" />;
  }

  return (
    <tbody>
      {data.map((container) => (
        <tr key={container.ID}>
          <td>{container.ID}</td>
          <td className="table-cell-expand">
            <p className="table-list-title">{container.Names}</p>
          </td>
          <td className="table-cell-expand">{container.Image}</td>
          <td>
            {container.Status.startsWith("Up") ? (
              <span className="label label-success">
                <span className="label-item label-item-expand">
                  {container.Status}
                </span>
              </span>
            ) : (
              <span className="label label-warning">
                <span className="label-item label-item-expand">
                  {container.Status}
                </span>
              </span>
            )}
          </td>
          <td>
            <div className={classes.actions}>
              <Link href={`/logs/${container.ID}/`} passHref>
                <a className="component-action quick-action-item" role="button">
                  <ClayIcon symbol="forms" />
                </a>
              </Link>
            </div>
          </td>
        </tr>
      ))}
    </tbody>
  );
}

function Logs() {
  const classes = useStyles();

  const { loginValid } = useLoginCheck();
  if (!loginValid) return <span className="loading-animation" />;

  return (
    <Layout breadcrumbsPath={[{ label: "Logs", path: "/logs" }]}>
      <div className={classes.root}>
        <table className="table table-autofit table-nowrap">
          <thead>
            <tr>
              <th>ID</th>
              <th className="table-cell-expand">Name</th>
              <th className="table-cell-expand">Image</th>
              <th>Status</th>
              <th>Logs</th>
            </tr>
          </thead>
          <TBody />
        </table>
      </div>
    </Layout>
  );
}

export default Logs;
