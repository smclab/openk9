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
import React from "react";
import { useTranslation } from "react-i18next";
import { useNavigate, useParams } from "react-router-dom";
import {
  BooleanInput,
  ContainerFluid,
  CreateDataEntity,
  CustomSelect,
  fromFieldValidators,
  MultiAssociationCustomQuery,
  NumberInput,
  TextInput,
  TitleEntity,
  TooltipDescription,
  useForm,
} from "../../components/Form";
import {
  BooleanOperator,
  Fuzziness,
  useAutocompleteQuery,
  useCreateOrUpdateAutocompleteMutation,
  useUnboundDocTypeFieldByAutocompleteQuery,
} from "../../graphql-generated";
import { useConfirmModal } from "../../utils/useConfirmModal";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";

export function SaveAutocomplete({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { t } = useTranslation();
  const { autocompletId = "new", view } = useParams();
  const [page, setPage] = React.useState(0);
  const isRecap = page === 1;
  const autocompleteQuery = useAutocompleteQuery({
    variables: { id: autocompletId as string },
    skip: !autocompletId || autocompletId === "new",
    fetchPolicy: "network-only",
  });

  const associationsQuery = useUnboundDocTypeFieldByAutocompleteQuery({
    variables: { autocompleteId: autocompletId !== "new" ? Number(autocompletId) : 0 },
    fetchPolicy: "network-only",
  });

  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: t("pages.autocompletes.edit-autocomplete"),
    body: t("pages.autocompletes.are-you-sure-you-want-to-edit"),
    labelConfirm: t("common.edit"),
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/autocomplete/${autocompletId}`);
    }
  };
  const toast = useToast();

  const [localUnassociated, setLocalUnassociated] = React.useState<Array<{ value: string; label: string }>>([]);

  const serverUnassociated = React.useMemo(
    () =>
      (associationsQuery?.data?.unboundDocTypeFieldByAutocomplete || [])
        .map((f) => ({ value: String(f?.id ?? ""), label: f?.name ?? "" }))
        .filter((x) => x.value),
    [associationsQuery.data],
  );

  const [createOrUpdateAutocompleteMutate, createOrUpdateAutocompleteMutation] = useCreateOrUpdateAutocompleteMutation({
    refetchQueries: ["autocomplete", "autocompletes"],
    onCompleted(data) {
      try {
        const parentId = data.autocomplete?.entity?.id;

        if (!parentId) {
          throw new Error("Name is invalid");
        }
        if (parentId) {
          toast({
            content: t("pages.autocompletes.created-content"),
            displayType: "success",
            title: t("pages.autocompletes.created-title"),
          });
          navigate(`/autocompletes`);
        }
      } catch (err: any) {
        console.error("Error during onCompleted processing:", err);
        toast({
          title: t("pages.autocompletes.unexpected-error"),
          content: t("pages.autocompletes.impossible-to-action", { action: err.message }),
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.error("Mutation error:", error);
      const isNew = autocompletId === "new";
      toast({
        title: isNew ? t("pages.autocompletes.create-error-title") : t("pages.autocompletes.update-error-title"),
        content: isNew ? t("pages.autocompletes.create-error-content") : t("pages.autocompletes.update-error-content"),
        displayType: "error",
      });
    },
  });

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        fuzziness: Fuzziness.Auto,
        minimumShouldMatch: "1",
        name: "",
        operator: BooleanOperator.And,
        resultSize: 10,
        perfectMatchIncluded: false,
        fieldIds:
          autocompleteQuery?.data?.autocomplete?.fields?.edges?.flatMap((field) => ({
            id: Number(field?.node?.id),
            label: field?.node?.name,
          })) || [],
      }),
      [autocompleteQuery.data],
    ),
    originalValues: autocompleteQuery?.data?.autocomplete,
    isLoading: autocompleteQuery.loading || createOrUpdateAutocompleteMutation.loading,
    onSubmit(data) {
      createOrUpdateAutocompleteMutate({
        variables: {
          id: autocompletId !== "new" ? autocompletId : undefined,
          autocompleteDTO: {
            ...data,
            fieldIds: (data.fieldIds || []).map((x: any) => Number(x?.id)).filter((id: number) => !Number.isNaN(id)),
          },
        },
      });
    },
    getValidationMessages: fromFieldValidators([]),
  });

  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        cell: [
          { key: "name" },
          { key: "fuzziness" },
          { key: "minimumShouldMatch", label: t("pages.autocompletes.min-should-match") },
          { key: "resultSize", label: t("pages.autocompletes.result-size") },
          { key: "operator" },
          { key: "perfectMatchIncluded", label: t("pages.autocompletes.perfect-match-included") },
          { key: "fieldIds", label: t("fields.fields") },
        ],
        label: t("pages.autocompletes.recap-label"),
      },
    ],
    valueOverride: {
      fieldIds: form.inputProps("fieldIds").value?.map((field, index) => ({ [index + 1]: field.label })) || [],
    },
  });

  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity={t("pages.autocompletes.entity-name")}
            description={t("pages.autocompletes.create-or-edit")}
            id={autocompletId}
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
            id={autocompletId}
            pathBack="/autocompletes/"
            setPage={setPage}
            haveConfirmButton={view ? false : true}
            informationSuggestion={[
              {
                content: (
                  <div>
                    <TextInput
                      label={t("common.name")}
                      {...form.inputProps("name")}
                      description={t("pages.autocompletes.name-description")}
                    />
                    <CustomSelect
                      label={t("pages.autocompletes.fuzziness")}
                      dict={Fuzziness}
                      {...form.inputProps("fuzziness")}
                      description={t("pages.autocompletes.fuzziness-description")}
                    />
                    <TextInput
                      label={t("pages.autocompletes.min-should-match")}
                      {...form.inputProps("minimumShouldMatch")}
                      description={t("pages.autocompletes.min-should-match-description")}
                    />
                    <NumberInput
                      label={t("pages.autocompletes.result-size")}
                      {...form.inputProps("resultSize")}
                      description={t("pages.autocompletes.result-size-description")}
                    />
                    <CustomSelect
                      label={t("pages.autocompletes.operator")}
                      value={form.inputProps("operator").value}
                      disabled={false}
                      validationMessages={[]}
                      dict={BooleanOperator}
                      id={"HybridSearch"}
                      onChange={(e: BooleanOperator) => form.inputProps("operator").onChange(e)}
                      description={t("pages.autocompletes.operator-description")}
                    />
                    <Box paddingBlock={2}>
                      <BooleanInput
                        label={t("pages.autocompletes.perfect-match-included")}
                        {...form.inputProps("perfectMatchIncluded")}
                        description={t("pages.autocompletes.perfect-match-included-description")}
                      />
                    </Box>
                    <Box display="flex" flexDirection="row" alignItems="center" gap="4px">
                      <Box component="label" sx={{ fontWeight: 600 }}>
                        {t("fields.fields")}
                      </Box>
                      <TooltipDescription informationDescription={t("pages.autocompletes.fields-description")} />
                    </Box>
                    <MultiAssociationCustomQuery
                      {...form.inputProps("fieldIds")}
                      list={{
                        associated: (() => {
                          const raw = form.inputProps("fieldIds").value || [];
                          return (Array.isArray(raw) ? raw : [])
                            .map((x: any) => {
                              const id = Number(x?.id);
                              if (Number.isNaN(id)) return null;
                              const label =
                                (typeof x?.label === "string" && x.label) ||
                                autocompleteQuery?.data?.autocomplete?.fields?.edges?.find(
                                  (e) => Number(e?.node?.id) === id,
                                )?.node?.name ||
                                associationsQuery?.data?.unboundDocTypeFieldByAutocomplete?.find(
                                  (f) => Number(f?.id) === id,
                                )?.name ||
                                "";
                              return { value: String(id), label };
                            })
                            .filter((x: any): x is { value: string; label: any } => x !== null);
                        })(),
                        unassociated: (() => {
                          const raw = form.inputProps("fieldIds").value || [];
                          const associatedIds = new Set(
                            (Array.isArray(raw) ? raw : [])
                              .map((x: any) => Number(x?.id))
                              .filter((id: number) => !Number.isNaN(id)),
                          );

                          const merged = [...serverUnassociated, ...localUnassociated];
                          const byId = new Map<string, { value: string; label: string }>();
                          merged.forEach((x) => {
                            if (x.value) byId.set(x.value, x);
                          });

                          return Array.from(byId.values()).filter((x) => !associatedIds.has(Number(x.value)));
                        })(),
                        isLoading: associationsQuery.loading,
                      }}
                      disabled={false}
                      isRecap={false}
                      onSelect={({ items, isAdd }) => {
                        const currentRaw = form.inputProps("fieldIds").value || [];
                        const current = (Array.isArray(currentRaw) ? currentRaw : [])
                          .map((x: any) => {
                            const id = Number(x?.id);
                            if (Number.isNaN(id)) return null;
                            const label = typeof x?.label === "string" ? x.label : "";
                            return { id, label };
                          })
                          .filter((x: any) => x);

                        const incoming = (items || [])
                          .map((it) => {
                            const id = Number(it?.value);
                            if (Number.isNaN(id)) return null;
                            return { id, label: it?.label ?? "" };
                          })
                          .filter((x: any) => x);

                        if (isAdd) {
                          setLocalUnassociated((prev) =>
                            prev.filter((x) => !incoming.some((inc: any) => String(inc.id) === x.value)),
                          );

                          const existing = new Set(current.map((x: any) => x.id));
                          const next = [
                            ...current,
                            ...incoming
                              .filter((x: any) => !existing.has(x.id))
                              .map((x: any) => ({
                                id: x.id,
                                label:
                                  x.label ||
                                  autocompleteQuery?.data?.autocomplete?.fields?.edges?.find(
                                    (e) => Number(e?.node?.id) === x.id,
                                  )?.node?.name ||
                                  associationsQuery?.data?.unboundDocTypeFieldByAutocomplete?.find(
                                    (f) => Number(f?.id) === x.id,
                                  )?.name ||
                                  "",
                              })),
                          ];
                          form.inputProps("fieldIds").onChange(next as any);
                          return;
                        }

                        const removeIds = new Set(incoming.map((x: any) => x.id));
                        const next = current.filter((x: any) => !removeIds.has(x.id));
                        form.inputProps("fieldIds").onChange(next as any);

                        setLocalUnassociated((prev) => {
                          const byId = new Map<string, { value: string; label: string }>();
                          [
                            ...prev,
                            ...incoming.map((x: any) => ({ value: String(x.id), label: x.label || "" })),
                          ].forEach((x) => {
                            if (x.value) byId.set(x.value, x);
                          });
                          return Array.from(byId.values());
                        });
                      }}
                    />
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
      </>
      <ConfirmModal />
      <Recap
        recapData={recapSections}
        setExtraFab={setExtraFab}
        forceFullScreen={isRecap}
        actions={{
          onBack: () => setPage(0),
          onSubmit: () => form.submit(),
          submitLabel: autocompletId === "new" ? "Create entity" : "Update entity",
          backLabel: "Back",
        }}
      />
    </ContainerFluid>
  );
}
