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
import { QueryResult } from "@apollo/client";
import SearchIcon from "@mui/icons-material/Search";
import MoreVertIcon from "@mui/icons-material/MoreVert";
import {
  Box,
  Button,
  Container,
  IconButton,
  InputAdornment,
  TableBody,
  TableCell,
  TableHead,
  Table as TableMaterial,
  TableRow,
  TextField,
  Typography,
  useTheme,
} from "@mui/material";
import React from "react";
import { Link } from "react-router-dom";
import { TableVirtuoso } from "react-virtuoso";
import useDebounced from "../common/useDebounced";
import { EmptySpace, ModalConfirm } from "../Form";

export function formatName(value: { id?: string | null; name?: string | null } | null | undefined) {
  return (
    value?.id && (
      <Link
        style={{
          color: "#da1414",
          textDecoration: "none",
          font: "Helvetica",
          fontWeight: "700",
          fontSize: "15px",
          lineHeight: "44px",
        }}
        to={value.id}
      >
        {value.name}
      </Link>
    )
  );
}

export function formatBoolean(value: boolean | undefined) {
  switch (value) {
    case true:
      return "yes";
    case false:
      return "no";
  }
}

export function Table<
  Query,
  Row extends { id?: string | null; name?: string | null },
  Parameters extends { searchText?: string | null; cursor?: string | null },
