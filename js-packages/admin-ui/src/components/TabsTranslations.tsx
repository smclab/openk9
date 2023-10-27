import React from "react";
import { gql } from "@apollo/client";
import { useNavigate, useParams } from "react-router-dom";
import { useCreateOrUpdateTabMutation, useLanguagesQuery, useTabQuery } from "../graphql-generated";
import { useForm, fromFieldValidators, TextInput, TextArea, NumberInput, MainTitle, CustomButtom, ContainerFluid } from "./Form";
import { useToast } from "./ToastProvider";
import { TabQuery } from "./Tab";
import DropDown from "@clayui/drop-down";
import ClayIcon from "@clayui/icon";

gql`
  mutation CreateOrUpdateTab($id: ID, $name: String!, $description: String, $priority: Int!) {
    tab(id: $id, tabDTO: { name: $name, description: $description, priority: $priority }) {
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

export function TabsTranslations() {
  const [flag, setFlag] = React.useState("en-us");
  const [translationsToPost, setTranslationsToPost] = React.useState<any[]>([]);
  const dataLanguagesQuery = useLanguagesQuery();

  const { tabId = "new" } = useParams();
  const showToast = useToast();
  const tabQuery = useTabQuery({
    variables: { id: tabId as string },
    skip: !tabId,
  });

  const originalTranslations = tabQuery.data?.tab?.translations;
  const originalTranslationsLength = originalTranslations?.length;
  for (let index = 0; index < originalTranslationsLength!; index++) {
    const element = originalTranslations![index];
    const translation = {
      language: element?.language?.toLowerCase().replace("_", "-"),
      key: element?.key,
      value: element?.value,
    };
    
    const translationsToPostIndex = translationsToPost.findIndex((item) => (item.language === element?.language?.toLowerCase().replace("_", "-") && item.key === element?.key));
    if (translationsToPostIndex === -1) {
      translationsToPost.push(translation);
    }
  }

  const [createOrUpdateTabMutate, createOrUpdateTabMutation] = useCreateOrUpdateTabMutation({
    refetchQueries: [TabQuery],
    onCompleted(data) {
      if (data.tab?.entity) {
        showToast({ displayType: "info", title: "Translation updated", content: data.tab.entity.name ?? "" });
      }
    },
  });


  const originalValuesArray = translationsToPost?.filter((element) => element?.language?.toLowerCase().replace("_", "-") === flag);
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
    isLoading: tabQuery.loading || createOrUpdateTabMutation.loading,
    onSubmit(data) {
      addTranslation(flag);
      const convertedTranslationsToPost = [];
      const translationsToPostLength = translationsToPost.length;
      for (let index = 0; index < translationsToPostLength; index++) {
        const element = translationsToPost[index];
        const convertedTranslation = {
          language: element.language.replace("-", "_").replace(/([^_]*$)/g, (s: string) => s.toUpperCase()),
          key: element.key,
          value: element.value,
        };
        convertedTranslationsToPost.push(convertedTranslation);
      }
      console.log(convertedTranslationsToPost); 
      // createOrUpdateTabMutate({
      //   variables: { id: tabId !== "new" ? tabId : undefined, ...data },
      // });
    },
    getValidationMessages: fromFieldValidators(createOrUpdateTabMutation.data?.tab?.fieldValidators),
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
                      const nameInputValue = translationsToPost?.find((element) => (element?.language?.toLowerCase().replace("_", "-") === languageCode && element.key === 'name'))?.value;
                      const descriptionInputValue = translationsToPost?.find((element) => (element?.language?.toLowerCase().replace("_", "-") === languageCode && element.key === 'description'))?.value;
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
        <TextInput label="Name" {...form.inputProps("name")} description="Name used to render tab in search frontend" />
        <TextArea label="Description" {...form.inputProps("description")} />
        <div className="sheet-footer">
          <CustomButtom nameButton={"Update"} canSubmit={!form.canSubmit} typeSelectet="submit" />
        </div>
      </form>
    </ContainerFluid>
  );
}
