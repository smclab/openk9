import { Box, Button } from "@mui/material";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";

import {
  combineErrorMessages,
  ContainerFluid,
  CreateDataEntity,
  fromFieldValidators,
  TextArea,
  TextInput,
  TitleEntity,
  useForm,
  useToast,
} from "@components/Form";

import { GenerateDynamicFieldsMemo } from "@components/Form/Form/GenerateDynamicFields";
import useTemplate, { createJsonString } from "@components/Form/Hook/Template";

import { useCharFilterQuery, useCreateOrUpdateCharFilterMutation } from "../../graphql-generated";

import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";
import { CharFilters } from "./gql";

export function SaveCharFilter() {
  const { charFilterId = "new", view } = useParams();
  const navigate = useNavigate();
  const toast = useToast();

  const [page, setPage] = React.useState(0);
  const isRecap = page === 1;

  const charFilterQuery = useCharFilterQuery({
    variables: { id: charFilterId },
    skip: charFilterId === "new",
  });

  const { template, typeSelected, changeType, changeValueKey } = useTemplate({
    templateSelected: CharFilters,
    jsonConfig: charFilterQuery.data?.charFilter?.jsonConfig,
    type: charFilterQuery.data?.charFilter?.type,
  });

  const computedJsonConfig = React.useMemo(
    () =>
      createJsonString({
        template: template?.value,
        type: typeSelected,
      }),
    [template, typeSelected],
  );

  const [mutate, mutation] = useCreateOrUpdateCharFilterMutation({
    refetchQueries: ["CharFilter", "Charfilters"],
    onCompleted(data) {
      if (data.charFilter?.entity) {
        toast({
          title: "Char Filter saved",
          content: "Char Filter saved successfully",
          displayType: "success",
        });
        navigate(`/char-filters/`, { replace: true });
      } else {
        toast({
          title: "Error",
          content: combineErrorMessages(data.charFilter?.fieldValidators),
          displayType: "error",
        });
      }
    },
  });

  const form = useForm({
    initialValues: {
      name: "",
      description: "",
      jsonConfig: "{}",
      type: "",
    },
    originalValues: charFilterQuery.data?.charFilter,
    isLoading: mutation.loading,
    onSubmit(data) {
      mutate({
        variables: {
          id: charFilterId !== "new" ? charFilterId : undefined,
          ...data,
          type: typeSelected,
          jsonConfig: computedJsonConfig,
        },
      });
    },
    getValidationMessages: fromFieldValidators(mutation.data?.charFilter?.fieldValidators),
  });

  const recapSections = React.useMemo(
    () =>
      mappingCardRecap({
        form: form as any,
        sections: [
          {
            label: "Recap Char Filter",
            cell: [{ key: "name" }, { key: "description" }, { key: "type" }, { key: "jsonConfig" }],
          },
        ],
        valueOverride: {
          type: typeSelected,
          jsonConfig: computedJsonConfig,
        },
      }),
    [form, typeSelected, computedJsonConfig],
  );

  if (charFilterQuery.loading) return null;

  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", mb: 2 }}>
          <TitleEntity nameEntity="Char Filter" description="Create or edit a Char Filter" id={charFilterId} />

          {view === "view" && (
            <Button variant="contained" onClick={() => navigate(`/char-filter/${charFilterId}`)}>
              Edit
            </Button>
          )}
        </Box>

        <form>
          <CreateDataEntity
            form={form}
            page={page}
            setPage={setPage}
            id={charFilterId}
            pathBack="/char-filters/"
            haveConfirmButton={!view}
            informationSuggestion={[
              {
                page: 0,
                validation: true,
                content: (
                  <>
                    <TextInput label="Name" {...form.inputProps("name")} />
                    <TextArea label="Description" {...form.inputProps("description")} />

                    <GenerateDynamicFieldsMemo
                      templates={CharFilters}
                      type={typeSelected}
                      template={template}
                      setType={changeType}
                      isRecap={isRecap}
                      changeValueKey={changeValueKey}
                    />
                  </>
                ),
              },
              { validation: true },
            ]}
            fieldsControll={["name"]}
          />
        </form>

        <Recap recapData={recapSections} />
      </>
    </ContainerFluid>
  );
}
