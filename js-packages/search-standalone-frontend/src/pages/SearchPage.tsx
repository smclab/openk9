import { Suspense } from "react";
import { Dockbar } from "@openk9/search-ui-components";
import { SearchQueryInput } from "../containers/SearchQueryInput";
import { SearchResults } from "../containers/SearchResults";
import { useLoginCheck } from "../state";

export function SearchPage() {
  const { canEnter, isGuest, goToLogin } = useLoginCheck();
  if (!canEnter) return <span className="loading-animation" />;

  return (
    <>
      <Dockbar onLoginAction={(isGuest && goToLogin) || undefined} />
      <Suspense fallback={<span className="loading-animation" />}>
        <SearchQueryInput />
      </Suspense>
      <Suspense fallback={<span className="loading-animation" />}>
        <SearchResults />
      </Suspense>
    </>
  );
}
