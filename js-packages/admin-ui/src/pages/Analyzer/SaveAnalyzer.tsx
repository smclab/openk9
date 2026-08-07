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
  combineErrorMessages,
  ContainerFluid,
  CreateDataEntity,
  CustomSelectRelationsOneToOne,
  fromFieldValidators,
  MultiAssociationCustomQuery,
  TextArea,
  TextInput,
  TitleEntity,
  useForm,
  useToast,
} from "@components/Form";
import { GenerateDynamicFieldsMemo } from "@components/Form/Form/GenerateDynamicFields";
import useTemplate, { createJsonString } from "@components/Form/Hook/Template";
import AssociationsLayout from "@components/Form/Tabs/LayoutTab";
import { Box, Button } from "@mui/material";
import React, { useMemo, useState } from "react";
import type { TFunction } from "i18next";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import {
  AnalyzersAssociationsQuery,
  useAnalyzerQuery,
  useAnalyzersAssociationsQuery,
  useCharfiltersQuery,
  useCreateOrUpdateAnalyzerMutation,
  useTokenFiltersQuery,
  useTokenizersQuery,
} from "../../graphql-generated";
import { AssociatedUnassociated, formatQueryToBE, formatQueryToFE } from "../../utils";
import { useConfirmModal } from "../../utils/useConfirmModal";
import { TemplateAnalyzers } from "./gql";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";

const getAssociationTabs = (t: TFunction): Array<{ label: string; id: string; tooltip?: string }> => [
  { label: t("pages.analyzers.char-filters"), id: "charFilters" },
  { label: t("pages.analyzers.token-filters"), id: "tokenFilters" },
];

