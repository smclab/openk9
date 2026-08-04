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
import { useTranslation } from "react-i18next";
import {
  FieldType,
  OffsetSourceType,
  useCreateOrUpdateDocumentTypeFieldMutation,
  useCreateOrUpdateDocumentTypeSubFieldsMutation,
  useDocumentTypeFieldQuery,
  useUnboundAnalyzersQuery,
} from "../../graphql-generated";
import useOptions from "../../utils/getOptions";

export function SaveSubDocType({
  subDocTypesId = "new",
  documentTypeId,
  formRef,
  callback,
  parentId,
  isChild,
  setExtraFab,
}: {
  subDocTypesId: string;
  documentTypeId: string;
  formRef: React.RefObject<HTMLFormElement | null>;
  callback(): void;
  parentId: string;
  isChild: boolean;
  setExtraFab: (fab: React.ReactNode | null) => void;
}) {
  const { t } = useTranslation();
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
    refetchQueries: ["DocumentTypeField", "DocTypeFields"],
    onCompleted(data) {
      if (data.docTypeFieldWithAnalyzer?.entity?.id) {
        toast({
          displayType: "success",
          title: t("pages.document-types.document-type-field") + (subDocTypesId === "new" ? "Create" : "Update"),
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
    refetchQueries: ["DocumentTypeField", "DocTypeFields"],
    onCompleted(data) {
      if (data.createSubField?.entity?.id) {
        toast({
          displayType: "success",
          title: t("pages.document-types.document-type-field") + (subDocTypesId === "new" ? "Create" : "Update"),
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
        offsetSource: OffsetSourceType.None,
        boost: 1,
        searchable: false,
        exclude: false,
        jsonConfig: "{}",
        sortable: false,
        analyzer: { ...documentTypeFieldQuery.data?.docTypeField?.analyzer },
        searchAnalyzer: { ...documentTypeFieldQuery.data?.docTypeField?.searchAnalyzer },
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
            docTypeFieldName: subDocTypesId !== "new" ? documentTypeFieldQuery.data?.docTypeField?.name : undefined,
            ...data,
            offsetSource: data.fieldType === FieldType.Text ? data.offsetSource : OffsetSourceType.None,
            ...(data.analyzer.id !== "-1" ? { analyzerId: data.analyzer.id } : {}),
            ...(data.searchAnalyzer.id && data.searchAnalyzer.id !== "-1"
              ? { searchAnalyzerId: data.searchAnalyzer.id }
              : {}),
          },
        });
      } else {
        updateSubDoctype({
          variables: {
            parentDocTypeFieldId: "" + parentId,
            ...data,
            offsetSource: data.fieldType === FieldType.Text ? data.offsetSource : OffsetSourceType.None,
          },
        });
      }
    },
  });

  return (
    <>
      <form
        ref={formRef as React.RefObject<HTMLFormElement>}
        onSubmit={(event) => {
          event.preventDefault();
          form.submit();
        }}
      >
        <TextInput label={t("common.name")} {...form.inputProps("name")} />
        <TextInput
          label={t("fields.field-name")}
          {...form.inputProps("fieldName")}
          description={t("pages.document-types.name-used-to-retrive-field-mapping-composed")}
        />
        <TextArea label={t("common.description")} {...form.inputProps("description")} />
        <CustomSelect
          label={t("fields.field-type")}
          dict={FieldType}
          {...form.inputProps("fieldType")}
          description={t("pages.document-types.type-associated-to-field-see-opensearch-documentation")}
        />
        {form.inputProps("fieldType").value === FieldType.Text && (
          <CustomSelect
            label={t("fields.offset-source")}
            dict={OffsetSourceType}
            {...form.inputProps("offsetSource")}
            description={t("pages.document-types.source-used-to-load-offsets-for-highlighting")}
          />
        )}
        <CustomSelectRelationsOneToOne
          options={analyzerOption}
          label={t("fields.analyzer-association")}
          onChange={(val) => {
            form.inputProps("analyzer").onChange({ id: val.id, name: val.name });
          }}
          value={{
            id: form.inputProps("analyzer").value.id || "-1",
            name: form.inputProps("analyzer").value.name || "",
          }}
          description={t("pages.document-types.analyzer-association-for-document-type-field")}
        />
        <CustomSelectRelationsOneToOne
          options={analyzerOption}
          label={t("fields.search-analyzer-association")}
          onChange={(val) => {
            form.inputProps("searchAnalyzer").onChange({ id: val.id, name: val.name });
          }}
          value={{
            id: form.inputProps("searchAnalyzer").value.id || "-1",
            name: form.inputProps("searchAnalyzer").value.name || "",
          }}
          description={t("pages.document-types.search-analyzer-association-for-document-type-field")}
        />
        <NumberInput
          label={t("fields.boost")}
          {...form.inputProps("boost")}
          description={t("pages.document-types.define-how-much-score-is-boosted-in")}
        />

        <BooleanInput
          label={t("fields.searchable")}
          {...form.inputProps("searchable")}
          description={t("pages.document-types.if-field-is-searchable-or-not")}
        />
        <BooleanInput
          label={t("fields.exclude")}
          {...form.inputProps("exclude")}
          description={t("pages.document-types.if-field-need-to-be-excluded-from")}
        />
        <BooleanInput label={t("fields.sortable")} {...form.inputProps("sortable")} description={t("pages.document-types.if-field-is-sortable-or-not")} />
        <CodeInput language="json" label={t("fields.configuration")} {...form.inputProps("jsonConfig")} />
      </form>
    </>
  );
}
