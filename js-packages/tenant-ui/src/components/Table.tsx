import React from "react";
import { QueryResult } from "@apollo/client";
import ClayTable from "@clayui/table";
import { TableVirtuoso } from "react-virtuoso";
import ClayToolbar from "@clayui/toolbar";
import ClayLayout from "@clayui/layout";
import ClayButton, { ClayButtonWithIcon } from "@clayui/button";
import useDebounced from "./useDebounced";
import { ClayDropDownWithItems } from "@clayui/drop-down";
import { Link } from "react-router-dom";
import ClayEmptyState from "@clayui/empty-state";
import { ClayCheckbox } from "@clayui/form";
import useMap from "./useMap";
import { ClayToggle } from "@clayui/form";
import ClayModal, { useModal } from "@clayui/modal";
import { useRestClient } from "./queryClient";
import { RequestId } from "../openapi-generated";
export function formatName(value: { id?: string | null; name?: string | null } | null | undefined) {
  return value?.id && <Link to={value.id}>{value.name}</Link>;
}
export function formatVirtualHost(value: { id?: string | null; virtualHost?: string | null } | null | undefined) {
  return value?.id && <Link to={"" + value.id}>{value.virtualHost}</Link>;
}
export function formatBoolean(value: boolean | undefined) {
  switch (value) {
    case true:
      return "yes";
    case false:
      return "no";
  }
}

export function formatDate(value: any) {
  return dateTimeFormatter.format(new Date(value));
}
const dateTimeFormatter = Intl.DateTimeFormat([], {
  dateStyle: "medium",
  timeStyle: "medium",
});
type isSelectedType = {
  selected: boolean;
  id: string;
};

export function Table<
  Query,
  Row extends { id?: string | null },
  Parameters extends { searchText?: string | null; cursor?: string | null }