export function SaveAnalyzer({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { t } = useTranslation();
  const associationTabs = React.useMemo(() => getAssociationTabs(t), [t]);
  const { analyzerId = "new", view } = useParams();
  const [page, setPage] = useState(0);
  const navigate = useNavigate();
  const toast = useToast();
  const [selectedAssociationTabs, setSelectedAssociationTabs] = useState<string>(associationTabs[0].id);

  const isNewAnalyzer = analyzerId === "new";

  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: t("pages.analyzers.edit-analyzer"),
    body: t("pages.analyzers.are-you-sure-you-want-to-edit"),
    labelConfirm: t("common.edit"),
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/analyzer/${analyzerId}`);
    }
  };

  const analyzerQuery = useAnalyzerQuery({
    variables: { id: analyzerId },
    skip: !analyzerId || isNewAnalyzer,
    fetchPolicy: "network-only",
  });

  const analyzerUnassociated = useAnalyzersAssociationsQuery({
    variables: { parentId: analyzerId, unassociated: true },
    skip: !analyzerId || isNewAnalyzer,
    fetchPolicy: "network-only",
  });

  const analyzerAssociated = useAnalyzersAssociationsQuery({
    variables: { parentId: analyzerId, unassociated: false },
    skip: !analyzerId || isNewAnalyzer,
    fetchPolicy: "network-only",
  });

  const { charFilters, tokenFilters } = useAnalyzerData({
    analyzerId,
    analyzerQuery: analyzerUnassociated.data,
    associatedAnalyzerQuery: analyzerAssociated.data,
  });

  const { OptionsTokenizer } = useOptions();

  const [createOrUpdateAnalyzerMutate, createOrUpdateAnalyzerMutation] = useCreateOrUpdateAnalyzerMutation({
    refetchQueries: ["Analyzers"],
    onCompleted(data) {
      if (data.analyzerWithLists?.entity) {
        const action = isNewAnalyzer ? "created" : "updated";
        toast({
          title: action === "created" ? t("pages.analyzers.created-title") : t("pages.analyzers.updated-title"),
          content: action === "created" ? t("pages.analyzers.created-content") : t("pages.analyzers.updated-content"),
          displayType: "success",
        });
        navigate(`/analyzers/`, { replace: true });
      } else {
        toast({
          title: t("common.error"),
          content: combineErrorMessages(data.analyzerWithLists?.fieldValidators),
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.error(error);
      const action = isNewAnalyzer ? "create" : "update";
      toast({
        title: action === "create" ? t("pages.analyzers.create-error-title") : t("pages.analyzers.update-error-title"),
        content: action === "create" ? t("pages.analyzers.create-error-content") : t("pages.analyzers.update-error-content"),
        displayType: "error",
      });
    },
  });

  const { template, typeSelected, changeType, changeValueKey, allTemplateDescriptions } = useTemplate({
    templateSelected: TemplateAnalyzers,
    jsonConfig: analyzerQuery.data?.analyzer?.jsonConfig,
    type: analyzerQuery.data?.analyzer?.type,
  });
  const isCustom = typeSelected === "custom";

  const form = useForm({
    initialValues: useMemo(
      () => ({
        name: "",
        description: "",
        type: "",
        jsonConfig: "{}",
        charFilters: charFilters?.associated || [],
        tokenFilters: tokenFilters?.associated || [],
        tokenizerId: {
          id: analyzerQuery.data?.analyzer?.tokenizer?.id || "-1",
          name: analyzerQuery.data?.analyzer?.tokenizer?.name || "",
        },
      }),
      [charFilters, tokenFilters, analyzerQuery.data?.analyzer?.tokenizer],
    ),
    originalValues: analyzerQuery.data?.analyzer,
    isLoading: analyzerQuery.loading || createOrUpdateAnalyzerMutation.loading,
    onSubmit(data) {
      const jsonConfig = createJsonString({ template: template?.value, type: typeSelected });
      const isCustomType = typeSelected === "custom";
      createOrUpdateAnalyzerMutate({
        variables: {
          id: !isNewAnalyzer ? analyzerId : undefined,
          ...data,
          type: typeSelected,
          name: data.name || "",
          description: data.description,
          charFilterIds: isCustomType ? formatQueryToBE({ information: data.charFilters }) : [],
          tokenFilterIds: isCustomType ? formatQueryToBE({ information: data.tokenFilters }) : [],
          tokenizerId: isCustomType && data.tokenizerId.id !== "-1" ? data.tokenizerId.id : null,
          jsonConfig,
        },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateAnalyzerMutation.data?.analyzerWithLists?.fieldValidators),
  });

  React.useEffect(() => {
    if (template?.description) {
      const current = form.inputProps("description").value;
      if (!current || allTemplateDescriptions.includes(current)) {
        form.inputProps("description").onChange(template.description);
      }
    }
  }, [template?.description]);

  const computedJsonConfig = React.useMemo(
    () =>
      createJsonString({
        template: template?.value,
        type: typeSelected,
      }),
    [template, typeSelected],
  );

  const recapSections = React.useMemo(
    () =>
      mappingCardRecap({
        form: form as any,
        sections: [
          {
            cell: [
              { key: "name" },
              { key: "description" },
              { key: "type" },
              ...(typeSelected === "custom"
                ? [
                  { key: "charFilters", label: t("pages.analyzers.char-filters") },
                  { key: "tokenFilters", label: t("pages.analyzers.token-filters") },
                  { key: "tokenizerId", label: t("fields.tokenizer") },
                ]
                : []),
              ...(typeSelected ? [{ key: "jsonConfig", label: t("fields.json-config"), keyNotView: "type" }] : []),
            ],
            label: t("pages.analyzers.recap-label"),
          },
        ],
        valueOverride: {
          type: typeSelected,
          jsonConfig: computedJsonConfig,
          tokenizerId: form.inputProps("tokenizerId").value?.name || "",
        },
      }),
    [form, typeSelected, computedJsonConfig],
  );

  if (analyzerQuery.loading) return null;

  const isRecap = page === 1;
  const isNew = analyzerId === "new";

  const handleAssociationSelect =
    (field: "charFilters" | "tokenFilters") =>
      ({ items, isAdd }: { items: any[]; isAdd: boolean }) => {
        const currentData = form.inputProps(field).value;
        const updatedData = isAdd
          ? [...currentData, ...items.filter((item) => !currentData.some((d: any) => d.value === item.value))]
          : currentData.filter((dataItem: any) => !items.some((item) => item.value === dataItem.value));
        form.inputProps(field).onChange(updatedData);
      };

  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity={t("pages.analyzers.entity-name")}
            description={t("pages.analyzers.create-or-edit-an-analyzer-to-definire")}
            id={analyzerId}
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
            id={analyzerId}
            pathBack="/analyzers/"
            setPage={setPage}
            haveConfirmButton={view ? false : true}
            informationSuggestion={[
              {
                content: (
                  <>
                    <TextInput label={t("common.name")} {...form.inputProps("name")} disabled={isRecap} />
                    <TextArea label={t("common.description")} {...form.inputProps("description")} disabled={isRecap} />
                    <GenerateDynamicFieldsMemo
                      templates={TemplateAnalyzers}
                      type={typeSelected}
                      template={template}
                      setType={changeType}
                      isRecap={isRecap}
                      changeValueKey={changeValueKey}
                    />
                    {isCustom && (
                      <>
                        <AssociationsLayout tabs={associationTabs} setTabsId={setSelectedAssociationTabs}>
                          <MultiAssociationCustomQuery
                            list={{ ...charFilters, associated: form.inputProps("charFilters").value }}
                            sx={selectedAssociationTabs === "charFilters" ? {} : { display: "none" }}
                            disabled={isRecap || view === "view"}
                            isRecap={isRecap}
                            createPath={{ path: "/char-filter/new", entity: "char-filters" }}
                            onSelect={handleAssociationSelect("charFilters")}
                          />
                          <MultiAssociationCustomQuery
                            list={{ ...tokenFilters, associated: form.inputProps("tokenFilters").value }}
                            sx={selectedAssociationTabs === "tokenFilters" ? {} : { display: "none" }}
                            disabled={isRecap || view === "view"}
                            createPath={{ path: "/token-filter/new", entity: "token-filters" }}
                            isRecap={isRecap}
                            onSelect={handleAssociationSelect("tokenFilters")}
                          />
                        </AssociationsLayout>
                        <CustomSelectRelationsOneToOne
                          sx={{ mt: 2 }}
                          options={OptionsTokenizer}
                          label={t("fields.tokenizer")}
                          onChange={(val) => form.inputProps("tokenizerId").onChange({ id: val.id, name: val.name })}
                          value={{
                            id: form.inputProps("tokenizerId").value.id,
                            name: form.inputProps("tokenizerId").value.name || "",
                          }}
                          disabled={isRecap}
                        />
                      </>
                    )}
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
    </ContainerFluid>
  );
}

type ReturnUserAnalyzerData = {
  charFilters: AssociatedUnassociated;
  tokenFilters: AssociatedUnassociated;
};

const useAnalyzerData = ({
  analyzerId,
  analyzerQuery,
  associatedAnalyzerQuery,
}: {
  analyzerId: string;
  analyzerQuery: AnalyzersAssociationsQuery | undefined;
  associatedAnalyzerQuery: AnalyzersAssociationsQuery | undefined;
}): ReturnUserAnalyzerData => {
  const skipFetchingInfo = analyzerId !== "new";

  const tokenFiltersQuery = useTokenFiltersQuery({ skip: skipFetchingInfo });
  const charFiltersQuery = useCharfiltersQuery({ skip: skipFetchingInfo });

  const data = useMemo(
    () => ({
      tokenFilters: {
        unassociated: formatQueryToFE({
          informationId: tokenFiltersQuery.data?.tokenFilters?.edges || analyzerQuery?.analyzer?.tokenFilters?.edges,
        }),
        isLoading: tokenFiltersQuery.loading,
        associated: formatQueryToFE({
          informationId: associatedAnalyzerQuery?.analyzer?.tokenFilters?.edges,
        }),
      },
      charFilters: {
        unassociated: formatQueryToFE({
          informationId: charFiltersQuery.data?.charFilters?.edges || analyzerQuery?.analyzer?.charFilters?.edges,
        }),
        isLoading: charFiltersQuery.loading,
        associated: formatQueryToFE({
          informationId: associatedAnalyzerQuery?.analyzer?.charFilters?.edges,
        }),
      },
    }),
    [tokenFiltersQuery, charFiltersQuery, analyzerQuery, associatedAnalyzerQuery],
  );

  return data;
};

const useOptions = () => {
  const searchConfigQuery = useTokenizersQuery();

  const getOptions = (data: any, key: "tokenizers") =>
    data?.[key]?.edges?.map((item: { node: { id: string; name: string } }) => ({
      value: item.node.id || "",
      label: item.node.name || "",
    })) || [];

  const OptionsTokenizer = getOptions(searchConfigQuery.data, "tokenizers");

  return { OptionsTokenizer };
};

