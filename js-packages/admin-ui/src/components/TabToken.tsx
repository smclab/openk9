import { gql } from "@apollo/client";
import ClayForm from "@clayui/form";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import { BooleanInput, EnumSelect, fromFieldValidators, SearchSelect, TextArea, TextInput, useForm } from "./Form";
import ClayButton from "@clayui/button";
import ClayLayout from "@clayui/layout";
import { ClayButtonWithIcon } from "@clayui/button";
import { Link } from "react-router-dom";
import ClayToolbar from "@clayui/toolbar";
import { TabTokens } from "./TabTokens";
import {
  TokenType,
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
    }
  }
`;

gql`
  mutation CreateOrUpdateTabToken(
    $tabId: ID!
    $tabTokenId: ID
    $name: String!
    $description: String
    $value: String!
    $filter: Boolean!
    $tokenType: TokenType!
  ) {
    tokenTab(
      tabId: $tabId
      tokenTabId: $tabTokenId
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

export function TabToken() {
  const { tabId, tabTokenId = "new" } = useParams();
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
          navigate(`/tabs/${tabId}/tab-tokens`, { replace: true });
          showToast({ displayType: "success", title: "Token Tab created", content: "" });
        } else {
          navigate(`/tabs/${tabId}/tab-tokens/${data.tokenTab.entity.id}`, { replace: true });
          showToast({ displayType: "success", title: "Token Tab update", content: "" });
        }
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        value: "",
        filter: true,
        tokenType: TokenType.Autocomplete,
      }),
      []
    ),
    originalValues: tabTokenTabQuery.data?.tokenTab,
    isLoading: tabTokenTabQuery.loading || createOrUpdateTabTokenMutation.loading,
    onSubmit(data) {
      createOrUpdateTabTokenMutate({
        variables: {
          tabId: tabId as string,
          tabTokenId: tabTokenId !== "new" ? tabTokenId : undefined,
          ...data,
        },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateTabTokenMutation.data?.tokenTab?.fieldValidators),
  });
  return (
    <React.Fragment>
      <ClayToolbar light>
        <ClayLayout.ContainerFluid>
          <ClayToolbar.Nav>
            <ClayToolbar.Item>
              <Link to={`/tabs/${tabId}/tab-tokens`}>
                <ClayButtonWithIcon aria-label="" symbol="angle-left" small />
              </Link>
            </ClayToolbar.Item>
          </ClayToolbar.Nav>
        </ClayLayout.ContainerFluid>
      </ClayToolbar>
      <ClayLayout.ContainerFluid view>
        <ClayForm
          className="sheet"
          onSubmit={(event) => {
            event.preventDefault();
            form.submit();
          }}
        >
          <TextInput label="Name" {...form.inputProps("name")}/>
          <TextArea label="Description" {...form.inputProps("description")} />
          <TextInput label="Value" {...form.inputProps("value")} description="Value it must match for this token"/>
          <EnumSelect label="Token Type" dict={TokenType} {...form.inputProps("tokenType")} />
          <BooleanInput label="Filter" {...form.inputProps("filter")} />
          {tabTokenId !== "new" && (
            <ClayForm
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
            </ClayForm>
          )}
          <div className="sheet-footer">
            <ClayButton type="submit" disabled={!form.canSubmit}>
              {tabTokenId === "new" ? "Create" : "Update"}
            </ClayButton>
          </div>
        </ClayForm>
      </ClayLayout.ContainerFluid>
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
