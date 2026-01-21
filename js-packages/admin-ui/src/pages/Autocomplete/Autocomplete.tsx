import { useToast } from "@components/Form/Form/ToastProvider";
import { Box, Button } from "@mui/material";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  ContainerFluid,
  CreateDataEntity,
  CustomSelect,
  fromFieldValidators,
  MultiAssociationCustomQuery,
  NumberInput,
  TextInput,
  TitleEntity,
  useForm,
} from "../../components/Form";
import {
  BooleanOperator,
  useAutocompleteQuery,
  useCreateOrUpdateAutocompleteMutation,
  useUnboundDocTypeFieldByAutocompleteQuery,
} from "../../graphql-generated";
import { useConfirmModal } from "../../utils/useConfirmModal";

export function SaveAutocomplete() {
  const { autocompletId = "new", view } = useParams();
  const [page, setPage] = React.useState(0);
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
    title: "Edit Autocomplete",
    body: "Are you sure you want to edit this Autocomplete?",
    labelConfirm: "Edit",
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
            content: "Autocomplete has been created successfully",
            displayType: "success",
            title: "Autocomplete Created",
          });
          navigate(`/autocompletes`);
        }
      } catch (err: any) {
        console.error("Error during onCompleted processing:", err);
        toast({
          title: `An unexpected error occurred`,
          content: `Impossible to ${err.message} autocomplete`,
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.error("Mutation error:", error);
      const isNew = autocompletId === "new" ? "create" : "update";
      toast({
        title: `Error ${isNew}`,
        content: `Impossible to ${isNew} Autocomplete`,
        displayType: "error",
      });
    },
  });

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        fuzziness: "",
        minimumShouldMatch: "1",
        name: "",
        operator: "AND" as BooleanOperator,
        resultSize: 10,
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

  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity="Autocomplete"
            description="Create or Edit a Autocomplete and add to it Token Tabs to create yoy personalized search to perform by tab."
            id={autocompletId}
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
            id={autocompletId}
            pathBack="/autocompletes/"
            setPage={setPage}
            haveConfirmButton={view ? false : true}
            informationSuggestion={[
              {
                content: (
                  <div>
                    <TextInput label="Name" {...form.inputProps("name")} />
                    <TextInput label="Fuzziness" {...form.inputProps("fuzziness")} />
                    <TextInput label="Min should Match" {...form.inputProps("minimumShouldMatch")} />
                    <NumberInput label="Result Size" {...form.inputProps("resultSize")} />
                    <CustomSelect
                      label={"Operator"}
                      value={form.inputProps("operator").value}
                      disabled={false}
                      validationMessages={[]}
                      dict={BooleanOperator}
                      id={"HybridSearch"}
                      onChange={(e: BooleanOperator) => form.inputProps("operator").onChange(e)}
                    />
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
    </ContainerFluid>
  );
}
