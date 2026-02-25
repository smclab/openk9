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
import { Box, Button, Typography } from "@mui/material";
import { useConfirmModal } from "../../utils/useConfirmModal";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";
import { useRestClient } from "@components/queryClient";
import FiberManualRecordIcon from "@mui/icons-material/FiberManualRecord";

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
  const isRecap = page === 1;
  const isNew = enrichItemId === "new";

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
          resourceUri: {
            baseUri: data.baseUri,
            path: data.path,
          },
        },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateEnrichItemMutation.data?.enrichItem?.fieldValidators),
  });

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
          { key: "script", label: "Script", jsonView: true },
        ],
        label: "Recap Enrich Item",
      },
    ],
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
      <form style={{ borderStyle: "unset", padding: 0 }}>
        <CreateDataEntity
          form={form}
          page={page}
          id={enrichItemId}
          pathBack="/enrich-items/"
          setPage={setPage}
          haveConfirmButton={view ? false : true}
          informationSuggestion={[
            {
              content: (
                <>
                  <ContainerFluid flexColumn>
                    <TextInput label="Name" {...form.inputProps("name")} />
                    <TextArea label="Description" {...form.inputProps("description")} />
                    <NumberInput
                      label="Request Timeout - Milliseconds"
                      {...form.inputProps("requestTimeout")}
                      description={"the value is expressed in milliseconds"}
                    />
                    <TextInput
                      label="Base URI"
                      {...form.inputProps("baseUri")}
                      description={"Base URL where enrich service listens"}
                    />
                    <TextInput label="Path" {...form.inputProps("path")} description={"API endpoint path"} />
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
                  <Box sx={{ display: "flex", marginBlock: 2, alignItems: "center" }}>
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
                  </Box>
                  <Box sx={{ mt: 1, display: "flex", alignItems: "center", gap: 1 }}>
                    <Typography variant="body2" color={color} sx={{ display: "flex", alignItems: "center", gap: 1 }}>
                      <FiberManualRecordIcon sx={{ color, fontSize: 18 }} />
                      {label}
                    </Typography>
                  </Box>

                  <ContainerFluid size="md">
                    <CodeInput
                      language="javascript"
                      label="Script"
                      {...form.inputProps("script")}
                      readonly={view === "view" || page === 1}
                      description={
                        "Use it to insert script to be executed in case of Groovy Script enrich, or use it for validation in case of Sync/Async enrich"
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
          submitLabel: isNew ? "Create entity" : "Update entity",
          backLabel: "Back",
        }}
      />
    </>
  );
}
