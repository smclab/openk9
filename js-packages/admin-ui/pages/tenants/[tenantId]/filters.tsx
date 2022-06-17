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
import { ClayInput } from "@clayui/form";
import { ClayTooltipProvider } from "@clayui/tooltip";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    margin: [theme.spacingUnit * 2, "auto"],
    backgroundColor: "white",
    boxShadow: theme.baseBoxShadow,
    width: "100%",
    maxWidth: 1000,
    borderRadius: theme.borderRadius,
    overflow: "auto",
    padding: theme.spacingUnit * 2,
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
  const isDirty =
    enabled !== suggestionCategoryField.enabled ||
    name !== suggestionCategoryField.name ||
    fieldName !== suggestionCategoryField.fieldName 
  return (
    <div
      style={{
        display: "flex",
        paddingLeft: "32px",
        marginBottom: "8px",
        alignItems: "center",
      }}
    >
      <div style={{ marginRight: "8px", marginLeft: "8px" }}>
        <input
          type="checkbox"
          className="custom-control-input"
          checked={enabled}
          onChange={(event) => setEnabled(event.currentTarget.checked)}
        />
      </div>
      <div
        style={{
          flexGrow: 1,
          display: "flex",
          marginRight: "8px",
          alignItems: "center",
        }}
      >
        <Label>Name</Label>
        <ClayInput
          value={name}
          onChange={(event) => setName(event.currentTarget.value)}
        />
      </div>
      <div
        style={{
          flexGrow: 1,
          display: "flex",
          marginRight: "8px",
          alignItems: "center",
        }}
      >
        <Label>Field name</Label>
        <ClayInput
          value={fieldName}
          onChange={(event) => setFieldName(event.currentTarget.value)}
        />
      </div>
      <ClayButtonWithIcon
        symbol="trash"
        onClick={() =>
          onRemove(suggestionCategoryField.suggestionCategoryFieldId)
        }
        style={{ marginRight: "8px" }}
      />
      <ClayButtonWithIcon
        symbol="disk"
        disabled={!isDirty}
        onClick={() =>
          onUpdate({
            suggestionCategoryFieldId:
              suggestionCategoryField.suggestionCategoryFieldId,
            enabled,
            name,
            fieldName,
          })
        }
        style={{ marginRight: "8px" }}
      ></ClayButtonWithIcon>
    </div>
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
  const isDirty =
    enabled !== suggestionCategory.enabled ||
    name !== suggestionCategory.name ||
    priority !== suggestionCategory.priority;
  return (
    <React.Fragment>
      <div
        style={{ display: "flex", alignItems: "center", marginBottom: "8px" }}
      >
        <div style={{ marginRight: "8px", marginLeft: "8px" }}>
          <input
            type="checkbox"
            checked={enabled}
            onChange={(event) => setEnabled(event.currentTarget.checked)}
          />
        </div>
        <div
          style={{
            flexGrow: 1,
            display: "flex",
            marginRight: "8px",
            alignItems: "center",
          }}
        >
          <Label>Name</Label>
          <ClayInput
            value={name}
            onChange={(event) => setName(event.currentTarget.value)}
          />
        </div>
        <div
          style={{ display: "flex", marginRight: "8px", alignItems: "center" }}
        >
          <Label>Priority</Label>
          <ClayInput
            type="number"
            step="1"
            value={priority}
            onChange={(event) => setPriority(event.currentTarget.valueAsNumber)}
          />
        </div>
        <ClayButtonWithIcon
          symbol="trash"
          onClick={() => onRemove(suggestionCategory.suggestionCategoryId)}
          style={{ marginRight: "8px" }}
        />
        <ClayButtonWithIcon
          symbol="disk"
          disabled={!isDirty}
          onClick={() =>
            onUpdate({
              suggestionCategoryId: suggestionCategory.suggestionCategoryId,
              enabled,
              name,
              priority,
            })
          }
          style={{ marginRight: "8px" }}
        >
          save
        </ClayButtonWithIcon>
        <ClayButtonWithIcon
          symbol="plus"
          onClick={() => onAddField(suggestionCategory.suggestionCategoryId)}
          style={{ marginRight: "8px" }}
        />
      </div>
      {suggestionCategoryFields
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
      <div style={{ width: "100%", height: "32px" }}></div>
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
    ["/api/datasource/v2/suggestion-category", {}],
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
    ["/api/datasource/v2/suggestion-category-field", {}],
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
