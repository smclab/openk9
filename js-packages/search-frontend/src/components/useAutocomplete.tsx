import { useQuery } from "react-query";
import { useOpenK9Client } from "../components/client";
import { useDebounce } from "../components/useDebounce";

export function useAutocomplete(textOnChange: string) {
  const client = useOpenK9Client();
  const debounced = useDebounce(textOnChange, 250);

  return useQuery(
    ["autocomplete", debounced],
    async () => {
      const q = debounced.trim();
      if (q.length < 1) return [];

      const es = await client.getAutocompletes({ searchText: textOnChange });
      return es;
    },
    {
      enabled: debounced.trim().length >= 1,
      keepPreviousData: true,
    },
  );
}
