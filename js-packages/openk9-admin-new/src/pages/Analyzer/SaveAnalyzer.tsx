import React, { useState, useMemo } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  combineErrorMessages,
  ContainerFluid,
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
import useTemplate, { createJsonString, NavigationButtons } from "@components/Form/Hook/Template";
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
import { AnalyzersRefetchQuery, TemplateAnalyzers } from "./gql";
import { Box, Button } from "@mui/material";
import { useConfirmModal } from "../../utils/useConfirmModal";

export function SaveAnalyzer() {
  const { analyzerId = "new", view } = useParams();
  const [page, setPage] = useState(0);
  const navigate = useNavigate();
  const toast = useToast();

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
    refetchQueries: [AnalyzersRefetchQuery],
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
              <MultiAssociationCustomQuery
                list={{ ...charFilters, associated: form.inputProps("charFilters").value }}
                disabled={isRecap || view === "view"}
                isRecap={isRecap}
                titleAssociation="Association with Char Filters"
                createPath={{ path: "/char-filter/new", entity: "char-filters" }}
                onSelect={handleAssociationSelect("charFilters")}
              />
              <MultiAssociationCustomQuery
                list={{ ...tokenFilters, associated: form.inputProps("tokenFilters").value }}
                disabled={isRecap || view === "view"}
                createPath={{ path: "/token-filter/new", entity: "token-filters" }}
                isRecap={isRecap}
                titleAssociation="Association with Token Filters"
                onSelect={handleAssociationSelect("tokenFilters")}
              />
              <CustomSelectRelationsOneToOne
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
          <NavigationButtons
            isRecap={isRecap}
            submitForm={form.submit}
            goToRecap={() => setPage(1)}
            removeRecap={() => setPage(0)}
            pathBack="/analyzers"
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
