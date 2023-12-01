import { gql } from "@apollo/client";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  BooleanInput,
  ContainerFluid,
  CustomButtom,
  EnumSelect,
  fromFieldValidators,
  SearchSelect,
  TextArea,
  TextInput,
  useForm,
} from "./Form";
import { TabTokens } from "./TabTokens";
import {
  TokenType,
  useAddExtraParamMutation,
  useBindDocTypeFieldToTabTokenMutation,
  useCreateOrUpdateTabTokenMutation,
  useDocTypeFieldOptionsTokenTabQuery,
  useDocTypeFieldValueQuery,
  useTabTokenTabQuery,
  useUnbindDocTypeFieldToTabTokenMutation,
} from "../graphql-generated";
import { useToast } from "./ToastProvider";

const TabTokenQuery = gql`
  query TabTokenTab($id: ID!) {
    tokenTab(id: $id) {
      id
      name
      description
      value
      filter
      tokenType
      docTypeField {
        id
      }
      extraParams {
        key
        value
      }
    }
  }
`;

gql`
  mutation CreateOrUpdateTabToken(
    $tabTokenId: ID
    $name: String!
    $description: String
    $value: String!
    $filter: Boolean!
    $tokenType: TokenType!
  ) {
    tokenTab(
      id: $tabTokenId
      tokenTabDTO: { name: $name, description: $description, filter: $filter, tokenType: $tokenType, value: $value }
    ) {
      entity {
        id
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;

gql`
  mutation addExtraParam($id: ID!, $key: String, $value: String!) {
    addExtraParam(id: $id, key: $key, value: $value) {
      name
    }
  }
