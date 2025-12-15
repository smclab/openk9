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
import { useMemo, useState } from "react";
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

const associationTabs: Array<{ label: string; id: string; tooltip?: string }> = [
  { label: "Char Filters", id: "charFilters" },
  { label: "Token Filters", id: "tokenFilters" },
];

export function SaveAnalyzer() {
  const { analyzerId = "new", view } = useParams();
  const [page, setPage] = useState(0);
  const navigate = useNavigate();
  const toast = useToast();
  const [selectedAssociationTabs, setSelectedAssociationTabs] = useState<string>(associationTabs[0].id);

  const isNewAnalyzer = analyzerId === "new";

  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Analyzer",
    body: "Are you sure you want to edit this analyzer?",
    labelConfirm: "Edit",
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
          title: `Analyzer ${action}`,
          content: `Analyzer has been ${action} successfully`,
          displayType: "success",
        });
        navigate(`/analyzers/`, { replace: true });
      } else {
        toast({
          title: "Error",
          content: combineErrorMessages(data.analyzerWithLists?.fieldValidators),
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.error(error);
      const action = isNewAnalyzer ? "create" : "update";
      toast({
        title: `Error ${action}`,
        content: `Impossible to ${action} Analyzer`,
        displayType: "error",
      });
    },
  });

  const { template, typeSelected, changeType, changeValueKey } = useTemplate({
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
      createOrUpdateAnalyzerMutate({
        variables: {
          id: !isNewAnalyzer ? analyzerId : undefined,
          ...data,
          type: typeSelected,
          name: data.name || "",
          description: data.description,
          charFilterIds: formatQueryToBE({ information: data.charFilters }),
          tokenFilterIds: formatQueryToBE({ information: data.tokenFilters }),
          tokenizerId: data.tokenizerId.id !== "-1" ? data.tokenizerId.id : null,
          jsonConfig,
        },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateAnalyzerMutation.data?.analyzerWithLists?.fieldValidators),
  });

  if (analyzerQuery.loading) return null;

  const isRecap = page === 1;

  const handleAssociationSelect =
    (field: "charFilters" | "tokenFilters") =>
    ({ items, isAdd }: { items: any[]; isAdd: boolean }) => {
      const currentData = form.inputProps(field).value;
      const updatedData = isAdd
        ? [...currentData, ...items.filter((item) => !currentData.some((d: any) => d.value === item.value))]
        : currentData.filter((dataItem: any) => !items.some((item) => item.value === dataItem.value));
      form.inputProps(field).onChange(updatedData);
    };

  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        keys: ["name", "description", "type", "jsonConfig", "charFilters", "tokenFilters", "tokenizerId"],
        label: "Recap Analyzer",
      },
    ],
  });

  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity="Analyzer"
            description="Create or Edit an analyzer to definire a specific analysis logic to apply to fields.
          You can choose between pre-built analyzer or create your custom analyzer.
          In case of custom analyzer associate to it tokenizers, token filters or char filters."
            id={analyzerId}
          />
          {view === "view" && (
            <Button variant="contained" onClick={handleEditClick} sx={{ height: "fit-content" }}>
              Edit
            </Button>
          )}
        </Box>
        <form style={{ borderStyle: "unset", padding: "0 16px" }}>
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
                    <TextInput label="Name" {...form.inputProps("name")} disabled={isRecap} />
                    <TextArea label="Description" {...form.inputProps("description")} disabled={isRecap} />
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
                          label="Tokenizer"
                          onChange={(val) => form.inputProps("tokenizerId").onChange({ id: val.id, name: val.name })}
                          value={{
                            id: form.inputProps("tokenizerId").value.id,
                            name: form.inputProps("tokenizerId").value.name || "",
                          }}
                          disabled={isRecap}
                        />
                      </>
                    )}
                    <Recap recapData={recapSections} />
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
