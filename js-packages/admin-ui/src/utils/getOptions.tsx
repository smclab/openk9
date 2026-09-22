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

export type SelectOption = { value: string; label: string };

/** `TVariables` viene dedotto dall'hook Apollo passato: ogni query porta le sue variabili. */
type Params<TVariables> = {
  useQuery?(options: {
    variables?: TVariables;
    fetchPolicy: "network-only" | "cache-first";
  }): { data?: unknown };
  data?: { data?: unknown };
  queryKeyPath: string;
  accessKey?: "node";
  variables?: TVariables;
  isNetworkOnly?: boolean;
};
const pathObject = {
  node: "node",
};

export default function useOptions<TVariables>({
  useQuery,
  data,
  queryKeyPath,
  accessKey = "node",
  variables,
  isNetworkOnly = false,
}: Params<TVariables>) {
  const queryData =
    useQuery?.({ variables, fetchPolicy: isNetworkOnly ? "network-only" : "cache-first" }) || {};
  const sourceData = data?.data || queryData?.data;

  const value = extract({ object: sourceData, pathKey: queryKeyPath });

  const getOptions = (value: unknown): SelectOption[] => {
    if (!Array.isArray(value)) return [];
    return value.map((item: unknown) => {
      const nested = extract({ object: item, pathKey: pathObject[accessKey] });
      return {
        value: readString(item, "id") || readString(nested, "id") || "",
        label: readString(item, "name") || readString(nested, "name") || "",
      };
    });
  };

  const OptionQuery: SelectOption[] = sourceData ? getOptions(value) : [];

  return {
    OptionQuery,
  };
}

function isRecord(value: unknown): value is Record<string, unknown> {
  return typeof value === "object" && value !== null;
}

/** I risultati GraphQL arrivano di forma ignota: si legge un campo solo dopo averlo verificato. */
function readString(source: unknown, key: string): string {
  if (!isRecord(source)) return "";
  const value = source[key];
  if (value === null || value === undefined) return "";
  return typeof value === "string" ? value : String(value);
}

function extract({ object, pathKey }: { object: unknown; pathKey: string }): unknown {
  return pathKey.split(".").reduce<unknown>((obj, key) => (isRecord(obj) ? obj[key] : undefined), object);
}
