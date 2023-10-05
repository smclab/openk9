import { ClayButtonWithIcon } from "@clayui/button";
import ClayIcon from "@clayui/icon";
import ClayToolbar from "@clayui/toolbar";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import { css } from "styled-components/macro";
import { gql } from "@apollo/client";
import {
  useAddEnrichItemToEnrichPipelineMutation,
  useAssociatedEnrichPipelineEnrichItemsQuery,
  useRemoveEnrichItemFromEnrichPipelineMutation,
  useSortEnrichItemsMutation,
  useUnassociatedEnrichPipelineEnrichItemsQuery,
} from "../graphql-generated";
import ClayList from "@clayui/list";
import ClayModal, { useModal } from "@clayui/modal";
import { ClayInput } from "@clayui/form";
import useDebounced from "./useDebounced";
import { ClassNameButton } from "../App";
import { ContainerFluidWithoutView, CustomButtomClay, CustomFormGroup, EmptySpace } from "./Form";
import { ClayDropDownWithItems } from "@clayui/drop-down";

export const AssociatedEnrichPipelineEnrichItemsQuery = gql`
  query AssociatedEnrichPipelineEnrichItems($enrichPipelineId: ID!) {
    enrichPipeline(id: $enrichPipelineId) {
      id
      enrichItems {
        edges {
          node {
            id
            name
            description
          }
        }
        pageInfo {
          hasNextPage
          endCursor
        }
      }
    }
  }
`;

export const UnassociatedEnrichPipelineEnrichItemsQuery = gql`
  query UnassociatedEnrichPipelineEnrichItems($enrichPipelineId: ID!, $searchText: String) {
    enrichPipeline(id: $enrichPipelineId) {
      id
      enrichItems(searchText: $searchText, not: true, first: 25) {
        edges {
          node {
            id
            name
            description
          }
        }
        pageInfo {
          hasNextPage
          endCursor
        }
      }
    }
  }
`;

gql`
  mutation AddEnrichItemToEnrichPipeline($childId: ID!, $parentId: ID!) {
    addEnrichItemToEnrichPipeline(enrichItemId: $childId, enrichPipelineId: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

gql`
  mutation RemoveEnrichItemFromEnrichPipeline($childId: ID!, $parentId: ID!) {
    removeEnrichItemFromEnrichPipeline(enrichItemId: $childId, enrichPipelineId: $parentId) {
      left {
        id
      }
      right {
        id
      }
    }
  }
`;

gql`
  mutation SortEnrichItems($enrichPipelineId: ID!, $enrichItemIdList: [BigInteger]) {
    sortEnrichItems(enrichPipelineId: $enrichPipelineId, enrichItemIdList: $enrichItemIdList) {
      id
      enrichItems {
        edges {
          node {
            id
            name
            description
          }
        }
      }
    }
  }
