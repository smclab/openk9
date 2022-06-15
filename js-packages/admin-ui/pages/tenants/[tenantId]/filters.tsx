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

import { useRouter } from "next/router";

import { Layout } from "../../../components/Layout";

import { firstOrString } from "../../../components/utils";
import { useQuery, useQueryClient, useMutation } from "react-query";
import { client } from "../../../components/client";
import ClayButton from "@clayui/button";
import { ClayInput } from "@clayui/form";
import {
  DatasourceSuggestionCategory,
  DatasourceSuggestionCategoryField,
} from "@openk9/rest-api";

// TODO look & feel
// TODO update suggestion category
// TODO update suggestion category field

export default function Filter() {
  const { query } = useRouter();
  const tenantId = query.tenantId && firstOrString(query.tenantId);

  const suggestionCategories = useSuggestionCategories(Number(tenantId));
  const suggestionCategoryFields = useSuggestionCategoryFields(
    Number(tenantId),
  );

  if (!tenantId) return null;

  return (
    <>
      <Layout
        breadcrumbsPath={[
          { label: "Tenants", path: "/tenants" },
          { label: tenantId },
          { label: "Filters", path: `/tenants/${tenantId}/filters` },
        ]}
      >
        <div>
          <ClayButton
            displayType="primary"
            onClick={() => suggestionCategories.add()}
          >
            add
          </ClayButton>
          {suggestionCategories.list
            ?.sort((a, b) => a.priority - b.priority)
            .map((suggestionCategory) => {
              return (
                <SuggestionCategoryRow
                  key={suggestionCategory.suggestionCategoryId}
                  suggestionCategory={suggestionCategory}
                  onRemove={suggestionCategories.remove}
                  onAddField={suggestionCategoryFields.add}
                  onRemoveField={suggestionCategoryFields.remove}
                  suggestionCategoryFields={suggestionCategoryFields.list}
                />
              );
            })}
        </div>
      </Layout>
    </>
  );
}

type SuggestionCategoryFieldRowProps = {
  suggestionCategoryField: DatasourceSuggestionCategoryField;
  onRemove(suggestionCategoryFieldId: number): void;
};
function SuggestionCategoryFieldRow({
  suggestionCategoryField,
  onRemove,
}: SuggestionCategoryFieldRowProps) {
  const [enabled, setEnabled] = React.useState(suggestionCategoryField.enabled);
  const [name, setName] = React.useState(suggestionCategoryField.name);
  const [fieldName, setFieldName] = React.useState(
    suggestionCategoryField.fieldName,
  );
  const [searchableFieldName, setSearchableFieldName] = React.useState(
    suggestionCategoryField.searchableFieldName,
  );
  return (
    <div style={{ display: "flex", paddingLeft: "30px" }}>
      <div>
        <input
          type="checkbox"
          className="custom-control-input"
          checked={enabled}
          onChange={(event) => setEnabled(event.currentTarget.checked)}
        />
      </div>
      <div>
        name:
        <ClayInput
          value={name}
          onChange={(event) => setName(event.currentTarget.value)}
        />
      </div>
      <div>
        fieldName:
        <ClayInput
          value={fieldName}
          onChange={(event) => setFieldName(event.currentTarget.value)}
        />
      </div>
      <div>
        searchableFieldName:
        <ClayInput
          value={searchableFieldName}
          onChange={(event) =>
            setSearchableFieldName(event.currentTarget.value)
          }
        />
      </div>
      <ClayButton
        displayType="primary"
        onClick={() =>
          onRemove(suggestionCategoryField.suggestionCategoryFieldId)
        }
      >
        delete
      </ClayButton>
    </div>
  );
}

