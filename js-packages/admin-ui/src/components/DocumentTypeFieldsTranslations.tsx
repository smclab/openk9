import { gql } from "@apollo/client";
import React from "react";
import { useParams } from "react-router-dom";
import {
  useAddDocTypeFieldTranslationMutation,
  useDocumentTypeFieldQuery,
  useLanguagesQuery,
  useEnabledBucketQuery,
} from "../graphql-generated";
import { ContainerFluid, CustomButtom, fromFieldValidators, MainTitle, TextArea, TextInput, useForm } from "./Form";
import { Link } from "react-router-dom";
import ClayToolbar from "@clayui/toolbar";
import { useToast } from "./ToastProvider";
import { ClayButtonWithIcon } from "@clayui/button";
import { ClassNameButton } from "../App";
import { DocumentTypeFieldQuery } from "./DocumentTypeField";
import DropDown from "@clayui/drop-down/lib/DropDown";
import ClayIcon from "@clayui/icon";

gql`
  mutation addDocTypeFieldTranslation($docTypeFieldId: ID!, $language: String!, $key: String, $value: String!) {
    addDocTypeFieldTranslation(docTypeFieldId: $docTypeFieldId, language: $language, key: $key, value: $value) {
      left
      right
    }
  }
`;

let mutationCounter = 0;
let totalTranslations = 0;

