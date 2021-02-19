import clsx from "clsx";
import { createUseStyles } from "react-jss";

import { useStore } from "../state";
import {
  ThemeType,
  SearchResultsList,
  ResultSidebar,
  ApplicationResultCard,
  DocumentResultCard,
  EmailResultCard,
  ResultRenderersType,
  SidebarRenderersType,
  ApplicationSidebar,
  DocumentSidebar,
  EmailSidebar,
  ContactResultCard,
  CalendarResultCard,
  ContactSidebar,
  CalendarSidebar,
} from "@openk9/search-ui-components";
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

export const resultRenderers: ResultRenderersType = {
  email: EmailResultCard as any,
  file: DocumentResultCard as any,
  application: ApplicationResultCard as any,
  user: ContactResultCard as any,
  calendar: CalendarResultCard as any,
  ...config.resultRenderers,
};

export const sidebarRenderers: SidebarRenderersType = {
  email: EmailSidebar as any,
  file: DocumentSidebar as any,
  application: ApplicationSidebar as any,
  user: ContactSidebar as any,
  calendar: CalendarSidebar as any,
  ...config.sidebarRenderers,
};

export function SearchResults() {
  const results = useStore((s) => s.results);
  const handleLoadMore = useStore((s) => s.doLoadMore);
  const focus = useStore((s) => s.focus);
  const suggestions = useStore((s) => s.suggestions);
  const selectedResult = useStore((s) => s.selectedResult);
  const setSelectedResult = useStore((s) => s.setSelectedResult);

  const classes = useStyles();

  const result =
    results &&
    selectedResult &&
    results.result.find((r) => r.source.id === selectedResult);

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
