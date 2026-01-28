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
type Nullable<T> = T | null | undefined;

export const fromFieldValidators =
  (
    fieldValidators: Nullable<
      Array<
        Nullable<{
          field?: Nullable<string>;
          message?: Nullable<string>;
        }>
      >
    >,
  ) =>
  (field: string) =>
    fieldValidators?.flatMap((entry) =>
      entry?.field === field ? (entry.message ? [entry.message] : []) : [],
    ) ?? [];

type FieldValidator = {
  __typename?: string;
  field?: string | null;
  message?: string | null;
} | null;

export const combineErrorMessages = (
  fieldValidators?: FieldValidator[] | null,
): string => {
  if (!fieldValidators || !Array.isArray(fieldValidators)) return "";

  return fieldValidators
    .filter((validator) => validator?.message)
    .map((validator) => validator!.message!)
    .join("\n");
};

