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
import { useMemo } from "react";
import { FieldType, useDocTypeFieldsByTypeQuery } from "../../graphql-generated";

type Option = { value: string; label: string };

type UseOptionsResult = {
  options: Option[];
  loading: boolean;
  hasNextPage: boolean;
  loadMore?: () => Promise<void>;
};

export const useDocTypeFieldsOptions = (
  searchText: string,
  _extraVariables?: { suggestionCategoryId?: number },
): UseOptionsResult => {
  const { data, loading } = useDocTypeFieldsByTypeQuery({
    variables: { fieldType: FieldType.Keyword },
  });

  const options: Option[] = useMemo(() => {
    const items = data?.docTypeFieldsByType ?? [];
    const needle = (searchText || "").trim().toLowerCase();
    return items
      .filter((item): item is NonNullable<typeof item> => !!item)
      .filter((item) => !needle || (item.name ?? "").toLowerCase().includes(needle))
      .map((item) => ({
        value: String(item.id ?? ""),
        label: item.name ?? "",
      }));
  }, [data, searchText]);

  return {
    options,
    loading,
    hasNextPage: false,
  };
};
