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
import { useMemo, useCallback, useState } from "react";
import { useDocTypeFieldsQuery } from "../../graphql-generated";

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
  const [canLoadMore, setCanLoadMore] = useState(true);

  const {
    data,
    loading: apolloLoading,
    fetchMore,
  } = useDocTypeFieldsQuery({
    variables: {
      searchText: searchText || undefined,
      first: 20,
      after: null,
    },
    notifyOnNetworkStatusChange: true,
  });

  const loading = apolloLoading && !data;

  const pageInfo = data?.docTypeFields?.pageInfo;

  const options: Option[] = useMemo(() => {
    const edges = data?.docTypeFields?.edges ?? [];
    return edges.map((item) => ({
      value: item?.node?.id || "",
      label: item?.node?.name || "",
    }));
  }, [data]);

  const hasNextPageFromServer = Boolean(pageInfo?.hasNextPage && pageInfo?.endCursor);
  const hasNextPage = canLoadMore && hasNextPageFromServer;

  const loadMore = useCallback(async () => {
    if (!hasNextPageFromServer || !pageInfo?.endCursor) {
      setCanLoadMore(false);
      return;
    }

    const result = await fetchMore({
      variables: {
        searchText: searchText || undefined,
        first: 20,
        after: pageInfo.endCursor,
      },
    });

    const newPageInfo = result.data?.docTypeFields?.pageInfo;

    if (!newPageInfo?.hasNextPage || !newPageInfo.endCursor) {
      setCanLoadMore(false);
    }
  }, [fetchMore, searchText, hasNextPageFromServer, pageInfo]);

  return {
    options,
    loading,
    hasNextPage,
    loadMore: hasNextPage ? loadMore : undefined,
  };
};