`;

enum fuzziness {
  ZERO = "ZERO",
  ONE = "ONE",
  TWO = "TWO",
  AUTO = "AUTO",
}

const fuzzinessDefaultValue = fuzziness.ZERO.toString();
const boostDefaultValue = "50";

export function TabToken() {
  const { tabTokenId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const tabTokenTabQuery = useTabTokenTabQuery({
    variables: { id: tabTokenId as string },
    skip: !tabTokenId || tabTokenId === "new",
  });
  const [createOrUpdateTabTokenMutate, createOrUpdateTabTokenMutation] = useCreateOrUpdateTabTokenMutation({
    refetchQueries: [TabTokenQuery, TabTokens],
    onCompleted(data: any) {
      if (data.tokenTab?.entity) {
        if (tabTokenId === "new") {
          navigate(`/token-tabs`, { replace: true });
          showToast({ displayType: "success", title: "Token Tab created", content: "" });
        } else {
          navigate(`/token-tabs/${data.tokenTab.entity.id}`, { replace: true });
          showToast({ displayType: "success", title: "Token Tab update", content: "" });
        }
      }
    },
  });

  const [addExtraParamMutate, addExtraParamMutation] = useAddExtraParamMutation({
    refetchQueries: [TabTokens],
  });

  const boostValue = tabTokenTabQuery.data?.tokenTab?.extraParams?.find((element: any) => element.key === "boost")?.value?.toString();
  const fuzzinessValue = tabTokenTabQuery.data?.tokenTab?.extraParams
    ?.find((element: any) => element.key === "fuzziness")
    ?.value?.toString();
  const tokenTypeInitialValue = tabTokenTabQuery.data?.tokenTab?.tokenType;

  const originalValues = {
    name: tabTokenTabQuery.data?.tokenTab?.name,
    description: tabTokenTabQuery.data?.tokenTab?.description,
    value: tabTokenTabQuery.data?.tokenTab?.value,
    filter: tabTokenTabQuery.data?.tokenTab?.filter,
    tokenType: tabTokenTabQuery.data?.tokenTab?.tokenType,
    boost: boostValue,
    fuzziness: fuzzinessValue,
  };

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        value: "",
        filter: true,
        tokenType: TokenType.Autocomplete,
        boost: boostDefaultValue,
        fuzziness: fuzzinessDefaultValue,
      }),
      []
    ),
    originalValues: originalValues,
    isLoading: tabTokenTabQuery.loading || createOrUpdateTabTokenMutation.loading,
    onSubmit(data) {
      createOrUpdateTabTokenMutate({
        variables: {
          tabTokenId: tabTokenId !== "new" ? tabTokenId : undefined,
          ...data,
        },
      });

      if (data.tokenType === "TEXT" || data.tokenType === "FILTER") {
        addExtraParamMutate({
          variables: {
            id: tabTokenId,
            key: "fuzziness",
            value: data.fuzziness ? data.fuzziness : fuzzinessDefaultValue,
          },
        });
        addExtraParamMutate({
          variables: {
            id: tabTokenId,
            key: "boost",
            value: data.boost ? data.boost : boostDefaultValue,
          },
        });
      }
    },
    getValidationMessages: fromFieldValidators(createOrUpdateTabTokenMutation.data?.tokenTab?.fieldValidators),
  });

  return (
    <React.Fragment>
      <ContainerFluid>
        <form
          className="sheet"
          onSubmit={(event) => {
            event.preventDefault();
            form.submit();
          }}
        >
          <TextInput label="Name" {...form.inputProps("name")} />
          <TextArea label="Description" {...form.inputProps("description")} />
          <TextInput label="Value" {...form.inputProps("value")} description="Value it must match for this token" />
          <EnumSelect
            label="Token Type"
            dict={TokenType}
            {...form.inputProps("tokenType")}
            onChange={(tokenType: TokenType) => {
              form.inputProps("tokenType").onChange(tokenType);
              if (tokenType !== tokenTypeInitialValue) {
                form.inputProps("boost").onChange(boostDefaultValue);
                form.inputProps("fuzziness").onChange(fuzzinessDefaultValue);
              } else {
                form.inputProps("boost").onChange(boostValue ? boostValue : boostDefaultValue);
                form.inputProps("fuzziness").onChange(fuzzinessValue ? fuzzinessValue : fuzzinessDefaultValue);
              }
            }}
          />
          <BooleanInput label="Filter" {...form.inputProps("filter")} />
          {tabTokenId !== "new" && (
            <form
              onSubmit={(event) => {
                event.preventDefault();
              }}
            >
              <SearchSelect
                label="Document Type Field"
                value={tabTokenTabQuery.data?.tokenTab?.docTypeField?.id}
                useValueQuery={useDocTypeFieldValueQuery}
                useChangeMutation={useBindDocTypeFieldToTabTokenMutation}
                mapValueToMutationVariables={(documentTypeFieldId) => ({ tokenTabId: tabTokenId, documentTypeFieldId })}
                useOptionsQuery={useDocTypeFieldOptionsTokenTabQuery}
                useRemoveMutation={useUnbindDocTypeFieldToTabTokenMutation}
                mapValueToRemoveMutationVariables={() => ({
                  tokenTabId: tabTokenId,
                  documentTypeFieldId: tabTokenTabQuery.data?.tokenTab?.docTypeField?.id!,
                })}
                invalidate={() => tabTokenTabQuery.refetch()}
                description={"Document Type Field associated to this token"}
              />
            </form>
          )}
          {(form.inputProps("tokenType").value === "TEXT" || form.inputProps("tokenType").value === "FILTER") && (
            <div>
              <TextInput label="boost" {...form.inputProps("boost")} />
              <EnumSelect label="fuzziness" dict={fuzziness} {...form.inputProps("fuzziness")} />
            </div>
          )}
          <div className="sheet-footer">
            <CustomButtom nameButton={tabTokenId === "new" ? "Create" : "Update"} canSubmit={!form.canSubmit} typeSelectet="submit" />
          </div>
        </form>
      </ContainerFluid>
    </React.Fragment>
  );
}

gql`
  query DocTypeFieldOptionsTokenTab($searchText: String, $cursor: String) {
    options: docTypeFields(searchText: $searchText, first: 5, after: $cursor) {
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
  query DocTypeFieldValue($id: ID!) {
    value: docTypeField(id: $id) {
      id
      name
      description
    }
  }
`;
gql`
  mutation BindDocTypeFieldToTabToken($documentTypeFieldId: ID!, $tokenTabId: ID!) {
    bindDocTypeFieldToTokenTab(docTypeFieldId: $documentTypeFieldId, tokenTabId: $tokenTabId) {
      left {
        id
        docTypeField {
          id
        }
      }
      right {
        id
      }
    }
  }
`;
gql`
  mutation UnbindDocTypeFieldToTabToken($documentTypeFieldId: ID!, $tokenTabId: ID!) {
    unbindDocTypeFieldFromTokenTab(docTypeFieldId: $documentTypeFieldId, id: $tokenTabId) {
      left {
        id
      }
    }
  }
`;
