import { CodeInput, CustomSelect, NumberInput } from "@components/Form";
import { SelectedValue } from "@components/Form/Association/MultiLinkedAssociation/types";
import {
  AutocompleteDropdown,
  Option,
  UseOptionsHook,
  UseOptionsResult,
} from "@components/Form/Select/AutocompleteDropdown";
import { useRestClient } from "@components/queryClient";
import {
  Box,
  Button,
  Checkbox,
  FormControl,
  FormControlLabel,
  Table as MuiTable,
  Paper,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import { useOptions } from "@pages/SuggestionCategories";
import { PluginDriverDocType } from "openapi-generated";
import React, { useEffect, useState } from "react";
import { ChunkType, useDocTypeFieldsQuery } from "../../../../../graphql-generated";

interface DefaultDataIndex {
  id: string;
  name: string;
}

interface MicroForm {
  name: string;
  description: string;
  knnIndex: boolean;
  docTypeId: number[];
}

interface DataIndex {
  id?: string | null;
  name?: string | null;
}

interface ChangeDataIndexParams {
  dataIndex: {
    id: string;
    name: string;
  };
}

interface SetDataIndexFormParams {
  key: "name" | "description";
  value: string | boolean;
}

type DataIndexFormsourceProps = {
  resetDataIndex: () => void;
  isDisabled: boolean;
  isCreated: boolean;
  defaultDataIndex: (data: DefaultDataIndex[]) => void;
  setActiveTab: (value: React.SetStateAction<string>) => void;
  setIsRecap: React.Dispatch<React.SetStateAction<boolean>>;
  id: string;
  microForm: MicroForm;
  setDataIndexForm: (params: SetDataIndexFormParams) => void;
  dataIndixes: DataIndex[] | undefined;
  changeDataIndex: (params: ChangeDataIndexParams) => void;
  extraParamsDataIndex: {
    knnIndex?: boolean | null | undefined;
    chunkType?: ChunkType | null | undefined;
    chunkWindowSize?: number | null | undefined;
    embeddingJsonConfig?: string | null | undefined;
    embeddingDocTypeFieldId?: { id: string; name: string } | null | undefined;
  };
  changeExtraParamsDataIndex: (
    key:
      | "knnIndex"
      | "chunkType"
      | "chunkWindowSize"
      | "embeddingJsonConfig"
      | "embeddingDocTypeFieldId"
      | "docTypeIds",
    value: string | number | boolean | number[] | { id: string; name: string } | null,
  ) => void;
};

export default function DataIndexFormsource({
  isDisabled,
  setDataIndexForm,
  dataIndixes,
  changeDataIndex,
  id,
  microForm,
  isCreated,
  setActiveTab,
  setIsRecap,
  defaultDataIndex,
  extraParamsDataIndex,
  changeExtraParamsDataIndex,
}: DataIndexFormsourceProps) {
  const restClient = useRestClient();
  const [documentTypes, setDocumentTypes] = useState<any[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const { docTypesQuery } = useOptions();

  React.useEffect(() => {
    const checkedIds = documentTypes
      .filter((item) => isCheckboxChecked(item.node.id))
      .map((item) => Number(item.node.id));
    changeExtraParamsDataIndex("docTypeIds", checkedIds);
  }, [documentTypes, dataIndixes]);

  useEffect(() => {
    let isMounted = true;

    const fetchDocumentTypes = async () => {
      try {
        const response = await restClient.pluginDriverResource.getApiDatasourcePluginDriversDocumentTypes(Number(id));
        if (isMounted) {
          const constructData = response?.docTypes?.map((item: PluginDriverDocType) => {
            return {
              node: {
                id: String(item.docTypeId || ""),
                name: item.name || "",
                selected: item?.selected,
              },
            };
          });
          setDocumentTypes(constructData || []);

          const defaultSelected = constructData
            ?.filter((item) => item.node.selected)
            .map((item) => ({ id: item.node.id, name: item.node.name }));
          if (defaultSelected) {
            defaultDataIndex(defaultSelected);
          }

          setIsLoading(false);
        }
      } catch (error) {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    fetchDocumentTypes();

    return () => {
      isMounted = false;
    };
  }, [id, restClient]);

  const isCheckboxChecked = (id: string) => {
    return dataIndixes?.some((it) => it.id === id);
  };

  const loadMoreOptions = async (): Promise<{ value: string; label: string }[]> => {
    if (!docTypesQuery.data?.docTypeFields?.pageInfo?.hasNextPage) return [];

    try {
      const response = await docTypesQuery.fetchMore({
        variables: {
          after: docTypesQuery.data.docTypeFields.pageInfo.endCursor,
        },
      });

      const newEdges = response.data?.docTypeFields?.edges || [];
      const newPageInfo = response.data?.docTypeFields?.pageInfo;

      if (!newEdges.length || !newPageInfo) {
        console.warn("No new data fetched or pageInfo is missing.");
        return [];
      }

      docTypesQuery.updateQuery((prev) => ({
        ...prev,
        docTypeFields: {
          ...prev.docTypeFields,
          edges: [...(prev.docTypeFields?.edges || []), ...newEdges],
          pageInfo: newPageInfo,
        },
      }));

      return newEdges
        .map((item) => ({
          value: item?.node?.id || "",
          label: item?.node?.name || "",
        }))
        .filter((option) => option.value && option.label);
    } catch (error) {
      console.error("Error loading more options:", error);
      return [];
    }
  };

  return (
    <div>
      {isLoading && "loading..."}
      {!isLoading && (
        <FormControl>
          <div>
            <FormControl fullWidth margin="normal">
              <Box sx={{ marginBottom: 1 }}>
                <Typography variant="subtitle1" component="label" htmlFor={"name-create-data-index"}>
                  {"Name"}
                </Typography>
              </Box>
              <TextField
                placeholder="Insert text here"
                id="name-create-data-index"
                disabled={isDisabled}
                value={microForm.name || ""}
                onChange={(e) => {
                  setDataIndexForm({ key: "name", value: e.target.value });
                }}
              />
              <Box sx={{ marginBottom: 1 }}>
                <Typography variant="subtitle1" component="label" htmlFor={"description-create-data-index"}>
                  {"Description"}
                </Typography>
              </Box>
              <TextField
                placeholder="Insert description here"
                id="description-create-data-index"
                disabled={isDisabled}
                value={microForm.description || ""}
                onChange={(e) => {
                  setDataIndexForm({ key: "description", value: e.target.value });
                }}
              />
            </FormControl>
            <FormControl fullWidth margin="normal">
              <Box sx={{ marginBottom: 1 }}>
                <Typography variant="subtitle1" component="label">
                  {"Associate Datasource"}
                </Typography>
              </Box>
              <TableContainer component={Paper} sx={{ width: "70vw" }}>
                <MuiTable>
                  <TableHead>
                    <TableRow>
                      <TableCell></TableCell>
                      <TableCell>Name</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {documentTypes?.map((item) => (
                      <TableRow key={item?.node?.id}>
                        <TableCell>
                          <FormControlLabel
                            control={
                              <Checkbox
                                checked={isCheckboxChecked(item?.node?.id || "")}
                                disabled={isDisabled}
                                onChange={() => {
                                  changeDataIndex({ dataIndex: { id: item?.node?.id || "", name: item?.node.name } });
                                }}
                              />
                            }
                            label=""
                          />
                        </TableCell>
                        <TableCell>{item?.node?.name}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </MuiTable>
              </TableContainer>
              <FormControlLabel
                control={
                  <Checkbox
                    checked={extraParamsDataIndex.knnIndex || false}
                    disabled={isDisabled}
                    onChange={(e) => {
                      changeExtraParamsDataIndex("knnIndex", e.target.checked);
                      if (!e.target.checked) {
                        changeExtraParamsDataIndex("chunkType", null);
                        changeExtraParamsDataIndex("chunkWindowSize", null);
                        changeExtraParamsDataIndex("embeddingJsonConfig", null);
                      }
                    }}
                  />
                }
                label="Enable KNN Index"
              />
              <Box sx={{ marginBottom: 1 }}>
                <CustomSelect
                  label="Chunk Type"
                  id="chunk-type-select"
                  dict={Object.fromEntries(Object.entries(ChunkType).filter(([key]) => key !== "Unrecognized"))}
                  disabled={isDisabled || !extraParamsDataIndex.knnIndex}
                  value={extraParamsDataIndex.chunkType || ChunkType.ChunkTypeCharacterTextSplitter}
                  onChange={(e: string) => {
                    changeExtraParamsDataIndex("chunkType", e);
                  }}
                  validationMessages={[]}
                />
              </Box>
              <NumberInput
                disabled={isDisabled || !extraParamsDataIndex.knnIndex}
                value={extraParamsDataIndex.chunkWindowSize || 0}
                onChange={(e) => {
                  changeExtraParamsDataIndex("chunkWindowSize", Number(e));
                }}
                id="chunk-window-size"
                label="Chunk Window Size"
                validationMessages={[]}
              />
              <CodeInput
                disabled={isDisabled || !extraParamsDataIndex.knnIndex}
                readonly={isDisabled || !extraParamsDataIndex.knnIndex}
                language="json"
                value={extraParamsDataIndex.embeddingJsonConfig || ""}
                onChange={(e) => {
                  changeExtraParamsDataIndex("embeddingJsonConfig", e);
                }}
                id="embedding-json-config"
                label="Embedding JSON Config"
                validationMessages={[]}
              />
              <AutocompleteDropdown
                label="Doc Type Field"
                onChange={(val) =>
                  changeExtraParamsDataIndex("embeddingDocTypeFieldId", { id: val.id, name: val.name })
                }
                value={
                  !extraParamsDataIndex?.embeddingDocTypeFieldId?.id
                    ? undefined
                    : {
                        id: extraParamsDataIndex.embeddingDocTypeFieldId?.id ?? "",
                        name: extraParamsDataIndex.embeddingDocTypeFieldId?.name ?? "",
                      }
                }
                onClear={() => changeExtraParamsDataIndex("embeddingDocTypeFieldId", null)}
                disabled={isDisabled}
                useOptions={useDocTypeOptions}
              />
            </FormControl>
          </div>
        </FormControl>
      )}
      <Box
        sx={{
          marginTop: "10px",
          display: "flex",
          justifyContent: "space-between",
        }}
      >
        <Button
          variant="contained"
          color="secondary"
          aria-label="Back"
          onClick={() => {
            setActiveTab("datasource");
          }}
        >
          Back
        </Button>
        <Button
          variant="contained"
          aria-label="Recap"
          onClick={() => {
            isCreated && setIsRecap(true);
            setActiveTab("dataIndex");
          }}
        >
          Recap
        </Button>
      </Box>
    </div>
  );
}

export const useDocTypeOptions: UseOptionsHook = (searchText: string): UseOptionsResult => {
  const { data, loading } = useDocTypeFieldsQuery({ variables: { searchText } });
  const edges = data?.docTypeFields?.edges ?? [];
  const options: Option[] = edges.map((e: any) => ({ value: e?.node?.id ?? "", label: e?.node?.name ?? "" }));
  return { options, loading: !!loading };
};
