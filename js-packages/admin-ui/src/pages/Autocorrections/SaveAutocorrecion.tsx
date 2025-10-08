import { useToast } from "@components/Form/Form/ToastProvider";
import { Box, Button } from "@mui/material";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  BooleanInput,
  ContainerFluid,
  CreateDataEntity,
  CustomSelect,
  fromFieldValidators,
  NumberInput,
  TextInput,
  TitleEntity,
  useForm,
} from "../../components/Form";
import {
  SortType,
  SuggestMode,
  useAutocorrectionValueQuery,
  useSaveAutocorrectionMutation,
} from "../../graphql-generated";
import { useConfirmModal } from "../../utils/useConfirmModal";

export function SaveAutocorrection() {
  const { autocorrectionId = "new", view } = useParams();
  const [page, setPage] = React.useState(0);
  const autocorrectionQuery = useAutocorrectionValueQuery({
    variables: { id: autocorrectionId as string },
    skip: !autocorrectionId || autocorrectionId === "new",
    fetchPolicy: "network-only",
  });
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Autocorrection",
    body: "Are you sure you want to edit this Autocorrection?",
    labelConfirm: "Edit",
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/autocorrection/${autocorrectionId}`);
    }
  };
  const toast = useToast();
  const { AutocorrectionTab } = useTabData({
    autocorrectionId,
    AutocorrectionDocTypeQuery: autocorrectionQuery.data,
    associatedDocTypeQuery: associatedTabQuery.data?.tab?.tokenTabs?.edges,
  });
  const [createOrUpdateTabMutate, createOrUpdateTabMutation] = useSaveAutocorrectionMutation({
    refetchQueries: ["Autocorrection", "Autocorrections"],
    onCompleted(data) {
      try {
        const parentId = data.autocorrection?.entity?.id;

        if (!parentId) {
          throw new Error("Name is invalid");
        }
        if (parentId) {
          toast({
            content: "Autocorrection has been created successfully",
            displayType: "success",
            title: "Autocorrection Created",
          });
          navigate(`/autocorrections`);
        }
      } catch (err: any) {
        console.error("Error during onCompleted processing:", err);
        toast({
          title: `An unexpected error occurred`,
          content: `Impossible to ${err.message} Autocorrection`,
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.error("Mutation error:", error);
      const isNew = autocorrectionId === "new" ? "create" : "update";
      toast({
        title: `Error ${isNew}`,
        content: `Impossible to ${isNew} Autocorrection`,
        displayType: "error",
      });
    },
  });

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        suggestMode: SuggestMode.Always,
        sort: SortType.Score,
        prefixLength: 1,
        minWordLength: 4,
        maxEdit: 2,
        enableSearchWithCorrection: false,
        autocorrectionDocTypeFieldId: undefined,
        tokenTabIds: tokenTab.associated || [],
      }),
      [],
    ),
    originalValues: autocorrectionQuery.data?.autocorrection,
    isLoading: autocorrectionQuery.loading || createOrUpdateTabMutation.loading,
    onSubmit(data) {
      createOrUpdateTabMutate({
        variables: {
          id: autocorrectionId !== "new" ? autocorrectionId : undefined,
          ...data,
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
            nameEntity="Autocorrection"
            description="Create or Edit a Autocorrection and add to it Token Tabs to create yoy personalized search to perform by tab."
            id={autocorrectionId}
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
            id={autocorrectionId}
            pathBack="/autocorrections/"
            setPage={setPage}
            haveConfirmButton={view ? false : true}
            informationSuggestion={[
              {
                content: (
                  <div>
                    <TextInput label="Name" {...form.inputProps("name")} />
                    <NumberInput label="Prefix" {...form.inputProps("prefixLength")} />
                    <NumberInput label="Min Word Length" {...form.inputProps("minWordLength")} />
                    <NumberInput label="Max edits" {...form.inputProps("maxEdit")} />
                    <CustomSelect
                      label={"Sort"}
                      value={form.inputProps("sort").value}
                      disabled={false}
                      validationMessages={[]}
                      dict={SortType}
                      id={"HybridSearch"}
                      onChange={(e: SortType) => form.inputProps("sort").onChange(e)}
                    />
                    <CustomSelect
                      label={"SuggestMode"}
                      value={form.inputProps("suggestMode").value}
                      disabled={false}
                      validationMessages={[]}
                      dict={SuggestMode}
                      id={"HybridSearch"}
                      onChange={(e: SuggestMode) => form.inputProps("suggestMode").onChange(e)}
                    />
                    <BooleanInput label="Max edits" {...form.inputProps("enableSearchWithCorrection")} />
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
