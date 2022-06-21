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

import React from "react";
import { createUseStyles } from "react-jss";
import { useRouter } from "next/router";
import { ThemeType } from "../../../components/theme";
import { Layout } from "../../../components/Layout";
import { firstOrString } from "../../../components/utils";
import { useQuery, useQueryClient, useMutation } from "react-query";
import { client } from "../../../components/client";
import { ClayButtonWithIcon } from "@clayui/button";
import {
  DatasourceSuggestionCategory,
  DatasourceSuggestionCategoryField,
} from "@openk9/rest-api";
import ClayForm, { ClayCheckbox, ClayInput } from "@clayui/form";
import { ClayTooltipProvider } from "@clayui/tooltip";
import ClayIcon from "@clayui/icon";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    margin: [theme.spacingUnit * 2, "auto"],
    backgroundColor: "white",
    boxShadow: theme.baseBoxShadow,
    width: "100%",
    maxWidth: 1000,
    borderRadius: theme.borderRadius,
    overflow: "auto",
  },
}));

export default function Filter() {
  const classes = useStyles();
  const { query } = useRouter();
  const tenantId = query.tenantId && firstOrString(query.tenantId);

  const suggestionCategories = useSuggestionCategories(Number(tenantId));

  const suggestionCategoryFields = useSuggestionCategoryFields(
    Number(tenantId),
  );

  if (!tenantId) return null;

  return (
    <Layout
      breadcrumbsPath={[
        { label: "Tenants", path: "/tenants" },
        { label: tenantId },
        { label: "Filters", path: `/tenants/${tenantId}/filters` },
      ]}
      breadcrumbsControls={
        <div className="navbar-nav" style={{ marginRight: 16 }}>
          <ClayTooltipProvider>
            <ClayButtonWithIcon
              data-tooltip-align="bottom"
              title="Add Filter"
              symbol="plus"
              onClick={() => suggestionCategories.add()}
            />
          </ClayTooltipProvider>
        </div>
      }
    >
      <div className={classes.root}>
        <ul className="list-group" style={{ margin: 0 }}>
          {suggestionCategories.list
            ?.sort((a, b) => a.priority - b.priority)
            .map((suggestionCategory) => {
              return (
                <SuggestionCategoryRow
                  key={suggestionCategory.suggestionCategoryId}
                  suggestionCategory={suggestionCategory}
                  onRemove={suggestionCategories.remove}
                  onUpdate={suggestionCategories.update}
                  onAddField={suggestionCategoryFields.add}
                  onRemoveField={suggestionCategoryFields.remove}
                  onUpdateField={suggestionCategoryFields.update}
                  suggestionCategoryFields={suggestionCategoryFields.list}
                />
              );
            })}
        </ul>
      </div>
    </Layout>
  );
}