export function DocumentTypeFieldsTranslations() {
  // convert languageCode from en_US to en-us
  const convertLanguageCodeForFrontend = (languageCode: string) => {
    return languageCode.toLowerCase().replace("_", "-");
  };

  // convert languageCode from en-us to en_US
  const convertLanguageCodeForBackend = (languageCode: string) => {
    return languageCode.replace("-", "_").replace(/([^_]*$)/g, (s: string) => s.toUpperCase());
  };
  const enabledBucketQuery = useEnabledBucketQuery({
    variables: {},
  });
  const [flag, setFlag] = React.useState(
    convertLanguageCodeForFrontend(enabledBucketQuery?.data?.enabledBucket?.language?.value || "en_US")
  );
  const [translationsToPost, setTranslationsToPost] = React.useState<any[]>([]);
  const dataLanguagesQuery = useLanguagesQuery();

  const { documentTypeId, documentTypeFieldId = "new" } = useParams();
  const showToast = useToast();
  const documentTypeFieldQuery = useDocumentTypeFieldQuery({
    variables: { id: documentTypeFieldId as string },
    skip: !documentTypeFieldId,
  });

  const originalTranslations = documentTypeFieldQuery.data?.docTypeField?.translations;
  const originalTranslationsLength = originalTranslations?.length;
  for (let index = 0; index < originalTranslationsLength!; index++) {
    const element = originalTranslations![index];
    const translation = {
      language: convertLanguageCodeForFrontend(element?.language!),
      key: element?.key,
      value: element?.value,
    };

    const translationsToPostIndex = translationsToPost.findIndex(
      (item) => item.language === convertLanguageCodeForFrontend(element?.language!) && item.key === element?.key
    );
    if (translationsToPostIndex === -1) {
      translationsToPost.push(translation);
    }
  }

  const [addDocTypeFieldTranslationMutate, addDocTypeFieldTranslationMutation] = useAddDocTypeFieldTranslationMutation({
    refetchQueries: [DocumentTypeFieldQuery],
    onCompleted(data) {
      mutationCounter++;
      if (data.addDocTypeFieldTranslation && mutationCounter === totalTranslations) {
        showToast({ displayType: "info", title: "Translations updated", content: "" });
      }
    },
  });

  const originalValuesArray = translationsToPost?.filter((element) => convertLanguageCodeForFrontend(element?.language!) === flag);
  const originalValues: { [x: string]: any } = { language: flag };

  originalValuesArray.forEach((element: any) => {
    originalValues[element.key] = element.value;
  });

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
      }),
      []
    ),
    originalValues: originalValues,
    isLoading: documentTypeFieldQuery.loading || addDocTypeFieldTranslationMutation.loading,
    onSubmit(data) {
      addTranslation(flag);
      const translationsToPostLength = translationsToPost.length;
      const convertedTranslationsToPost = [];
      for (let index = 0; index < translationsToPostLength; index++) {
        const element = translationsToPost[index];
        if (
          !originalTranslations!.some(
            (translation) =>
              convertLanguageCodeForBackend(element?.language!) === translation?.language &&
              element.key === translation?.key &&
              element.value === translation?.value
          )
        ) {
          const convertedTranslation = {
            language: convertLanguageCodeForBackend(element?.language!),
            key: element.key,
            value: element.value,
          };
          convertedTranslationsToPost.push(convertedTranslation);
          totalTranslations++;
        }
      }
      const convertedTranslationsToPostLength = convertedTranslationsToPost.length;
      for (let index = 0; index < convertedTranslationsToPostLength; index++) {
        const element = convertedTranslationsToPost[index];
        addDocTypeFieldTranslationMutate({
          variables: {
            docTypeFieldId: documentTypeFieldId,
            language: element.language,
            value: element.value,
            key: element.key,
          },
        });
      }
    },
    getValidationMessages: () => {
      const errorText = (addDocTypeFieldTranslationMutation as any).error?.body?.details;
      if (errorText) return [errorText];
      return [];
    },
  });

  const addTranslation = (languageCode: string) => {
    const nameTranslation = {
      language: languageCode!,
      key: "name",
      value: form.inputProps("name").value,
    };
    const descriptionTranslation = {
      language: languageCode!,
      key: "description",
      value: form.inputProps("description").value,
    };

    const nameIndex = translationsToPost.findIndex((item) => item.language === languageCode && item.key === "name");
    const descriptionIndex = translationsToPost.findIndex((item) => item.language === languageCode && item.key === "description");

    if (nameIndex !== -1) {
      translationsToPost[nameIndex].value = nameTranslation.value;
    } else {
      translationsToPost.push(nameTranslation);
    }

    if (descriptionIndex !== -1) {
      translationsToPost[descriptionIndex].value = descriptionTranslation.value;
    } else {
      translationsToPost.push(descriptionTranslation);
    }
  };

  return (
    <React.Fragment>
      <ClayToolbar light>
        <ContainerFluid>
          <ClayToolbar.Nav>
            <ClayToolbar.Item>
              <Link to={`/document-types/${documentTypeId}/document-type-fields`}>
                <ClayButtonWithIcon className={ClassNameButton} aria-label="" symbol="angle-left" small />
              </Link>
            </ClayToolbar.Item>
          </ClayToolbar.Nav>
        </ContainerFluid>
      </ClayToolbar>
      <ContainerFluid>
        <MainTitle title="Translations" />
        <DropDown
          closeOnClick
          trigger={
            <button
              className="btn btn-unstyled nav-btn nav-btn-monospaced"
              style={{ border: "1px solid #8F8F8F", width: "100px", height: "35px", backgroundColor: "#ffffff" }}
              onClick={() => {
                addTranslation(flag);
              }}
            >
              <ClayIcon symbol={flag} fontSize="25px" />
              <span style={{ marginLeft: "10px" }}>{flag}</span>
            </button>
          }
        >
          <DropDown.ItemList>
            {dataLanguagesQuery.data?.languages?.edges?.map((language) => {
              const languageCode = convertLanguageCodeForFrontend(language?.node?.value!);
              return (
                <div key={language?.node?.id}>
                  <DropDown.Item>
                    <div
                      style={{ display: "inline-block", width: "100%", outline: "none" }}
                      onClick={() => {
                        setFlag(languageCode!);
                        const nameInputValue = translationsToPost?.find(
                          (element) => convertLanguageCodeForFrontend(element?.language) === languageCode && element.key === "name"
                        )?.value;
                        const descriptionInputValue = translationsToPost?.find(
                          (element) => convertLanguageCodeForFrontend(element?.language) === languageCode && element.key === "description"
                        )?.value;
                        form.inputProps("name").onChange(nameInputValue ? nameInputValue : "");
                        form.inputProps("description").onChange(descriptionInputValue ? descriptionInputValue : "");
                      }}
                    >
                      <ClayIcon symbol={languageCode!} />
                      <span style={{ marginLeft: "10px" }}>{languageCode!}</span>
                    </div>
                  </DropDown.Item>
                  <DropDown.Divider />
                </div>
              );
            })}
          </DropDown.ItemList>
        </DropDown>
        <form
          className="sheet"
          onSubmit={(event) => {
            event.preventDefault();
            form.submit();
          }}
        >
          <TextInput label="Name" {...form.inputProps("name")} />
          <TextArea label="Description" {...form.inputProps("description")} />
          <div className="sheet-footer">
            <CustomButtom nameButton={"Update"} canSubmit={!form.canSubmit} typeSelectet="submit" />
          </div>
        </form>
      </ContainerFluid>
    </React.Fragment>
  );
}
