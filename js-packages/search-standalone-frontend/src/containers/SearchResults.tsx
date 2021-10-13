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

import clsx from "clsx";
import { createUseStyles } from "react-jss";

import { useLoginInfo, useStore } from "../state";
import {
  ThemeType,
  SearchResultsList,
  ResultSidebar,
  getPluginResultRenderers,
} from "@openk9/search-ui-components";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: { display: "flex" },
  spacer: { flexGrow: 1 },
  results: {
    flexGrow: 1,
    minWidth: 0,
    maxWidth: theme.pageMaxWidth,
    // margin: [0, "auto"],
    margin: [0, theme.spacingUnit * 8],
    marginTop: [theme.spacingUnit * 4],
    paddingBottom: theme.spacingUnit * 4,
  },
  actions: {
    display: "flex",
    justifyContent: "center",
    margin: [theme.spacingUnit * 2, 0],
  },
  loadMore: {
    minWidth: "40%",
  },
  title: {
    textTransform: "uppercase",
    color: theme.digitalLakeMainL3,
    fontWeight: "bold",
    fontSize: 16,
    marginLeft: theme.spacingUnit * 2,
    marginBottom: theme.spacingUnit * 1,
  },
}));

export function SearchResults() {
  const results = useStore((s) => s.results);
  const handleLoadMore = useStore((s) => s.doLoadMore);
  const selectedResult = useStore((s) => s.selectedResult);
  const setSelectedResult = useStore((s) => s.setSelectedResult);
  const pluginInfos = useStore((s) => s.pluginInfos);
  const loginInfo = useLoginInfo();

  const classes = useStyles();

  const result =
    results &&
    selectedResult &&
    results.result.find((r) => r.source.id === selectedResult);

  const { resultRenderers, sidebarRenderers } = getPluginResultRenderers(
    pluginInfos,
  );

  return (
    results && (
      <div className={classes.root}>
        <div className={classes.results}>
          <div className={classes.title}>Results ({results.total})</div>

          <SearchResultsList
            renderers={resultRenderers}
            searchResults={results.result}
            keyboardFocusEnabled={false}
            onSelectResult={setSelectedResult}
            otherProps={{ loginInfo }}
          />

          {results.total > results.result.length && (
            <div className={classes.actions}>
              <button
                className={clsx("btn btn-secondary", classes.loadMore)}
                onClick={handleLoadMore}
              >
                Load more results
              </button>
            </div>
          )}
        </div>

        <div className={classes.spacer} />

        <ResultSidebar
          renderers={sidebarRenderers}
          result={result || null}
          otherProps={{ loginInfo }}
        />
      </div>
    )
  );
}