type SuggestionCategoryFieldRowProps = {
  suggestionCategoryField: DatasourceSuggestionCategoryField;
  onRemove(suggestionCategoryFieldId: number): void;
  onUpdate(params: {
    suggestionCategoryFieldId: number;
    enabled?: boolean;
    fieldName?: string;
    name?: string;
  }): void;
};
function SuggestionCategoryFieldRow({
  suggestionCategoryField,
  onRemove,
  onUpdate,
}: SuggestionCategoryFieldRowProps) {
  const [enabled, setEnabled] = React.useState(suggestionCategoryField.enabled);
  const [name, setName] = React.useState(suggestionCategoryField.name);
  const [fieldName, setFieldName] = React.useState(
    suggestionCategoryField.fieldName,
  );
  const [isEditing, setIsEditing] = React.useState(false);
  return (
    <li
      className="list-group-item list-group-item-flex"
      style={{ marginLeft: "64px" }}
    >
      {/* <div className="autofit-col">
        <div className="custom-control custom-checkbox">
          <label>
            <input
              className="custom-control-input"
              type="checkbox"
            />
            <span className="custom-control-label"></span>
          </label>
        </div>
      </div> */}
      <div className="autofit-col autofit-col-expand">
        {!isEditing && <p className="list-group-title text-truncate">{name}</p>}
        {isEditing && (
          <ClayForm.Group>
            <label htmlFor="#">Name</label>
            <ClayInput
              value={name}
              onChange={(event) => setName(event.currentTarget.value)}
            />
          </ClayForm.Group>
        )}
        {!isEditing && (
          <p className="list-group-subtitle text-truncate">{fieldName}</p>
        )}
        {isEditing && (
          <ClayForm.Group>
            <label htmlFor="#">Field Name</label>
            <ClayInput
              value={fieldName}
              onChange={(event) => setFieldName(event.currentTarget.value)}
            />
          </ClayForm.Group>
        )}
        {!isEditing && (
          <div className="list-group-detail">
            {enabled ? (
              <span className="label label-success">
                <span className="label-item label-item-expand">ENABLED</span>
              </span>
            ) : (
              <span className="label label-warning">
                <span className="label-item label-item-expand">DISABLED</span>
              </span>
            )}
          </div>
        )}
        {isEditing && (
          <ClayCheckbox
            checked={enabled}
            onChange={() => setEnabled(!enabled)}
            label="Enabled"
          />
        )}
      </div>
      <div className="autofit-col">
        <div style={{ display: "flex", justifyContent: "end" }}>
          {isEditing ? (
            <ClayTooltipProvider>
              <div>
                <button
                  className="component-action quick-action-item"
                  onClick={() => {
                    onUpdate({
                      suggestionCategoryFieldId:
                        suggestionCategoryField.suggestionCategoryFieldId,
                      enabled,
                      name,
                      fieldName,
                    });
                    setIsEditing(false);
                  }}
                  data-tooltip-align="top"
                  title="Save changes to Category Field"
                >
                  <ClayIcon symbol="disk" />
                </button>
              </div>
            </ClayTooltipProvider>
          ) : (
            <ClayTooltipProvider>
              <div>
                <button
                  className="component-action quick-action-item"
                  onClick={() => {
                    setIsEditing(true);
                  }}
                  data-tooltip-align="top"
                  title="Edit Category Field"
                >
                  <ClayIcon symbol="pencil" />
                </button>
              </div>
            </ClayTooltipProvider>
          )}
          {isEditing && (
            <ClayTooltipProvider>
              <div>
                <button
                  className="component-action quick-action-item"
                  onClick={() =>
                    onRemove(suggestionCategoryField.suggestionCategoryFieldId)
                  }
                  data-tooltip-align="top"
                  title="Delete Category Field"
                >
                  <ClayIcon symbol="trash" />
                </button>
              </div>
            </ClayTooltipProvider>
          )}
        </div>
      </div>
    </li>
  );
}

