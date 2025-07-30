import React, { useState, useEffect, useRef } from "react";
import {
  Box,
  Button,
  Container,
  Typography,
  Breadcrumbs,
  Link,
  Chip,
  TextField,
  InputAdornment,
  keyframes,
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import SearchIcon from "@mui/icons-material/Search";
import {
  FieldType,
  useCreateOrUpdateDocumentTypeFieldMutation,
  useDocTypeFieldsByParentQuery,
} from "../../graphql-generated";
import { useParams } from "react-router-dom";
import { Logo } from "@components/common";
import { ModalConfirm } from "@components/Form";
import { SaveSubDocType } from "./SaveSubDocTypes";
import { useTheme } from "@mui/material/styles";
import SubdirectoryArrowLeftIcon from "@mui/icons-material/SubdirectoryArrowLeft";
import { Link as LinkRRD } from "react-router-dom";

type ChipProperties = {
  sortable: boolean;
  exclude: boolean;
  searchable: boolean;
};

type TreeNode = {
  id: string;
  name: string;
  description?: string;
  fieldType?: FieldType;
  jsonConfig?: string;
  boost?: number;
  fieldName?: string;
  chipProperties: ChipProperties;
  children: TreeNode[];
};

export function SubDocTypes() {
  const { documentTypeId = "new" } = useParams();
  const topRef = useRef<HTMLDivElement>(null);
  const [search, setSearch] = useState<string>("");

  const documentTypesQuery = useDocTypeFieldsByParentQuery({
    variables: { docTypeId: documentTypeId, parentId: 0, searchText: "" },
  });

  const [currentPath, setCurrentPath] = useState<string[]>([]);
  const [parentId, setParentId] = useState<number>(0);
  const [data, setData] = useState<TreeNode[]>([]);
  const [loading, setLoading] = useState<boolean>(true);
  const [processing, setProcessing] = useState<boolean>(false);
  const [fetchedData, setFetchedData] = useState<TreeNode[]>([]);
  const [updateDoctype] = useCreateOrUpdateDocumentTypeFieldMutation({
    onCompleted: () => {},
  });
  const [idModal, setIdModal] = useState<{
    id: string;
    action: "add" | "edit";
    isChild: boolean;
    parentId: string;
  } | null>(null);
  const formRef = useRef<HTMLFormElement>(null);
  const theme = useTheme();

  useEffect(() => {
    setLoading(true);
    documentTypesQuery.refetch({ docTypeId: documentTypeId, parentId, searchText: "" });
  }, [parentId]);

  useEffect(() => {
    if (documentTypesQuery.loading) {
      setLoading(true);
    } else if (documentTypesQuery.data && documentTypesQuery.data.docTypeFieldsFromDocTypeByParent?.edges) {
      const edges = documentTypesQuery.data.docTypeFieldsFromDocTypeByParent?.edges || [];
      const formattedData = edges.map((edge: any) => ({
        id: edge.node.id,
        name: edge.node.name,
        description: edge.node.description || "",
        fieldType: edge.node.fieldType || "",
        boost: edge.node.boost || 0,
        jsonConfig: edge.node.jsonConfig || null,
        fieldName: edge.node.fieldName || "",
        chipProperties: {
          sortable: edge.node.sortable || false,
          exclude: edge.node.exclude || false,
          searchable: edge.node.searchable || false,
        },
        children: [],
      }));

      setFetchedData(formattedData);
      setLoading(false);
    }
  }, [documentTypesQuery.data, documentTypesQuery.loading]);

  useEffect(() => {
    if (!loading) {
      setProcessing(true);
      setData((prevData) => {
        if (parentId === 0) {
          const filteredData = fetchedData.filter((item) => item.name.toLowerCase().includes(search.toLowerCase()));
          setProcessing(false);
          return filteredData;
        }

        const updateTree = (nodes: TreeNode[]): TreeNode[] =>
          nodes.map((node) => {
            if (node.id === String(parentId)) {
              const filteredChildren = fetchedData.filter((item) =>
                item.name.toLowerCase().includes(search.toLowerCase()),
              );
              return { ...node, children: filteredChildren.length > 0 ? filteredChildren : [] };
            }
            if (node.children) {
              return { ...node, children: updateTree(node.children) };
            }
            return node;
          });

        setProcessing(false);
        return updateTree(prevData);
      });
    }
  }, [loading, fetchedData, parentId, search]);

  const findNodeByPath = (path: string[], nodes: TreeNode[]): TreeNode | TreeNode[] | undefined => {
    if (path.length === 0) return nodes;
    const [currentId, ...rest] = path;
    const currentNode = nodes.find((node) => node.id === currentId);
    if (currentNode && rest.length > 0 && currentNode.children) {
      return findNodeByPath(rest, currentNode.children);
    }
    return currentNode;
  };

  const toggleChip = async (child: TreeNode, id: string, property: keyof ChipProperties) => {
    const updatedValue = !child.chipProperties[property];

    try {
      await updateDoctype({
        variables: {
          documentTypeId: documentTypeId,
          documentTypeFieldId: child.id,
          name: child.name,
          description: child.description || "",
          fieldType: child.fieldType || FieldType.AnnotatedText,
          boost: child.boost || 0,
          searchable: property === "searchable" ? updatedValue : child.chipProperties.searchable,
          fieldName: child.fieldName || "",
          jsonConfig: child.jsonConfig || null,
          sortable: property === "sortable" ? updatedValue : child.chipProperties.sortable,
          exclude: property === "exclude" ? updatedValue : child.chipProperties.exclude,
        },
      });

      const updateTree = (nodes: TreeNode[]): TreeNode[] =>
        nodes.map((node) => {
          if (node.id === id) {
            return {
              ...node,
              chipProperties: {
                ...node.chipProperties,
                [property]: updatedValue,
              },
            };
          }
          if (node.children) {
            return { ...node, children: updateTree(node.children) };
          }
          return node;
        });

      setData((prev) => updateTree(prev));
    } catch (error) {
      console.error("Error updating document type field:", error);
    }
  };

  const currentNode = findNodeByPath(currentPath, data) as TreeNode | undefined;
  const children = Array.isArray(currentNode) ? currentNode : currentNode?.children || [];

  const handleBreadcrumbClick = (index: number) => {
    setCurrentPath((prev) => prev.slice(0, index + 1));
    setParentId(Number(currentPath[index] || 0));
  };

  const handleChildClick = (childId: string) => {
    setCurrentPath((prev) => [...prev, childId]);
    setParentId(Number(childId));
    topRef?.current?.scrollIntoView({ behavior: "smooth" });
  };

  return (
    <Container sx={{ position: "relative" }}>
      <>
        {idModal && (
          <ModalConfirm
            title="Create sub doc types"
            actionConfirm={() => {
              formRef.current && formRef.current.requestSubmit();
            }}
            body=""
            close={() => {
              setIdModal(null);
            }}
            labelConfirm={idModal.action === "edit" ? "Modify entity" : "Create entity"}
          >
            <SaveSubDocType
              documentTypeId={idModal.action === "edit" ? documentTypeId : idModal.id}
              subDocTypesId={idModal.action === "edit" ? idModal.id : "new"}
              formRef={formRef}
              parentId={idModal.parentId}
              isChild={idModal.isChild}
              callback={() => {
                documentTypesQuery.refetch({ docTypeId: documentTypeId, parentId, searchText: "" });
              }}
            />
          </ModalConfirm>
        )}
        {!processing && (
          <>
            <div
              style={{
                display: "flex",
                justifyContent: "flex-end",
                marginBottom: "16px",
              }}
            >
              <TextField
                id="basicInputTypeText"
                ref={topRef}
                placeholder="Search"
                type="text"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                variant="outlined"
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      <SearchIcon sx={{ color: "lightgray" }} />
                    </InputAdornment>
                  ),
                }}
              />
            </div>
            <Box sx={{ display: "flex", alignItems: "center", justifyContent: "space-between", marginBottom: "20px" }}>
              <Breadcrumbs aria-label="breadcrumb" separator="›" sx={{ overflow: "auto", width: "100%" }}>
                <LinkRRD
                  to="/document-types"
                  color="inherit"
                  style={{
                    cursor: "pointer",
                    textDecoration: "none",
                    color: "inherit",
                    textDecorationLine: "underline",
                    fontWeight: "500",
                  }}
                >
                  Document Types
                </LinkRRD>
                <Link
                  color="inherit"
                  onClick={() => {
                    setCurrentPath([]);
                    setParentId(0);
                  }}
                  sx={{ cursor: "pointer" }}
                >
                  Root
                </Link>
                {currentPath.map((id, index) => {
                  const node = findNodeByPath(currentPath.slice(0, index + 1), data) as TreeNode;
                  if (!node) return null;
                  return (
                    <Link
                      key={id}
                      color="inherit"
                      onClick={() => handleBreadcrumbClick(index)}
                      sx={{ cursor: "pointer" }}
                    >
                      {node?.name}
                    </Link>
                  );
                })}
              </Breadcrumbs>
              <Button
                variant="outlined"
                sx={{ display: "flex", flex: "none" }}
                onClick={() =>
                  setIdModal({ id: documentTypeId, action: "add", isChild: parentId > 0, parentId: "" + parentId })
                }
              >
                create new field +
              </Button>
            </Box>
            <Box sx={{ position: "relative", minHeight: "800px" }}>
              {loading && (
                <Box
                  sx={{
                    boxShadow: "0px 0px 1px 1px #0000001a",
                    background: "inherit",
                    display: "flex",
                    justifyContent: "center",
                    alignItems: "center",
                    position: "absolute",
                    top: "0",
                    left: "0",
                    width: "100%",
                    height: "100%",
                    zIndex: "10",
                    backgroundColor: theme.palette.mode === "dark" ? "#2c2a29cc" : "#ffffffcc",
                  }}
                >
                  <Box
                    sx={{
                      animation: `${pulseAnimation} 1s infinite alternate`,
                    }}
                  >
                    <Logo size={150} color={theme.palette.mode === "dark" ? "white" : "black"} />{" "}
                  </Box>
                </Box>
              )}
              {children.length === 0 && !loading && (
                <Box
                  sx={{
                    display: "flex",
                    flexDirection: "column",
                    alignItems: "center",
                    justifyContent: "center",
                    height: "100%",
                    textAlign: "center",
                    minHeight: "550px",
                  }}
                >
                  <Logo size={150} color="gray" />
                  <Typography variant="h6" color="textSecondary" sx={{ marginTop: "16px" }}>
                    No results found
                  </Typography>
                </Box>
              )}
              {children.map((child: any) => (
                <Box
                  key={child.id}
                  display="flex"
                  justifyContent="space-between"
                  alignItems="center"
                  padding="10px"
                  border="1px solid #ccc"
                  borderRadius="5px"
                  marginBottom="10px"
                >
                  <Box sx={{ width: "200px" }}>
                    <Typography variant="body1">{child.name || "Unnamed"}</Typography>
                  </Box>
                  <Box display="flex" gap="10px">
                    <Chip
                      label="Sortable"
                      color={child.chipProperties.sortable ? "success" : "default"}
                      onClick={(event) => {
                        event.stopPropagation();
                        toggleChip(child, child.id, "sortable");
                      }}
                      sx={{
                        border: child.chipProperties.sortable ? "1px solid #4caf50" : "1px solid #ccc",
                      }}
                    />
                    <Chip
                      label="Exclude"
                      color={child.chipProperties.exclude ? "warning" : "default"}
                      onClick={(event) => {
                        event.stopPropagation();
                        toggleChip(child, child.id, "exclude");
                      }}
                      sx={{
                        border: child.chipProperties.exclude ? "1px solid #ff9800" : "1px solid #ccc",
                      }}
                    />
                    <Chip
                      label="Searchable"
                      color={child.chipProperties.searchable ? "info" : "default"}
                      onClick={(event) => {
                        event.stopPropagation();
                        toggleChip(child, child.id, "searchable");
                      }}
                      sx={{
                        border: child.chipProperties.searchable ? "1px solid #2196f3" : "1px solid #ccc",
                      }}
                    />
                  </Box>
                  <Box display="flex" gap="10px">
                    <Button
                      size="small"
                      variant="outlined"
                      startIcon={<AddIcon />}
                      onClick={(event) => {
                        event.stopPropagation();
                        setIdModal({ id: child.id, action: "add", isChild: true, parentId: child.id });
                      }}
                    >
                      Add
                    </Button>
                    <Button
                      size="small"
                      variant="outlined"
                      startIcon={<EditIcon />}
                      onClick={(event) => {
                        event.stopPropagation();
                        setIdModal({ id: child.id, action: "edit", isChild: false, parentId: child.id });
                      }}
                    >
                      Edit
                    </Button>
                    <Button
                      size="small"
                      variant="outlined"
                      startIcon={<SubdirectoryArrowLeftIcon />}
                      onClick={(event) => {
                        event.stopPropagation();
                        handleChildClick(child.id);
                      }}
                    >
                      Sub Doc Types
                    </Button>
                  </Box>
                </Box>
              ))}
            </Box>
          </>
        )}
      </>
    </Container>
  );
}

const pulseAnimation = keyframes`
  0% {
    opacity: 1;
  }
  100% {
    opacity: 0.5;
  }
`;
