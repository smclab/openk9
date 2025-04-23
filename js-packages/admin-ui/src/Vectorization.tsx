import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import { useToast } from "./components/ToastProvider";
import { ChunkType, useCreateOrUpdateVectorIndexMutation, useVectorIndexQuery } from "./graphql-generated";
import {
  ContainerFluid,
  CustomButtom,
  EnumSelect,
  fromFieldValidators,
  MainTitle,
  NumberInput,
  TextArea,
  TextInput,
  useForm,
} from "./components/Form";
import { CodeInput } from "./components/CodeInput";

export const VectorIndexQuery = gql`
  query VectorIndex($id: ID!) {
    vectorIndex(id: $id) {
      id
      name
      description
      name
      chunkType
      chunkWindowSize
      jsonConfig
      textEmbeddingField
      titleField
      urlField
    }
  }
`;

gql`
  mutation CreateOrUpdateVectorIndex($id: ID, $name: String!, $description: String, $configurations: ConfigurationsDTOInput!) {
    vectorIndex(id: $id, vectorIndexDTO: { name: $name, description: $description, configurations: $configurations }) {
      entity {
        id
        name
      }
      fieldValidators {
        field
        message
      }
    }
  }
`;

export function VectorIndex() {
  const { vectorIndexId = "new" } = useParams();
  const navigate = useNavigate();
  const showToast = useToast();
  const vectorIndexQuery = useVectorIndexQuery({
    variables: { id: vectorIndexId as string },
    skip: !vectorIndexId || vectorIndexId === "new",
  });
  const [createOrUpdateVectorIndexMutate, createOrUpdateVectorIndexMutation] = useCreateOrUpdateVectorIndexMutation({
    refetchQueries: [VectorIndexQuery],
    onCompleted(data) {
      if (data.vectorIndex?.entity) {
        if (vectorIndexId === "new") {
          showToast({ displayType: "success", title: "Vector Index created", content: data.vectorIndex.entity.name ?? "" });
        } else {
          showToast({ displayType: "info", title: "Vector Index updated", content: data.vectorIndex.entity.name ?? "" });
        }
        navigate(`/vector-indices/`);
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        chunkType: ChunkType.CharacterTextSplitter,
        textEmbeddingField: "",
        titleField: "",
        urlField: "",
        chunkWindowSize: 0,
        jsonConfig: "{}",
      }),
      []
    ),
    originalValues: vectorIndexQuery.data?.vectorIndex,
    isLoading: vectorIndexQuery.loading || createOrUpdateVectorIndexMutation.loading,
    onSubmit(data) {
      createOrUpdateVectorIndexMutate({
        variables: {
          id: vectorIndexId !== "new" ? vectorIndexId : undefined,
          ...data,
          configurations: {
            jsonConfig: data.jsonConfig,
            textEmbeddingField: data.textEmbeddingField,
            titleField: data.titleField,
            chunkType: data.chunkType,
            urlField: data.urlField,
          },
        },
      });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateVectorIndexMutation.data?.vectorIndex?.fieldValidators),
  });
  console.log(vectorIndexQuery.data);

  return (
    <ContainerFluid>
      {vectorIndexId !== "new" && <MainTitle title="Vector Index Details" />}
      <form
        className="sheet"
        onSubmit={(event) => {
          event.preventDefault();
          form.submit();
        }}
      >
        <TextInput label="Name" {...form.inputProps("name")} />
        <TextArea label="Description" {...form.inputProps("description")} />
        <EnumSelect
          label="Chunk Type"
          {...form.inputProps("chunkType")}
          dict={ChunkType}
          description="Select the type of chunking to use."
        />
        <TextInput label="Text Embedding Field" {...form.inputProps("textEmbeddingField")} />
        <TextInput label="Title Field" {...form.inputProps("titleField")} />
        <TextInput label="URL Field" {...form.inputProps("urlField")} />
        <NumberInput
          label="Chunk Window Size"
          {...form.inputProps("chunkWindowSize")}
          description="The size of the chunk window."
          disabled
        />
        <CodeInput
          label="JSON Config"
          {...form.inputProps("jsonConfig")}
          description="The JSON configuration for the vector index."
          language="json"
        />
        <div className="sheet-footer">
          <CustomButtom nameButton={vectorIndexId === "new" ? "Create" : "Update"} canSubmit={!form.canSubmit} typeSelectet="submit" />
        </div>
      </form>
    </ContainerFluid>
  );
}
