import React from "react";
import { gql } from "@apollo/client";
import { useParams } from "react-router-dom";
import { useCreateOrUpdateSuggestionCategoryMutation, useSuggestionCategoryQuery, useLanguagesQuery } from "../graphql-generated";
import { useForm, fromFieldValidators, TextInput, TextArea, NumberInput, BooleanInput, CustomButtom, ContainerFluid, MainTitle } from "./Form";
import { useToast } from "./ToastProvider";
import DropDown from "@clayui/drop-down";
import ClayIcon from "@clayui/icon";
import { SuggestionCategoryQuery } from "./SuggestionCategory";

gql`
  mutation CreateOrUpdateSuggestionCategory($id: ID, $name: String!, $description: String, $priority: Float!, $multiSelect: Boolean!) {
    suggestionCategory(
      id: $id
      suggestionCategoryDTO: { name: $name, description: $description, priority: $priority, multiSelect: $multiSelect }
    ) {
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

  const formOriginalValues = suggestionCategoryQuery.data?.suggestionCategory?.translations;
  const formOriginalValuesLength = formOriginalValues?.length;
  for (let index = 0; index < formOriginalValuesLength!; index++) {
    const element = formOriginalValues![index];
    const translation = {
      language: element?.language?.toLowerCase().replace("_", "-"),
      key: "name",
      value: element?.value,
    };
    const translationsToPostIndex = translationsToPost.findIndex(item => item.language === element?.language?.toLowerCase().replace("_", "-"));  
    if (translationsToPostIndex === -1) {
      translationsToPost.push(translation);
    }
  }  
  
  const [createOrUpdateSuggestionCategoryMutate, createOrUpdateSuggestionCategoryMutation] = useCreateOrUpdateSuggestionCategoryMutation({
    refetchQueries: [SuggestionCategoryQuery],
    onCompleted(data) {
      if (data.suggestionCategory?.entity) {
        showToast({ displayType: "info", title: "Translation updated", content: data.suggestionCategory.entity.name ?? "" });
      }
    },
  });
  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        value: "",
      }),
      []
    ),
    originalValues: translationsToPost?.find((element) => element?.language?.toLowerCase().replace("_", "-") === flag),
    isLoading: suggestionCategoryQuery.loading || createOrUpdateSuggestionCategoryMutation.loading,
    onSubmit(data) {
      addTranslation(flag);
      console.log(translationsToPost);     
      // createOrUpdateSuggestionCategoryMutate({
      //   variables: { id: suggestionCategoryId, ...data },
      // });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateSuggestionCategoryMutation.data?.suggestionCategory?.fieldValidators),
  });

  const addTranslation = (languageCode: string) => {
    const translation = {
      language: languageCode!,
      key: "name",
      value: form.inputProps("value").value,
    };
    const index = translationsToPost.findIndex(item => item.language === languageCode);  
    if (index !== -1) {
      translationsToPost[index].value = translation.value;
    } else {
      translationsToPost.push(translation);
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
                      const inputValue = translationsToPost?.find((element) => element?.language?.toLowerCase().replace("_", "-") === languageCode)?.value;     
                      form.inputProps("value").onChange(inputValue ? inputValue : "");
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
        <TextInput label="Name" {...form.inputProps("value")} />
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
