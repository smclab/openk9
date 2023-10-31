import React from "react";
import { gql } from "@apollo/client";
import { useParams } from "react-router-dom";
import { useAddSuggestionCategoryTranslationMutation, useSuggestionCategoryQuery, useLanguagesQuery } from "../graphql-generated";
import { useForm, fromFieldValidators, TextInput, TextArea, NumberInput, BooleanInput, CustomButtom, ContainerFluid, MainTitle } from "./Form";
import { useToast } from "./ToastProvider";
import DropDown from "@clayui/drop-down";
import ClayIcon from "@clayui/icon";
import { SuggestionCategoryQuery } from "./SuggestionCategory";

gql`
  mutation AddSuggestionCategoryTranslation($suggestionCategoryId: ID!, $language: String!, $key: String, $value: String!) {
    addSuggestionCategoryTranslation(suggestionCategoryId: $suggestionCategoryId, language: $language, key: $key, value: $value) {
      left
      right
    }
  }
`;

export function SuggestionCategoryTranslations() {
  const [flag, setFlag] = React.useState("en-us");
  const [translationsToPost, setTranslationsToPost] = React.useState<any[]>([]);
  const dataLanguagesQuery = useLanguagesQuery();
  
  const { suggestionCategoryId = "new" } = useParams();
  const showToast = useToast();
  const suggestionCategoryQuery = useSuggestionCategoryQuery({
    variables: { id: suggestionCategoryId as string },
    skip: !suggestionCategoryId,
  });

  // convert languageCode from en_US to en-us
  const convertLanguageCodeForFrontend = (languageCode: string) => {
    return languageCode.toLowerCase().replace("_", "-");
  };

  // convert languageCode from en-us to en_US
  const convertLanguageCodeForBackend = (languageCode: string) => {
    return languageCode.replace("-", "_").replace(/([^_]*$)/g, (s: string) => s.toUpperCase());
  };

  const originalTranslations = suggestionCategoryQuery.data?.suggestionCategory?.translations;
  const originalTranslationsLength = originalTranslations?.length;
  for (let index = 0; index < originalTranslationsLength!; index++) {
    const element = originalTranslations![index];
    const translation = {
      language: convertLanguageCodeForFrontend(element?.language!),
      key: element?.key,
      value: element?.value,
    };

    const translationsToPostIndex = translationsToPost.findIndex((item) => item.language === convertLanguageCodeForFrontend(element?.language!) && item.key === element?.key);
    if (translationsToPostIndex === -1) {
      translationsToPost.push(translation);
    }
  } 
  
  const [addSuggestionCategoryMutate, addSuggestionCategoryMutation] = useAddSuggestionCategoryTranslationMutation({
    refetchQueries: [SuggestionCategoryQuery],
    onCompleted(data) {
      if (data.addSuggestionCategoryTranslation) {
        showToast({ displayType: "info", title: "Translation updated", content: "" });
      }
    },
  });

  const originalValuesArray = translationsToPost?.filter((element) => convertLanguageCodeForFrontend(element?.language!) === flag);
  const originalValues: { [x: string]: any; } = { language: flag };

  originalValuesArray.forEach((element: any) => {
    originalValues[element.key] = element.value;
  });

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: ""
      }),
      []
    ),
    originalValues: originalValues,
    isLoading: suggestionCategoryQuery.loading || addSuggestionCategoryMutation.loading,
    onSubmit(data) {
      addTranslation(flag);
      const translationsToPostLength = translationsToPost.length;
      for (let index = 0; index < translationsToPostLength; index++) {
        const element = translationsToPost[index];
        if (!originalTranslations!.some((translation) => convertLanguageCodeForBackend(element?.language!) === translation?.language && element.key === translation?.key && element.value === translation?.value)) {
          const convertedTranslation = {
            language: convertLanguageCodeForBackend(element?.language!),
            key: element.key,
            value: element.value,
          };

          addSuggestionCategoryMutate({
            variables: {
              suggestionCategoryId: suggestionCategoryId,
              language: convertedTranslation.language,
              value: convertedTranslation.value,
              key: convertedTranslation.key,
            },
          });
        }
      }
    },
    getValidationMessages: () => {
      const errorText = (addSuggestionCategoryMutation as any).error?.body?.details;
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

    const nameIndex = translationsToPost.findIndex((item) => (item.language === languageCode && item.key === "name"));
    const descriptionIndex = translationsToPost.findIndex((item) => (item.language === languageCode && item.key === "description"));
    
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
            const languageCode = language?.node?.value?.toLowerCase().replace("_", "-");
            return (
              <div key={language?.node?.id}>
                <DropDown.Item>
                  <div
                    style={{ display: "inline-block", width: "100%", outline: "none" }}
                    onClick={() => {
                      setFlag(languageCode!);
                      const nameInputValue = translationsToPost?.find((element) => convertLanguageCodeForFrontend(element?.language) === languageCode && element.key === "name")?.value;
                      const descriptionInputValue = translationsToPost?.find((element) => convertLanguageCodeForFrontend(element?.language) === languageCode && element.key === "description")?.value;
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
          <CustomButtom
            nameButton="Update"
            canSubmit={!form.canSubmit}
            typeSelectet="submit"
          />
        </div>
      </form>
    </ContainerFluid>
  );
}
