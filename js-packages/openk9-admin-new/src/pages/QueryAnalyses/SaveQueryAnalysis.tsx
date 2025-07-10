import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  combineErrorMessages,
  ContainerFluid,
  CreateDataEntity,
  fromFieldValidators,
  MultiAssociationCustomQuery,
  TextArea,
  TextInput,
  TitleEntity,
  useForm,
  CodeInput,
  TooltipDescription,
} from "@components/Form";
import {
  QueryAnalysisAssociationsQuery,
  useAnnotatorsQuery,
  useCreateOrUpdateQueryAnalysisMutation,
  useQueryAnalysisAssociationsQuery,
  useQueryAnalysisQuery,
  useRulesQuery,
} from "../../graphql-generated";
import { QueryAnalysisQuery, QueryAnalysesQuery } from "./gql";
import { AssociatedUnassociated, formatQueryToBE, formatQueryToFE } from "../../utils";
import { useToast } from "@components/Form/Form/ToastProvider";
import { Box, Button } from "@mui/material";
import { useConfirmModal } from "../../utils/useConfirmModal";

type ReturnQueryAnalysis = {
  annotators: AssociatedUnassociated;
  rules: AssociatedUnassociated;
};

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

export function SaveQueryAnalysis() {
  const { queryAnalysisId = "new", view } = useParams();
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Query Analysis",
    body: "Are you sure you want to edit this Query Analysis?",
    labelConfirm: "Edit",
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/query-analysis/${queryAnalysisId}`);
    }
  };
  const [page, setPage] = React.useState(0);

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
      refetchQueries: [QueryAnalysisQuery, QueryAnalysesQuery],
      onCompleted(data) {
        if (data.queryAnalysisWithLists?.entity) {
          const isNew = queryAnalysisId === "new" ? "created" : "updated";
          toast({
            title: `Query Analysis ${isNew}`,
            content: `Query Analysis has been ${isNew} successfully`,
            displayType: "success",
          });
          navigate(`/query-analyses/`, { replace: true });
        } else {
          toast({
            title: `Error`,
            content: combineErrorMessages(data.queryAnalysisWithLists?.fieldValidators),
            displayType: "error",
          });
        }
      },
      onError(error) {
        console.log(error);
        const isNew = queryAnalysisId === "new" ? "create" : "update";
        toast({
          title: `Error ${isNew}`,
          content: `Impossible to ${isNew} Query Analysis`,
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

  return (
    <>
      <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
        <TitleEntity
          nameEntity="Query Analysis"
          description="Configure Query Analysis tool. Add to it rules and annotators to create your Query Analysis logic."
          id={queryAnalysisId}
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
          id={queryAnalysisId}
          pathBack="/query-analyses/"
          setPage={setPage}
          haveConfirmButton={view ? false : true}
          informationSuggestion={[
            {
              content: (
                <>
                  <ContainerFluid flexColumn>
                    <TextInput label="Name" {...form.inputProps("name")} />
                    <TextArea label="Description" {...form.inputProps("description")} />
                    <TooltipDescription informationDescription="Annotators associated to current Query Analysis configuration">
                      <MultiAssociationCustomQuery
                        list={{
                          ...annotators,
                          associated: form.inputProps("annotatorsIds").value,
                        }}
                        createPath={{ path: "/annotator/new", entity: "annotators" }}
                        disabled={false}
                        isRecap={page === 1}
                        titleAssociation="Association with annotators"
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
                    </TooltipDescription>
                    <TooltipDescription informationDescription="Rules associated to current Query Analysis configuration">
                      <MultiAssociationCustomQuery
                        list={{
                          ...rules,
                          associated: form.inputProps("rulesIds").value,
                        }}
                        disabled={false}
                        isRecap={page === 1}
                        titleAssociation="Association with rules"
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
                    </TooltipDescription>
                  </ContainerFluid>
                  <ContainerFluid size="md">
                    <CodeInput
                      label="Stop Words"
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
    </>
  );
}
