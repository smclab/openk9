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
  combineErrorMessages,
  ContainerFluid,
  CreateDataEntity,
  fromFieldValidators,
  MultiAssociationCustomQuery,
  TextArea,
  TextInput,
  TitleEntity,
  useForm,
} from "@components/Form";
import { useToast } from "@components/Form/Form/ToastProvider";
import AssociationsLayout from "@components/Form/Tabs/LayoutTab";
import { Box, Button } from "@mui/material";
import React, { useState } from "react";
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import {
  QueryAnalysisAssociationsQuery,
  useAnnotatorsQuery,
  useCreateOrUpdateQueryAnalysisMutation,
  useQueryAnalysisAssociationsQuery,
  useQueryAnalysisQuery,
  useRulesQuery,
} from "../../graphql-generated";
import { AssociatedUnassociated, formatQueryToBE, formatQueryToFE } from "../../utils";
import { useConfirmModal } from "../../utils/useConfirmModal";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";

type ReturnQueryAnalysis = {
  annotators: AssociatedUnassociated;
  rules: AssociatedUnassociated;
};

const getAssociationTabs = (t: TFunction): Array<{ label: string; id: string; tooltip?: string }> => [
  {
    label: t("pages.query-analyses.annotators"),
    id: "annotatorsIds",
    tooltip: t("pages.query-analyses.annotators-associated-to-current-query-analysis-configuration"),
  },
  {
    label: t("pages.query-analyses.rules"),
    id: "rulesIds",
    tooltip: t("pages.query-analyses.rules-associated-to-current-query-analysis-configuration"),
  },
];

const useQueryAnalysisData = ({
  queryAnalysisId,
  queryAnalysisQuery,
  associatedQueryAnalysisQuery,
}: {
  queryAnalysisId: string;
  queryAnalysisQuery: QueryAnalysisAssociationsQuery | undefined;
  associatedQueryAnalysisQuery: QueryAnalysisAssociationsQuery | undefined;
}): ReturnQueryAnalysis => {
  const skipRecoveryAllInformation = queryAnalysisId !== "new";

  const annotatorsQuery = useAnnotatorsQuery({
    skip: skipRecoveryAllInformation,
  });

  const rulesQuery = useRulesQuery({
    skip: skipRecoveryAllInformation,
  });

  const data = React.useMemo(
    () => ({
      annotators: {
        unassociated: formatQueryToFE({
          informationId:
            annotatorsQuery.data?.annotators?.edges || queryAnalysisQuery?.queryAnalysis?.annotators?.edges,
        }),
        isLoading: annotatorsQuery.loading,
        associated: formatQueryToFE({
          informationId: associatedQueryAnalysisQuery?.queryAnalysis?.annotators?.edges,
        }),
      },
      rules: {
        unassociated: formatQueryToFE({
          informationId: rulesQuery.data?.rules?.edges || queryAnalysisQuery?.queryAnalysis?.rules?.edges,
        }),
        isLoading: rulesQuery.loading,
        associated: formatQueryToFE({
          informationId: associatedQueryAnalysisQuery?.queryAnalysis?.rules?.edges,
        }),
      },
    }),
    [rulesQuery, annotatorsQuery, queryAnalysisQuery, associatedQueryAnalysisQuery],
  );

  return { ...data };
};

