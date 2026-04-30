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
import {
  CodeInput,
  ContainerFluid,
  CreateDataEntity,
  CustomSelect,
  NumberInput,
  TextArea,
  TextInput,
  TitleEntity,
  combineErrorMessages,
  fromFieldValidators,
  useForm,
  useToast,
} from "@components/Form";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  BehaviorMergeType,
  BehaviorOnError,
  EnrichItemType,
  useCreateOrUpdateEnrichItemMutation,
  useEnrichItemQuery,
} from "../../graphql-generated";
import { Box, Button, CircularProgress, Typography } from "@mui/material";
import { useConfirmModal } from "../../utils/useConfirmModal";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";
import { useRestClient } from "@components/queryClient";
import FiberManualRecordIcon from "@mui/icons-material/FiberManualRecord";
import {
  ChangeValueKey,
  GenerateDynamicForm,
  Template,
} from "../datasources/components/Sections/DataSource/DynamicForm";
import useDynamicForm from "../datasources/components/Sections/DataSource/DynamicForm";

export function SaveEnrichItem({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { enrichItemId = "new", name, view } = useParams();
  const [testResult, setTestResult] = React.useState<"success" | "error" | null>(null);
  const STATUS_CONFIG = {
    null: { color: "text.secondary", label: "Waiting for test" },
    success: { color: "success.main", label: "Connection successful" },
    error: { color: "error.main", label: "Endpoint unreachable" },
  } as const;

  const statusKey = testResult === null ? "null" : testResult;
  const { color, label } = STATUS_CONFIG[statusKey];
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Enrich Item",
    body: "Are you sure you want to edit this Enrich Item?",
    labelConfirm: "Edit",
  });
  const restClient = useRestClient();

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/enrich-item/${enrichItemId}`);
    }
  };

  const enrichItemQuery = useEnrichItemQuery({
    variables: { id: enrichItemId as string },
    skip: !enrichItemId || enrichItemId === "new",
    fetchPolicy: "network-only",
  });

  const [page, setPage] = React.useState(0);
  const [step, setStep] = React.useState<"configureBase" | "configureDynamic">("configureBase");
  const isRecap = page === 1;
  const isNew = enrichItemId === "new";

  const [dynamicTemplateState, setDynamicTemplateState] = React.useState<Template | null>(null);
  const [loadingForm, setLoadingForm] = React.useState(false);

  const toast = useToast();
  const [createOrUpdateEnrichItemMutate, createOrUpdateEnrichItemMutation] = useCreateOrUpdateEnrichItemMutation({
    refetchQueries: ["EnrichItem", "EnrichItems"],
    onCompleted(data) {
      if (data.enrichItem?.entity) {
        const isNew = enrichItemId === "new" ? "created" : "updated";
        toast({
          title: `Enrich Item ${isNew}`,
          content: `Enrich Item has been ${isNew} successfully`,
          displayType: "success",
        });
        navigate(`/enrich-items/`, { replace: true });
      } else {
        toast({
          title: `Error`,
          content: combineErrorMessages(data.enrichItem?.fieldValidators),
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.log(error);
      const isNew = enrichItemId === "new" ? "create" : "update";
      toast({
        title: `Error ${isNew}`,
        content: `Impossible to ${isNew} Enrich Item`,
        displayType: "error",
      });
    },
  });

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: name ?? "",
        description: "",
        type: EnrichItemType.HttpAsync,
        baseUri: "",
        path: "",
        script: "",
        behaviorMergeType: BehaviorMergeType.Merge,
        jsonPath: "",
        requestTimeout: 1000,
        behaviorOnError: BehaviorOnError.Skip,
      }),
      [name],
    ),
    originalValues: {
      name: enrichItemQuery.data?.enrichItem?.name || "",
      description: enrichItemQuery.data?.enrichItem?.description || "",
      type: enrichItemQuery.data?.enrichItem?.type,
      baseUri: enrichItemQuery.data?.enrichItem?.resourceUri?.baseUri ?? "",
      path: enrichItemQuery.data?.enrichItem?.resourceUri?.path ?? "",
      script: enrichItemQuery.data?.enrichItem?.script || "",
      behaviorMergeType: enrichItemQuery.data?.enrichItem?.behaviorMergeType,
      jsonPath: enrichItemQuery.data?.enrichItem?.jsonPath || "",
      requestTimeout: enrichItemQuery.data?.enrichItem?.requestTimeout || 1000,
      behaviorOnError: enrichItemQuery.data?.enrichItem?.behaviorOnError,
    },
    isLoading: enrichItemQuery.loading || createOrUpdateEnrichItemMutation.loading,
    onSubmit(data) {
      createOrUpdateEnrichItemMutate({
        variables: {
          id: enrichItemId !== "new" ? enrichItemId : undefined,
          ...data,
          jsonConfig: dynamicFormJson || undefined,
          resourceUri: {
            baseUri: data.baseUri,
            path: data.path,
          },
        },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateEnrichItemMutation.data?.enrichItem?.fieldValidators),
  });

  React.useEffect(() => {
    if (enrichItemId !== "new" && enrichItemQuery.data?.enrichItem?.resourceUri) {
      const { baseUri, path } = enrichItemQuery.data.enrichItem.resourceUri;
      if (baseUri && path) {
        restClient.formApi
          .form({ baseUri, path })
          .then((res) => {
            if (res) {
              setDynamicTemplateState(res as Template);
            }
          })
          .catch((err) => {
            console.error("Error fetching initial dynamic form", err);
          });
      }
    }
  }, [enrichItemQuery.data, enrichItemId, restClient.formApi]);

  const baseUriValue = form.inputProps("baseUri").value;
  const pathValue = form.inputProps("path").value;

  React.useEffect(() => {
    setDynamicTemplateState(null);
  }, [baseUriValue, pathValue]);

  const { dynamicTemplate, changeValueTemplate, dynamicFormJson } = useDynamicForm({
    template: dynamicTemplateState,
    jsonConfig: enrichItemQuery.data?.enrichItem?.jsonConfig || "",
  });

  const handleNextStep = async () => {
    const baseUri = form.inputProps("baseUri").value;
    const path = form.inputProps("path").value;
    const name = form.inputProps("name").value;

    if (!name) {
      toast({
        title: "Missing data",
        content: "Please provide a Name for the Enrich Item",
        displayType: "error",
      });
      return;
    }

    if (!baseUri || !path) {
      toast({
        title: "Missing data",
        content: "Please provide both Base URI and Path",
        displayType: "error",
      });
      return;
    }

    if (testResult !== "success") {
      toast({
        title: "Connection not verified",
        content: "Please test the connection before proceeding, or ensure the endpoint is reachable.",
        displayType: "warning",
      });
      // For now, allowing to proceed as per "imposta anche la possibilità di poterlo bloccare"
      // but keeping it as a warning unless we want to strictly block.
    }

    try {
      setLoadingForm(true);
      const res = await restClient.formApi.form({
        baseUri,
        path,
      });
      if (res) {
        setDynamicTemplateState(res as Template);
      }
      setStep("configureDynamic");
    } catch (e) {
      toast({
        title: "Error",
        content: "Impossible to fetch dynamic form. Check connection settings.",
        displayType: "warning",
      });
      setStep("configureDynamic");
    } finally {
      setLoadingForm(false);
    }
  };

  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        cell: [
          { key: "name" },
          { key: "description" },
          { key: "type" },
          { key: "baseUri", label: "Base URI" },
          { key: "path", label: "Path" },
          { key: "jsonPath", label: "Json Path" },
          { key: "requestTimeout", label: "Request Timeout" },
          { key: "behaviorMergeType", label: "Behavior Merge Type" },
          { key: "behaviorOnError", label: "Behavior On Error" },
          {
            key: "jsonConfig",
            label: "Configuration",
            jsonView: true,
          },
        ],
        label: "Recap Enrich Item",
      },
    ],
    valueOverride: {
      jsonConfig: dynamicFormJson || enrichItemQuery.data?.enrichItem?.jsonConfig || "",
    },
  });

  return (
    <>
      <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
        <TitleEntity
          nameEntity="Enrich Item"
          description="Create or Edit a Enrich Item to define enrichment steps to perform on ingested data.
          Choose between the possibility of hook up external sync/async service or configure internal Groovys script enrichment."
          id={enrichItemId}
        />
        {view === "view" && (
          <Button variant="contained" onClick={handleEditClick} sx={{ height: "fit-content" }}>
            Edit
          </Button>
        )}
      </Box>
      <form style={{ borderStyle: "unset", padding: "0 16px", marginBottom: "50px" }}>
        {step === "configureBase" ? (
          <CreateDataEntity
            form={form}
            page={page}
            id={enrichItemId}
            pathBack="/enrich-items/"
            setPage={setPage}
            isFooterButton={false}
            haveConfirmButton={view ? false : true}
            informationSuggestion={[
              {
                content: (
                  <>
                    <ContainerFluid flexColumn>
                      <TextInput label="Name" {...form.inputProps("name")} />
                      <TextArea label="Description" {...form.inputProps("description")} />
                      <TextInput
                        label="Base URI"
                        {...form.inputProps("baseUri")}
                        description={"Base URL where enrich service listens"}
                      />
                      <TextInput label="Path" {...form.inputProps("path")} description={"API endpoint path"} />
                      <Box sx={{ display: "flex", marginBlock: 2, alignItems: "center", gap: 2 }}>
                        <Button
                          onClick={async () => {
                            try {
                              const res = await restClient.healthApi.health({
                                baseUri: form.inputProps("baseUri").value,
                                path: form.inputProps("path").value,
                              });
                              setTestResult(res ? "success" : "error");
                            } catch {
                              setTestResult("error");
                            }
                          }}
                          variant="outlined"
                        >
                          Test Connection
                        </Button>
                        <Box sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                          <Typography
                            variant="body2"
                            color={color}
                            sx={{ display: "flex", alignItems: "center", gap: 1 }}
                          >
                            <FiberManualRecordIcon sx={{ color, fontSize: 18 }} />
                            {label}
                          </Typography>
                        </Box>
                      </Box>
                      <NumberInput
                        label="Request Timeout - Milliseconds"
                        {...form.inputProps("requestTimeout")}
                        description={"the value is expressed in milliseconds"}
                      />
                      <TextInput
                        label="Json Path"
                        {...form.inputProps("jsonPath")}
                        description={"Json Path for merging result. To merge entire Json response set $"}
                      />
                      <CustomSelect
                        label="Type"
                        dict={EnrichItemType}
                        {...form.inputProps("type")}
                        description={
                          "Enrich Type. Set Sync/Async for external Openk9 compatible service or Groovy Script for simple script enrich."
                        }
                      />
                      <CustomSelect
                        label="Behavior Merge Type"
                        dict={BehaviorMergeType}
                        {...form.inputProps("behaviorMergeType")}
                        description={"If merge or replace original message with enrich response"}
                      />
                      <CustomSelect
                        label="Behavior On Error"
                        dict={BehaviorOnError}
                        {...form.inputProps("behaviorOnError")}
                        description={
                          "Behavior in case of error. If Fail, retry and error handling flow is performed for message. If Skip message go to next step, ignoring the error"
                        }
                      />
                    </ContainerFluid>
                  </>
                ),
                page: 0,
                validation: view ? true : false,
              },
              {
                validation: true,
              },
            ]}
            fieldsControll={["name"]}
          />
        ) : (
          <ContainerFluid size="lg">
            <Typography variant="h6" sx={{ mb: 2 }}>
              Dynamic Configuration
            </Typography>
            {!loadingForm ? (
              dynamicTemplate ? (
                <GenerateDynamicForm
                  templates={dynamicTemplate}
                  changeValueKey={changeValueTemplate}
                  disabled={!!view}
                />
              ) : (
                (() => {
                  const isGroovy = form.inputProps("type").value === EnrichItemType.GroovyScript;
                  return (
                    <CodeInput
                      language={isGroovy ? "groovy" : "json"}
                      label={isGroovy ? "Script" : "Configuration"}
                      disabled={!!view}
                      id="code-input-enricher"
                      onChange={(e) => {
                        form.inputProps("script").onChange(e);
                      }}
                      validationMessages={[]}
                      value={form.inputProps("script").value || ""}
                      description={
                        isGroovy
                          ? "Groovy script executed during enrich step"
                          : "Json configuration sended to corresponding external parser when execution start"
                      }
                    />
                  );
                })()
              )
            ) : (
              <Box sx={{ display: "flex", justifyContent: "center", alignItems: "center", minHeight: "200px" }}>
                <CircularProgress />
              </Box>
            )}
          </ContainerFluid>
        )}
        {!isRecap && (
          <Box display="flex" justifyContent="space-between" mt={4} mb={2}>
            {step === "configureBase" ? (
              <>
                <Button variant="outlined" onClick={() => navigate("/enrich-items/")}>
                  Back
                </Button>
                <Button
                  variant="contained"
                  onClick={handleNextStep}
                  disabled={loadingForm || !form.inputProps("name").value}
                >
                  Next Step
                </Button>
              </>
            ) : (
              <>
                <Button variant="outlined" onClick={() => setStep("configureBase")}>
                  Back
                </Button>
                <Button
                  variant="contained"
                  onClick={() => {
                    setPage(1);
                  }}
                >
                  Save and continue
                </Button>
              </>
            )}
          </Box>
        )}
      </form>
      <ConfirmModal />
      <Recap
        recapData={recapSections}
        setExtraFab={setExtraFab}
        forceFullScreen={isRecap}
        actions={{
          onBack: () => setPage(0),
          onSubmit: () => form.submit(),
          submitLabel: isNew ? "Create entity" : "Update entity",
          backLabel: "Back",
        }}
      />
    </>
  );
}
