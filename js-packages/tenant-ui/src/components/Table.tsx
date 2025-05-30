import React from "react";
import { QueryResult } from "@apollo/client";
import { TableVirtuoso } from "react-virtuoso";
import { Link } from "react-router-dom";
import SearchIcon from "@mui/icons-material/Search";
import {
  TextField,
  Table as TableMaterial,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Container,
  Typography,
  InputAdornment,
  useTheme,
  Box,
  Button,
} from "@mui/material";
import { ModalConfirm } from "./ModalConfirm";

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
  Parameters extends { searchText?: string | null; cursor?: string | null }
>({
  data: {
    queryResult: { data, fetchMore, refetch },
    field,
  },
  isItemsSelectable,
  columns,
  rowActions,
  deleted,
  loadMoreResults,
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
  loadMoreResults?: () => void;
  deleted?:
    | {
        actionDeleted: (id: string, name: string) => void;
        messsage: string;
        wordConfirm: string;
        title: string;
      }
    | undefined;
  onCreatePath: string;
  rowActions: Array<{ label: string; action(suggestionCategory?: any): void; isDisabled?: (dat: any) => boolean }>;
}) {
  const [searchText, setSearchText] = React.useState("");
  const [showSelectedItemsTable] = React.useState(false);
  const [viewDeleteModal, setViewDeleteModal] = React.useState({ isView: false, name: "", id: "" });

  React.useEffect(() => {
    refetch({ searchText: searchText } as any);
  }, [refetch, searchText]);
  const theme = useTheme();
  const borderColor = theme.palette.mode === "dark" ? "rgba(255, 255, 255, 0.12)" : "rgba(0, 0, 0, 0.12)";

  return (
    <Container style={{ paddingInline: "0px" }}>
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
            Table: (props) => <TableMaterial {...props} style={{ ...props.style, tableLayout: "fixed", position: "relative" }} />,
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
                  <TableCell key={colIndex}>{column.content(row)}</TableCell>
                ))}
                <TableCell>
                  <Box sx={{ display: "flex", justifyContent: "flex-start", alignItems: "center", gap: "10px" }}>
                    {rowActions.map((rowAction, indexMap) => {
                      const row = rowAction.isDisabled === undefined;
                      const isActive = row || (rowAction?.isDisabled && rowAction.isDisabled(field(data)?.edges?.[index]?.node));
                      return (
                        <Button
                          key={indexMap}
                          sx={{
                            border: "none",
                            background: "none",
                            padding: "0",
                            marginLeft: indexMap > 0 ? "20px" : "unset",
                            textDecoration: "none",
                            cursor: !isActive ? "not-allowed" : "pointer",
                            color: !isActive ? "gray" : "unset",
                            "&:hover": {
                              textDecoration: !isActive ? "none" : "underline",
                            },
                            fontSize: "14px",
                            fontWeight: "400",
                            minWidth: "unset",
                            textTransform: "none",
                          }}
                          onClick={() => {
                            if (isActive) {
                              rowAction.action(field(data)?.edges?.[index]?.node);
                            }
                          }}
                        >
                          {rowAction.label}
                        </Button>
                      );
                    })}
                    {deleted && (
                      <Box
                        sx={{
                          border: "none",
                          background: "none",
                          padding: "0",
                          marginLeft: "20px",
                          textDecoration: "none",
                          cursor: "pointer",
                          color: "unset",
                          "&:hover": {
                            textDecoration: "underline",
                          },
                        }}
                        onClick={() => {
                          setViewDeleteModal({ isView: true, name: row?.name || "Unnamed", id: row?.id || "" });
                        }}
                      >
                        Delete
                      </Box>
                    )}
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

export function EmptySpace({ title, description, extraClass = "" }: { title: string; description: string; extraClass?: string }) {
  const classNames = `empty-state ${extraClass}`;
  return (
    <div
      className={classNames}
      style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        height: "30vh",
        flexDirection: "column",
      }}
    >
      <div>
        <h3 className="empty-state-title">{title}</h3>
      </div>
      <div className="empty-state-description">{description}</div>
    </div>
  );
}

export function formatVirtualHost(value: { id?: string | null; virtualHost?: string | null } | null | undefined) {
  return (
    value?.id && (
      <Link
        style={{ color: "#da1414", textDecoration: "none", font: "Helvetica", fontWeight: "700", fontSize: "15px", lineHeight: "44px" }}
        to={"" + value.id}
      >
        {value.virtualHost}
      </Link>
    )
  );
}
