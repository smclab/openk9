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
import { useToast } from "@components/Form/Form/ToastProvider";
import { Box, Button } from "@mui/material";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";
import React from "react";
import { useTranslation } from "react-i18next";
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
import {
  TabQuery,
  useCreateOrUpdateTabMutation,
  useSortingsQuery,
  useTabQuery,
  useTabTokensQuery,
} from "../../graphql-generated";
import { formatQueryToBE, formatQueryToFE } from "../../utils";
import { useConfirmModal } from "../../utils/useConfirmModal";
import { ReturnUserTabData } from "./gql";

export function SaveTab({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { t } = useTranslation();
  const { tabId = "new", view } = useParams();
  const [page, setPage] = React.useState(0);
  const isRecap = page === 1;
  const tabQuery = useTabQuery({
    variables: { id: tabId as string },
    skip: !tabId || tabId === "new",
    fetchPolicy: "network-only",
  });
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: t("pages.tabs.edit-tab"),
    body: t("pages.tabs.are-you-sure-you-want-to-edit"),
    labelConfirm: t("common.edit"),
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/tab/${tabId}`);
    }
  };
  const isNew = tabId === "new";
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
            content: t("pages.tabs.tab-has-been-created-successfully"),
            displayType: "success",
            title: t("pages.tabs.tab-created"),
          });
          navigate(`/tabs`);
        }
      } catch (err: any) {
        console.error("Error during onCompleted processing:", err);
        toast({
          title: t("pages.tabs.unexpected-error"),
          content: t("pages.tabs.impossible-to-action", { action: err.message }),
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.error("Mutation error:", error);
      const isNew = tabId === "new" ? "create" : "update";
      toast({
        title: isNew === "create" ? t("pages.tabs.create-error-title") : t("pages.tabs.update-error-title"),
        content: isNew === "create" ? t("pages.tabs.create-error-content") : t("pages.tabs.update-error-content"),
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

  const { tokenTab, sorting } = useTabData({
    tabId,
    tabQuery: tabTokenTab.data,
    associatedTabQuery: associatedTabQuery.data?.tab?.tokenTabs?.edges,
    associatedSortingQuery: associatedTabQuery.data?.tab?.sortings?.edges,
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        priority: 0,
        tokenTabIds: tokenTab.associated || [],
        sortingIds: sorting.associated || [],
      }),
      [tokenTab, sorting],
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
          sortingIds: formatQueryToBE({
            information: data.sortingIds || [],
          }) as number[],
        },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateTabMutation.data?.tabWithTokenTabs?.fieldValidators),
  });
  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        cell: [
          { key: "name" },
          { key: "description" },
          { key: "priority" },
          { key: "tokenTabIds", label: t("pages.tabs.token-tabs") },
          { key: "sortingIds", label: t("pages.tabs.sortings") },
        ],
        label: t("pages.tabs.recap-label"),
      },
    ],
    valueOverride: {
      tokenTabIds:
        form.inputProps("tokenTabIds").value?.map((tokentab, index) => ({ [index + 1]: tokentab.label })) || [],
      sortingIds: form.inputProps("sortingIds").value?.map((sort, index) => ({ [index + 1]: sort.label })) || [],
    },
  });
  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity={t("pages.tabs.entity-name")}
            description={t("pages.tabs.create-or-edit-a-tab-and-add")}
            id={tabId}
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
            id={tabId}
            pathBack="/tabs/"
            setPage={setPage}
            haveConfirmButton={view ? false : true}
            informationSuggestion={[
              {
                content: (
                  <div>
                    <TextInput label={t("common.name")} {...form.inputProps("name")} />
                    <TextArea label={t("common.description")} {...form.inputProps("description")} />
                    <NumberInput
                      label={t("fields.priority")}
                      {...form.inputProps("priority")}
                      description={t("pages.tabs.define-priority-according-to-which-suggestion-cateogories")}
                    />
                    <TooltipDescription informationDescription={t("pages.tabs.token-tabs-associated-to-current-tab")}>
                      <MultiAssociationCustomQuery
                        list={{
                          ...tokenTab,
                          associated: form.inputProps("tokenTabIds").value,
                        }}
                        createPath={{ path: "/token-tab/new", entity: "token-tabs" }}
                        disabled={page === 1 || view === "view"}
                        isRecap={page === 1}
                        titleAssociation={t("pages.tabs.association-with-token-tabs")}
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
                    <TooltipDescription informationDescription={t("pages.tabs.sortings-associated-to-current-tab")}>
                      <MultiAssociationCustomQuery
                        list={{
                          ...sorting,
                          associated: form.inputProps("sortingIds").value,
                        }}
                        createPath={{ path: "/sorting/new", entity: "sortings" }}
                        disabled={page === 1 || view === "view"}
                        isRecap={page === 1}
                        titleAssociation={t("pages.tabs.association-with-sortings")}
                        onSelect={({ items, isAdd }) => {
                          const data = form.inputProps("sortingIds").value;

                          if (isAdd) {
                            const updatedData = [
                              ...data,
                              ...items.filter((item) => !data.some((d) => d.value === item.value)),
                            ];
                            form.inputProps("sortingIds").onChange(updatedData);
                          } else {
                            const updatedData = data.filter((dat) => !items.some((item) => item.value === dat.value));
                            form.inputProps("sortingIds").onChange(updatedData);
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
      <ConfirmModal />
    </ContainerFluid>
  );
}

const useTabData = ({
  tabId,
  tabQuery,
  associatedTabQuery,
  associatedSortingQuery,
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
  associatedSortingQuery:
  | ({
    __typename?: "DefaultEdge_Sorting";
    node?: {
      __typename?: "Sorting";
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
    fetchPolicy: "cache-and-network",
  });

  const SortingsAllQuery = useSortingsQuery({
    skip: skipRecoveryAllInformation,
    fetchPolicy: "cache-and-network",
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
      sorting: {
        unassociated: formatQueryToFE({
          informationId: SortingsAllQuery.data?.totalSortings?.edges || tabQuery?.tab?.sortings?.edges,
        }),
        isLoading: SortingsAllQuery.loading,
        associated: formatQueryToFE({
          informationId: associatedSortingQuery,
        }),
      },
    }),
    [tabQuery, TabTokenQuery, associatedTabQuery, SortingsAllQuery, associatedSortingQuery],
  );

  return { ...data };
};

