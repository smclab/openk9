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
import { KeyValue } from "@components/Form";

type Params = {
  useQuery?: any;
  data?: any;
  queryKeyPath: string;
  accessKey?: "node";
  variables?: KeyValue;
  isNetworkOnly?: boolean;
};
const pathObject = {
  node: "node",
};

export default function useOptions({
  useQuery,
  data,
  queryKeyPath,
  accessKey = "node",
  variables,
  isNetworkOnly = false,
}: Params) {
  const queryData =
    useQuery?.({ variables: { ...variables }, fetchPolicy: isNetworkOnly ? "network-only" : "cache-first" }) || {};
  const sourceData = data?.data || queryData?.data;

  const value = extract({ object: sourceData, pathKey: queryKeyPath });

  const getOptions = (value: any) => {
    return (
      value?.map((item: any) => ({
        value: item?.id || extract({ object: item, pathKey: pathObject[accessKey] })?.id || "",
        label: item?.name || extract({ object: item, pathKey: pathObject[accessKey] })?.name || "",
      })) || []
    );
  };

  const OptionQuery = (sourceData && getOptions(value)) || [];

  return {
    OptionQuery,
  };
}

function extract({ object, pathKey }: { object: any; pathKey: string }) {
  return pathKey.split(".")?.reduce((obj, key) => obj?.[key], object);
}

