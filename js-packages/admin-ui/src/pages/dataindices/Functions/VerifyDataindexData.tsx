import { CodeInput, ContainerFluid, useToast } from "@components/Form";
import { Box, Button, Stack, TextField, Typography } from "@mui/material";
import React from "react";
import { Link, useNavigate } from "react-router-dom";
import { ChunkType, useCreateDataIndexMutation } from "../../../graphql-generated";
import { DataindexData } from "../SaveDataindex";

export const bytesToMegabytes = (bytes: number): number => parseFloat((bytes / (1024 * 1024)).toFixed(2));

export function VerifyDataindexData({
  dataindexData,
  verifyData,
  setVerifyData,
}: {
  dataindexData: DataindexData | null | undefined;
  verifyData: string;
  setVerifyData: React.Dispatch<React.SetStateAction<string | undefined>>;
}) {
  const toast = useToast();
  const navigate = useNavigate();

  const [createOrUpdateDataIndexModelMutate] = useCreateDataIndexMutation({
    onCompleted(data) {
      if (data.dataIndex?.entity) {
        toast({
          title: `Data Index Model is Created`,
          content: ``,
          displayType: "success",
        });
        navigate(`/dataindices/`);
      } else {
        toast({
          title: `Error`,
          content: "",
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.log(error);
      const isNew = "create";
      toast({
        title: `Error ${isNew}`,
        content: `Impossible to ${isNew} Data Index Model`,
        displayType: "error",
      });
    },
  });

  const saveDataindex = () => {
    if (dataindexData) {
      const variables = {
        ...(Number(dataindexData.datasourceId?.id) > 0 && { datasourceId: Number(dataindexData.datasourceId?.id) }),
        name: dataindexData.name,
        description: dataindexData.description,
        docTypeIds: dataindexData.docTypeIds,
        ...(dataindexData.embeddingDocTypeFieldId?.id && {
          embeddingDocTypeFieldId: dataindexData.embeddingDocTypeFieldId?.id,
        }),
        settings: dataindexData.settings,
        embeddingJsonConfig: dataindexData.embeddingJsonConfig,
        ...(dataindexData.knnIndex !== null &&
          dataindexData.knnIndex !== undefined && { knnIndex: dataindexData.knnIndex }),
        ...(dataindexData.chunkType && { chunkType: dataindexData.chunkType as ChunkType }),
        ...(dataindexData.chunkWindowSize !== null &&
          dataindexData.chunkWindowSize !== undefined && { chunkWindowSize: dataindexData.chunkWindowSize }),
      };

      createOrUpdateDataIndexModelMutate({ variables: { ...variables, datasourceId: variables.datasourceId as any } });
    }
  };

  if (!dataindexData || !dataindexData?.name) return null;

  return (
    <ContainerFluid>
      <Box sx={{ paddingBlock: "16px" }}>
        <Typography component="h1" variant="h1" fontWeight="600">
          View Data Index
        </Typography>
      </Box>
      <Stack spacing={3}>
        <TextField label="Dataindex Name" value={dataindexData.name} disabled fullWidth />
        <TextField label="Description" value={dataindexData.description} disabled fullWidth />
        <TextField label="Datasource" value={dataindexData.datasourceId?.name || ""} disabled fullWidth />
        <TextField
          label="Embedding Doc Type Field"
          value={dataindexData.embeddingDocTypeFieldId?.name || ""}
          disabled
          fullWidth
        />
        <TextField label="Document Type" value={dataindexData.docTypeIds?.join(", ") || ""} disabled fullWidth />
        {dataindexData.knnIndex === true && (
          <>
            <TextField label="Chunk Type" value={dataindexData.chunkType || "N/A"} disabled fullWidth />
            <TextField label="Chunk Window Size" value={dataindexData.chunkWindowSize || "N/A"} disabled fullWidth />
          </>
        )}
      </Stack>
      <Box sx={{ mt: 3 }}>
        <CodeInput
          disabled
          readonly
          id="code"
          label="embedding json config"
          language="json"
          onChange={() => {}}
          validationMessages={[]}
          value={dataindexData.embeddingJsonConfig || "{}"}
        />
      </Box>
      <Box sx={{ mt: 3 }}>
        <CodeInput
          onChange={() => {}}
          validationMessages={[]}
          disabled
          readonly
          id="idRecapDataIndex"
          label="Recap JSON Config"
          language="json"
          value={dataindexData?.settings || "{}"}
        />
      </Box>
      <Stack direction="row" spacing={2} justifyContent="flex-end" sx={{ mt: 4 }}>
        {verifyData === "editView" && (
          <>
            <Button variant="contained" color="secondary" onClick={() => setVerifyData("edit")}>
              Back
            </Button>
            <Button variant="contained" color="error" onClick={saveDataindex}>
              Create Dataindex
            </Button>
          </>
        )}
        {verifyData === "view" && (
          <Button variant="contained" color="secondary" component={Link} to="/dataindices">
            Back
          </Button>
        )}
      </Stack>
    </ContainerFluid>
  );
}
