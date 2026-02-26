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
  fromFieldValidators,
  TextArea,
  TextInput,
  TitleEntity,
  useForm,
} from "@components/Form";
import { GenerateDynamicFieldsMemo } from "@components/Form/Form/GenerateDynamicFields";
import { useToast } from "@components/Form/Form/ToastProvider";
import useTemplate, { createJsonString } from "@components/Form/Hook/Template";
import { Box, Button } from "@mui/material";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useCreateOrUpdateTokenFilterMutation, useTokenFilterQuery } from "../../graphql-generated";
import { useConfirmModal } from "../../utils/useConfirmModal";
import { Filters } from "./gql";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";

export function SaveTokenFilter({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { tokenFilterId = "new", view } = useParams();
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Token Filter",
    body: "Are you sure you want to edit this Token Filter?",
    labelConfirm: "Edit",
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/token-filter/${tokenFilterId}`);
    }
  };

  const [page, setPage] = React.useState(0);
  const isRecap = page === 1;
  const isNew = tokenFilterId === "new";
  const tokenFilterQuery = useTokenFilterQuery({
    variables: { id: tokenFilterId as string },
    skip: !tokenFilterId || tokenFilterId === "new",
  });

  const toast = useToast();
  const [createOrUpdateTokenFilterMutate, createOrUpdateTokenFilterMutation] = useCreateOrUpdateTokenFilterMutation({
    refetchQueries: ["TokenFilter", "TokenFilters"],
    onCompleted(data) {
      if (data.tokenFilter?.entity) {
        const isNew = tokenFilterId === "new" ? "created" : "updated";
        toast({
          title: `Token Filter ${isNew}`,
          content: `Token Filter has been ${isNew} successfully`,
          displayType: "success",
        });
        navigate(`/token-filters/`, { replace: true });
      } else {
        toast({
          title: `Error`,
          content: combineErrorMessages(data.tokenFilter?.fieldValidators),
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.log(error);
      const isNew = tokenFilterId === "new" ? "create" : "update";
      toast({
        title: `Error ${isNew}`,
        content: `Impossible to ${isNew} Token Filter`,
        displayType: "error",
      });
    },
  });
  const { template, typeSelected, changeType, changeValueKey } = useTemplate({
    templateSelected: Filters,
    jsonConfig: tokenFilterQuery.data?.tokenFilter?.jsonConfig,
    type: tokenFilterQuery.data?.tokenFilter?.type,
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        jsonConfig: "",
        type: "",
      }),
      [],
    ),
    originalValues: tokenFilterQuery.data?.tokenFilter,
    isLoading: tokenFilterQuery.loading || createOrUpdateTokenFilterMutation.loading,
    onSubmit(data) {
      const jsonConfig = createJsonString({ template: template?.value, type: typeSelected });
      createOrUpdateTokenFilterMutate({
        variables: {
          id: tokenFilterId !== "new" ? tokenFilterId : undefined,
          ...data,
          type: typeSelected,
          jsonConfig,
        },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateTokenFilterMutation.data?.tokenFilter?.fieldValidators),
  });

  const computedJsonConfig = React.useMemo(
    () =>
      createJsonString({
        template: template?.value,
        type: typeSelected,
      }),
    [template, typeSelected],
  );

  const recapSections = React.useMemo(
    () =>
      mappingCardRecap({
        form: form as any,
        sections: [
          {
            label: "Recap Char Filter",
            cell: [
              { key: "name" },
              { key: "description" },
              { key: "type" },
              { key: "jsonConfig", label: "JSON Config", keyNotView: "type" },
            ],
          },
        ],
        valueOverride: {
          type: typeSelected,
          jsonConfig: computedJsonConfig,
        },
      }),
    [form, typeSelected, computedJsonConfig],
  );

  if (tokenFilterQuery.loading) {
    return <div></div>;
  }

  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity="Token Filter"
            description="Create or Edit an Token Filter to definire a specific token analysis logic to apply to fields. 
            You can choose between pre-built Token Filters choosing prefer type."
            id={tokenFilterId}
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
            id={tokenFilterId}
            pathBack="/token-filters/"
            setPage={setPage}
            haveConfirmButton={view ? false : true}
            informationSuggestion={[
              {
                content: (
                  <>
                    <TextInput label="Name" {...form.inputProps("name")} disabled={isRecap} />
                    <TextArea label="Description" {...form.inputProps("description")} disabled={isRecap} />
                    <GenerateDynamicFieldsMemo
                      templates={Filters}
                      type={typeSelected}
                      template={template}
                      setType={changeType}
                      isRecap={isRecap}
                      changeValueKey={changeValueKey}
                    />
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
      </>
      <ConfirmModal />
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
    </ContainerFluid>
  );
}