>({
  data: {
    queryResult: { data, fetchMore, refetch },
    field,
  },
  isItemsSelectable,
  columns,
  onCreatePath,
  onDelete,
  rowActions = () => [],
  rowsActions = () => [],
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
  columns: Array<{ header: React.ReactNode; content(row: Row | undefined): React.ReactNode }>;
  onDelete(row: Row | undefined): void;
  onCreatePath: string;
  rowActions?(row: Row | undefined): Array<{ label: string; icon: string; onClick(): void }>;
  rowsActions?(rows: Array<Row>): Array<{ label: string; onClick(): void }>;
}) {
  const [searchText, setSearchText] = React.useState("");
  const { observer, onOpenChange, open } = useModal();
  const restClient = useRestClient();
  let test: RequestId;
  const searchTextDebounced = useDebounced(searchText);
  const selection = useMap<Row>();
  const [valueModal, setValueModal] = React.useState("");
  const [showSelectedItemsTable, setShowSelectedItemsTable] = React.useState(false);
  React.useEffect(() => {
    refetch({ searchText: searchTextDebounced } as any);
  }, [refetch, searchTextDebounced]);
  const scrollerRef = React.useRef<HTMLElement>();
  return (
    <React.Fragment>
      {open && (
        <ClayModal observer={observer}>
          <ClayModal.Header>{"Create Tenant"}</ClayModal.Header>
          <ClayModal.Body>
            <div className="form-group-item">
              <label style={{ paddingTop: "18px" }}>Virtualhost</label>
              <input
                type="text"
                className="form-control"
                value={valueModal}
                onChange={(event) => {
                  setValueModal(event.currentTarget.value);
                }}
              ></input>
            </div>
          </ClayModal.Body>
          <ClayModal.Footer
            first={<ClayButton.Group spaced></ClayButton.Group>}
            last={
              <ClayButton
                onClick={async () => {
                  test = await restClient.tenantManagerResource.postTenantManagerTenant({ virtualHost: valueModal });
                  console.log(test);
                }}
              >
                {"Create"}
              </ClayButton>
            }
          />
        </ClayModal>
      )}
      <ClayLayout.ContainerFluid view>
        <table hidden={!showSelectedItemsTable} className="table table-list" style={{ tableLayout: "fixed" }}>
          <thead>
            <tr>
              <ClayTable.Cell headingCell style={{ width: "40px" }} />
              {columns.map((column, index) => {
                return (
                  <ClayTable.Cell key={index} headingCell headingTitle>
                    {column.header && <span className="text-truncate">{column.header}</span>}
                  </ClayTable.Cell>
                );
              })}
              <ClayTable.Cell headingCell style={{ width: "56px" }} />
            </tr>
          </thead>
          <tbody>
            {selection.entries.map(([id, row]) => {
              return (
                <tr key={id}>
                  <ClayTable.Cell>
                    <ClayCheckbox
                      checked={true}
                      onChange={() => {
                        selection.rem(id);
                        if (selection.size === 1) {
                          setShowSelectedItemsTable(false);
                        }
                      }}
                    />
                  </ClayTable.Cell>
                  {columns.map((column, index) => {
                    return <ClayTable.Cell key={index}>{column.content(row)}</ClayTable.Cell>;
                  })}
                  <ClayTable.Cell style={{ height: "57px" }} />
                </tr>
              );
            })}
          </tbody>
        </table>
        <TableVirtuoso
          hidden={showSelectedItemsTable}
          totalCount={field(data)?.edges?.length}
          scrollerRef={(element) => (scrollerRef.current = element as any)}
          style={{ height: "80vh" }}
          components={{
            Table: (props) => (
              <table
                {...props}
                style={{ ...props.style, tableLayout: "fixed" }}
                className="table table-hover show-quick-actions-on-Hover table-list"
              />
            ),
            TableBody: ClayTable.Body,
            TableHead: ClayTable.Head,
            TableRow: ClayTable.Row,
            EmptyPlaceholder: () => (
              <tbody>
                <tr>
                  <td colSpan={columns.length + 1 + (isItemsSelectable ? 1 : 0)} style={{ backgroundColor: "white" }}>
                    <ClayEmptyState description="There are no matching entities" title="No entities" className="c-empty-state-animation" />
                  </td>
                </tr>
              </tbody>
            ),
          }}
          fixedHeaderContent={() => (
            <ClayTable.Row>
              {isItemsSelectable && <ClayTable.Cell headingCell style={{ width: "40px" }} />}
              {columns.map((column, index) => {
                return (
                  <ClayTable.Cell headingCell headingTitle key={index}>
                    {column.header && <span className="text-truncate">{column.header}</span>}
                  </ClayTable.Cell>
                );
              })}
              <ClayTable.Cell headingCell style={{ width: "56px" }}>
                <ClayButtonWithIcon
                  symbol="plus"
                  small
                  onClick={() => {
                    onOpenChange(true);
                  }}
                />
              </ClayTable.Cell>
            </ClayTable.Row>
          )}
          itemContent={(index) => {
            const row = field(data)?.edges?.[index]?.node ?? undefined;
            return (
              <React.Fragment>
                {isItemsSelectable && (
                  <ClayTable.Cell>
                    <ClayCheckbox
                      checked={row?.id ? selection.has(row.id) : false}
                      onChange={() => {
                        if (row?.id) {
                          if (selection.has(row.id)) {
                            selection.rem(row.id);
                          } else {
                            selection.set(row.id, row);
                          }
                        }
                      }}
                    />
                  </ClayTable.Cell>
                )}
                {columns.map((column, index) => {
                  return <ClayTable.Cell key={index}>{column.content(row)}</ClayTable.Cell>;
                })}
                <ClayTable.Cell>
                  <TableRowActions
                    actions={[
                      ...rowActions(row),
                      {
                        label: "Delete",
                        icon: "trash",
                        onClick: () => onDelete(row),
                      },
                    ]}
                  />
                </ClayTable.Cell>
              </React.Fragment>
            );
          }}
          endReached={() => {
            if (field(data)?.pageInfo?.hasNextPage) {
              fetchMore({
                variables: {
                  cursor: field(data)?.pageInfo?.endCursor,
                },
              });
            }
          }}
          isScrolling={(isScrolling) => {
            if (scrollerRef.current) {
              if (isScrolling) {
                scrollerRef.current.style.pointerEvents = "none";
              } else {
                scrollerRef.current.style.pointerEvents = "auto";
              }
            }
          }}
        />
      </ClayLayout.ContainerFluid>
    </React.Fragment>
  );
}

function TableRowActions({ actions }: { actions: Array<{ label: string; icon: string; onClick: () => void }> }) {
  return (
    <React.Fragment>
      <div className="quick-action-menu" style={{ alignItems: "center" }}>
        {actions.map((action, index) => {
          if (action.icon !== "") {
            return (
              <ClayButtonWithIcon
                key={index}
                symbol={action.icon}
                className="component-action quick-action-item"
                onClick={action.onClick}
              />
            );
          }
        })}
      </div>

      <ClayDropDownWithItems
        trigger={<ClayButtonWithIcon symbol="ellipsis-v" className="component-action" />}
        items={actions.map((action) => ({
          label: action.label,
          onClick: action.onClick,
        }))}
      />
    </React.Fragment>
  );
}

