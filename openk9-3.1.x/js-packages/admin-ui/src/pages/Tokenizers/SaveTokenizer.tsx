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
  useToast,
} from "@components/Form";
import { GenerateDynamicFieldsMemo } from "@components/Form/Form/GenerateDynamicFields";
import useTemplate, { createJsonString } from "@components/Form/Hook/Template";
import { Box, Button } from "@mui/material";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import { useCreateOrUpdateTokenizerMutation, useTokenizerQuery } from "../../graphql-generated";
import { useConfirmModal } from "../../utils/useConfirmModal";
import { TemplateTokenizer } from "./gql";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";

export function SaveTokenizer({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
  const { tokenizerId = "new", view } = useParams();
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Tokenizer",
    body: "Are you sure you want to edit this Tokenizer?",
    labelConfirm: "Edit",
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/tokenizer/${tokenizerId}`);
    }
  };
  const [page, setPage] = React.useState(0);
  const isRecap = page === 1;
  const isNew = tokenizerId === "new";
  const tokenizerQuery = useTokenizerQuery({
    variables: { id: tokenizerId as string },
    skip: !tokenizerId || tokenizerId === "new",
  });

  const toast = useToast();
  const [createOrUpdateTokenizerMutate, createOrUpdateTokenizerMutation] = useCreateOrUpdateTokenizerMutation({
    refetchQueries: ["Tokenizer", "Tokenizers"],
    onCompleted(data) {
      if (data.tokenizer?.entity) {
        const isNew = tokenizerId === "new" ? "created" : "updated";
        toast({
          title: `Tokenizer ${isNew}`,
          content: `Tokenizer has been ${isNew} successfully`,
          displayType: "success",
        });
        navigate(`/tokenizers/`, { replace: true });
      } else {
        toast({
          title: `Error`,
          content: combineErrorMessages(data.tokenizer?.fieldValidators),
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.log(error);
      const isNew = tokenizerId === "new" ? "create" : "update";
      toast({
        title: `Error ${isNew}`,
        content: `Impossible to ${isNew} Tokenizer`,
        displayType: "error",
      });
    },
  });

  const { template, typeSelected, changeType, changeValueKey } = useTemplate({
    templateSelected: TemplateTokenizer,
    jsonConfig: tokenizerQuery.data?.tokenizer?.jsonConfig,
    type: tokenizerQuery.data?.tokenizer?.type,
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
    originalValues: tokenizerQuery.data?.tokenizer,
    isLoading: tokenizerQuery.loading || createOrUpdateTokenizerMutation.loading,
    onSubmit(data) {
      const jsonConfig = createJsonString({ template: template?.value, type: typeSelected });

      createOrUpdateTokenizerMutate({
        variables: {
          id: tokenizerId !== "new" ? tokenizerId : undefined,
          ...data,
          type: typeSelected,
          jsonConfig,
        },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateTokenizerMutation.data?.tokenizer?.fieldValidators),
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
            cell: [
              { key: "name" },
              { key: "description" },
              { key: "type" },
              { key: "jsonConfig", label: "JSON Config", keyNotView: "type" },
            ],
            label: "Recap Tokenizer",
          },
        ],
        valueOverride: {
          type: typeSelected,
          jsonConfig: computedJsonConfig,
        },
      }),
    [form, typeSelected, computedJsonConfig],
  );

  if (tokenizerQuery.loading) {
    return <div></div>;
  }

  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity="Tokenizers"
            description="Create or Edit an Tokenizer to definire a specific token splitting logic to apply to fields. 
            You can choose between pre-built Tokenizers choosing prefer type."
            id={tokenizerId}
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
            id={tokenizerId}
            pathBack="/tokenizers/"
            setPage={setPage}
            haveConfirmButton={view ? false : true}
            informationSuggestion={[
              {
                content: (
                  <>
                    <TextInput label="Name" {...form.inputProps("name")} disabled={isRecap} />
                    <TextArea label="Description" {...form.inputProps("description")} disabled={isRecap} />
                    <GenerateDynamicFieldsMemo
                      templates={TemplateTokenizer}
                      type={typeSelected}
                      template={template}
                      setType={changeType}
                      isRecap={isRecap}
                      changeValueKey={changeValueKey}
                    />{" "}
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