`;

export function EnrichPipelineEnrichItems() {
  const { enrichPipelineId } = useParams();
  const associatedListQuery = useAssociatedEnrichPipelineEnrichItemsQuery({
    variables: { enrichPipelineId: enrichPipelineId! },
  });
  const items =
    associatedListQuery.data?.enrichPipeline?.enrichItems?.edges?.flatMap((edge) => {
      if (!edge) return [];
      return [{ id: edge.node?.id ?? "", name: edge.node?.name ?? "", description: edge.node?.description ?? "" }];
    }) ?? [];
  const itemHeigthPx = 55;
  const verticalOffset = 32;
  const [movingItemIndex, setMovingItemIndex] = React.useState<number | null>(null);
  const [pointerPosition, setPointerPosition] = React.useState({ top: 0, left: 0 });
  const targetItemIndex = Math.trunc((pointerPosition.top - verticalOffset) / itemHeigthPx);
  const reorderedItems = movingItemIndex !== null ? moveItem(movingItemIndex, targetItemIndex, items) : items;
  const { observer, onOpenChange, open } = useModal();
  const [searchText, setSearchText] = React.useState("");
  const searchTextDebounced = useDebounced(searchText);
  const unassociatedListQuery = useUnassociatedEnrichPipelineEnrichItemsQuery({
    variables: { enrichPipelineId: enrichPipelineId!, searchText: searchTextDebounced },
  });
  const [addMutate, addMutation] = useAddEnrichItemToEnrichPipelineMutation({
    refetchQueries: [AssociatedEnrichPipelineEnrichItemsQuery, UnassociatedEnrichPipelineEnrichItemsQuery],
  });
  const [removeMutate, removeMutation] = useRemoveEnrichItemFromEnrichPipelineMutation({
    refetchQueries: [AssociatedEnrichPipelineEnrichItemsQuery, UnassociatedEnrichPipelineEnrichItemsQuery],
  });
  const [sortMutate, sortMutation] = useSortEnrichItemsMutation({
    refetchQueries: [AssociatedEnrichPipelineEnrichItemsQuery],
  });
  const navigate = useNavigate();
  const canAct = !addMutation.loading && !removeMutation.loading && !sortMutation.loading;
  if (!enrichPipelineId || enrichPipelineId === "new") return null;
  return (
    <ContainerFluidWithoutView>
      <ClayToolbar>
        <ContainerFluidWithoutView>
          <ClayToolbar.Nav>
            <ClayToolbar.Item className="text-left" expand>
              <ClayToolbar.Section>
                <label className="component-title">Associated Enrich Items</label>
              </ClayToolbar.Section>
            </ClayToolbar.Item>
            <ClayToolbar.Item>
              <CustomButtomClay
                className="btn btn-danger btn btn-monospaced btn-sm btn-primary"
                label={
                  <svg className="lexicon-icon lexicon-icon-plus" role="presentation">
                    <use xlinkHref="/admin/static/media/icons.9f9b3ff4d13e0ff2f5d340dee4c72dd0.svg#plus"></use>
                  </svg>
                }
                action={() => onOpenChange(true)}
              />
            </ClayToolbar.Item>
          </ClayToolbar.Nav>
        </ContainerFluidWithoutView>
      </ClayToolbar>
      <ul
        className="list-group show-quick-actions-on-hover flex-grow-1"
        css={css`
          height: ${itemHeigthPx * items.length + verticalOffset * 2}px;
          position: relative;
          user-select: none;
          overflow: hidden;
        `}
        onMouseMove={(event) => {
          const offset = event.currentTarget.getBoundingClientRect();
          const top = event.clientY - offset.top;
          const left = event.clientX - offset.left;
          setPointerPosition({ top, left });
        }}
        onMouseUp={() => {
          setMovingItemIndex(null);
          sortMutate({
            variables: {
              enrichPipelineId,
              enrichItemIdList: reorderedItems.map(({ id }) => Number(id)),
            },
            optimisticResponse: {
              sortEnrichItems: {
                __typename: "EnrichPipeline",
                id: enrichPipelineId,
                enrichItems: {
                  __typename: "DefaultConnection_EnrichItem",
                  edges: reorderedItems.map(({ id, name, description }) => {
                    return {
                      __typename: "DefaultEdge_EnrichItem",
                      node: {
                        __typename: "EnrichItem",
                        id,
                        name,
                        description,
                      },
                    };
                  }),
                },
              },
            },
          });
        }}
        onMouseLeave={() => {
          setMovingItemIndex(null);
        }}
      >
        <li
          className="list-group-header"
          css={css`
            position: absolute;
            width: 100%;
            height: ${verticalOffset}px;
            box-sizing: border-box;
            top: 1px;
          `}
        >
          <h3 className="list-group-header-title c-ml-sm-2">Input</h3>
        </li>
        {reorderedItems.map((item, index) => {
          if (movingItemIndex !== null && index === targetItemIndex) return null;
          return (
            <li
              className="list-group-item"
              key={item.id}
              css={css`
                display: flex;
                align-items: center;
                position: absolute;
                width: 100%;
                height: ${itemHeigthPx}px;
                top: ${itemHeigthPx * index + verticalOffset}px;
                transition: 0.3s;
              `}
            >
              <ClayIcon
                symbol="drag"
                className="c-mr-sm-2 c-ml-sm-2"
                style={{ cursor: "grab" }}
                onMouseDown={(event) => {
                  setMovingItemIndex(index);
                }}
              />
              <div
                className="autofit-col autofit-col-expand"
                style={{ cursor: "grab" }}
                onMouseDown={(event) => {
                  setMovingItemIndex(index);
                }}
              >
                <p className="list-group-title text-truncate">{item.name}</p>
                <p className="list-group-subtitle text-truncate">{item.description}</p>
              </div>
              <ItemActions
                actions={[
                  {
                    label: "Go to",
                    icon: "tap-ahead",
                    onClick() {
                      navigate(`/enrich-items/${item.id}`);
                    },
                  },
                  {
                    label: "Remove",
                    icon: "times",
                    onClick() {
                      removeMutate({ variables: { parentId: enrichPipelineId, childId: item.id } });
                    },
                  },
                ]}
              />
            </li>
          );
        })}
        <li
          className="list-group-header"
          css={css`
            position: absolute;
            width: 100%;
            height: ${verticalOffset}px;
            box-sizing: border-box;
            bottom: 2px;
          `}
        >
          <h3 className="list-group-header-title c-ml-sm-2">Output</h3>
        </li>
        <svg
          style={{
            width: "12px",
            height: `${items.length * itemHeigthPx + verticalOffset * 2}px`,
            position: "absolute",
            top: verticalOffset * 0.5,
            left: 8,
          }}
        >
          <defs>
            <marker id="arrowhead" markerWidth="12" markerHeight="8" refX="6" refY="8">
              <polygon points="0,0 12,0 6,8"></polygon>
            </marker>
          </defs>
          <line
            x1="6"
            y1="0"
            x2="6"
            y2={items.length * itemHeigthPx + verticalOffset}
            stroke="black"
            opacity="0.5"
            strokeDasharray="4"
            markerEnd="url(#arrowhead)"
          ></line>
        </svg>
        {movingItemIndex !== null && (
          <div
            className="list-group-item card"
            css={css`
              display: flex;
              position: absolute;
              width: 100%;
              height: ${itemHeigthPx}px;
              background-color: white;
              align-items: center;
              cursor: grabbing;
            `}
            style={{
              top: `${pointerPosition.top - itemHeigthPx * 0.5}px`,
            }}
          >
            <ClayIcon symbol="drag" className="c-mr-sm-2" />
            <div className="autofit-col autofit-col-expand">
              <p className="list-group-title text-truncate">{items[movingItemIndex].name}</p>
              <p className="list-group-subtitle text-truncate">{items[movingItemIndex].description}</p>
            </div>
          </div>
        )}
      </ul>
      {open && (
        <ClayModal observer={observer}>
          <ClayModal.Header>Add Enrich Item to Enrich Pipeline</ClayModal.Header>
          <ClayModal.Body>
            <CustomFormGroup>
              <ClayInput
                type="search"
                placeholder="search"
                value={searchText}
                onChange={(event) => setSearchText(event.currentTarget.value)}
              />
            </CustomFormGroup>
            {(unassociatedListQuery.data?.enrichPipeline?.enrichItems?.edges?.length ?? 0) === 0 && !unassociatedListQuery.loading && (
              <EmptySpace description="There are no matching unassociated entities" title="No entities" />
            )}
            <div style={{ overflow: "auto", maxHeight: "400px" }}>
              <ClayList showQuickActionsOnHover>
                {
                  unassociatedListQuery.data?.enrichPipeline?.enrichItems?.edges?.map((edge) => {
                    return (
                      <ClayList.Item flex key={edge?.node?.id}>
                        <ClayList.ItemField expand>
                          <ClayList.ItemTitle>{edge?.node?.name || "..."}</ClayList.ItemTitle>
                          <ClayList.ItemText>{edge?.node?.description || "..."}</ClayList.ItemText>
                        </ClayList.ItemField>
                        <ClayList.ItemField>
                          <ClayList.QuickActionMenu>
                            {canAct && (
                              <ClayList.QuickActionMenu.Item
                                onClick={() => {
                                  if (edge?.node?.id) {
                                    addMutate({ variables: { parentId: enrichPipelineId, childId: edge.node.id } });
                                  }
                                }}
                                symbol="link"
                              />
                            )}
                          </ClayList.QuickActionMenu>
                        </ClayList.ItemField>
                      </ClayList.Item>
                    );
                  }) as any
                }
              </ClayList>
            </div>
          </ClayModal.Body>
        </ClayModal>
      )}
    </ContainerFluidWithoutView>
  );
}

function moveItem<T>(fromIndex: number, toIndex: number, array: Array<T>): Array<T> {
  if (fromIndex < toIndex) {
    return [...array.slice(0, fromIndex), ...array.slice(fromIndex + 1, toIndex + 1), array[fromIndex], ...array.slice(toIndex + 1)];
  }
  if (fromIndex > toIndex) {
    return [...array.slice(0, toIndex), array[fromIndex], ...array.slice(toIndex, fromIndex), ...array.slice(fromIndex + 1)];
  }
  return array;
}
function ItemActions({ actions }: { actions: Array<{ label: string; icon: string; onClick: () => void }> }) {
  return (
    <React.Fragment>
      <div style={{ position: "relative" }}>
        <div style={{ display: "flex", alignItems: "center" }}>
          {actions.map((action, index) => {
            return (
              <ClayButtonWithIcon
                aria-label=""
                key={index}
                symbol={action.icon}
                className="component-action quick-action-item"
                onClick={action.onClick}
              />
            );
          })}
        </div>
      </div>
      <ClayDropDownWithItems
        trigger={<ClayButtonWithIcon aria-label="" symbol="ellipsis-v" className="component-action" />}
        items={actions.map((action) => ({
          label: action.label,
          onClick: action.onClick,
        }))}
      />
    </React.Fragment>
  );
}
