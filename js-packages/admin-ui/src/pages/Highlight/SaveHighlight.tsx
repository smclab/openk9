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
  CustomSelect,
  fromFieldValidators,
  MultiAssociationCustomQuery,
  NumberInput,
  TextArea,
  TextInput,
  TitleEntity,
  useForm,
  useToast,
} from "@components/Form";
import { InformationField } from "@components/Form/utils/informationField";
import { Box, Button, FormControl, Typography } from "@mui/material";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
  BoundaryScannerType,
  FieldType,
  FragmenterType,
  HighlightType,
  OffsetSourceType,
  OrderType,
  useCreateOrUpdateHighlightMutation,
  useDocTypeFieldsByOffsetSourceQuery,
  useDocTypeFieldsByTypeQuery,
  useHighlightQuery,
} from "../../graphql-generated";

type FieldOption = { value: string; label: string };

const DEFAULT_FRAGMENT_SIZE = 100;
const DEFAULT_NUMBER_OF_FRAGMENTS = 5;
const MIN_FVH_FRAGMENT_SIZE = 18;

// UNIFIED highlighter does not support the CHARS boundary scanner.
const unifiedBoundaryScannerDict = {
  SENTENCE: BoundaryScannerType.Sentence,
  WORD: BoundaryScannerType.Word,
};

