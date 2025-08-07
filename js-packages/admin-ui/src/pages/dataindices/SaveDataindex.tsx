import { CircularProgress, Container } from "@mui/material";
import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { ChunkType, useDataIndexQuery } from "../../graphql-generated";
import { CreateDataindex, VerifyDataindexData } from "./Functions";

export type DataindexData = {
  dataindexId: string;
  datasourceId: { id: string; name: string } | null | undefined;
  name: string;
  description: string;
  knnIndex: boolean;
  docTypeIds: number[];
  docTypeString: string[];
  chunkType: string | null | undefined;
  chunkWindowSize: number | null | undefined;
  embeddingDocTypeFieldId: { id: string; name: string } | null | undefined;
  settings: string | null | undefined;
  embeddingJsonConfig: string | null | undefined;
};

export function SaveDataindex() {
  const { dataindexId = "new", mode } = useParams();
  const [dataindexData, setDataindexData] = useState<DataindexData | null | undefined>(null);
  const [verifyData, setVerifyData] = useState(mode);

  const dataindexQuery = useDataIndexQuery({
    variables: { id: dataindexId },
    skip: !dataindexId || dataindexId === "new",
    errorPolicy: "ignore",
  });

  useEffect(() => {
    const fetchDataindexValues = () => {
      if (dataindexId === "new") {
        return {
          dataindexId: dataindexId,
          datasourceId: { id: "", name: "" },
          name: "",
          description: "",
          knnIndex: false,
          docTypeIds: [],
          docTypeString: [],
          chunkWindowSize: 0,
          settings: "{}",
          embeddingJsonConfig: "{}",
          associatedDatasource: {},
          associatedDocumentTypes: [],
          embeddingDocTypeFieldId: { id: "", name: "" },
          chunkType: ChunkType.ChunkTypeCharacterTextSplitter,
        };
      } else {
        const associatedDocumentTypes =
          dataindexQuery.data?.dataIndex?.docTypes?.edges?.map((edge) => ({
            id: edge?.node?.id,
            name: edge?.node?.name,
          })) || [];

        return {
          dataindexId: dataindexId,
          datasourceId: {
            id: dataindexQuery?.data?.dataIndex?.datasource?.id || "",
            name: dataindexQuery?.data?.dataIndex?.datasource?.name || "",
          },
          name: dataindexQuery.data?.dataIndex?.name || "",
          description: dataindexQuery.data?.dataIndex?.description || "",
          knnIndex: dataindexQuery.data?.dataIndex?.knnIndex || false,
          docTypeIds:
            dataindexQuery.data?.dataIndex?.docTypes?.edges
              ?.map((doc) => (doc?.node?.id ? Number(doc.node.id) : null))
              .filter((id): id is number => id !== null) || [],
          docTypeString:
            dataindexQuery.data?.dataIndex?.docTypes?.edges
              ?.map((doc) => (doc?.node?.name ? doc.node.name : null))
              .filter((name): name is string => name !== null) || [],
          chunkType: dataindexQuery.data?.dataIndex?.chunkType || "ChunkTypeCharacterTextSplitter",
          chunkWindowSize: dataindexQuery.data?.dataIndex?.chunkWindowSize || 2000,
          embeddingDocTypeFieldId: {
            id: dataindexQuery?.data?.dataIndex?.embeddingDocTypeField?.id || "",
            name: dataindexQuery?.data?.dataIndex?.embeddingDocTypeField?.name || "",
          },
          settings: dataindexQuery.data?.dataIndex?.settings || "{}",
          embeddingJsonConfig: dataindexQuery.data?.dataIndex?.embeddingJsonConfig || "{}",
          associatedDatasource: {},
          associatedDocumentTypes: associatedDocumentTypes,
        };
      }
    };

    const newDataindexValues = fetchDataindexValues();
    setDataindexData(newDataindexValues);
  }, [dataindexId, dataindexQuery]);

  if (dataindexQuery.loading) {
    return <CircularProgress />;
  }

  return (
    <Container maxWidth={false}>
      {verifyData === "edit" && (
        <CreateDataindex
          dataindexData={dataindexData}
          setDataindexData={setDataindexData}
          verifyData={verifyData}
          setVerifyData={setVerifyData}
          isReadOnly={false}
        />
      )}
      {(verifyData === "view" || verifyData === "editView") && (
        <VerifyDataindexData dataindexData={dataindexData} verifyData={verifyData} setVerifyData={setVerifyData} />
      )}
    </Container>
  );
}
