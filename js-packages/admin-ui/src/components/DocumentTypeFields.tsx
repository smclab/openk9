import React from "react";
import { QueryResult, gql, useQuery } from "@apollo/client";
import {
  DocTypeFieldsByParentQuery,
  Exact,
  FieldType,
  InputMaybe,
  useCreateOrUpdateDocumentTypeFieldMutation,
  useDeleteDocumentTypeFieldMutation,
  useDocTypeFieldsByParentQuery,
  useDocumentTypeFieldsQuery,
} from "../graphql-generated";
import { formatName, TableWithSubFields } from "./Table";
import { Link, useParams } from "react-router-dom";
import { DocumentTypeFieldsQuery } from "./SubFieldsDocumentType";
import { ClayToggle } from "@clayui/form";
import { ContainerFluid, StyleToggle } from "./Form";
import { ClayTooltipProvider } from "@clayui/tooltip";
import { CategoricalChartFunc } from "recharts/types/chart/generateCategoricalChart";
import ClayIcon from "@clayui/icon";
import ClayList from "@clayui/list";
import DropDown from "@clayui/drop-down";
import { apolloClient } from "./apolloClient";

gql`
  mutation DeleteDocumentTypeField($documentTypeId: ID!, $documentTypeFieldId: ID!) {
    removeDocTypeField(docTypeId: $documentTypeId, docTypeFieldId: $documentTypeFieldId) {
      right
    }
  }
`;

export const DocumentTypeFieldsParentQuery = gql`
  query DocTypeFieldsByParent($searchText: String, $parentId: BigInteger!) {
    docTypeFieldsByParent(parentId: $parentId, searchText: $searchText, first: 10) {
      edges {
        node {
          id
          name
          fieldName
          searchable
          exclude
          sortable
          parent {
            id
            fieldName
          }
        }
      }
    }
  }
`;

export function DocumentTypeFields() {
  const { documentTypeId } = useParams();

  const [selectedDocumentId, setSelectedDocumentId] = React.useState<string | null>(null);

  const { data, loading, error } = useQuery(DocumentTypeFieldsParentQuery, {
    variables: {
      searchText: "",
      parentId: 0,
    },
  });

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error.message}</div>;
  }

  const handleDocumentClick = (documentId: string) => {
    setSelectedDocumentId(documentId);
  };

  return (
    <div>
      <ContainerFluid>
        <div style={{ display: "flex", background: "white" }}>
          <ClayList style={{ marginBottom: "0" }}>
            {data.docTypeFieldsByParent.edges.map(({ node }: { node: { id: string; name: string } }) => (
              <ClayList.Item
                key={node.id}
                onClick={() => handleDocumentClick(node.id)}
                style={{ cursor: "pointer" }}
                className={node.id === selectedDocumentId ? "selected" : ""}
              >
                {node.name} - ID: {node.id}
              </ClayList.Item>
            ))}
          </ClayList>
          {selectedDocumentId && <ChildListComponent documentId={selectedDocumentId} />}
        </div>
      </ContainerFluid>
    </div>
  );
}

interface ChildListComponentProps {
  documentId: string;
}

const ChildListComponent: React.FC<ChildListComponentProps> = ({ documentId }) => {
  const { data, loading, error } = useQuery(DocumentTypeFieldsParentQuery, {
    variables: {
      searchText: "",
      parentId: Number(documentId),
    },
  });

  const [selectedChildDocumentId, setSelectedChildDocumentId] = React.useState<string | null>(null);

  React.useEffect(() => {
    setSelectedChildDocumentId(null);
  }, [documentId]);

  if (loading) {
    return <div>Loading...</div>;
  }

  if (error) {
    return <div>Error: {error.message}</div>;
  }

  const childDocuments = data.docTypeFieldsByParent.edges;

  const handleChildClick = (childId: string) => {
    setSelectedChildDocumentId(childId);
  };

  return (
    <div>
      <div style={{ display: "flex" }}>
        <ClayList>
          {childDocuments.map(({ node }: { node: { id: string; name: string } }) => (
            <ClayList.Item
              key={node.id}
              onClick={() => handleChildClick(node.id)}
              style={{ cursor: "pointer" }}
              className={node.id === selectedChildDocumentId ? "selected" : ""}
            >
              {node.name} - ID: {node.id}
            </ClayList.Item>
          ))}
        </ClayList>
        {selectedChildDocumentId && <ChildListComponent documentId={selectedChildDocumentId} />}
      </div>
    </div>
  );
};
