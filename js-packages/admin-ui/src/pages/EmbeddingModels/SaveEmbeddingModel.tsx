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
  BooleanInput,
  combineErrorMessages,
  ContainerFluid,
  CreateDataEntity,
  TextArea,
  TextInput,
  TitleEntity,
  useForm,
  useToast,
  TooltipDescription,
  NumberInput,
  CodeInput,
  fromFieldValidators,
} from "@components/Form";
import React from "react";
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { useCreateOrUpdateEmbeddingModelMutation, useEmbeddingModelQuery, VectorDataType } from "../../graphql-generated";
import { Box, Button } from "@mui/material";
import { AutocompleteDropdownWithOptions } from "@components/Form/Select/AutocompleteDropdown";
import { useConfirmModal } from "../../utils/useConfirmModal";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";

const getProviderOptions = (t: TFunction) => [
  { value: "openai", label: t("pages.embedding-models.openai") },
  { value: "ollama", label: t("pages.embedding-models.ollama") },
  { value: "watsonx", label: t("pages.embedding-models.ibm-watsonx") },
  { value: "chat_vertex_ai", label: t("pages.embedding-models.chat-vertex-ai") },
  { value: "aws_bedrock", label: t("pages.embedding-models.aws-bedrock") },
];

const getVectorDataTypeOptions = (t: TFunction) => [
  { value: "FLOAT32", label: t("pages.embedding-models.float32-full-precision-default") },
  { value: "BYTE", label: t("pages.embedding-models.byte-int8-quantized") },
  { value: "BINARY", label: t("pages.embedding-models.binary-packed-vector-size-multiple-of-8") },
];