type SuggestionCategoryRowProps = {
  suggestionCategory: DatasourceSuggestionCategory;
  onRemove(suggestionCategoryId: number): void;
  onUpdate(params: {
    suggestionCategoryId: number;
    enabled: boolean;
    name: string;
    priority: number;
  }): void;
  onAddField(suggestionCategoryId: number): void;
  onRemoveField(suggestionCategoryFieldId: number): void;
  onUpdateField(params: {
    suggestionCategoryFieldId: number;
    enabled: boolean;
    fieldName: string;
    name: string;
  }): void;
  suggestionCategoryFields:
    | Array<DatasourceSuggestionCategoryField>
    | undefined;
};
function SuggestionCategoryRow({
  suggestionCategory,
  onRemove,
  onUpdate,
  onAddField,
  suggestionCategoryFields,
  onRemoveField,
  onUpdateField,
}: SuggestionCategoryRowProps) {
  const [enabled, setEnabled] = React.useState(suggestionCategory.enabled);
  const [name, setName] = React.useState(suggestionCategory.name);
  const [priority, setPriority] = React.useState(suggestionCategory.priority);
  const [isEditing, setIsEditing] = React.useState(false);
  const [isExpanded, setIsExpanded] = React.useState(false);
  return (
    <React.Fragment>
      <li className="list-group-item list-group-item-flex">
        {/* <div className="autofit-col">
          <div className="custom-control custom-checkbox">
            <label>
              <input
                className="custom-control-input"
                type="checkbox"
              />
              <span className="custom-control-label"></span>
            </label>
          </div>
        </div> */}
        <div className="autofit-col autofit-col-expand">
          {!isEditing && (
            <p className="list-group-title text-truncate">{name}</p>
          )}
          {isEditing && (
            <ClayForm.Group>
              <label htmlFor="#">Name</label>
              <ClayInput
                value={name}
                onChange={(event) => setName(event.currentTarget.value)}
              />
            </ClayForm.Group>
          )}
          {!isEditing && (
            <p className="list-group-subtitle text-truncate">
              Priority: {priority}
            </p>
          )}
          {isEditing && (
            <ClayForm.Group>
              <label htmlFor="#">Priority</label>
              <ClayInput
                type="number"
                step="1"
                value={priority}
                onChange={(event) =>
                  setPriority(event.currentTarget.valueAsNumber)
                }
              />
            </ClayForm.Group>
          )}
          {!isEditing && (
            <div className="list-group-detail">
              {enabled ? (
                <span className="label label-success">
                  <span className="label-item label-item-expand">ENABLED</span>
                </span>
              ) : (
                <span className="label label-warning">
                  <span className="label-item label-item-expand">DISABLED</span>
                </span>
              )}
            </div>
          )}
          {isEditing && (
            <ClayCheckbox
              checked={enabled}
              onChange={() => setEnabled(!enabled)}
              label="Enabled"
            />
          )}
        </div>
        <div className="autofit-col">
          <div style={{ display: "flex", justifyContent: "end" }}>
            {!isExpanded &&
              (isEditing ? (
                <ClayTooltipProvider>
                  <div>
                    <button
                      className="component-action quick-action-item"
                      onClick={() => {
                        onUpdate({
                          suggestionCategoryId:
                            suggestionCategory.suggestionCategoryId,
                          enabled,
                          name,
                          priority,
                        });
                        setIsEditing(false);
                      }}
                      data-tooltip-align="top"
                      title="Save changes to Category"
                    >
                      <ClayIcon symbol="disk" />
                    </button>
                  </div>
                </ClayTooltipProvider>
              ) : (
                <ClayTooltipProvider>
                  <div>
                    <button
                      className="component-action quick-action-item"
                      onClick={() => {
                        setIsEditing(true);
                      }}
                      data-tooltip-align="top"
                      title="Edit Category"
                    >
                      <ClayIcon symbol="pencil" />
                    </button>
                  </div>
                </ClayTooltipProvider>
              ))}
            {!isExpanded && isEditing && (
              <ClayTooltipProvider>
                <div>
                  <button
                    className="component-action quick-action-item"
                    onClick={() =>
                      onRemove(suggestionCategory.suggestionCategoryId)
                    }
                    data-tooltip-align="top"
                    title="Delete Category"
                  >
                    <ClayIcon symbol="trash" />
                  </button>
                </div>
              </ClayTooltipProvider>
            )}
            {isExpanded && (
              <ClayTooltipProvider>
                <div>
                  <button
                    className="component-action quick-action-item"
                    onClick={() =>
                      onAddField(suggestionCategory.suggestionCategoryId)
                    }
                    data-tooltip-align="top"
                    title="Add Category Field under this Category"
                  >
                    <ClayIcon symbol="plus" />
                  </button>
                </div>
              </ClayTooltipProvider>
            )}
            {!isEditing && (
              <ClayTooltipProvider>
                <div>
                  <button
                    className="component-action quick-action-item"
                    onClick={() => {
                      setIsExpanded(!isExpanded);
                    }}
                    data-tooltip-align="top"
                    title="Show Category Fields"
                  >
                    <ClayIcon
                      symbol={
                        isExpanded ? "angle-up-small" : "angle-down-small"
                      }
                    />
                  </button>
                </div>
              </ClayTooltipProvider>
            )}
          </div>
        </div>
      </li>
      {isExpanded &&
        suggestionCategoryFields
          ?.filter(
            (suggestionCategoryField) =>
              suggestionCategoryField.categoryId ===
              suggestionCategory.suggestionCategoryId,
          )
          .map((suggestionCategoryField) => {
            return (
              <SuggestionCategoryFieldRow
                key={suggestionCategoryField.suggestionCategoryFieldId}
                suggestionCategoryField={suggestionCategoryField}
                onRemove={onRemoveField}
                onUpdate={onUpdateField}
              />
            );
          })}
    </React.Fragment>
  );
}

