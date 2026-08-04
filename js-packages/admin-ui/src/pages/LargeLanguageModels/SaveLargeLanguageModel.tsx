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
  CodeInput,
  ContainerFluid,
  CreateDataEntity,
  NumberInput,
  TextArea,
  TextInput,
  TitleEntity,
  TooltipDescription,
  combineErrorMessages,
  useForm,
} from "@components/Form";
import { useToast } from "@components/Form/Form/ToastProvider";
import { Box, Button } from "@mui/material";
import { AutocompleteDropdownWithOptions } from "@components/Form/Select/AutocompleteDropdown";
import { maskApiKey } from "@pages/EmbeddingModels";
import React from "react";
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import { useCreateOrUpdateLargeLanguageModelMutation, useLargeLanguageModelQuery } from "../../graphql-generated";
import { useConfirmModal } from "../../utils/useConfirmModal";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";

const getProviderOptions = (t: TFunction) => [
  { value: "openai", label: t("pages.large-language-models.openai") },
  { value: "ollama", label: t("pages.large-language-models.ollama") },
  { value: "hugging-face-custom", label: t("pages.large-language-models.hugging-face-custom") },
  { value: "watsonx", label: t("pages.large-language-models.ibm-watsonx") },
  { value: "chat_vertex_ai", label: t("pages.large-language-models.chat-vertex-ai") },
  { value: "chat_vertex_ai_model_garden", label: t("pages.large-language-models.chat-vertex-ai-model-garden") },
  { value: "aws_bedrock", label: t("pages.large-language-models.aws-bedrock") },
];

