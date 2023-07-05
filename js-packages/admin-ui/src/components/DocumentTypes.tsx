import React from "react";
import { QueryResult, gql } from "@apollo/client";
import {
  DataSourcesQuery,
  Exact,
  InputMaybe,
  useDataSourcesQuery,
  useDeleteDocumentTypeMutation,
  useDocumentTypesQuery,
} from "../graphql-generated";
import { formatName, Table } from "./Table";
import { useMutation } from "@tanstack/react-query";
import { useRestClient } from "./queryClient";
import { useToast } from "./ToastProvider";
import ClayModal, { useModal } from "@clayui/modal";
import { CustomButtomClay, TextInput, useForm } from "./Form";
import { useNavigate } from "react-router-dom";
import { CreateDataIndexFromDocTypesRequest } from "../openapi-generated";
import { ClaySelect } from "@clayui/form";

export const DocumentTypesQuery = gql`
  query DocumentTypes($searchText: String, $cursor: String) {
    docTypes(searchText: $searchText, first: 25, after: $cursor) {
      edges {
        node {
          id
          name
          description
        }
      }
      pageInfo {
        hasNextPage
        endCursor
      }
    }
  }
`;

gql`
  mutation DeleteDocumentType($id: ID!) {
    deleteDocType(docTypeId: $id) {
      id
    }
  }
`;

export function DocumentTypes() {
  const documentTypesQuery = useDocumentTypesQuery();
  const navigate = useNavigate();
  const [deleteDocumentTypeMutate] = useDeleteDocumentTypeMutation({
    refetchQueries: [DocumentTypesQuery],
  });
  const restClient = useRestClient();
  const dataSourcesQuery = useDataSourcesQuery();
  const [selectDatasource, setSelectDataSource] = React.useState<string | undefined | null>(
    dataSourcesQuery?.data?.datasources?.edges?.[0]?.node?.id
  );
  React.useEffect(() => {
    if (dataSourcesQuery.data?.datasources?.edges?.[0]?.node?.id) {
      setSelectDataSource(dataSourcesQuery.data.datasources.edges[0].node.id);
    }
  }, [dataSourcesQuery.data]);
  const createDataIndexModal = useCreateDataIndexModal({ dataSourcesQuery, setSelectDataSource, selectDatasource });
  if (dataSourcesQuery.loading) return null;
  return (
    <React.Fragment>
      <Table
        data={{
          queryResult: documentTypesQuery,
          field: (data) => data?.docTypes,
        }}
        isItemsSelectable
        onCreatePath="/document-types/new"
        onDelete={(documentType) => {
          if (documentType?.id) deleteDocumentTypeMutate({ variables: { id: documentType.id } });
        }}
        columns={[
          { header: "Name", content: (documentType) => formatName(documentType) },
          { header: "Description", content: (documentType) => documentType?.description },
        ]}
        rowsActions={(rows) => {
          return [
            {
              label: "Create Data Index",
              disabled: rows.length === 0,
              onClick() {
                createDataIndexModal.open(rows);
              },
            },
            {
              label: "Show Settings",
              disabled: rows.length === 0,
              async onClick() {
                const data = await restClient.dataIndexResource.postApiDatasourceV1DataIndexGetSettingsFromDocTypes({
                  docTypeIds: rows.map((row) => Number(row.id!)),
                });
                navigate(`settings`, { state: { data: data } });
              },
            },
          ];
        }}
      />
      {createDataIndexModal.content}
    </React.Fragment>
  );
}

function useCreateDataIndexModal({
  dataSourcesQuery,
  setSelectDataSource,
  selectDatasource,
}: {
  dataSourcesQuery: QueryResult<
    DataSourcesQuery,
    Exact<{ searchText?: InputMaybe<string> | undefined; cursor?: InputMaybe<string> | undefined }>
  >;
  setSelectDataSource: React.Dispatch<React.SetStateAction<string | null | undefined>>;
  selectDatasource: string | null | undefined;
}) {
  const restClient = useRestClient();
  const showToast = useToast();
  const createDataIndexMutation = useMutation(
    async ({ datasourceId, requestBody }: { datasourceId: string; requestBody: { docTypeIds?: Array<number>; indexName?: string } }) => {
      return await restClient.dataIndexResource.postApiDatasourceV1DataIndexCreateDataIndexFromDocTypes(
        parseFloat(datasourceId),
        requestBody
      );
    },
    {
      onSuccess(data, variables, context) {
        showToast({ displayType: "info", title: "Data Index created", content: variables.datasourceId });
        modal.onClose();
      },
    }
  );
  const modal = useModal();
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        indexName: "",
        docTypeIds: [] as Array<string>,
      }),
      []
    ),
    originalValues: undefined,
    isLoading: createDataIndexMutation.isLoading,
    onSubmit(data) {
      const convertedData = {
        indexName: data.indexName,
        docTypeIds: data.docTypeIds.map((id) => parseFloat(id)),
      };
      if (selectDatasource) createDataIndexMutation.mutate({ datasourceId: selectDatasource, requestBody: convertedData });
    },
    getValidationMessages: () => {
      const errorText = (createDataIndexMutation as any).error?.body?.details;
      if (errorText) return [errorText];
      return [];
    },
  });
  const edges = dataSourcesQuery?.data?.datasources?.edges ?? [];
  const open = (documentTypes: Array<{ id?: string | null }>) => {
    form.inputProps("docTypeIds").onChange(documentTypes.flatMap((documentType) => (documentType?.id ? [documentType.id] : [])));
    modal.onOpenChange(true);
  };
  const content = modal.open && (
    <ClayModal observer={modal.observer}>
      <ClayModal.Header>Create Data Index</ClayModal.Header>
      <ClayModal.Body>
        <form
          onSubmit={(event) => {
            event.preventDefault();
            form.submit();
          }}
        >
          <TextInput label="Name" {...form.inputProps("indexName")} />
          <ClaySelect
            aria-label="Select Label"
            id="mySelectId"
            onChange={(element) => {
              setSelectDataSource(element.currentTarget.value);
            }}
          >
            {edges.map((item) => (
              <ClaySelect.Option key={item?.node?.id} label={item?.node?.name || "..."} value={item?.node?.id || 0} />
            ))}
          </ClaySelect>
        </form>
      </ClayModal.Body>
      <ClayModal.Footer last={<CustomButtomClay label="Create" action={() => form.submit()} disabled={!form.canSubmit} />} />
    </ClayModal>
  );
  return { open, content };
}
