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
import { useTranslation } from "react-i18next";
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
import { extractProblemDetails, mapHealthStatus } from "../../utils/health";
import FiberManualRecordIcon from "@mui/icons-material/FiberManualRecord";
import {
  ChangeValueKey,
  GenerateDynamicForm,
  Template,
} from "../datasources/components/Sections/DataSource/DynamicForm";
import useDynamicForm from "../datasources/components/Sections/DataSource/DynamicForm";

export function SaveEnrichItem({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { t } = useTranslation();
  const { enrichItemId = "new", name, view } = useParams();
  const [testResult, setTestResult] = React.useState<"success" | "down" | "unknown" | "error" | null>(null);
  const [testError, setTestError] = React.useState<{ title?: string; detail?: string } | null>(null);
  const STATUS_CONFIG = {
    null: { color: "text.secondary", label: t("pages.enrich-items.waiting-for-test") },
    success: { color: "success.main", label: t("pages.enrich-items.connection-successful") },
    down: { color: "error.main", label: t("pages.enrich-items.service-unavailable") },
    unknown: { color: "warning.main", label: t("pages.enrich-items.service-status-unknown") },
    error: { color: "error.main", label: t("pages.enrich-items.endpoint-unreachable") },
  } as const;

  const statusKey = testResult === null ? "null" : testResult;
  const { color, label } = STATUS_CONFIG[statusKey];
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: t("pages.enrich-items.edit-enrich-item"),
    body: t("pages.enrich-items.are-you-sure-you-want-to-edit"),
    labelConfirm: t("common.edit"),
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
          title: isNew === "created" ? t("pages.enrich-items.created-title") : t("pages.enrich-items.updated-title"),
          content: isNew === "created" ? t("pages.enrich-items.created-content") : t("pages.enrich-items.updated-content"),
          displayType: "success",
        });
        navigate(`/enrich-items/`, { replace: true });
      } else {
        toast({
          title: t("common.error"),
          content: combineErrorMessages(data.enrichItem?.fieldValidators),
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.log(error);
      const isNew = enrichItemId === "new" ? "create" : "update";
      toast({
        title: isNew === "create" ? t("pages.enrich-items.create-error-title") : t("pages.enrich-items.update-error-title"),
        content: isNew === "create" ? t("pages.enrich-items.create-error-content") : t("pages.enrich-items.update-error-content"),
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
        jsonConfig: "",
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
      jsonConfig: enrichItemQuery.data?.enrichItem?.jsonConfig || "",
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
          jsonConfig: dynamicFormJson || data.jsonConfig || undefined,
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
        title: t("pages.enrich-items.missing-data"),
        content: t("pages.enrich-items.please-provide-a-name-for-the-enrich"),
        displayType: "error",
      });
      return;
    }

    if (!baseUri || !path) {
      toast({
        title: t("pages.enrich-items.missing-data"),
        content: t("pages.enrich-items.please-provide-both-base-uri-and-path"),
        displayType: "error",
      });
      return;
    }

    const connectionNotVerified = testResult !== "success";
    if (connectionNotVerified) {
      toast({
        title: t("pages.enrich-items.connection-not-verified"),
        content: t("pages.enrich-items.please-test-the-connection-before-proceeding-or"),
        displayType: "warning",
      });
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
      if (!connectionNotVerified) {
        toast({
          title: t("common.error"),
          content: t("pages.enrich-items.impossible-to-fetch-dynamic-form-check-connection"),
          displayType: "warning",
        });
      }
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
          { key: "baseUri", label: t("fields.base-uri") },
          { key: "path", label: t("fields.path") },
          { key: "jsonPath", label: t("fields.json-path") },
          { key: "requestTimeout", label: t("pages.enrich-items.request-timeout") },
          { key: "behaviorMergeType", label: t("fields.behavior-merge-type") },
          { key: "behaviorOnError", label: t("fields.behavior-on-error") },
          ...(form.inputProps("type").value === EnrichItemType.GroovyScript
            ? [
                {
                  key: "script",
                  label: t("fields.script"),
                  jsonView: true,
                },
              ]
            : []),
          {
            key: "jsonConfig",
            label: t("fields.configuration"),
            jsonView: true,
          },
        ],
        label: t("pages.enrich-items.recap-label"),
      },
    ],
    valueOverride: {
      jsonConfig: dynamicFormJson || form.inputProps("jsonConfig").value || "",
    },
  });

  return (
    <>
      <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
        <TitleEntity
          nameEntity={t("pages.enrich-items.entity-name")}
          description={t("pages.enrich-items.create-or-edit-a-enrich-item-to")}
          id={enrichItemId}
        />
        {view === "view" && (
          <Button variant="contained" onClick={handleEditClick} sx={{ height: "fit-content" }}>
            {t("common.edit")}
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
                      <TextInput label={t("common.name")} {...form.inputProps("name")} />
                      <TextArea label={t("common.description")} {...form.inputProps("description")} />
                      <TextInput
                        label={t("fields.base-uri")}
                        {...form.inputProps("baseUri")}
                        description={t("pages.enrich-items.base-uri-description")}
                      />
                      <TextInput label={t("fields.path")} {...form.inputProps("path")} description={t("pages.enrich-items.path-description")} />
                      <Box sx={{ display: "flex", marginBlock: 2, alignItems: "center", gap: 2 }}>
                        <Button
                          onClick={async () => {
                            try {
                              const res = await restClient.healthApi.health({
                                baseUri: form.inputProps("baseUri").value,
                                path: form.inputProps("path").value,
                              });
                              setTestError(null);
                              setTestResult(mapHealthStatus((res as { status?: string } | null)?.status));
                            } catch (err) {
                              setTestResult("error");
                              setTestError(extractProblemDetails(err, t));
                            }
                          }}
                          variant="outlined"
                        >
                          {t("pages.enrich-items.test-connection")}
                        </Button>
                        <Box sx={{ display: "flex", flexDirection: "column" }}>
                          <Typography
                            variant="body2"
                            color={color}
                            sx={{ display: "flex", alignItems: "center", gap: 1 }}
                          >
                            <FiberManualRecordIcon sx={{ color, fontSize: 18 }} />
                            {testResult === "error" ? testError?.title ?? label : label}
                          </Typography>
                          {testResult === "error" && testError?.detail && (
                            <Typography variant="caption" color={color} sx={{ ml: 3 }}>
                              {testError.detail}
                            </Typography>
                          )}
                        </Box>
                      </Box>
                      <NumberInput
                        label={t("fields.request-timeout-milliseconds")}
                        {...form.inputProps("requestTimeout")}
                        description={t("pages.enrich-items.request-timeout-description")}
                      />
                      <TextInput
                        label={t("fields.json-path")}
                        {...form.inputProps("jsonPath")}
                        description={t("pages.enrich-items.json-path-description")}
                      />
                      <CustomSelect
                        label={t("fields.type")}
                        dict={EnrichItemType}
                        {...form.inputProps("type")}
                        description={t("pages.enrich-items.type-description")}
                      />
                      <CustomSelect
                        label={t("fields.behavior-merge-type")}
                        dict={BehaviorMergeType}
                        {...form.inputProps("behaviorMergeType")}
                        description={t("pages.enrich-items.behavior-merge-type-description")}
                      />
                      <CustomSelect
                        label={t("fields.behavior-on-error")}
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
              {t("pages.enrich-items.dynamic-configuration")}
            </Typography>
            {!loadingForm ? (
              dynamicTemplate ? (
                <GenerateDynamicForm
                  templates={dynamicTemplate}
                  changeValueKey={changeValueTemplate}
                  disabled={!!view}
                />
              ) : form.inputProps("type").value === EnrichItemType.GroovyScript ? (
                <Box sx={{ display: "flex", flexDirection: "column", gap: 2 }}>
                  <CodeInput
                    language="groovy"
                    label={t("fields.script")}
                    disabled={!!view}
                    id="code-input-enricher-script"
                    onChange={(e) => {
                      form.inputProps("script").onChange(e);
                    }}
                    validationMessages={[]}
                    value={form.inputProps("script").value || ""}
                    description={t("pages.enrich-items.groovy-script-executed-during-enrich-step")}
                  />
                  <CodeInput
                    language="json"
                    label={t("fields.json-config")}
                    disabled={!!view}
                    id="code-input-enricher-json-config"
                    onChange={(e) => {
                      form.inputProps("jsonConfig").onChange(e);
                    }}
                    validationMessages={[]}
                    value={form.inputProps("jsonConfig").value || ""}
                    description={t("pages.enrich-items.json-configuration-sended-to-corresponding-external-parser")}
                  />
                </Box>
              ) : (
                <CodeInput
                  language="json"
                  label={t("fields.configuration")}
                  disabled={!!view}
                  id="code-input-enricher"
                  onChange={(e) => {
                    form.inputProps("jsonConfig").onChange(e);
                  }}
                  validationMessages={[]}
                  value={form.inputProps("jsonConfig").value || ""}
                  description={t("pages.enrich-items.json-configuration-sended-to-corresponding-external-parser")}
                />
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
                  {t("common.back")}
                </Button>
                <Button
                  variant="contained"
                  onClick={handleNextStep}
                  disabled={loadingForm || !form.inputProps("name").value}
                >
                  {t("common.next-step")}
                </Button>
              </>
            ) : (
              <>
                <Button variant="outlined" onClick={() => setStep("configureBase")}>
                  {t("common.back")}
                </Button>
                <Button
                  variant="contained"
                  onClick={() => {
                    setPage(1);
                  }}
                >
                  {t("common.save-and-continue")}
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
          submitLabel: isNew ? t("entity.create") : t("entity.update"),
          backLabel: t("common.back"),
        }}
      />
    </>
  );
}
