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
import { useDocTypeFieldsQuery, useUnboundDocTypeFieldsBySuggestionCategoryQuery } from "../../graphql-generated";

export default function useOptionsSuggestionCategory({
  suggestionCategoryId,
}: {
  suggestionCategoryId?: string | null | undefined;
}) {
  const { data: AllDocTypeData } = useDocTypeFieldsQuery({
    skip: suggestionCategoryId !== "new",
  });
  const { data: docTypeData } = useUnboundDocTypeFieldsBySuggestionCategoryQuery({
    variables: { suggestionCategoryId: Number(suggestionCategoryId) },
    skip: suggestionCategoryId === "new",
  });
  const data = AllDocTypeData
    ? extractAllData({ AllDocTypeData: AllDocTypeData as any })
    : extractSuggestionData({ docTypeData: docTypeData as any });
  return data;
}

function extractAllData({
  AllDocTypeData,
}: {
  AllDocTypeData?: { docTypeFields: { edges: [{ node: { id: string; name: string } }] } };
}) {
  return (
    AllDocTypeData?.docTypeFields?.edges?.map((edge) => ({
      value: edge.node.id,
      label: edge.node.name,
    })) || []
  );
}

function extractSuggestionData({
  docTypeData,
}: {
  docTypeData?: { unboundDocTypeFieldsBySuggestionCategory: [{ id: string; name: string }] | null | undefined };
}) {
  return (
    docTypeData?.unboundDocTypeFieldsBySuggestionCategory?.map((item) => ({
      value: item.id,
      label: item.name,
    })) || []
  );
}

