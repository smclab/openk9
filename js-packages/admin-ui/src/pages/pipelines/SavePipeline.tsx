/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import { combineErrorMessages, ModalConfirm, TitleEntity, useForm, useToast } from "@components/Form";
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
import { useTranslation } from "react-i18next";
import { Link, useNavigate, useParams } from "react-router-dom";
import {
  useAssociatedEnrichPipelineEnrichItemsQuery,
  useEnrichItemsQuery,
  useEnrichPipelineQuery,
  useEnrichPipelineWithItemsMutation,
} from "../../graphql-generated";
import { useConfirmModal } from "../../utils/useConfirmModal";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";

export function SavePipeline({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { t } = useTranslation();
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
  const enrichItems = useEnrichItemsQuery({ fetchPolicy: "network-only" });
  const enrichItemsClean = (enrichItems.data?.enrichItems?.edges || []).map((element) => ({
    id: element?.node?.id,
    name: element?.node?.name,
    description: element?.node?.description,
  }));

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: pipelineData?.name || "",
        description: pipelineData?.description || "",
        associatedEnrichItems: pipelineData?.associatedEnrichItems || [],
      }),
      [pipelineData],
    ),

    originalValues: pipelineData,
    isLoading: pipelineQuery.loading || associatedEnrichItemsQuery.loading,
    onSubmit(updated: any) {
      setPipelineData((prev) => ({
        ...prev,
        ...updated,
      }));
    },
  });

  const getMaxEnrichItemOrder = () => {
    if (pipelineData.associatedEnrichItems.length === 0) {
      return 0;
    }
    return Math.max(0, ...pipelineData.associatedEnrichItems.map((item: any) => item?.weight ?? 0));
  };

  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: t("pages.pipelines.edit-enrich-item"),
    body: t("pages.pipelines.are-you-sure-you-want-to-edit"),
    labelConfirm: t("common.edit"),
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/pipeline/${pipelineId}/mode/edit`);
    }
  };

  const [createOrUpdatePipelineMutate] = useEnrichPipelineWithItemsMutation({
    refetchQueries: [
      "EnrichPipelines",
      "EnrichPipeline",
      "EnrichPipelineWithItems",
      "AssociatedEnrichPipelineEnrichItems",
    ],
    onCompleted(data) {
      if (data.enrichPipelineWithEnrichItems?.entity) {
        const isNew = pipelineId === "new" ? "created" : "updated";
        toast({
          title: isNew === "created" ? t("pages.pipelines.created-title") : t("pages.pipelines.updated-title"),
          content: isNew === "created" ? t("pages.pipelines.created-content") : t("pages.pipelines.updated-content"),
          displayType: "success",
        });
        navigate(`/pipelines/`, { replace: true });
      } else {
        toast({
          title: t("common.error"),
          content: combineErrorMessages(data.enrichPipelineWithEnrichItems?.fieldValidators),
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.log(error);
      const isNew = pipelineId === "new" ? "create" : "update";
      toast({
        title: isNew === "create" ? t("pages.pipelines.create-error-title") : t("pages.pipelines.update-error-title"),
        content: isNew === "create" ? t("pages.pipelines.create-error-content") : t("pages.pipelines.update-error-content"),
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
  const isRecap = verifyData === "confirm";

  React.useEffect(() => {
    setVerifyData(mode);
  }, [mode]);

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

  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        cell: [
          { key: "name" },
          { key: "description" },
          { key: "associatedEnrichItems", label: t("pages.pipelines.associated-enrich-items") },
        ],
        label: t("pages.pipelines.recap-label"),
      },
    ],
    valueOverride: {
      associatedEnrichItems:
        form
          .inputProps("associatedEnrichItems")
          .value?.map((e: any, index: number) => ({ [index + 1]: e.name || e.label })) || [],
    },
  });

  return (
    <>
      {open && !modalDataLost && (
        <Dialog open={open} onClose={() => setOpen(false)} fullWidth maxWidth="lg">
          <DialogTitle>
            {modalDataLost ? "Attention" : "Enrich Items"}
            <IconButton
              aria-label={t("common.close")}
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
                      <TableCell>{t("common.name")}</TableCell>
                      <TableCell>{t("common.description")}</TableCell>
                      <TableCell>{t("table.actions")}</TableCell>
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
                                {t("pages.pipelines.link")}
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
              {t("common.close")}
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
                  {t("common.confirm")}
                </Button>
              </Link>
            )}
          </DialogActions>
        </Dialog>
      )}
      {modalDataLost && (
        <ModalConfirm
          title={t("pages.pipelines.confirm-to-leave-from-this-page")}
          body={t("pages.pipelines.are-you-sure-you-want-to-leave")}
          type="info"
          labelConfirm={t("common.confirm")}
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
            nameEntity={t("pages.pipelines.entity-name")}
            description={t("pages.pipelines.create-or-edit-an-enrich-pipeline-to")}
            id={pipelineId}
          />
          {verifyData === "view" && (
            <Button variant="contained" onClick={handleEditClick} sx={{ height: "fit-content" }}>
              {t("common.edit")}
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
          {t("pages.pipelines.associated-enrich-items")}
        </Typography>
        <TableContainer component={Paper}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>{t("fields.order")}</TableCell>
                <TableCell>{t("common.name")}</TableCell>
                <TableCell>{t("common.description")}</TableCell>
                <TableCell>{t("table.actions")}</TableCell>
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
                        {t("pages.pipelines.unlink")}
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
              {t("pages.pipelines.add-enrich-item")}
            </Button>
            <Button
              color="primary"
              style={{ marginLeft: "auto" }}
              onClick={() => {
                setOpen(true);
                setModalDataLost(true);
              }}
            >
              {t("pages.pipelines.create-enrich-item")}
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
            aria-label={t("common.back")}
            onClick={() => {
              navigate("/pipelines");
            }}
          >
            {t("common.back")}
          </Button>
          {!(verifyData === "view" || verifyData === "confirm") && verifyData !== "confirm" && (
            <Button
              variant="contained"
              color="primary"
              onClick={() => setVerifyData("confirm")}
              disabled={!pipelineData.name}
            >
              {t("common.save-and-continue")}
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
      <Recap
        recapData={recapSections}
        setExtraFab={setExtraFab}
        forceFullScreen={isRecap}
        actions={{
          onBack: () => {
            setVerifyData("edit");
          },
          onSubmit: () => {
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
          },
          submitLabel: pipelineData.pipelineId === "new" ? t("entity.create") : t("entity.update"),
        }}
      />
    </>
  );
}
