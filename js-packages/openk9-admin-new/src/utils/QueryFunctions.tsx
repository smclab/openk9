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
