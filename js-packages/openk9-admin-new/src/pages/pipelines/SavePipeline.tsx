import { combineErrorMessages, ModalConfirm, TitleEntity, useToast } from "@components/Form";
import { useSideNavigation } from "@components/sideNavigationContext";
import { ArrowDropDown, ArrowDropUp, Close } from "@mui/icons-material";
import {
  Box,
  Button,
  Container,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";
import React, { useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { useConfirmModal } from "../../utils/useConfirmModal";
import {
  useAssociatedEnrichPipelineEnrichItemsQuery,
  useEnrichItemsQuery,
  useEnrichPipelineQuery,
  useEnrichPipelineWithItemsMutation,
} from "../../graphql-generated";
import {
  AssociatedEnrichPipelineEnrichItemsQuery,
  EnrichPipelineQuery,
  EnrichPipelinesQuery,
  EnrichPipelineWithItemsQuery,
} from "./gql";

export function SavePipeline() {
  const { pipelineId = "new", mode } = useParams();
  type KeyValue = {
    [key: string]: any;
  };

  const pipelineQuery = useEnrichPipelineQuery({
    variables: { id: pipelineId as string },
    skip: !pipelineId || pipelineId === "new",
  });

  const associatedEnrichItemsQuery = useAssociatedEnrichPipelineEnrichItemsQuery({
    variables: { enrichPipelineId: pipelineId! },
    skip: !pipelineId || pipelineId === "new",
  });

  const { changaSideNavigation } = useSideNavigation();

  const pipelineValues = {};
  const navigate = useNavigate();
  const [pipelineData, setPipelineData] = React.useState<KeyValue>(pipelineValues);
  const toast = useToast();
  const [open, setOpen] = useState(false);
  const [modalDataLost, setModalDataLost] = useState(false);
  const enrichItems = useEnrichItemsQuery();
  const enrichItemsClean = (enrichItems.data?.enrichItems?.edges || []).map((element) => ({
    id: element?.node?.id,
    name: element?.node?.name,
    description: element?.node?.description,
  }));

  const getMaxEnrichItemOrder = () => {
    if (pipelineData.associatedEnrichItems.length === 0) {
      return 0;
    }
    return Math.max(0, ...pipelineData.associatedEnrichItems.map((item: any) => item?.weight ?? 0));
  };

  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Enrich Item",
    body: "Are you sure you want to edit this Enrich Item?",
    labelConfirm: "Edit",
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/enrich-item/${pipelineId}`);
    }
  };

  const [createOrUpdatePipelineMutate] = useEnrichPipelineWithItemsMutation({
    refetchQueries: [
      EnrichPipelinesQuery,
      EnrichPipelineQuery,
      EnrichPipelineWithItemsQuery,
      AssociatedEnrichPipelineEnrichItemsQuery,
    ],
    onCompleted(data) {
      if (data.enrichPipelineWithEnrichItems?.entity) {
        const isNew = pipelineId === "new" ? "created" : "updated";
        toast({
          title: `Enrich Pipeline ${isNew}`,
          content: `Enrich Pipeline has been ${isNew} successfully`,
          displayType: "success",
        });
        navigate(`/pipelines/`, { replace: true });
      } else {
        toast({
          title: `Error`,
          content: combineErrorMessages(data.enrichPipelineWithEnrichItems?.fieldValidators),
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.log(error);
      const isNew = pipelineId === "new" ? "create" : "update";
      toast({
        title: `Error ${isNew}`,
        content: `Impossible to ${isNew} Enrich Pipeline`,
        displayType: "error",
      });
    },
  });

  const handleMoveUp = (index: any) => {
    const enrichItems = [...pipelineData.associatedEnrichItems];
    if (index > 0 && enrichItems[index]?.weight !== undefined) {
      const orderToOverwrite = enrichItems[index - 1].weight;
      enrichItems[index - 1].weight = enrichItems[index].weight;
      enrichItems[index].weight = orderToOverwrite;
      enrichItems.sort((a, b) => a.weight - b.weight);
      setPipelineData((prevData) => ({
        ...prevData,
        associatedEnrichItems: enrichItems,
      }));
    }
  };

  const handleMoveDown = (index: any) => {
    const enrichItems = [...pipelineData.associatedEnrichItems];
    if (index < enrichItems.length - 1 && enrichItems[index]?.weight !== undefined) {
      const orderToOverwrite = enrichItems[index + 1].weight;
      enrichItems[index + 1].weight = enrichItems[index].weight;
      enrichItems[index].weight = orderToOverwrite;
      enrichItems.sort((a, b) => a.weight - b.weight);
      setPipelineData((prevData) => ({
        ...prevData,
        associatedEnrichItems: enrichItems,
      }));
    }
  };

  const [verifyData, setVerifyData] = React.useState(mode);

  React.useEffect(() => {
    let pipelineValues: KeyValue = {
      pipelineId: pipelineId,
      name: "",
      description: "",
      associatedEnrichItemsInitialValues: [],
      associatedEnrichItemsOrder: [],
      associatedEnrichItems: [],
    };
    if (pipelineId === "new") {
      pipelineValues = {
        pipelineId: pipelineId,
        name: "",
        description: "",
        associatedEnrichItemsInitialValues: [],
        associatedEnrichItemsOrder: [],
        associatedEnrichItems: [],
      };
    } else {
      const associatedEnrichItemsClean = [];
      const associatedEnrichItemsOrder = [];
      const associatedEnrichItems = associatedEnrichItemsQuery.data?.enrichPipeline?.enrichItems?.edges;
      const associatedEnrichItemsLength = associatedEnrichItems?.length;
      for (let index = 0; index < associatedEnrichItemsLength!; index++) {
        const element = associatedEnrichItems![index];
        associatedEnrichItemsClean.push({
          id: element?.node?.id,
          name: element?.node?.name,
          description: element?.node?.description,
          weight: index + 1,
        });
        associatedEnrichItemsOrder.push(Number(element?.node?.id));
      }

      pipelineValues = {
        pipelineId: pipelineId,
        name: pipelineQuery.data?.enrichPipeline?.name,
        description: pipelineQuery.data?.enrichPipeline?.description,
        associatedEnrichItemsInitialValues: associatedEnrichItemsClean,
        associatedEnrichItemsOrder,
        associatedEnrichItems: associatedEnrichItemsClean,
      };
    }
    setPipelineData(pipelineValues);
  }, [
    associatedEnrichItemsQuery.data?.enrichPipeline?.enrichItems?.edges,
    pipelineId,
    pipelineQuery.data?.enrichPipeline?.description,
    pipelineQuery.data?.enrichPipeline?.name,
  ]);

  const isLoading = pipelineQuery.loading && associatedEnrichItemsQuery.loading;

  if (isLoading || pipelineData.associatedEnrichItems === undefined) {
    return null;
  }

  return (
    // <CreatePipeline
    //   pipelineId={pipelineId}
    //   disabled={verifyData === "view"}
    //   pipelineData={pipelineData}
    //   setPipelineData={setPipelineData}
    //   verifyData={verifyData || "edit"}
    //   setVerifyData={setVerifyData}
    //   createOrUpdatePipelineMutate={createOrUpdatePipelineMutate}
    // />
    <>
      {open && !modalDataLost && (
        <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="lg">
          <DialogTitle>
            {modalDataLost ? "Attention" : "Enrich Items"}
            <IconButton
              aria-label="close"
              onClick={() => setOpen(false)}
              sx={{ position: "absolute", right: 8, top: 8 }}
            >
              <Close />
            </IconButton>
          </DialogTitle>
          <DialogContent>
            {!modalDataLost && (
              <TableContainer component={Paper}>
                <Table>
                  <TableHead>
                    <TableRow>
                      <TableCell>Name</TableCell>
                      <TableCell>Description</TableCell>
                      <TableCell>Actions</TableCell>
                    </TableRow>
                  </TableHead>
                  <TableBody>
                    {enrichItemsClean.map((item) => {
                      if (
                        !pipelineData.associatedEnrichItems.some(
                          (enrichItem: { id: string }) => enrichItem.id === item.id,
                        )
                      ) {
                        return (
                          <TableRow key={item.id}>
                            <TableCell>{item?.name}</TableCell>
                            <TableCell>{item?.description}</TableCell>
                            <TableCell>
                              <Button
                                onClick={() => {
                                  const weight = getMaxEnrichItemOrder() + 1;
                                  const newEnrichItem = { ...item, weight };
                                  setPipelineData((prevData) => ({
                                    ...prevData,
                                    associatedEnrichItems: [...prevData.associatedEnrichItems, newEnrichItem],
                                  }));
                                }}
                                color="primary"
                                variant="outlined"
                              >
                                Link
                              </Button>
                            </TableCell>
                          </TableRow>
                        );
                      }
                      return null;
                    })}
                  </TableBody>
                </Table>
              </TableContainer>
            )}
          </DialogContent>
          <DialogActions>
            <Button onClick={() => setOpen(false)} color="secondary">
              Close
            </Button>
            {modalDataLost && (
              <Link to={"/enrich-item/new"}>
                <Button
                  color="error"
                  onClick={() => {
                    setOpen(false);
                    changaSideNavigation("enrich-items");
                  }}
                >
                  Confirm
                </Button>
              </Link>
            )}
          </DialogActions>
        </Dialog>
      )}
      {modalDataLost && (
        <ModalConfirm
          title="Confirm to leave from this page?"
          body="Are you sure you want to leave this page? This action is irreversible and all associated data will be lost."
          type="info"
          labelConfirm="Confirm"
          actionConfirm={() => {
            setModalDataLost(false);
            setOpen(false);
            navigate("/enrich-item/new");
            changaSideNavigation("enrich-items");
          }}
          close={() => {
            setModalDataLost(false);
            setOpen(false);
          }}
        />
      )}

      <Container>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity="Pipeline"
            description="Create or Edit an Enrich Pipeline to construct a series of enrichment steps to enrich and trasform data. Add
          Enrich Items to it or go to create it if not present."
            id={pipelineId}
          />
          {verifyData === "view" && (
            <Button variant="contained" onClick={handleEditClick} sx={{ height: "fit-content" }}>
              Edit
            </Button>
          )}
        </Box>
        <Box sx={{ marginBottom: -1 }}>
          <Typography variant="subtitle1" component="label" htmlFor={"name-create-pipeline"}>
            {"Name:"}
          </Typography>
        </Box>
        <TextField
          id="name-create-pipeline"
          fullWidth
          disabled={verifyData === "view" || verifyData === "confirm"}
          margin="normal"
          value={pipelineData.name}
          onChange={(e) =>
            setPipelineData((prevData) => ({
              ...prevData,
              name: e.target.value,
            }))
          }
        />
        <Box sx={{ marginBottom: -1, marginTop: 1 }}>
          <Typography variant="subtitle1" component="label" htmlFor={"description-create-pipeline"}>
            {"Description:"}
          </Typography>
        </Box>
        <TextField
          fullWidth
          disabled={verifyData === "view" || verifyData === "confirm"}
          margin="normal"
          value={pipelineData.description}
          onChange={(e) =>
            setPipelineData((prevData) => ({
              ...prevData,
              description: e.target.value,
            }))
          }
        />

        <Typography variant="h6" gutterBottom>
          Associated Enrich items
        </Typography>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Order</TableCell>
                <TableCell>Name</TableCell>
                <TableCell>Description</TableCell>
                <TableCell>Actions</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {pipelineData.associatedEnrichItems.map((item: any, index: any) => (
                <TableRow key={item.id}>
                  <TableCell>
                    {item.weight !== 1 && (
                      <IconButton
                        disabled={verifyData === "view" || verifyData === "confirm"}
                        onClick={() => handleMoveUp(index)}
                      >
                        <ArrowDropUp />
                      </IconButton>
                    )}
                    {item.weight !== pipelineData.associatedEnrichItems.length && (
                      <IconButton
                        disabled={verifyData === "view" || verifyData === "confirm"}
                        onClick={() => handleMoveDown(index)}
                      >
                        <ArrowDropDown />
                      </IconButton>
                    )}
                  </TableCell>
                  <TableCell>{item.name}</TableCell>
                  <TableCell>{item.description}</TableCell>
                  <TableCell>
                    {!(verifyData === "view" || verifyData === "confirm") && (
                      <Button
                        onClick={() => {
                          const associatedEnrichItems = [...pipelineData.associatedEnrichItems];

                          const updatedAssociatedEnrichItems = associatedEnrichItems.filter(
                            (element) => element.id !== item.id,
                          );

                          setPipelineData((prevData) => ({
                            ...prevData,
                            associatedEnrichItems: updatedAssociatedEnrichItems,
                          }));
                        }}
                      >
                        Unlink
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>

        {!(verifyData === "view" || verifyData === "confirm") && (
          <div
            style={{
              display: "flex",
              flexWrap: "wrap",
              minHeight: "50px",
              alignItems: "center",
            }}
          >
            <Button
              color="primary"
              variant="outlined"
              onClick={() => {
                setOpen(true);
                setModalDataLost(false);
              }}
            >
              Add Enrich Item
            </Button>
            <Button
              color="primary"
              style={{ marginLeft: "auto" }}
              onClick={() => {
                setOpen(true);
                setModalDataLost(true);
              }}
            >
              Create Enrich Item
            </Button>
          </div>
        )}
        <hr />

        <Box
          sx={{
            display: "flex",
            marginTop: "20px",
            justifyContent: "space-between",
            width: "100%",
            paddingBlock: "20px",
          }}
        >
          <Button
            variant="contained"
            color="secondary"
            aria-label="Back"
            onClick={() => {
              navigate("/pipelines");
            }}
          >
            Back
          </Button>
          {!(verifyData === "view" || verifyData === "confirm") && verifyData !== "confirm" && (
            <Button
              variant="contained"
              color="primary"
              onClick={() => setVerifyData("confirm")}
              disabled={!pipelineData.name}
            >
              SAVE AND CONTINUE
            </Button>
          )}
          {verifyData === "confirm" && (
            <Button
              variant="contained"
              color="primary"
              onClick={() => {
                createOrUpdatePipelineMutate({
                  variables: {
                    id: pipelineData.pipelineId !== "new" ? pipelineData.pipelineId : null,
                    name: pipelineData.name,
                    description: pipelineData.description,
                    items: pipelineData.associatedEnrichItems.map((val: { id: string; weight: number }) => ({
                      enrichItemId: val.id,
                      weight: val.weight,
                    })),
                  },
                });
              }}
              disabled={!pipelineData.name}
            >
              {pipelineData.pipelineId === "new" ? "Create Pipeline" : "Update Pipeline"}
            </Button>
          )}
        </Box>
      </Container>
      <ConfirmModal />
    </>
  );
}