export function SaveEmbeddingModel({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { t } = useTranslation();
  const PROVIDER_OPTIONS = React.useMemo(() => getProviderOptions(t), [t]);
  const VECTOR_DATA_TYPE_OPTIONS = React.useMemo(() => getVectorDataTypeOptions(t), [t]);
  const { embeddingModelsId = "new", view } = useParams();
  const navigate = useNavigate();
  const [isCleaning, setIsCleaning] = React.useState(false);
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: t("pages.embedding-models.edit-embedding-model"),
    body: t("pages.embedding-models.are-you-sure-you-want-to-edit"),
    labelConfirm: t("common.edit"),
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/embedding-model/${embeddingModelsId}`);
    }
  };
  const [page, setPage] = React.useState(0);
  const isRecap = page === 1;
  const isNew = embeddingModelsId === "new";
  const [providerModel, setProviderModel] = React.useState<{
    provider: string | null | undefined;
    model: string | null | undefined;
  }>({ provider: "", model: "" });

  const embeddingModelQuery = useEmbeddingModelQuery({
    variables: { id: embeddingModelsId as string },
    skip: !embeddingModelsId || embeddingModelsId === "new",
  });
  // const showModal = useModal();
  const toast = useToast();

  const [createOrUpdateEmbeddingModelsMutate, createOrUpdateEmbeddingModelsMutation] =
    useCreateOrUpdateEmbeddingModelMutation({
      refetchQueries: ["EmbeddingModel", "EmbeddingModels"],
      onCompleted(data) {
        if (data.embeddingModel?.entity) {
          const isNew = embeddingModelsId === "new" ? "created" : "updated";
          toast({
            title: isNew === "created" ? t("pages.embedding-models.created-title") : t("pages.embedding-models.updated-title"),
            content: isNew === "created" ? t("pages.embedding-models.created-content") : t("pages.embedding-models.updated-content"),
            displayType: "success",
          });
          navigate(`/embedding-models/`, { replace: true });
        } else {
          // The save runs from the recap, which covers the form: leaving it
          // open would hide the per-field messages the backend just returned.
          setPage(0);
          toast({
            title: t("common.error"),
            content: combineErrorMessages(data.embeddingModel?.fieldValidators),
            displayType: "error",
          });
        }
      },
      onError(error) {
        console.log(error);
        const isNew = embeddingModelsId === "new" ? "create" : "update";
        toast({
          title: isNew === "create" ? t("pages.embedding-models.create-error-title") : t("pages.embedding-models.update-error-title"),
          content: isNew === "create" ? t("pages.embedding-models.create-error-content") : t("pages.embedding-models.update-error-content"),
          displayType: "error",
        });
      },
    });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        apiKey: "",
        apiUrl: "",
        description: "",
        provider: "openai",
        vectorSize: 1,
        vectorDataType: "FLOAT32",
        multimodal: false,
        model: "",
        jsonConfig: "{}",
      }),
      [],
    ),
    originalValues: embeddingModelQuery.data?.embeddingModel
      ? {
        ...embeddingModelQuery.data.embeddingModel,
        provider: embeddingModelQuery.data.embeddingModel.providerModel?.provider || "openai",
        model: embeddingModelQuery.data.embeddingModel.providerModel?.model || "",
      }
      : undefined,
    isLoading: embeddingModelQuery.loading || createOrUpdateEmbeddingModelsMutation.loading,
    getValidationMessages: fromFieldValidators(
      createOrUpdateEmbeddingModelsMutation.data?.embeddingModel?.fieldValidators,
    ),
    onSubmit(data) {
      createOrUpdateEmbeddingModelsMutate({
        variables: {
          id: embeddingModelsId !== "new" ? embeddingModelsId : undefined,
          providerModel: { provider: providerModel.provider || "", model: providerModel.model || "" },
          ...data,
          vectorDataType: data.vectorDataType as VectorDataType,
        },
      });
    },
  });

  React.useEffect(() => {
    setProviderModel({
      provider: embeddingModelQuery.data?.embeddingModel?.providerModel?.provider || "openai",
      model: embeddingModelQuery.data?.embeddingModel?.providerModel?.model || "",
    });
  }, [embeddingModelQuery]);

  const viewMaskApiKey = React.useMemo(() => {
    return !!((view || embeddingModelsId !== "new") && form.inputProps("apiKey").value);
  }, [view, embeddingModelsId, form.inputProps("apiKey").value]);

  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        cell: [
          { key: "name" },
          { key: "description" },
          { key: "vectorSize", label: t("fields.vector-size") },
          { key: "vectorDataType", label: t("fields.vector-data-type") },
          { key: "multimodal", label: t("fields.multimodal") },
          { key: "provider" },
          { key: "model" },
          { key: "apiKey", label: t("fields.api-key") },
          { key: "apiUrl", label: t("pages.embedding-models.api-url") },
          { key: "jsonConfig", label: t("fields.json-config"), jsonView: true },
        ],
        label: t("pages.embedding-models.recap-label"),
      },
    ],
  });

  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity={t("pages.embedding-models.entity-name")}
            description={t("pages.embedding-models.create-or-edit-a-embedding-model-to")}
            id={embeddingModelsId}
          />
          {view === "view" && (
            <Button variant="contained" onClick={handleEditClick} sx={{ height: "fit-content" }}>
              {t("common.edit")}
            </Button>
          )}
        </Box>
        <form style={{ borderStyle: "unset", padding: "0 16px", marginBottom: "50px" }}>
          <CreateDataEntity
            form={form}
            page={page}
            id={embeddingModelsId}
            pathBack="/embedding-models/"
            setPage={setPage}
            haveConfirmButton={view ? false : true}
            informationSuggestion={[
              {
                content: (
                  <div>
                    <TextInput
                      label={t("common.name")}
                      {...form.inputProps("name")}
                      description={t("pages.embedding-models.unique-identifier-of-the-embedding-model-configuration")}
                    />
                    <TextArea
                      label={t("common.description")}
                      {...form.inputProps("description")}
                      description={t("pages.embedding-models.free-text-description-of-the-embedding-model")}
                    />
                    <NumberInput
                      label={t("fields.vector-size")}
                      {...form.inputProps("vectorSize")}
                      isNumber={false}
                      description={t("pages.embedding-models.dimensionality-of-the-embedding-vectors-produced-by")}
                    />
                    <BooleanInput
                      label={t("fields.multimodal")}
                      {...form.inputProps("multimodal")}
                      disabled={view ? true : false}
                      description={t("pages.embedding-models.marks-the-model-as-multimodal-image-refs")}
                    />
                    <AutocompleteDropdownWithOptions
                      label={t("fields.vector-data-type")}
                      description={t("pages.embedding-models.type-of-the-vector-written-to-the")}
                      allowClear={false}
                      disabled={view ? true : false}
                      optionsDefault={VECTOR_DATA_TYPE_OPTIONS}
                      value={(() => {
                        const current = form.inputProps("vectorDataType").value || "FLOAT32";
                        const match = VECTOR_DATA_TYPE_OPTIONS.find((o) => o.value === current);
                        return { id: current, name: match?.label || current };
                      })()}
                      onChange={(val) => {
                        form.inputProps("vectorDataType").onChange(val.id);
                      }}
                    />
                    <AutocompleteDropdownWithOptions
                      label={t("fields.provider")}
                      description={t("pages.embedding-models.embedding-provider-integration-to-use-e-g")}
                      allowClear={false}
                      disabled={view ? true : false}
                      optionsDefault={PROVIDER_OPTIONS}
                      value={(() => {
                        const current = providerModel.provider || "openai";
                        const match = PROVIDER_OPTIONS.find((o) => o.value === current);
                        return { id: current, name: match?.label || current };
                      })()}
                      onChange={(val) => {
                        setProviderModel((value) => ({ ...value, provider: val.id }));
                        form.inputProps("provider").onChange(val.id);
                      }}
                    />
                    <TextInput
                      label={t("fields.model")}
                      id="modelId"
                      onChange={(s) => {
                        setProviderModel((value) => ({ ...value, model: s }));
                      }}
                      validationMessages={[]}
                      value={providerModel.model || ""}
                      disabled={view ? true : false}
                      description={t("pages.embedding-models.specific-embedding-model-identifier-of-the-selected")}
                    />
                    <TooltipDescription informationDescription={t("fields.api-key-in-case-of-external-api")}>
                      <TextInput
                        label={t("fields.api-key")}
                        {...form.inputProps("apiKey")}
                        value={
                          viewMaskApiKey && !isCleaning
                            ? maskApiKey(form.inputProps("apiKey").value)
                            : form.inputProps("apiKey").value
                        }
                        disabled={viewMaskApiKey && !isCleaning ? true : false}
                        haveReset={{
                          isVisible: !view && viewMaskApiKey,
                          callback: () => {
                            setIsCleaning(true);
                          },
                        }}
                      />
                    </TooltipDescription>
                    <TooltipDescription informationDescription={t("fields.api-url-in-case-of-service-hosted")}>
                      <TextInput label={t("fields.api-url")} {...form.inputProps("apiUrl")} />
                    </TooltipDescription>
                    <ContainerFluid size="md" style={{ marginRight: 0 }}>
                      <CodeInput
                        id="settings-code-input"
                        readonly={page === 1 || view === "view"}
                        label={t("fields.settings")}
                        value={form.inputProps("jsonConfig").value}
                        onChange={(value) => form.inputProps("jsonConfig").onChange(value)}
                        language="json"
                        validationMessages={[]}
                        disabled={false}
                        height="400px"
                        tooltip={
                          <TooltipDescription
                            informationDescription={t("pages.embedding-models.json-config-to-set-up-embedding-model")}
                          />
                        }
                      />
                    </ContainerFluid>
                  </div>
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
        </form>
      </>
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
    </ContainerFluid>
  );
}

export const maskApiKey = (apiKey: string) => {
  if (!apiKey || apiKey.length <= 4) return apiKey;
  const firstTwo = apiKey.slice(0, 2);
  const lastTwo = apiKey.slice(-2);
  const masked = `${firstTwo}${"*".repeat(apiKey.length - 4)}${lastTwo}`;
  return masked;
};

