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

import { useStore } from "../state";
import {
  ThemeType,
  SearchResultsList,
  ResultSidebar,
  getPluginResultRenderers,
  getPluginSidebarRenderers,
} from "@openk9/search-ui-components";
import { ResultRenderersType, SidebarRenderersType } from "@openk9/http-api";
import { config } from "../config";

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

export const staticResultRenderers: ResultRenderersType<{}> = {
  ...config.resultRenderers,
};

export const staticSidebarRenderers: SidebarRenderersType<{}> = {
  ...config.sidebarRenderers,
};

export function SearchResults() {
  const results = useStore((s) => s.results);
  const handleLoadMore = useStore((s) => s.doLoadMore);
  const focus = useStore((s) => s.focus);
  const suggestions = useStore((s) => s.suggestions);
  const selectedResult = useStore((s) => s.selectedResult);
  const setSelectedResult = useStore((s) => s.setSelectedResult);
  const pluginInfos = useStore((s) => s.pluginInfos);

  const classes = useStyles();

  const result =
    results &&
    selectedResult &&
    results.result.find((r) => r.source.id === selectedResult);

  const resultRenderers = {
    ...staticResultRenderers,
    ...getPluginResultRenderers(pluginInfos),
  };
  const sidebarRenderers = {
    ...staticSidebarRenderers,
    ...getPluginSidebarRenderers(pluginInfos),
  };

  // useLayoutEffect(() => {
  //   function onKeyDown(e: KeyboardEvent) {
  //     if (e.key === "Escape") {
  //       setFocus("RESULTS");
  //     }
  //   }

  //   document.addEventListener("keydown", onKeyDown);
  //   return () => document.removeEventListener("keydown", onKeyDown);
  // }, []);

  return (
    results && (
      <div className={classes.root}>
        <div className={classes.results}>
          <div className={classes.title}>Results ({results.total})</div>

          <SearchResultsList
            renderers={resultRenderers}
            searchResults={results.result}
            keyboardFocusEnabled={
              focus === "RESULTS" || suggestions.length === 0
            }
            onSelectResult={setSelectedResult}
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

        <ResultSidebar renderers={sidebarRenderers} result={result || null} />
      </div>
    )
  );
}