function TableRowActionsSubFields({ actions }: { actions: Array<{ label: string; icon: string; onClick: () => void }> }) {
  return (
    <React.Fragment>
      <span>
        {actions.map((action, index) => {
          if (action.icon !== "") {
            return <ClayButtonWithIcon key={index} className="component-action " symbol={action.icon} onClick={action.onClick} />;
          }
        })}
      </span>
      <span>
        <ClayDropDownWithItems
          trigger={<ClayButtonWithIcon symbol="ellipsis-v" className="component-action" />}
          items={actions.map((action) => ({
            label: action.label,
            onClick: action.onClick,
          }))}
        />
      </span>
    </React.Fragment>
  );
}
function TableRowsActions({ actions }: { actions: Array<{ label: string; disabled?: boolean; onClick: () => void }> }) {
  return (
    <ClayButton.Group>
      {actions.map((action, index) => {
        return (
          <ClayButton key={index} onClick={action.onClick} small displayType="secondary" disabled={action.disabled}>
            {action.label}
          </ClayButton>
        );
      })}
      <ClayDropDownWithItems trigger={<ClayButtonWithIcon symbol="ellipsis-v" displayType="secondary" />} items={actions} />
    </ClayButton.Group>
  );
}

export function TableWithSubFields<
  Query,
  Row extends { id?: string | null },
  Parameters extends { searchText?: string | null; cursor?: string | null }
>({
  data: {
    queryResult: { data, fetchMore, refetch },
    field,
  },
  isItemsSelectable,
  columns,
  subColumns,
  complessColumns,
  onCreatePath,
  onDelete,
  rowActions = () => [],
  rowsActions = () => [],
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
  columns: Array<{ header: React.ReactNode; content(row: Row | undefined): React.ReactNode }>;
  complessColumns: string;
  subColumns: string;
  onDelete(row: Row | undefined): void;
  onCreatePath: string;
  rowActions?(row: Row | undefined): Array<{ label: string; icon: string; onClick(): void }>;
  rowsActions?(rows: Array<Row>): Array<{ label: string; onClick(): void }>;
}) {
  const [isSelected, setIsSelected] = React.useState<isSelectedType>({ id: "", selected: false });

  return (
    <React.Fragment>
      <ClayLayout.ContainerFluid view>
        <ClayTable className="table table-list" style={{ tableLayout: "fixed" }}>
          <ClayTable.Head>
            <ClayTable.Row>
              <ClayTable.Cell headingCell headingTitle>
                <span className="text-truncate">Name</span>
              </ClayTable.Cell>
              {columns.map((column, index) => {
                return (
                  <ClayTable.Cell key={index + "column"} headingCell headingTitle>
                    <span className="text-truncate" key={"spanHead" + index}>
                      {column.header}
                    </span>
                  </ClayTable.Cell>
                );
              })}
              <ClayTable.Cell headingCell headingTitle>
                <span className="text-truncate"></span>
              </ClayTable.Cell>
            </ClayTable.Row>
          </ClayTable.Head>
          <ClayTable.Body>
            {field(data)?.edges?.map((rows, index) => {
              const row = field(data)?.edges?.[index]?.node ?? undefined;
              return (
                <React.Fragment key={"fragment" + index}>
                  <ClayTable.Row key={"row" + index}>
                    <ClayTable.Cell key={index + "printData" + row?.id}>
                      <div key={index + "spanPrintData" + row?.id} className="text-truncate">
                        {JSON.parse(JSON.stringify(field(data)?.edges?.[index]?.node))[complessColumns]}
                      </div>
                      <span className={"label label-success process-status"}>
                        {JSON.parse(JSON.stringify(field(data)?.edges?.[index]?.node))[subColumns]}
                      </span>
                    </ClayTable.Cell>
                    {columns.map((column, index) => {
                      return (
                        <React.Fragment key={"fragmentfirst" + index}>
                          <ClayTable.Cell key={index + "printData" + row?.id}>
                            <span key={index + "spanPrintData" + row?.id} className="text-truncate">
                              {column.content(row)}
                            </span>
                          </ClayTable.Cell>
                        </React.Fragment>
                      );
                    })}
                    <ClayTable.Cell className="table-column-text-end" key={index + "button" + row?.id}>
                      <div
                        key={index + "divButton" + row?.id}
                        style={{ listStyle: "none", display: "flex", alignItems: "center", justifyContent: "right" }}
                      >
                        <span key={index + "subField" + row?.id}>
                          <ClayButtonWithIcon
                            className="component-action "
                            key={"button" + index}
                            symbol={isSelected.id === row?.id && isSelected.selected ? "angle-up-small" : "angle-down-small"}
                            onClick={() => {
                              if (isSelected.id !== row?.id) {
                                setIsSelected({ id: row?.id || "", selected: true });
                              } else {
                                setIsSelected({ id: row?.id || "", selected: !isSelected.selected });
                              }
                            }}
                          ></ClayButtonWithIcon>
                        </span>

                        <TableRowActionsSubFields
                          actions={[
                            ...rowActions(row),
                            {
                              label: "Delete",
                              icon: "",
                              onClick: () => onDelete(row),
                            },
                          ]}
                        />
                      </div>
                    </ClayTable.Cell>
                  </ClayTable.Row>
                </React.Fragment>
              );
            })}
          </ClayTable.Body>
        </ClayTable>
      </ClayLayout.ContainerFluid>
    </React.Fragment>
  );
}
