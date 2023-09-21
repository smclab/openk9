import React from "react";
import { QueryResult, gql } from "@apollo/client";
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

  const [allDocTypes, setAllDocTypes] = React.useState<Array<DocTypesQueryType>>([
    {
      idParent: 0,
      queryData: useDocTypeFieldsByParentQuery({
        variables: { searchText: "", parentId: 0 },
        skip: !documentTypeId,
      }),
    },
  ]);

  React.useEffect(() => {
    console.log(allDocTypes);
  }, [allDocTypes]);
  // allDocTypes.forEach((all, index) => {
  //   console.log(all, index);
  // });
  const documentTypeFieldsParentsQuery = useDocTypeFieldsByParentQuery({
    variables: { searchText: "", parentId: 0 },
    skip: !documentTypeId,
  });
  const [deleteDocumentTypeFieldMutate] = useDeleteDocumentTypeFieldMutation({
    refetchQueries: [{ query: DocumentTypeFieldsQuery, variables: { documentTypeId: documentTypeId! } }],
  });
  const [updateDocumentTypeFieldMutate] = useCreateOrUpdateDocumentTypeFieldMutation({
    refetchQueries: [{ query: DocumentTypeFieldsQuery, variables: { documentTypeId: documentTypeId! } }],
  });

  // async function addElement({ parentId, level }: { parentId: number; level: number }) {
  //   try {
  //     const { data } = await apolloClient.query({
  //       query: DocumentTypeFieldsParentQuery,
  //       variables: { searchText: "", parentId: parentId },
  //     });
  //     setAllDocTypes((prevAllDocTypes) => [
  //       ...prevAllDocTypes,
  //       {
  //         idParent: parentId,
  //         queryData: data,
  //         level: level,
  //       },
  //     ]);
  //   } catch (error) {
  //     console.error("Errore nella query:", error);
  //   }
  // }

  // const allLevels: (
  //   | {
  //       __typename?: "DocTypeField" | undefined;
  //       id?: string | null | undefined;
  //       name?: string | null | undefined;
  //       fieldName?: string | null | undefined;
  //       searchable: boolean;
  //       exclude?: boolean | null | undefined;
  //       sortable: boolean;
  //       parent?:
  //         | { __typename?: "DocTypeField" | undefined; id?: string | null | undefined; fieldName?: string | null | undefined }
  //         | null
  //         | undefined;
  //     }
  //   | null
  //   | undefined
  // )[][] = [];
  // const firstLevel: (
  //   | {
  //       __typename?: "DocTypeField" | undefined;
  //       id?: string | null | undefined;
  //       name?: string | null | undefined;
  //       fieldName?: string | null | undefined;
  //       searchable: boolean;
  //       exclude?: boolean | null | undefined;
  //       sortable: boolean;
  //       parent?:
  //         | { __typename?: "DocTypeField" | undefined; id?: string | null | undefined; fieldName?: string | null | undefined }
  //         | null
  //         | undefined;
  //     }
  //   | null
  //   | undefined
  // )[] = [];
  // documentTypeFieldsParentsQuery.data?.docTypeFieldsByParent?.edges?.forEach((element) => {
  //   // console.log(element?.node)
  //   firstLevel.push(element?.node);
  // });
  // allLevels.push(firstLevel);

  async function addRemoveLevel({ parentId }: { parentId: number }) {
    try {
      const { data } = await apolloClient.query({
        query: DocumentTypeFieldsParentQuery,
        variables: { searchText: "", parentId: parentId },
      });

      const queryResult = data?.docTypeFieldsByParent?.edges;
      const allDocTypesLength = allDocTypes.length;
      for (let index = allDocTypesLength - 1; index >= 0; index--) {
        const parent = allDocTypes[index];
        const queryResultLength = queryResult.length;
        if (queryResultLength === 0) {
          allDocTypes.splice(index + 1, 1);
        }
        for (let i = 0; i < queryResultLength; i++) {
          const son = queryResult[i].node;
          if (
            parent.queryData.data?.docTypeFieldsByParent?.edges?.find((document) => document?.node?.id === son.parent.id) ||
            !parent.queryData.data
          ) {
            allDocTypes.splice(index + 1, 1);
          }
        }
      }
      const elementToAdd = {
        idParent: parentId,
        queryData: data,
      };
      if (elementToAdd.queryData?.docTypeFieldsByParent?.edges.length > 0) {
        // allDocTypes.push(elementToAdd);
        // setAllDocTypes(allDocTypes);
        setAllDocTypes((prevArray) => [...prevArray, elementToAdd]);
      }

      console.log("allDocTypes", allDocTypes);
    } catch (error) {
      console.error("Errore nella query:", error);
    }
  }
  if (!documentTypeId) throw new Error();

  function CreateDocType({
    documentType,
  }: {
    documentType: {
      idParent: number;
      queryData: QueryResult<
        DocTypeFieldsByParentQuery,
        Exact<{
          searchText?: InputMaybe<string> | undefined;
          parentId: any;
        }>
      >;
    } | null;
  }): JSX.Element {
    const sameData = documentType?.queryData.data?.docTypeFieldsByParent?.edges;
    return (
      <div>{sameData?.map((sinlgeData) => <div   onClick={() => {
                    addRemoveLevel({ parentId: Number(sinlgeData?.node?.id || 0) });
                  }}>{sinlgeData?.node?.name}</div>)}</div>
      // <React.Fragment>{sameData?.map((sinlgeData) => {
      //   return(
      //     <ClayList.Item flex className="align-items-center">
      //     <ClayList.ItemField expand>
      //       <ClayList.ItemTitle>{formatName({ id: sinlgeData?.node?.id, name: sinlgeData?.node?.name })}</ClayList.ItemTitle>
      //     </ClayList.ItemField>
      //     <ClayList.ItemField>
      //       <ClayList.Item flex style={{ border: "none" }}>
      //         <Link to={`newSubFields/${sinlgeData?.node?.id}/new`}>
      //           <ClayList.QuickActionMenu.Item aria-label="Plus doc type fields" title="Plus doc type fields" symbol="plus" />
      //         </Link>

      //         <DropDown
      //           trigger={
      //             <button className="btn btn-unstyled nav-btn nav-btn-monospaced" style={{ width: "50px", height: "35px" }}>
      //               <ClayIcon symbol={"angle-down"} style={{ color: "black" }} />
      //             </button>
      //           }
      //         >
      //           <DropDown.ItemList>
      //             <DropDown.Item>
      //               <div style={{ display: "flex", justifyContent: "space-between" }}>
      //                 <div>
      //                   <label>Searchable</label>
      //                 </div>
      //                 <div>
      //                   <ClayToggle
      //                     toggled={sinlgeData?.node?.searchable ?? false}
      //                     onToggle={(schedulable) => {
      //                       const documentTypeElement = sinlgeData?.node;
      //                       if (documentTypeElement && documentTypeElement.id && documentTypeElement.name && documentTypeElement.parent) {
      //                       }
      //                     }}
      //                   />
      //                 </div>
      //               </div>
      //             </DropDown.Item>
      //             <DropDown.Divider />
      //             <DropDown.Item>
      //               <div style={{ display: "flex", justifyContent: "space-between" }}>
      //                 <div>
      //                   <label>Exclude</label>
      //                 </div>
      //                 <div>
      //                   <ClayToggle
      //                     toggled={sinlgeData?.node?.exclude ?? false}
      //                     onToggle={(schedulable) => {
      //                       const documentTypeElement = sinlgeData?.node;
      //                       if (documentTypeElement && documentTypeElement.id && documentTypeElement.name && documentTypeElement.parent) {
      //                       }
      //                     }}
      //                   />
      //                 </div>
      //               </div>
      //             </DropDown.Item>
      //             <DropDown.Divider />
      //             <DropDown.Item>
      //               <div style={{ display: "flex", justifyContent: "space-between" }}>
      //                 <div>
      //                   <label>Sortable</label>
      //                 </div>
      //                 <div>
      //                   <ClayToggle
      //                     toggled={sinlgeData?.node?.sortable ?? false}
      //                     onToggle={(schedulable) => {
      //                       const documentTypeElement = sinlgeData?.node;
      //                       if (documentTypeElement && documentTypeElement.id && documentTypeElement.name && documentTypeElement.parent) {
      //                       }
      //                     }}
      //                   />
      //                 </div>
      //               </div>
      //             </DropDown.Item>
      //           </DropDown.ItemList>
      //         </DropDown>
      //         <ClayList.QuickActionMenu.Item
      //           aria-label="View sub doc type fields"
      //           title="View sub doc type fields"
      //           onClick={() => {
      //             addRemoveLevel({ parentId: Number(sinlgeData?.node?.id || 0) });
      //           }}
      //           symbol="angle-right"
      //         />
      //       </ClayList.Item>
      //     </ClayList.ItemField>
      //   </ClayList.Item>
      //   )
      // })}
      // </React.Fragment>
    );
  }

  return (
    <React.Fragment>
      {allDocTypes.map((docType, index) => {
        console.log(docType)
        return (
          <div>
            <ContainerFluid>
              {index}
              <ClayList>
                <CreateDocType documentType={docType} />
              </ClayList>
            </ContainerFluid>
          </div>
        );
      })}
    </React.Fragment>
  );
}

type DocTypesType = {
  idParent: number;
  element: React.ReactNode;
};

type DocTypesQueryType = {
  idParent: number;
  queryData: QueryResult<
    DocTypeFieldsByParentQuery,
    Exact<{
      searchText?: InputMaybe<string> | undefined;
      parentId: any;
    }>
  >;
};

// function removeElementsByLevels(elements: DocTypesType[], levelSelect: number): DocTypesType[] {
//   return elements.filter((elementInfo) => elementInfo.level <= levelSelect);
// }

// function findLevel({ docTypes, addedId }: { docTypes: Array<DocTypesQueryType>; addedId: number }): number {
//   const docElement = docTypes.find((document) => document.idParent === addedId);
//   let nextLevel = 0;
//   if (docElement?.level === undefined) nextLevel = docTypes.reduce((max, doc) => (doc.level > max ? doc.level : max), 0);
//   return docElement?.level || nextLevel;
// }