function Label(props: { children: React.ReactNode }) {
  return (
    <div
      style={{ whiteSpace: "nowrap", marginRight: "4px", fontWeight: "bold" }}
    >
      {props.children}:{" "}
    </div>
  );
}

const USE_MOCK = false;

function useSuggestionCategories(tenantId: number) {
  const queryClient = useQueryClient();
  const { data: list } = useQuery(
    ["/api/datasource/v2/suggestion-category", { tenantId }],
    async ({ queryKey: [path, parameters] }) => {
      if (USE_MOCK) return mockSuggestionCategories;
      return (await client.getDatasourceSuggestionCategories()).filter(
        (suggestionCategory) => suggestionCategory.tenantId === tenantId,
      );
    },
  );

  const { mutate: remove } = useMutation(
    async (suggestionCategoryId: number) => {
      if (USE_MOCK) {
        mockSuggestionCategories = mockSuggestionCategories.filter(
          (suggestionCategory) =>
            suggestionCategory.suggestionCategoryId !== suggestionCategoryId,
        );
        return;
      }

      await client.deleteDatasourceSuggestionCategory(suggestionCategoryId);
    },
    {
      onSuccess() {
        queryClient.invalidateQueries("/api/datasource/v2/suggestion-category");
      },
    },
  );
  const { mutate: update } = useMutation(
    async ({
      suggestionCategoryId,
      ...params
    }: {
      suggestionCategoryId: number;
      enabled?: boolean;
      name?: string;
      priority?: number;
    }) => {
      if (USE_MOCK) {
        mockSuggestionCategories.map((suggestionCategory) => {
          if (suggestionCategory.suggestionCategoryId !== suggestionCategoryId)
            return suggestionCategory;
          return { ...suggestionCategory, ...params };
        });
        return;
      }
      await client.updateDatasourceSuggestionCategory(
        suggestionCategoryId,
        params,
      );
    },
    {
      onSuccess() {
        queryClient.invalidateQueries("/api/datasource/v2/suggestion-category");
      },
    },
  );
  const { mutate: add } = useMutation(
    async () => {
      if (USE_MOCK) {
        mockSuggestionCategories = [
          ...mockSuggestionCategories,
          {
            enabled: true,
            name: "category",
            parentCategoryId: -1,
            priority: 0,
            suggestionCategoryId: Math.trunc(Math.random() * 100),
            tenantId: 0,
          },
        ];
        return;
      }
      await client.createDatasourceSuggestionCategory({
        tenantId,
        parentCategoryId: -1,
        name: "Suggestion Category Name",
        enabled: true,
        priority: 0,
      });
    },
    {
      onSuccess() {
        queryClient.invalidateQueries("/api/datasource/v2/suggestion-category");
      },
    },
  );
  return {
    list,
    remove,
    add,
    update,
  };
}