export function SaveHighlight({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { highlightId = "new", view } = useParams();
  const navigate = useNavigate();
  const toast = useToast();

  const [page, setPage] = React.useState(0);
  const isRecap = page === 1;
  const isNew = highlightId === "new";
  const disabled = isRecap || view === "view";

  const highlightQuery = useHighlightQuery({
    variables: { id: highlightId },
    skip: isNew,
    fetchPolicy: "network-only",
  });

  // UNIFIED / PLAIN highlight the TEXT fields; FVH highlights the fields indexed
  // with term vectors (offsetSource = TERM_VECTOR).
  const textFieldsQuery = useDocTypeFieldsByTypeQuery({
    variables: { fieldType: FieldType.Text },
  });
  const termVectorFieldsQuery = useDocTypeFieldsByOffsetSourceQuery({
    variables: { offsetSource: OffsetSourceType.TermVector },
  });

  const textFieldOptions: FieldOption[] = React.useMemo(
    () =>
      (textFieldsQuery.data?.docTypeFieldsByType ?? [])
        .filter((field): field is NonNullable<typeof field> => !!field)
        .map((field) => ({ value: String(field.id ?? ""), label: field.name ?? "" })),
    [textFieldsQuery.data],
  );

  const termVectorFieldOptions: FieldOption[] = React.useMemo(
    () =>
      (termVectorFieldsQuery.data?.docTypeFieldsByOffsetSource ?? [])
        .filter((field): field is NonNullable<typeof field> => !!field)
        .map((field) => ({ value: String(field.id ?? ""), label: field.name ?? "" })),
    [termVectorFieldsQuery.data],
  );

  const [mutate, mutation] = useCreateOrUpdateHighlightMutation({
    refetchQueries: ["Highlights", "Highlight"],
    onCompleted(data) {
      if (data.highlight?.entity) {
        const action = isNew ? "created" : "updated";
        toast({
          title: `Highlight ${action}`,
          content: `Highlight has been ${action} successfully`,
          displayType: "success",
        });
        navigate(`/highlights/`, { replace: true });
      } else {
        toast({
          title: "Error",
          content: combineErrorMessages(data.highlight?.fieldValidators),
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.error(error);
      const action = isNew ? "create" : "update";
      toast({
        title: `Error ${action}`,
        content: `Impossible to ${action} Highlight`,
        displayType: "error",
      });
    },
  });

  const highlight = highlightQuery.data?.highlight;

  const form = useForm({
    // Memoized: the fields association reads its list from here, so a new
    // identity on every render would re-seed the association lists.
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        type: HighlightType.Unified,
        fields: [] as FieldOption[],
        boundaryScanner: BoundaryScannerType.Sentence,
        boundaryChars: "",
        fragmenter: FragmenterType.Span,
        fragmentSize: DEFAULT_FRAGMENT_SIZE,
        numberOfFragments: DEFAULT_NUMBER_OF_FRAGMENTS,
        order: OrderType.None,
      }),
      [],
    ),
    originalValues: React.useMemo(
      () =>
        highlight
          ? {
              ...highlight,
              fields: (highlight.fields ?? [])
                .filter((field): field is NonNullable<typeof field> => !!field)
                .map((field) => ({ value: String(field.id ?? ""), label: field.name ?? "" })),
            }
          : undefined,
      [highlight],
    ),
    isLoading: highlightQuery.loading || mutation.loading,
    onSubmit(data) {
      const type = data.type as HighlightType;
      const isFvhType = type === HighlightType.Fvh;
      const isPlainType = type === HighlightType.Plain;
      mutate({
        variables: {
          id: !isNew ? highlightId : undefined,
          name: data.name || "",
          description: data.description || null,
          type,
          fieldIds: data.fields.map((field) => Number(field.value)),
          boundaryScanner: isPlainType ? null : (data.boundaryScanner as BoundaryScannerType),
          boundaryChars: isFvhType ? data.boundaryChars || null : null,
          fragmenter: isPlainType ? (data.fragmenter as FragmenterType) : null,
          fragmentSize: data.fragmentSize,
          numberOfFragments: data.numberOfFragments,
          order: data.order as OrderType,
        },
      });
    },
    getValidationMessages: fromFieldValidators(mutation.data?.highlight?.fieldValidators),
  });

  const typeValue = form.inputProps("type").value as HighlightType;
  const isFvh = typeValue === HighlightType.Fvh;
  const isPlain = typeValue === HighlightType.Plain;
  const showBoundaryScanner = !isPlain;
  const showBoundaryChars = isFvh;
  const showFragmenter = isPlain;
  const fieldOptions = isFvh ? termVectorFieldOptions : textFieldOptions;
  const fieldsLoading = isFvh ? termVectorFieldsQuery.loading : textFieldsQuery.loading;
  const associatedFields: FieldOption[] = form.inputProps("fields").value ?? [];

  const unassociatedFields = React.useMemo(
    () => fieldOptions.filter((option) => !associatedFields.some((field) => field.value === option.value)),
    [fieldOptions, associatedFields],
  );

  const handleFieldsSelect = ({ items, isAdd }: { items: FieldOption[]; isAdd: boolean }) => {
    const currentFields = form.inputProps("fields").value;
    const updatedFields = isAdd
      ? [...currentFields, ...items.filter((item) => !currentFields.some((field) => field.value === item.value))]
      : currentFields.filter((field) => !items.some((item) => item.value === field.value));
    form.inputProps("fields").onChange(updatedFields);
  };

  const recapSections = React.useMemo(
    () =>
      mappingCardRecap({
        form: form as any,
        sections: [
          {
            label: "Recap Highlight",
            cell: [
              { key: "name" },
              { key: "description" },
              { key: "type" },
              { key: "fields", label: "Fields" },
              ...(showBoundaryScanner ? [{ key: "boundaryScanner", label: "Boundary Scanner" }] : []),
              ...(showBoundaryChars ? [{ key: "boundaryChars", label: "Boundary Chars" }] : []),
              ...(showFragmenter ? [{ key: "fragmenter", label: "Fragmenter" }] : []),
              { key: "fragmentSize", label: "Fragment Size" },
              { key: "numberOfFragments", label: "Number Of Fragments" },
              { key: "order", label: "Order" },
            ],
          },
        ],
        valueOverride: {
          fields: form.inputProps("fields").value?.map((field: FieldOption) => field.label).join(", ") || "",
        },
      }),
    [form, showBoundaryScanner, showBoundaryChars, showFragmenter],
  );

  if (highlightQuery.loading) return null;

  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity="Highlight"
            description="Create or edit a Highlight to configure how search results are highlighted. Choose a highlighter type and associate the document type fields to highlight."
            id={highlightId}
          />
          {view === "view" && (
            <Button
              variant="contained"
              onClick={() => navigate(`/highlight/${highlightId}`)}
              sx={{ height: "fit-content" }}
            >
              Edit
            </Button>
          )}
        </Box>

        <form>
          <CreateDataEntity
            form={form}
            page={page}
            setPage={setPage}
            id={highlightId}
            pathBack="/highlights/"
            haveConfirmButton={!view}
            informationSuggestion={[
              {
                page: 0,
                validation: false,
                content: (
                  <>
                    <TextInput label="Name" {...form.inputProps("name")} disabled={disabled} />
                    <TextArea label="Description" {...form.inputProps("description")} disabled={disabled} />
                    <CustomSelect label="Type" dict={HighlightType} {...form.inputProps("type")} disabled={disabled} />
                    <FormControl fullWidth sx={{ marginBottom: 2 }}>
                      <Box
                        sx={{ marginBottom: 1 }}
                        display="flex"
                        flexDirection="row"
                        alignItems="center"
                        gap="4px"
                      >
                        <Typography variant="subtitle1" component="label">
                          Fields
                        </Typography>
                        <InformationField description="Document type fields highlighted by current highlight" />
                      </Box>
                      <MultiAssociationCustomQuery
                        list={{
                          unassociated: unassociatedFields,
                          associated: associatedFields,
                          isLoading: fieldsLoading,
                        }}
                        isLoading={fieldsLoading}
                        disabled={disabled}
                        isRecap={isRecap}
                        onSelect={handleFieldsSelect}
                      />
                    </FormControl>
                    {showBoundaryScanner && (
                      <CustomSelect
                        label="Boundary Scanner"
                        dict={isFvh ? BoundaryScannerType : unifiedBoundaryScannerDict}
                        {...form.inputProps("boundaryScanner")}
                        disabled={disabled}
                      />
                    )}
                    {showBoundaryChars && (
                      <TextInput
                        label="Boundary Chars"
                        {...form.inputProps("boundaryChars")}
                        disabled={disabled}
                      />
                    )}
                    {showFragmenter && (
                      <CustomSelect
                        label="Fragmenter"
                        dict={FragmenterType}
                        {...form.inputProps("fragmenter")}
                        disabled={disabled}
                      />
                    )}
                    <NumberInput
                      label="Fragment Size"
                      {...form.inputProps("fragmentSize")}
                      description={isFvh ? `For the FVH highlighter the fragment size must be at least ${MIN_FVH_FRAGMENT_SIZE}.` : undefined}
                      disabled={disabled}
                    />
                    <NumberInput
                      label="Number Of Fragments"
                      {...form.inputProps("numberOfFragments")}
                      disabled={disabled}
                    />
                    <CustomSelect label="Order" dict={OrderType} {...form.inputProps("order")} disabled={disabled} />
                  </>
                ),
              },
              { validation: true },
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
            submitLabel: isNew ? "Create entity" : "Update entity",
            backLabel: "Back",
          }}
        />
      </>
    </ContainerFluid>
  );
}