export function SaveLargeLanguageModel({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { t } = useTranslation();
  const PROVIDER_OPTIONS = React.useMemo(() => getProviderOptions(t), [t]);
  const { LargeLanguageModelId = "new", view } = useParams();
  const navigate = useNavigate();
  const [isCleaning, setIsCleaning] = React.useState(false);
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: t("pages.large-language-models.edit-large-language-model"),
    body: t("pages.large-language-models.are-you-sure-you-want-to-edit"),
    labelConfirm: t("common.edit"),
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/large-language-model/${LargeLanguageModelId}`);
    }
  };
  const [page, setPage] = React.useState(0);
  const isRecap = page === 1;
  const isNew = LargeLanguageModelId === "new";
  const embeddingModelQuery = useLargeLanguageModelQuery({
    variables: { id: LargeLanguageModelId as string },
    skip: !LargeLanguageModelId || LargeLanguageModelId === "new",
  });
  const toast = useToast();
  const [createOrUpdateLargeLanguageModelMutate, createOrUpdateLargeLanguageModelMutation] =
    useCreateOrUpdateLargeLanguageModelMutation({
      refetchQueries: ["CreateOrUpdateLargeLanguageModel", "LargeLanguageModels", "LargeLanguageModel"],
      onCompleted(data) {
        if (data.largeLanguageModel?.entity) {
          const isNew = LargeLanguageModelId === "new" ? "created" : "updated";
          toast({
            title: isNew === "created" ? t("pages.large-language-models.created-title") : t("pages.large-language-models.updated-title"),
            content: isNew === "created" ? t("pages.large-language-models.created-content") : t("pages.large-language-models.updated-content"),
            displayType: "success",
          });
          navigate(`/large-languages-model/`, { replace: true });
        } else {
          toast({
            title: t("common.error"),
            content: combineErrorMessages(data.largeLanguageModel?.fieldValidators),
            displayType: "error",
          });
        }
      },
      onError(error) {
        console.log(error);
        const isNew = LargeLanguageModelId === "new" ? "create" : "update";
        toast({
          title: isNew === "create" ? t("pages.large-language-models.create-error-title") : t("pages.large-language-models.update-error-title"),
          content: isNew === "create" ? t("pages.large-language-models.create-error-content") : t("pages.large-language-models.update-error-content"),
          displayType: "error",
        });
      },
    });

  const [providerModel, setProviderModel] = React.useState<{
    provider: string | null | undefined;
    model: string | null | undefined;
  }>({ provider: "", model: "" });

  React.useEffect(() => {
    setProviderModel({
      provider: embeddingModelQuery.data?.largeLanguageModel?.providerModel?.provider || "openai",
      model: embeddingModelQuery.data?.largeLanguageModel?.providerModel?.model || "",
    });
  }, [embeddingModelQuery]);

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        apiKey: "",
        apiUrl: "",
        contextWindow: 0,
        retrieveCitations: false,
        description: "",
        jsonConfig: "{}",
        provider: "openai",
        model: "",
      }),
      [],
    ),
    originalValues: embeddingModelQuery.data?.largeLanguageModel
      ? {
        ...embeddingModelQuery.data.largeLanguageModel,
        provider: embeddingModelQuery.data.largeLanguageModel.providerModel?.provider || "openai",
        model: embeddingModelQuery.data.largeLanguageModel.providerModel?.model || "",
      }
      : undefined,
    isLoading: embeddingModelQuery.loading || createOrUpdateLargeLanguageModelMutation.loading,
    onSubmit(data) {
      createOrUpdateLargeLanguageModelMutate({
        variables: {
          id: LargeLanguageModelId !== "new" ? LargeLanguageModelId : undefined,
          providerModel: { provider: providerModel.provider || "", model: providerModel.model || "" },
          ...data,
        },
      });
    },
  });
  const viewMaskApiKey = React.useMemo(() => {
    return !!((view || LargeLanguageModelId !== "new") && form.inputProps("apiKey").value);
  }, [view, LargeLanguageModelId, form.inputProps("apiKey").value]);

  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        cell: [
          { key: "name" },
          { key: "description" },
          { key: "apiKey", label: t("fields.api-key") },
          { key: "apiUrl", label: t("pages.large-language-models.api-url") },
          { key: "contextWindow", label: t("fields.context-window") },
          { key: "retrieveCitations", label: t("fields.retrieve-citations") },
          { key: "provider", label: t("fields.provider") },
          { key: "model", label: t("fields.model") },
          { key: "jsonConfig", label: t("fields.json-config"), jsonView: true },
        ],
        label: t("pages.large-language-models.recap-label"),
      },
    ],
  });

  return (
    <>
      <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
        <TitleEntity
          nameEntity={t("pages.large-language-models.entity-name")}
          description={t("pages.large-language-models.create-or-edit-a-large-languae-model")}
          id={LargeLanguageModelId}
        />
        {view === "view" && (
          <Button variant="contained" onClick={handleEditClick} sx={{ height: "fit-content" }}>
            Edit
          </Button>
        )}
      </Box>
      <form style={{ borderStyle: "unset", padding: "0 16px", marginBottom: "50px" }}>
        <CreateDataEntity
          form={form}
          page={page}
          id={LargeLanguageModelId}
          pathBack="/large-languages-model/"
          setPage={setPage}
          haveConfirmButton={view ? false : true}
          informationSuggestion={[
            {
              content: (
                <>
                  <ContainerFluid flexColumn>
                    <TextInput
                      label={t("common.name")}
                      {...form.inputProps("name")}
                      description={t("pages.large-language-models.unique-identifier-of-the-large-language-model")}
                    />
                    <TextArea
                      label={t("common.description")}
                      {...form.inputProps("description")}
                      description={t("pages.large-language-models.free-text-description-of-the-llm-e")}
                    />
                    <TooltipDescription informationDescription="Api key in case of external api service">
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
                    <TooltipDescription informationDescription="Api url in case of service hosted on on premise service">
                      <TextInput label={t("fields.api-url")} {...form.inputProps("apiUrl")} />
                    </TooltipDescription>
                    <NumberInput
                      label={t("fields.context-window")}
                      {...form.inputProps("contextWindow")}
                      isNumber={false}
                      description={t("pages.large-language-models.maximum-number-of-tokens-the-model-can")}
                    />
                    <BooleanInput
                      label={t("fields.retrieve-citations")}
                      {...form.inputProps("retrieveCitations")}
                      description={t("pages.large-language-models.if-enabled-the-model-is-asked-to")}
                    />
                    <AutocompleteDropdownWithOptions
                      label={t("fields.provider")}
                      description={t("pages.large-language-models.llm-provider-integration-to-use-e-g")}
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
                        form.inputProps("model").onChange(s);
                      }}
                      validationMessages={[]}
                      value={providerModel.model || ""}
                      disabled={view ? true : false}
                      description={t("pages.large-language-models.specific-model-identifier-of-the-selected-provider")}
                    />
                  </ContainerFluid>
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
                        <TooltipDescription informationDescription="Json config to set up Large Language Model settings" />
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

