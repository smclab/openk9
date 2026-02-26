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
export type associateType = {
  label: string | null | undefined;
  value: string | null | undefined;
};

export type AssociatedUnassociated = {
  unassociated: associateType[] | undefined;
  isLoading: boolean;
  associated: associateType[] | undefined;
};

export function formatQueryToFE({
  informationId,
}: {
  informationId:
    | ({
        __typename?: string;
        node?: {
          __typename?: string;
          name?: string | null;
          id?: string | null;
        } | null;
      } | null)[]
    | null
    | undefined;
}) {
  const remapping = informationId?.map((info) => ({
    label: info?.node?.name,
    value: info?.node?.id,
  }));
  return remapping;
}

export function formatQueryToBE({
  information,
}: {
  information: associateType[];
}) {
  const remappingInformation = information?.flatMap((info) =>
    Number(info.value),
  );
  return remappingInformation;
}

