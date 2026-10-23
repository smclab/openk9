import { keepPreviousData, useQuery } from "@tanstack/react-query";
import { useOpenK9Client } from "../components/client";
import { useDebounce } from "../components/useDebounce";

export function useAutocomplete(
  textOnChange: string,
  autocompleteEnabled: boolean = true,
) {
  const client = useOpenK9Client();
  const debounced = useDebounce(textOnChange, 250);

  return useQuery({
    queryKey: ["autocomplete", debounced],
    queryFn: async () => {
      const q = debounced.trim();
      if (q.length < 1) return [];

      const es = await client.getAutocompletes({ searchText: textOnChange });
      return es;
    },
    enabled: autocompleteEnabled && debounced.trim().length >= 1,
    placeholderData: keepPreviousData,
  });
}