export function SaveQueryAnalysis({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { t } = useTranslation();
  const associationTabs = React.useMemo(() => getAssociationTabs(t), [t]);
  const { queryAnalysisId = "new", view } = useParams();
  const navigate = useNavigate();
  const [selectedAssociationTabs, setSelectedAssociationTabs] = useState<string>(associationTabs[0].id);
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: t("pages.query-analyses.edit-query-analysis"),
    body: t("pages.query-analyses.are-you-sure-you-want-to-edit"),
    labelConfirm: t("common.edit"),
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/query-analysis/${queryAnalysisId}`);
    }
  };
  const [page, setPage] = React.useState(0);
  const isRecap = page === 1;
  const isNew = queryAnalysisId === "new";

  const queryAnalysisQuery = useQueryAnalysisQuery({
    variables: { id: queryAnalysisId as string },
    skip: !queryAnalysisId || queryAnalysisId === "new",
  });

  const queryAnalysisUnassociated = useQueryAnalysisAssociationsQuery({
    variables: { parentId: queryAnalysisId as string, unassociated: true },
    skip: !queryAnalysisId || queryAnalysisId === "new",
    fetchPolicy: "network-only",
  });

  const queryAnalysisAssociated = useQueryAnalysisAssociationsQuery({
    variables: { parentId: queryAnalysisId as string, unassociated: false },
    skip: !queryAnalysisId || queryAnalysisId === "new",
    fetchPolicy: "network-only",
  });

  const { annotators, rules } = useQueryAnalysisData({
    queryAnalysisId,
    queryAnalysisQuery: queryAnalysisUnassociated.data,
    associatedQueryAnalysisQuery: queryAnalysisAssociated.data,
  });
  const toast = useToast();
  const [createOrUpdateQueryAnalysisMutate, createOrUpdateQueryAnalysisMutation] =
    useCreateOrUpdateQueryAnalysisMutation({
      refetchQueries: ["QueryAnalysis", "QueryAnalyses"],
      onCompleted(data) {
        if (data.queryAnalysisWithLists?.entity) {
          const isNew = queryAnalysisId === "new" ? "created" : "updated";
          toast({
            title: isNew === "created" ? t("pages.query-analyses.created-title") : t("pages.query-analyses.updated-title"),
            content: isNew === "created" ? t("pages.query-analyses.created-content") : t("pages.query-analyses.updated-content"),
            displayType: "success",
          });
          navigate(`/query-analyses/`, { replace: true });
        } else {
          toast({
            title: t("common.error"),
            content: combineErrorMessages(data.queryAnalysisWithLists?.fieldValidators),
            displayType: "error",
          });
        }
      },
      onError(error) {
        console.log(error);
        const isNew = queryAnalysisId === "new" ? "create" : "update";
        toast({
          title: isNew === "create" ? t("pages.query-analyses.create-error-title") : t("pages.query-analyses.update-error-title"),
          content: isNew === "create" ? t("pages.query-analyses.create-error-content") : t("pages.query-analyses.update-error-content"),
          displayType: "error",
        });
      },
    });

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        stopWords: "",
        annotatorsIds: annotators?.associated || [],
        rulesIds: rules?.associated || [],
      }),
      [annotators, rules],
    ),
    originalValues: queryAnalysisQuery.data?.queryAnalysis,
    isLoading: queryAnalysisQuery.loading || createOrUpdateQueryAnalysisMutation.loading,
    onSubmit(data) {
      createOrUpdateQueryAnalysisMutate({
        variables: {
          id: queryAnalysisId !== "new" ? queryAnalysisId : undefined,
          ...data,
          annotatorsIds: formatQueryToBE({
            information: data.annotatorsIds,
          }),
          rulesIds: formatQueryToBE({
            information: data.rulesIds,
          }),
        },
      });
    },
    getValidationMessages: fromFieldValidators(
      createOrUpdateQueryAnalysisMutation.data?.queryAnalysisWithLists?.fieldValidators,
    ),
  });

  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        cell: [{ key: "name" }, { key: "description" }, { key: "stopWords" }],
        label: t("pages.query-analyses.recap-label"),
      },
      {
        cell: [{ key: "annotatorsIds" }, { key: "rulesIds" }],
        label: t("pages.query-analyses.associations"),
      },
    ],
    valueOverride: {
      annotatorsIds:
        form.inputProps("annotatorsIds").value?.map((annotator, index) => ({ [index + 1]: annotator.label })) || [],
      rulesIds: form.inputProps("rulesIds").value?.map((rule, index) => ({ [index + 1]: rule.label })) || [],
    },
  });

  return (
    <>
      <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
        <TitleEntity
          nameEntity={t("pages.query-analyses.entity-name")}
          description={t("pages.query-analyses.configure-query-analysis-tool-add-to-it")}
          id={queryAnalysisId}
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
          id={queryAnalysisId}
          pathBack="/query-analyses/"
          setPage={setPage}
          haveConfirmButton={view ? false : true}
          informationSuggestion={[
            {
              content: (
                <>
                  <ContainerFluid flexColumn size="md">
                    <TextInput label={t("common.name")} {...form.inputProps("name")} />
                    <TextArea label={t("common.description")} {...form.inputProps("description")} />
                    <AssociationsLayout tabs={associationTabs} setTabsId={setSelectedAssociationTabs}>
                      <MultiAssociationCustomQuery
                        list={{
                          ...annotators,
                          associated: form.inputProps("annotatorsIds").value,
                        }}
                        createPath={{ path: "/annotator/new", entity: "annotators" }}
                        disabled={false}
                        isRecap={page === 1}
                        sx={selectedAssociationTabs === "annotatorsIds" ? {} : { display: "none" }}
                        onSelect={({ items, isAdd }) => {
                          const data = form.inputProps("annotatorsIds").value;
                          if (isAdd) {
                            const updatedData = [
                              ...data,
                              ...items.filter((item) => !data.some((d) => d.value === item.value)),
                            ];
                            form.inputProps("annotatorsIds").onChange(updatedData);
                          } else {
                            const updatedData = data.filter((dat) => !items.some((item) => item.value === dat.value));
                            form.inputProps("annotatorsIds").onChange(updatedData);
                          }
                        }}
                      />
                      <MultiAssociationCustomQuery
                        list={{
                          ...rules,
                          associated: form.inputProps("rulesIds").value,
                        }}
                        disabled={false}
                        isRecap={page === 1}
                        sx={selectedAssociationTabs === "rulesIds" ? {} : { display: "none" }}
                        onSelect={({ items, isAdd }) => {
                          const data = form.inputProps("rulesIds").value;
                          if (isAdd) {
                            const updatedData = [
                              ...data,
                              ...items.filter((item) => !data.some((d) => d.value === item.value)),
                            ];
                            form.inputProps("rulesIds").onChange(updatedData);
                          } else {
                            const updatedData = data.filter((dat) => !items.some((item) => item.value === dat.value));
                            form.inputProps("rulesIds").onChange(updatedData);
                          }
                        }}
                      />
                    </AssociationsLayout>
                  </ContainerFluid>
                  <ContainerFluid size="md">
                    <CodeInput
                      label={t("fields.stop-words")}
                      readonly={view === "view" || page === 1}
                      language="text"
                      {...form.inputProps("stopWords")}
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

