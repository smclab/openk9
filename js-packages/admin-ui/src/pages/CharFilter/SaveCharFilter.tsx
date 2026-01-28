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

export function SaveCharFilter({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { charFilterId = "new", view } = useParams();
  const navigate = useNavigate();
  const toast = useToast();

  const [page, setPage] = React.useState(0);
  const isRecap = page === 1;
  const isNew = charFilterId === "new";

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
                validation: false,
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

