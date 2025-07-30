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