>({
  data: {
    queryResult: { data, fetchMore, refetch },
    field,
  },
  isItemsSelectable,
  columns,
  rowActions,
  deleted,
  edgesPath = "queryResult.data.edges",
  pageInfoPath = "queryResult.data.pageInfo",
  maxVisibleActions,
}: {
  data: {
    queryResult: QueryResult<Query, Parameters>;
    field(queryResult: Query | undefined):
      | {
          edges?: Array<{ node?: Row | null } | null> | null;
          pageInfo?: { hasNextPage: boolean; endCursor?: string | null } | null;
        }
      | null
      | undefined;
  };
  isItemsSelectable?: boolean;
  columns: Array<{
    header: React.ReactNode;
    content(row: Row | undefined): React.ReactNode;
  }>;
  onDelete(row: Row | undefined): void;
  deleted?:
    | {
        actionDeleted: (id: string, name: string) => void;
        messsage: string;
        wordConfirm: string;
        title: string;
      }
    | undefined;
  onCreatePath: string;
  edgesPath?: string;
  pageInfoPath?: string;
  rowActions: Array<{ label: string; action(suggestionCategory?: any): void; isDisabled?: (dat: any) => boolean }>;
  maxVisibleActions?: number;
}) {
  const [searchText, setSearchText] = React.useState("");
  const searchTextDebounced = useDebounced(searchText);
  const [showSelectedItemsTable] = React.useState(false);
  const [viewDeleteModal, setViewDeleteModal] = React.useState({ isView: false, name: "", id: "" });
  const [openMenuIndex, setOpenMenuIndex] = React.useState<number | null>(null);

  const loadMoreResults = () => {
    const pageInfo = getByPath(data, pageInfoPath);
    if (pageInfo?.hasNextPage) {
      fetchMore({
        variables: {
          first: 20,
          after: pageInfo.endCursor,
        },
        updateQuery: (prev, { fetchMoreResult }) => {
          const prevEdges = getByPath(prev, edgesPath) || [];
          const newEdges = getByPath(fetchMoreResult, edgesPath) || [];
          const newPageInfo = getByPath(fetchMoreResult, pageInfoPath);
          if (!newEdges.length) return prev;
          const updated = { ...prev };
          let ref: any = updated;
          const keys = edgesPath.split(".");
          for (let i = 0; i < keys.length - 1; i++) {
            ref[keys[i]] = { ...ref[keys[i]] };
            ref = ref[keys[i]];
          }
          ref[keys[keys.length - 1]] = [...prevEdges, ...newEdges];

          let refPage: any = updated;
          const pageKeys = pageInfoPath.split(".");
          for (let i = 0; i < pageKeys.length - 1; i++) {
            refPage[pageKeys[i]] = { ...refPage[pageKeys[i]] };
            refPage = refPage[pageKeys[i]];
          }
          refPage[pageKeys[pageKeys.length - 1]] = newPageInfo;

          return updated;
        },
      });
    }
  };

  React.useEffect(() => {
    refetch({ searchText: searchTextDebounced } as any);
  }, [refetch, searchTextDebounced]);
  const theme = useTheme();
  const borderColor = theme.palette.mode === "dark" ? "rgba(255, 255, 255, 0.12)" : "rgba(0, 0, 0, 0.12)";

  return (
    <Container>
      {viewDeleteModal.isView && deleted && (
        <ModalConfirm
          title={deleted?.title}
          body={deleted?.messsage}
          labelConfirm="Delete"
          actionConfirm={() => {
            deleted?.actionDeleted(viewDeleteModal.id, viewDeleteModal.name);
          }}
          confirmationWord={viewDeleteModal.name}
          close={() => setViewDeleteModal({ isView: false, name: "", id: "" })}
        />
      )}

      <div
        style={{
          display: "flex",
          flexDirection: "column",
          width: "100%",
          background: "raop",
        }}
      >
        <div
          style={{
            display: "flex",
            justifyContent: "flex-end",
            marginBottom: "16px",
          }}
        >
          <TextField
            id="basicInputTypeText"
            placeholder="Search"
            type="text"
            value={searchText}
            onChange={(e) => setSearchText(e.currentTarget.value)}
            variant="outlined"
            InputProps={{
              startAdornment: (
                <InputAdornment position="start">
                  <SearchIcon style={{ color: "lightgray" }} />
                </InputAdornment>
              ),
            }}
            style={{ width: "200px" }}
          />
        </div>
        <TableVirtuoso
          hidden={showSelectedItemsTable}
          totalCount={field(data)?.edges?.length}
          style={{
            height: "80vh",
            backgroundColor: theme.palette.background.paper,
            border: `1px solid ${borderColor}`,
            borderRadius: "8px",
          }}
          components={{
            Table: (props) => (
              <TableMaterial {...props} style={{ ...props.style, tableLayout: "fixed", position: "relative" }} />
            ),
            TableBody: TableBody,
            TableHead: TableHead,
            TableRow: TableRow,
            EmptyPlaceholder: () => (
              <TableRow>
                <TableCell colSpan={columns.length + 1 + (isItemsSelectable ? 1 : 0)}>
                  <EmptySpace
                    description="There are no matching unassociated entities"
                    title="No entities"
                    extraClass="c-empty-state-animation"
                  />
                </TableCell>
              </TableRow>
            ),
          }}
          fixedHeaderContent={() => (
            <>
              {isItemsSelectable && <TableCell style={{ width: "40px" }} />}
              {columns.map((column, index) => (
                <TableCell key={index}>
                  <Typography variant="subtitle2">{column.header}</Typography>
                </TableCell>
              ))}
              <TableCell>
                <Typography variant="subtitle2">Actions</Typography>
              </TableCell>
            </>
          )}
          itemContent={(index) => {
            const row = field(data)?.edges?.[index]?.node ?? undefined;
            return (
              <React.Fragment>
                {columns.map((column, colIndex) => (
                  <TableCell
                    key={colIndex}
                    sx={{
                      maxWidth: 0,
                      "& > *": {
                        overflow: "hidden",
                        textOverflow: "ellipsis",
                        whiteSpace: "nowrap",
                      },
                    }}
                    ref={(el: HTMLTableCellElement | null) => {
                      if (el) {
                        const child = el.firstElementChild as HTMLElement | null;
                        if (child) {
                          el.title = child.scrollWidth > child.clientWidth ? el.textContent || "" : "";
                        }
                      }
                    }}
                  >
                    {column.content(row)}
                  </TableCell>
                ))}
                <TableCell>
                  <Box
                    sx={{
                      display: "flex",
                      justifyContent: "flex-start",
                      alignItems: "center",
                      gap: "30px",
                    }}
                  >
                    {(() => {
                      const allActions = [
                        ...rowActions.map((rowAction, indexMap) => {
                          const isActive = !rowAction.isDisabled || Boolean(rowAction.isDisabled(row));
                          return {
                            key: indexMap,
                            label: rowAction.label,
                            isActive,
                            onClick: () => {
                              if (isActive) rowAction.action(row);
                            },
                          };
                        }),
                        ...(deleted
                          ? [
                              {
                                key: "delete" as const,
                                label: "Delete",
                                isActive: true,
                                onClick: () => {
                                  setViewDeleteModal({
                                    isView: true,
                                    name: row?.name || "Unnamed",
                                    id: row?.id || "",
                                  });
                                },
                              },
                            ]
                          : []),
                      ];

                      const shouldCollapse =
                        maxVisibleActions !== undefined && allActions.length > maxVisibleActions;
                      const visibleActions = shouldCollapse
                        ? allActions.slice(0, maxVisibleActions)
                        : allActions;

                      return (
                        <>
                          {visibleActions.map((action) => (
                            <Button
                              key={action.key}
                              sx={{
                                border: "none",
                                background: "none",
                                padding: "0",
                                textDecoration: "none",
                                cursor: !action.isActive ? "not-allowed" : "pointer",
                                color: !action.isActive ? "gray" : "unset",
                                "&:hover": {
                                  textDecoration: !action.isActive ? "none" : "underline",
                                },
                                fontSize: "14px",
                                fontWeight: "400",
                                minWidth: "unset",
                                textTransform: "none",
                              }}
                              onClick={action.onClick}
                            >
                              {action.label}
                            </Button>
                          ))}
                          {shouldCollapse && (
                            <Box sx={{ position: "relative", display: "inline-block" }}>
                              <IconButton
                                size="small"
                                onClick={() =>
                                  setOpenMenuIndex(openMenuIndex === index ? null : index)
                                }
                              >
                                <MoreVertIcon fontSize="small" />
                              </IconButton>
                              {openMenuIndex === index && (
                                <Box
                                  sx={{
                                    position: "absolute",
                                    top: "100%",
                                    right: 0,
                                    zIndex: 1300,
                                    bgcolor: "background.paper",
                                    borderRadius: "4px",
                                    boxShadow: 3,
                                    py: 0.5,
                                    minWidth: "120px",
                                  }}
                                >
                                  {allActions.slice(maxVisibleActions).map((action) => (
                                    <Box
                                      key={action.key}
                                      sx={{
                                        px: 2,
                                        py: 1,
                                        cursor: !action.isActive ? "not-allowed" : "pointer",
                                        color: !action.isActive ? "text.disabled" : "text.primary",
                                        fontSize: "14px",
                                        "&:hover": {
                                          bgcolor: action.isActive ? "action.hover" : "transparent",
                                        },
                                      }}
                                      onClick={() => {
                                        if (action.isActive) action.onClick();
                                        setOpenMenuIndex(null);
                                      }}
                                    >
                                      {action.label}
                                    </Box>
                                  ))}
                                </Box>
                              )}
                            </Box>
                          )}
                        </>
                      );
                    })()}
                  </Box>
                </TableCell>
              </React.Fragment>
            );
          }}
          endReached={() => {
            loadMoreResults && loadMoreResults();
          }}
        />
      </div>
    </Container>
  );
}

export function getByPath(obj: any, path: string): any {
  return path.split(".").reduce((acc, key) => acc?.[key], obj);
}

