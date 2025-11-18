import { useToast } from "@components/Form/Form/ToastProvider";
import { Box, Button } from "@mui/material";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  ContainerFluid,
  CreateDataEntity,
  fromFieldValidators,
  MultiAssociationCustomQuery,
  NumberInput,
  TextArea,
  TextInput,
  TitleEntity,
  TooltipDescription,
  useForm,
} from "../../components/Form";
import { TabQuery, useCreateOrUpdateTabMutation, useTabQuery, useTabTokensQuery } from "../../graphql-generated";
import { formatQueryToBE, formatQueryToFE } from "../../utils";
import { useConfirmModal } from "../../utils/useConfirmModal";
import { ReturnUserTabData } from "./gql";

export function SaveTab() {
  const { tabId = "new", view } = useParams();
  const [page, setPage] = React.useState(0);
  const tabQuery = useTabQuery({
    variables: { id: tabId as string },
    skip: !tabId || tabId === "new",
    fetchPolicy: "network-only",
  });
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Tab",
    body: "Are you sure you want to edit this Tab?",
    labelConfirm: "Edit",
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/tab/${tabId}`);
    }
  };
  const toast = useToast();
  const [createOrUpdateTabMutate, createOrUpdateTabMutation] = useCreateOrUpdateTabMutation({
    refetchQueries: ["Tab", "Tabs"],
    onCompleted(data) {
      try {
        const parentId = data.tabWithTokenTabs?.entity?.id;

        if (!parentId) {
          throw new Error("Name is invalid");
        }
        if (parentId) {
          toast({
            content: "Tab has been created successfully",
            displayType: "success",
            title: "Tab Created",
          });
          navigate(`/tabs`);
        }
      } catch (err: any) {
        console.error("Error during onCompleted processing:", err);
        toast({
          title: `An unexpected error occurred`,
          content: `Impossible to ${err.message} Tab`,
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.error("Mutation error:", error);
      const isNew = tabId === "new" ? "create" : "update";
      toast({
        title: `Error ${isNew}`,
        content: `Impossible to ${isNew} Tab`,
        displayType: "error",
      });
    },
  });
  const tabTokenTab = useTabQuery({
    variables: { id: tabId as string, unasociated: true },
    skip: !tabId || tabId === "new",
    fetchPolicy: "network-only",
  });
  const associatedTabQuery = useTabQuery({
    variables: { id: tabId as string, unasociated: false },
    skip: !tabId || tabId === "new",
    fetchPolicy: "network-only",
  });

  const { tokenTab } = useTabData({
    tabId,
    tabQuery: tabTokenTab.data,
    associatedTabQuery: associatedTabQuery.data?.tab?.tokenTabs?.edges,
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        priority: 0,
        tokenTabIds: tokenTab.associated || [],
      }),
      [tokenTab],
    ),
    originalValues: tabQuery.data?.tab,
    isLoading: tabQuery.loading || createOrUpdateTabMutation.loading,
    onSubmit(data) {
      createOrUpdateTabMutate({
        variables: {
          id: tabId !== "new" ? tabId : undefined,
          ...data,
          tokenTabIds: formatQueryToBE({
            information: data.tokenTabIds || [],
          }) as number[],
        },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateTabMutation.data?.tabWithTokenTabs?.fieldValidators),
  });
  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [{ keys: ["name", "description", "priority", "tokenTabIds"], label: "Tab Information" }],
  });
  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity="Tab"
            description="Create or Edit a Tab and add to it Token Tabs to create yoy personalized search to perform by tab."
            id={tabId}
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
            id={tabId}
            pathBack="/tabs/"
            setPage={setPage}
            haveConfirmButton={view ? false : true}
            informationSuggestion={[
              {
                content: (
                  <div>
                    <TextInput label="Name" {...form.inputProps("name")} />
                    <TextArea label="Description" {...form.inputProps("description")} />
                    <NumberInput
                      label="Priority"
                      {...form.inputProps("priority")}
                      description="Define priority according to which suggestion cateogories are
      orderder by search frontend during rendering"
                    />
                    <TooltipDescription informationDescription="Token Tabs associated to current Tab">
                      <MultiAssociationCustomQuery
                        list={{
                          ...tokenTab,
                          associated: form.inputProps("tokenTabIds").value,
                        }}
                        createPath={{ path: "/token-tab/new", entity: "token-tabs" }}
                        disabled={page === 1 || view === "view"}
                        isRecap={page === 1}
                        titleAssociation="Association with token tabs"
                        onSelect={({ items, isAdd }) => {
                          const data = form.inputProps("tokenTabIds").value;

                          if (isAdd) {
                            const updatedData = [
                              ...data,
                              ...items.filter((item) => !data.some((d) => d.value === item.value)),
                            ];
                            form.inputProps("tokenTabIds").onChange(updatedData);
                          } else {
                            const updatedData = data.filter((dat) => !items.some((item) => item.value === dat.value));
                            form.inputProps("tokenTabIds").onChange(updatedData);
                          }
                        }}
                      />
                    </TooltipDescription>
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
        <Recap recapData={recapSections} />
      </>
      <ConfirmModal />
    </ContainerFluid>
  );
}

const useTabData = ({
  tabId,
  tabQuery,
  associatedTabQuery,
}: {
  tabId: string;
  tabQuery: TabQuery | undefined;
  associatedTabQuery:
    | ({
        __typename?: "DefaultEdge_TokenTab";
        node?: {
          __typename?: "TokenTab";
          name?: string | null;
          id?: string | null;
        } | null;
      } | null)[]
    | null
    | undefined;
}): ReturnUserTabData => {
  const skipRecoveryAllInformation = tabId !== "new";

  const TabTokenQuery = useTabTokensQuery({
    skip: skipRecoveryAllInformation,
  });

  const data = React.useMemo(
    () => ({
      tokenTab: {
        unassociated: formatQueryToFE({
          informationId: TabTokenQuery.data?.totalTokenTabs?.edges || tabQuery?.tab?.tokenTabs?.edges,
        }),
        isLoading: TabTokenQuery.loading,
        associated: formatQueryToFE({
          informationId: associatedTabQuery,
        }),
      },
    }),
    [tabQuery, TabTokenQuery, associatedTabQuery],
  );

  return { ...data };
};
