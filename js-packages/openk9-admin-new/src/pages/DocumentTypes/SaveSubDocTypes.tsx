import {
  BooleanInput,
  CodeInput,
  CustomSelect,
  CustomSelectRelationsOneToOne,
  NumberInput,
  TextArea,
  TextInput,
  useForm,
  useToast,
} from "@components/Form";
import React from "react";
import {
  FieldType,
  useCreateOrUpdateDocumentTypeFieldMutation,
  useCreateOrUpdateDocumentTypeSubFieldsMutation,
  useDocumentTypeFieldQuery,
  useUnboundAnalyzersQuery,
} from "../../graphql-generated";
import useOptions from "../../utils/getOptions";
import { DocumentTypeFieldQuery } from "./gql";

export function SaveSubDocType({
  subDocTypesId = "new",
  documentTypeId,
  formRef,
  callback,
  parentId,
  isChild,
}: {
  subDocTypesId: string;
  documentTypeId: string;
  formRef: React.RefObject<HTMLFormElement>;
  callback(): void;
  parentId: string;
  isChild: boolean;
}) {
  const documentTypeFieldQuery = useDocumentTypeFieldQuery({
    variables: { id: subDocTypesId as string },
    skip: !subDocTypesId || subDocTypesId === "new",
  });
  const toast = useToast();
  const { OptionQuery: analyzerOption } = useOptions({
    useQuery: useUnboundAnalyzersQuery,
    queryKeyPath: "analyzers.edges",
    accessKey: "node",
  });

  const [createOrUpdateDocumentTypeFieldMutate] = useCreateOrUpdateDocumentTypeFieldMutation({
    refetchQueries: [DocumentTypeFieldQuery],
    onCompleted(data) {
      if (data.docTypeFieldWithAnalyzer?.entity?.id) {
        toast({
          displayType: "success",
          title: "Document Type Field " + (subDocTypesId === "new" ? "Create" : "Update"),
          content: "",
        });
      } else {
        toast({
          displayType: "error",
          title: "",
          content: "" + JSON.stringify(data),
        });
      }

      callback();
    },
  });
  const [updateSubDoctype] = useCreateOrUpdateDocumentTypeSubFieldsMutation({
    refetchQueries: [DocumentTypeFieldQuery],
    onCompleted(data) {
      if (data.createSubField?.entity?.id) {
        toast({
          displayType: "success",
          title: "Document Type Field " + (subDocTypesId === "new" ? "Create" : "Update"),
          content: "",
        });
      } else {
        toast({
          displayType: "error",
          title: "",
          content: "" + JSON.stringify(data),
        });
      }

      callback();
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        fieldName: "",
        description: "",
        fieldType: FieldType.Text,
        boost: 1,
        searchable: false,
        exclude: false,
        jsonConfig: "{}",
        sortable: false,
        analyzer: { ...documentTypeFieldQuery.data?.docTypeField?.analyzer },
      }),
      [documentTypeFieldQuery],
    ),
    originalValues: documentTypeFieldQuery.data?.docTypeField,
    isLoading: documentTypeFieldQuery.loading || documentTypeFieldQuery.loading,
    onSubmit(data) {
      if (!isChild) {
        createOrUpdateDocumentTypeFieldMutate({
          variables: {
            documentTypeId: documentTypeId,
            documentTypeFieldId: subDocTypesId !== "new" ? subDocTypesId : undefined,
            ...data,
            ...(data.analyzer.id !== "-1" ? { analyzerId: data.analyzer.id } : {}),
          },
        });
      } else {
        updateSubDoctype({
          variables: {
            parentDocTypeFieldId: "" + parentId,
            ...data,
          },
        });
      }
    },
  });

  return (
    <>
      <form
        ref={formRef}
        onSubmit={(event) => {
          event.preventDefault();
          form.submit();
        }}
      >
        <TextInput label="Name" {...form.inputProps("name")} />
        <TextInput
          label="Field Name"
          {...form.inputProps("fieldName")}
          description="Name used to retrive field mapping, composed of Document Type and the name of indexed field"
        />
        <TextArea label="Description" {...form.inputProps("description")} />
        <CustomSelect
          label="Field Type"
          dict={FieldType}
          {...form.inputProps("fieldType")}
          description="Type associated to field. See Elasticsearch documentation for field data types"
        />
        <CustomSelectRelationsOneToOne
          options={analyzerOption}
          label="Analyzer association"
          onChange={(val) => {
            form.inputProps("analyzer").onChange({ id: val.id, name: val.name });
          }}
          value={{
            id: form.inputProps("analyzer").value.id || "-1",
            name: form.inputProps("analyzer").value.name || "",
          }}
          description="Analyzer association for Document Type Field"
        />
        <NumberInput
          label="Boost"
          {...form.inputProps("boost")}
          description="Define how much score is boosted in case of match on this field"
        />

        <BooleanInput
          label="Searchable"
          {...form.inputProps("searchable")}
          description="If field is searchable or not"
        />
        <BooleanInput
          label="Exclude"
          {...form.inputProps("exclude")}
          description="If field need to be excluded from search response or not"
        />
        <BooleanInput label="Sortable" {...form.inputProps("sortable")} description="If field is searchable or not" />
        <CodeInput language="json" label="Configuration" {...form.inputProps("jsonConfig")} />
      </form>
    </>
  );
}
