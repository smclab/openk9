import React from "react";
import { gql } from "@apollo/client";
import { useDeleteDocumentTypeMutation, useDocumentTypesQuery } from "../graphql-generated";
import { formatName, Table } from "./Table";
import { useMutation } from "@tanstack/react-query";
import { useRestClient } from "./queryClient";
import { useToast } from "./ToastProvider";
import ClayModal, { useModal } from "@clayui/modal";
import ClayButton from "@clayui/button";
import { TextInput, useForm } from "./Form";
import { useNavigate } from "react-router-dom";
import { ClassNameButton } from "../App";

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
  const createDataIndexModal = useCreateDataIndexModal();
  const restClient = useRestClient();
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

function useCreateDataIndexModal() {
  const restClient = useRestClient();
  const showToast = useToast();
  const createDataIndexMutation = useMutation(
    async ({ indexName, docTypeIds }: { indexName: string; docTypeIds: Array<string> }) => {
      return await restClient.dataIndexResource.postApiDatasourceV1DataIndexCreateDataIndexFromDocTypes({
        indexName,
        docTypeIds: docTypeIds.map(Number),
      });
    },
    {
      onSuccess(data, variables, context) {
        showToast({ displayType: "info", title: "Data Index created", content: variables.indexName });
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
      createDataIndexMutation.mutate(data);
    },
    getValidationMessages: () => {
      const errorText = (createDataIndexMutation as any).error?.body?.details;
      if (errorText) return [errorText];
      return [];
    },
  });
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
        </form>
      </ClayModal.Body>
      <ClayModal.Footer
        last={
          <ClayButton
            className={ClassNameButton}
            disabled={!form.canSubmit}
            onClick={() => {
              form.submit();
            }}
          >
            Create
          </ClayButton>
        }
      />
    </ClayModal>
  );
  return { open, content };
}
