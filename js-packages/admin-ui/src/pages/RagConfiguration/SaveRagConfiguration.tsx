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
  ContainerFluid,
  CreateDataEntity,
  CustomSelect,
  fromFieldValidators,
  TitleEntity,
  TooltipDescription,
  useForm,
  useToast,
} from "@components/Form";
import { NumberInput, TextArea, TextInput } from "@components/Form/Inputs";
import { Box, Button, Checkbox, FormControlLabel, FormHelperText } from "@mui/material";
import React from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import {
  RagType,
  useCreateRagConfigMutation,
  useRagConfigurationQuery,
  useUpdateRagConfigurationMutation,
} from "../../graphql-generated";
import { useConfirmModal } from "../../utils/useConfirmModal";
import { RagConfigurationQuery } from "./gql";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";

export function SaveRagConfiguration({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { t } = useTranslation();
  const { ragConfigId = "new", view } = useParams();
  const [page, setPage] = React.useState<number>(0);
  const isRecap = page === 1;
  const isNew = ragConfigId === "new";
  const navigate = useNavigate();
  const toast = useToast();

  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: t("pages.rag-configurations.edit-rag-configuration"),
    body: t("pages.rag-configurations.are-you-sure-you-want-to-edit"),
    labelConfirm: t("common.edit"),
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/rag-configuration/${ragConfigId}`);
    }
  };

  const ragConfigQuery = useRagConfigurationQuery({
    variables: { id: ragConfigId as string },
    skip: !ragConfigId || ragConfigId === "new",
    fetchPolicy: "network-only",
  });

  const [createRagConfigMutate, resultCreateRagConfigMutation] = useCreateRagConfigMutation({
    refetchQueries: ["RagConfigurations", RagConfigurationQuery, "Buckets"],
    onCompleted(data) {
      if (data.createRAGConfiguration?.entity) {
        const isNew = ragConfigId === "new" ? "created" : "updated";
        toast({
          title: isNew === "created" ? t("pages.rag-configurations.created-title") : t("pages.rag-configurations.updated-title"),
          content: isNew === "created" ? t("pages.rag-configurations.created-content") : t("pages.rag-configurations.updated-content"),
          displayType: "success",
        });
        const redirectPath = `/rag-configurations/`;
        navigate(redirectPath, { replace: true });
      } else {
        toast({
          title: t("common.error"),
          content: fromFieldValidators(data.createRAGConfiguration?.fieldValidators)("") || "Validation error",
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.log(error);
      const isNew = ragConfigId === "new" ? "create" : "update";
      toast({
        title: isNew === "create" ? t("pages.rag-configurations.create-error-title") : t("pages.rag-configurations.update-error-title"),
        content: isNew === "create" ? t("pages.rag-configurations.create-error-content") : t("pages.rag-configurations.update-error-content"),
        displayType: "error",
      });
    },
  });

  const [updateRagConfigMutate, resultUpdateRagConfigMutation] = useUpdateRagConfigurationMutation({
    refetchQueries: ["RagConfigurations", RagConfigurationQuery],
    onCompleted(data) {
      if (data.updateRAGConfiguration?.entity) {
        toast({
          title: t("pages.rag-configurations.updated-title"),
          content: t("pages.rag-configurations.updated-content"),
          displayType: "success",
        });
        const redirectPath = `/rag-configurations/`;
        navigate(redirectPath, { replace: true });
      } else {
        toast({
          title: t("common.error"),
          content: fromFieldValidators(data.updateRAGConfiguration?.fieldValidators)("") || "Validation error",
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.log(error);
      toast({
        title: t("pages.rag-configurations.update-error-title"),
        content: t("pages.rag-configurations.update-error-content"),
        displayType: "error",
      });
    },
  });

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: ragConfigQuery.data?.ragConfiguration?.name || "",
        description: ragConfigQuery.data?.ragConfiguration?.description || "",
        type: ragConfigQuery.data?.ragConfiguration?.type || RagType.ChatRag,
        reformulate: ragConfigQuery.data?.ragConfiguration?.reformulate || false,
        chunkWindow: ragConfigQuery.data?.ragConfiguration?.chunkWindow || 0,
        rephrasePrompt: ragConfigQuery.data?.ragConfiguration?.rephrasePrompt || "",
        prompt: ragConfigQuery.data?.ragConfiguration?.prompt || "",
        jsonConfig: ragConfigQuery.data?.ragConfiguration?.jsonConfig || "{}",
        ragToolDescription: ragConfigQuery.data?.ragConfiguration?.ragToolDescription || "",
        promptNoRag: ragConfigQuery.data?.ragConfiguration?.promptNoRag || "",
        enableConversationTitle: ragConfigQuery.data?.ragConfiguration?.enableConversationTitle || false,
        rangeStart: ragConfigQuery.data?.ragConfiguration?.range?.start ?? 0,
        rangeEnd: ragConfigQuery.data?.ragConfiguration?.range?.end ?? 0,
      }),
      [ragConfigQuery.data?.ragConfiguration],
    ),
    originalValues: ragConfigQuery.data?.ragConfiguration,
    isLoading: ragConfigQuery.loading || resultCreateRagConfigMutation.loading || resultUpdateRagConfigMutation.loading,
    onSubmit({ rangeStart, rangeEnd, ...data }) {
      if ((rangeStart !== 0 || rangeEnd !== 0) && rangeEnd <= rangeStart) return;
      const isNew = ragConfigId === "new";
      const range = rangeStart === 0 && rangeEnd === 0 ? undefined : { start: rangeStart, end: rangeEnd };
      isNew
        ? createRagConfigMutate({
          variables: {
            ...data,
            range,
          },
        })
        : updateRagConfigMutate({
          variables: {
            id: ragConfigId,
            ...data,
            range,
          },
        });
    },
    getValidationMessages:
      fromFieldValidators(resultCreateRagConfigMutation.data?.createRAGConfiguration?.fieldValidators) ||
      fromFieldValidators(resultUpdateRagConfigMutation.data?.updateRAGConfiguration?.fieldValidators),
  });

  if (ragConfigQuery.loading) return null;

  const selectedType = form.inputProps("type").value;
  const rangeStartValue = form.inputProps("rangeStart").value;
  const rangeEndValue = form.inputProps("rangeEnd").value;
  const rangeError = rangeStartValue !== 0 || rangeEndValue !== 0
    ? rangeEndValue <= rangeStartValue ? "Range End must be greater than Range Start" : ""
    : "";

  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        cell: [
          { key: "name" },
          { key: "description" },
          { key: "type" },
          { key: "prompt" },
          { key: "reformulate" },
          { key: "rephrasePrompt", label: t("fields.rephrase-prompt") },
          { key: "chunkWindow" },
          { key: "jsonConfig", label: t("fields.json-config"), jsonView: true },
          { key: "enableConversationTitle", label: t("fields.enable-conversation-title") },
          { key: "rangeStart", label: t("fields.range-start") },
          { key: "rangeEnd", label: t("fields.range-end") },
          ...(form.inputProps("type").value === RagType.ChatRag ||
            form.inputProps("type").value === RagType.ChatRagTool
            ? [
              { key: "ragToolDescription", label: t("fields.rag-tool-description") },
              { key: "promptNoRag", label: t("fields.prompt-no-rag") },
            ]
            : []),
        ],
        label: t("pages.rag-configurations.recap-label"),
      },
    ],
  });

  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity={t("pages.rag-configurations.entity-name")}
            description={t("pages.rag-configurations.create-or-edit-a-rag-configuration-to")}
            id={ragConfigId}
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
            id={ragConfigId}
            pathBack="/rag-configurations/"
            setPage={setPage}
            haveConfirmButton={!view}
            isValid={!rangeError}
            informationSuggestion={[
              {
                content: (
                  <>
                    <TextInput
                      label={t("common.name")}
                      {...form.inputProps("name")}
                      disabled={view === "view"}
                      description={t("pages.rag-configurations.unique-identifier-of-the-rag-configuration")}
                    />
                    <TextArea
                      label={t("common.description")}
                      {...form.inputProps("description")}
                      disabled={view === "view"}
                      description={t("pages.rag-configurations.free-text-description-explaining-the-purpose-of")}
                    />
                    {ragConfigId === "new" && (
                      <CustomSelect
                        label={t("fields.type")}
                        dict={RagType}
                        {...form.inputProps("type")}
                        disabled={view === "view" || page === 1}
                        description={t("pages.rag-configurations.type-of-rag-configuration")}
                      />
                    )}
                    <>
                      <TextArea
                        label={t("fields.prompt")}
                        {...form.inputProps("prompt")}
                        disabled={view === "view" || page === 1}
                        description={t("pages.rag-configurations.the-main-prompt-for-the-rag-system")}
                      />
                      <TooltipDescription informationDescription="If enabled, the user question is reformulated using the Rephrase Prompt before being sent to the retriever.">
                        <FormControlLabel
                          control={
                            <Checkbox
                              name="Reformulate"
                              checked={form.inputProps("reformulate").value}
                              onChange={(e, checked) => form.inputProps("reformulate").onChange(checked)}
                              disabled={view === "view" || page === 1}
                            />
                          }
                          sx={{ marginLeft: "0", marginRight: "0", marginBottom: "16px" }}
                          label={t("fields.reformulate")}
                          labelPlacement="start"
                        />
                      </TooltipDescription>
                      <TextArea
                        label={t("fields.rephrase-prompt")}
                        {...form.inputProps("rephrasePrompt")}
                        disabled={view === "view" || page === 1}
                        description={t("pages.rag-configurations.prompt-used-for-rephrasing")}
                      />

                      <NumberInput
                        label={t("fields.chunk-window")}
                        {...form.inputProps("chunkWindow")}
                        disabled={view === "view" || page === 1}
                        description={t("pages.rag-configurations.number-of-chunk-to-consider-during-retrieve")}
                      />

                      <TextArea
                        label={t("fields.json-config")}
                        {...form.inputProps("jsonConfig")}
                        disabled={view === "view" || page === 1}
                        description={t("pages.rag-configurations.json-configuration-for-the-rag-system")}
                      />
                      <TooltipDescription informationDescription="If enabled, an automatic title is generated for each conversation based on its content.">
                        <FormControlLabel
                          control={
                            <Checkbox
                              name="EnableConversationTitle"
                              checked={form.inputProps("enableConversationTitle").value}
                              onChange={(e: any, checked: any) => form.inputProps("enableConversationTitle").onChange(checked)}
                              disabled={view === "view" || page === 1}
                            />
                          }
                          sx={{ marginLeft: "0", marginRight: "0", marginBottom: "16px" }}
                          label={t("fields.enable-conversation-title")}
                          labelPlacement="start"
                        />
                      </TooltipDescription>

                      <Box sx={{ display: "flex", gap: 2 }}>
                        <NumberInput
                          label={t("fields.range-start")}
                          {...form.inputProps("rangeStart")}
                          disabled={view === "view" || page === 1}
                          description={t("pages.rag-configurations.start-of-range-must-be-0-and")}
                        />
                        <NumberInput
                          label={t("fields.range-end")}
                          {...form.inputProps("rangeEnd")}
                          disabled={view === "view" || page === 1}
                          description={t("pages.rag-configurations.end-of-range-must-be-start")}
                        />
                      </Box>
                      {rangeError && (
                        <FormHelperText error sx={{ mt: -1, mb: 2 }}>
                          {rangeError}
                        </FormHelperText>
                      )}

                      {(selectedType === RagType.ChatRag || selectedType === RagType.ChatRagTool) && (
                        <>
                          <TextArea
                            label={t("fields.rag-tool-description")}
                            {...form.inputProps("ragToolDescription")}
                            disabled={view === "view" || page === 1}
                            description={t("pages.rag-configurations.description-of-the-rag-tool")}
                          />

                          <TextArea
                            label={t("fields.prompt-no-rag")}
                            {...form.inputProps("promptNoRag")}
                            disabled={view === "view" || page === 1}
                            description={t("pages.rag-configurations.prompt-to-use-when-rag-is-not")}
                          />
                        </>
                      )}
                    </>
                  </>
                ),
                page: 0,
                validation: !!view,
              },
              {
                validation: true,
              },
            ]}
            fieldsControll={["name", "type"]}
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