let mockSuggestionCategories: Array<DatasourceSuggestionCategory> = [
  {
    enabled: true,
    name: "categoria1",
    parentCategoryId: -1,
    priority: 2,
    suggestionCategoryId: 1,
    tenantId: 0,
  },
  {
    enabled: false,
    name: "categoria2",
    parentCategoryId: -1,
    priority: 1,
    suggestionCategoryId: 2,
    tenantId: 0,
  },
];

function useSuggestionCategoryFields(tenantId: number) {
  const queryClient = useQueryClient();
  const { data: list } = useQuery(
    ["/api/datasource/v2/suggestion-category-field", { tenantId }],
    async ({ queryKey: [path, parameters] }) => {
      if (USE_MOCK) return mockSuggestionCategoryFields;
      return await (
        await client.getDatasourceSuggestionCategoryFields()
      ).filter(
        (suggestionCategoryField) =>
          suggestionCategoryField.tenantId === tenantId,
      );
    },
  );

  const { mutate: remove } = useMutation(
    async (suggestionCategoryFieldId: number) => {
      if (USE_MOCK) {
        mockSuggestionCategoryFields = mockSuggestionCategoryFields.filter(
          (suggestionCategoryField) =>
            suggestionCategoryField.suggestionCategoryFieldId !==
            suggestionCategoryFieldId,
        );
        return;
      }
      await client.deleteDatasourceSuggestionCategoryField(
        suggestionCategoryFieldId,
      );
    },
    {
      onSuccess() {
        queryClient.invalidateQueries(
          "/api/datasource/v2/suggestion-category-field",
        );
      },
    },
  );

  const { mutate: update } = useMutation(
    async ({
      suggestionCategoryFieldId,
      ...params
    }: {
      suggestionCategoryFieldId: number;
      enabled?: boolean;
      fieldName?: string;
      name?: string;
    }) => {
      if (USE_MOCK) {
        mockSuggestionCategoryFields = mockSuggestionCategoryFields.map(
          (suggestionCategoryField) => {
            if (
              suggestionCategoryField.suggestionCategoryFieldId !==
              suggestionCategoryFieldId
            )
              return suggestionCategoryField;
            return { ...suggestionCategoryField, ...params };
          },
        );
        return;
      }
      await client.updateDatasourceSuggestionCategoryField(
        suggestionCategoryFieldId,
        params,
      );
    },
    {
      onSuccess() {
        queryClient.invalidateQueries(
          "/api/datasource/v2/suggestion-category-field",
        );
      },
    },
  );

  const { mutate: add } = useMutation(
    async (categoryId: number) => {
      if (USE_MOCK) {
        mockSuggestionCategoryFields = [
          ...mockSuggestionCategoryFields,
          {
            enabled: true,
            name: "category",
            fieldName: "",
            suggestionCategoryFieldId: Math.trunc(Math.random() * 100),
            categoryId,
            tenantId: 0,
          },
        ];
        return;
      }
      await client.createDatasourceSuggestionCategoryField({
        tenantId,
        categoryId,
        fieldName: "",
        name: "Suggestion Category Name",
        enabled: true,
      });
    },
    {
      onSuccess() {
        queryClient.invalidateQueries(
          "/api/datasource/v2/suggestion-category-field",
        );
      },
    },
  );
  return { list, add, remove, update };
}

let mockSuggestionCategoryFields: Array<DatasourceSuggestionCategoryField> = [
  {
    categoryId: 1,
    suggestionCategoryFieldId: 1,
    tenantId: 0,
    fieldName: "fieldname1",
    name: "field1",
    enabled: true,
  },
  {
    categoryId: 2,
    suggestionCategoryFieldId: 2,
    tenantId: 0,
    fieldName: "fieldname2",
    name: "field2",
    enabled: false,
  },

  {
    categoryId: 2,
    suggestionCategoryFieldId: 2,
    tenantId: 0,
    fieldName: "fieldname3",
    name: "field3",
    enabled: true,
  },
];
