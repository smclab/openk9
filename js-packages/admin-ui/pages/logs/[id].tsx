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
import { useRouter } from "next/router";
import useSWR from "swr";
import ansicolor from "ansicolor";
import { firstOrString, ThemeType } from "@openk9/search-ui-components";
import { getContainerLogs, getContainerStatus } from "@openk9/http-api";
import { Layout } from "../../components/Layout";

const convertStylesStringToObject = (stringStyles) =>
  typeof stringStyles === "string"
    ? stringStyles.split(";").reduce((acc, style) => {
        const colonPosition = style.indexOf(":");

        if (colonPosition === -1) {
          return acc;
        }

        const camelCaseProperty = style
            .substr(0, colonPosition)
            .trim()
            .replace(/^-ms-/, "ms-")
            .replace(/-./g, (c) => c.substr(1).toUpperCase()),
          value = style.substr(colonPosition + 1).trim();

        return value ? { ...acc, [camelCaseProperty]: value } : acc;
      }, {})
    : {};

ansicolor.rgb = {
  black: [0, 0, 0],
  darkGray: [100, 100, 100],
  lightGray: [200, 200, 200],
  white: [255, 255, 255],
  red: [204, 0, 0],
  lightRed: [255, 51, 0],
  green: [0, 204, 0],
  lightGreen: [51, 204, 51],
  yellow: [204, 102, 0],
  lightYellow: [255, 153, 51],
  blue: [0, 0, 255],
  lightBlue: [26, 140, 255],
  magenta: [204, 0, 204],
  lightMagenta: [255, 0, 255],
  cyan: [0, 153, 255],
  lightCyan: [0, 204, 255],
};

const useStyles = createUseStyles((theme: ThemeType) => ({
  container: {
    height: "100%",
    display: "flex",
    flexDirection: "column",
  },
  root: {
    overflow: "auto",
    width: "100%",
    flexGrow: 1,
    padding: theme.spacingUnit * 2,
    backgroundColor: theme.digitalLakeMainL2,
  },
  code: {
    color: "white",
  },
}));

const N = 300;

function LogId() {
  const classes = useStyles();

  const { query } = useRouter();
  const contId = query.id && firstOrString(query.id);

  const { data: info } = useSWR(`/logs/status`, getContainerStatus, {
    refreshInterval: 10000,
  });

  const { data: log } = useSWR(
    `/logs/status/${contId}/${N}`,
    () => getContainerLogs(contId, N),
    { refreshInterval: 5000 },
  );

  const parsed = ansicolor.parse(log);

  return (
    <Layout
      breadcrumbsPath={[
        { label: "Logs", path: "/logs" },
        { label: (info || []).find((e) => e.ID === contId)?.Names },
      ]}
    >
      <div className={classes.container}>
        <div className={classes.root}>
          <pre className={classes.code}>
            {parsed.spans.map((s, i) => (
              <span
                style={convertStylesStringToObject(s.css)}
                key={s.text + "-" + i}
              >
                {ansicolor.strip(s.text)}
              </span>
            ))}
          </pre>
        </div>
      </div>
    </Layout>
  );
}

export default LogId;