type SuggestionCategoryRowProps = {
  suggestionCategory: DatasourceSuggestionCategory;
  onRemove(suggestionCategoryId: number): void;
  onAddField(suggestionCategoryId: number): void;
  onRemoveField(suggestionCategoryFieldId: number): void;
  suggestionCategoryFields:
    | Array<DatasourceSuggestionCategoryField>
    | undefined;
};
function SuggestionCategoryRow({
  suggestionCategory,
  onRemove,
  onAddField,
  suggestionCategoryFields,
  onRemoveField,
}: SuggestionCategoryRowProps) {
  const [enabled, setEnabled] = React.useState(suggestionCategory.enabled);
  const [name, setName] = React.useState(suggestionCategory.name);
  const [priority, setPriority] = React.useState(suggestionCategory.priority);
  return (
    <div>
      <div style={{ display: "flex" }}>
        <div>
          <input
            type="checkbox"
            checked={enabled}
            onChange={(event) => setEnabled(event.currentTarget.checked)}
          />
        </div>
        <div>
          name:
          <ClayInput
            value={name}
            onChange={(event) => setName(event.currentTarget.value)}
          />
        </div>
        <div>
          priority:
          <ClayInput
            type="number"
            step="1"
            value={priority}
            onChange={(event) => setPriority(event.currentTarget.valueAsNumber)}
          />
        </div>
        <div>
          <ClayButton
            displayType="primary"
            onClick={() => onRemove(suggestionCategory.suggestionCategoryId)}
          >
            delete
          </ClayButton>
          <ClayButton
            displayType="primary"
            onClick={() => onAddField(suggestionCategory.suggestionCategoryId)}
          >
            add field
          </ClayButton>
        </div>
      </div>
      <div>
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
              />
            );
          })}
      </div>
    </div>
  );
}

const USE_MOCK = false;

function useSuggestionCategories(tenantId: number) {
  const queryClient = useQueryClient();
  const { data: list } = useQuery(
    ["/api/datasource/v2/suggestion-category", {}],
    async ({ queryKey: [path, paramiters] }) => {
      if (USE_MOCK) return mockSuggestionCategories;
      return await client.getDatasourceSuggestionCategories();
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
  // const { mutate: change } = useMutation(
  //   async (suggestionCategoryId: number) => {
  //     if (USE_MOCK) {
  //       mockSuggestionCategories.filter(
  //        (suggestionCategory) =>{
  //           if(suggestionCategory.suggestionCategoryId === newSuggestionCategory.id){
  //             suggestionCategory=newSuggestionCategory
  //           }
  //         }
  //       );
  //      return;
  //     }
  //     await client.changeDatasourceSuggestionCategory(suggestionCategoryId);
  //   },
  //   {
  //     onSuccess() {
  //       queryClient.invalidateQueries("/api/datasource/v2/suggestion-category");
  //     },
  //   },
  // );
  const { mutate: add } = useMutation(
    async () => {
      if (USE_MOCK) {
        mockSuggestionCategories = [
          ...mockSuggestionCategories,
          {
            enabled: false,
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
        enabled: false,
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
    //change,g
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
    async ({ queryKey: [path, paramiters] }) => {
      if (USE_MOCK) return mockSuggestionCategoryFields;
      return await client.getDatasourceSuggestionCategoryFields();
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
  const { mutate: add } = useMutation(
    async (categoryId: number) => {
      if (USE_MOCK) {
        mockSuggestionCategoryFields = [
          ...mockSuggestionCategoryFields,
          {
            enabled: false,
            name: "category",
            fieldName: "",
            searchableFieldName: "",
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
        searchableFieldName: "",
        name: "Suggestion Category Name",
        enabled: false,
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
  return { list, add, remove };
}

let mockSuggestionCategoryFields: Array<DatasourceSuggestionCategoryField> = [
  {
    categoryId: 1,
    suggestionCategoryFieldId: 1,
    tenantId: 0,
    fieldName: "fieldname1",
    searchableFieldName: "name1",
    name: "field1",
    enabled: true,
  },
  {
    categoryId: 2,
    suggestionCategoryFieldId: 2,
    tenantId: 0,
    fieldName: "fieldname2",
    searchableFieldName: "name2",
    name: "field2",
    enabled: false,
  },

  {
    categoryId: 2,
    suggestionCategoryFieldId: 2,
    tenantId: 0,
    fieldName: "fieldname3",
    searchableFieldName: "name3",
    name: "field3",
    enabled: true,
  },
];
