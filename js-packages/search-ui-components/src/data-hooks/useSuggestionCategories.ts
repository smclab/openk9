import { getSuggestionCategories } from "@openk9/http-api";
import { useQuery } from "react-query";

export function useSuggestionCategories() {
  return useQuery(["suggestion-categories"], async ({ queryKey }) => {
    const result = await getSuggestionCategories();
    return result;
  });
}
