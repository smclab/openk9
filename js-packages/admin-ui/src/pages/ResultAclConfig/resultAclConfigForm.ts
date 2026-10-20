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
import { ResultAclConfigDtoInput } from "../../graphql-generated";

export type FieldOption = { value: string; label: string };

export type ResultAclConfigValues = {
  enabled: boolean;
  superUserRoles: string[];
  visibleFields: FieldOption[];
};

type DocTypeFieldItem = { id?: string | null; name?: string | null } | null;

// The server replaces the name of the single configuration of the tenant.
const RESULT_ACL_CONFIG_NAME = "Default Result ACL Configuration";

export function toFieldOptions(fields: ReadonlyArray<DocTypeFieldItem> | null | undefined): FieldOption[] {
  return (fields ?? [])
    .filter((field): field is NonNullable<DocTypeFieldItem> => !!field)
    .map((field) => ({ value: String(field.id ?? ""), label: field.name ?? "" }));
}

/**
 * The fields the administrator can choose from. The query of the selectable
 * fields leaves out the visible ones, so they are added back.
 */
export function toSelectableFieldOptions(
  unboundFields: ReadonlyArray<DocTypeFieldItem> | null | undefined,
  visibleFields: ReadonlyArray<DocTypeFieldItem> | null | undefined,
): FieldOption[] {
  const options = [...toFieldOptions(unboundFields), ...toFieldOptions(visibleFields)];
  return options.filter((option, index) => options.findIndex((other) => other.value === option.value) === index);
}

/** The whole configuration: a full update turns an omitted value into its default. */
export function toResultAclConfigDto(values: ResultAclConfigValues): ResultAclConfigDtoInput {
  return {
    name: RESULT_ACL_CONFIG_NAME,
    enabled: values.enabled,
    superUserRoles: values.superUserRoles,
    visibleFieldIds: values.visibleFields.map((field) => Number(field.value)),
  };
}
