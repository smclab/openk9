import {
  BooleanInput,
  combineErrorMessages,
  ContainerFluid,
  CreateDataEntity,
  CustomSelect,
  fromFieldValidators,
  TextArea,
  TextInput,
  TitleEntity,
  TooltipDescription,
  useForm,
} from "@components/Form";
import { useToast } from "@components/Form/Form/ToastProvider";
import { AutocompleteDropdown } from "@components/Form/Select/AutocompleteDropdown";
import { Box, Button } from "@mui/material";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import { isValidId, useDocTypeTokenTab } from "../../utils/RelationOneToOne";
import { TokenType, useCreateOrUpdateTabTokenMutation, useTabTokenTabQuery } from "../../graphql-generated";
import { useConfirmModal } from "../../utils/useConfirmModal";

enum fuzziness {
  ZERO = "ZERO",
  ONE = "ONE",
  TWO = "TWO",
  AUTO = "AUTO",
}

const fuzzinessDefaultValue = fuzziness.ZERO.toString();
const boostDefaultValue = "50";

enum valuesQueryType {
  MUST = "MUST",
  SHOULD = "SHOULD",
  MIN_SHOULD_1 = "MIN_SHOULD_1",
  MIN_SHOULD_2 = "MIN_SHOULD_2",
  MIN_SHOULD_3 = "MIN_SHOULD_3",
  MUST_NOT = "MUST_NOT",
  FILTER = "FILTER",
}

const valuesQueryTypeDefaultValue = valuesQueryType.MUST.toString();

enum globalQueryType {
  MUST = "MUST",
  SHOULD = "SHOULD",
  MIN_SHOULD_1 = "MIN_SHOULD_1",
  MIN_SHOULD_2 = "MIN_SHOULD_2",
  MIN_SHOULD_3 = "MIN_SHOULD_3",
  MUST_NOT = "MUST_NOT",
  FILTER = "FILTER",
}

const globalQueryTypeDefaultValue = globalQueryType.MUST.toString();

export function SaveTokenTab() {
  const { tokenTabId = "new", view } = useParams();
  const [page, setPage] = React.useState(0);
  const navigate = useNavigate();
  const { openConfirmModal, ConfirmModal } = useConfirmModal({
    title: "Edit Token Tab",
    body: "Are you sure you want to edit this Token Tab?",
    labelConfirm: "Edit",
  });

  const handleEditClick = async () => {
    const confirmed = await openConfirmModal();
    if (confirmed) {
      navigate(`/token-tab/${tokenTabId}`);
    }
  };
  const disabled = page === 1 || view === "view";
  const tabTokenTabQuery = useTabTokenTabQuery({
    variables: { id: tokenTabId as string },
    skip: !tokenTabId || tokenTabId === "new",
  });
  const toast = useToast();
  const [createOrUpdateTabTokenMutate, createOrUpdateTabTokenMutation] = useCreateOrUpdateTabTokenMutation({
    refetchQueries: ["TabTokenTab", "TabTokens"],
    onCompleted(data: any) {
      if (data.tokenTabWithDocTypeField?.entity) {
        const isNew = tokenTabId === "new" ? "created" : "updated";
        toast({
          title: `Token Tab ${isNew}`,
          content: `Token Tab has been ${isNew} successfully`,
          displayType: "success",
        });
        navigate(`/token-tabs/`, {
          replace: true,
        });
      } else {
        toast({
          title: `Error`,
          content: combineErrorMessages(data.tokenTab?.fieldValidators),
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.log(error);
      const isNew = tokenTabId === "new" ? "create" : "update";
      toast({
        title: `Error ${isNew}`,
        content: `Impossible to ${isNew} Token Tab`,
        displayType: "error",
      });
    },
  });

  const extraParams = JSON.parse(tabTokenTabQuery.data?.tokenTab?.extraParams || "{}") as {
    globalQueryType: string;
    valuesQueryType: string;
    boost: string;
    fuziness: string;
  };

  const tokenTypeInitialValue = tabTokenTabQuery.data?.tokenTab?.tokenType;

  const originalValues = {
    name: tabTokenTabQuery.data?.tokenTab?.name,
    description: tabTokenTabQuery.data?.tokenTab?.description,
    value: tabTokenTabQuery.data?.tokenTab?.value,
    filter: tabTokenTabQuery.data?.tokenTab?.filter,
    tokenType: tabTokenTabQuery.data?.tokenTab?.tokenType,
    docTypeFieldId: {
      id: String(tabTokenTabQuery.data?.tokenTab?.docTypeField?.id),
      name: tabTokenTabQuery.data?.tokenTab?.docTypeField?.name || "",
    },
  };

  const form = useForm({
    initialValues: React.useMemo(
      () => ({
        name: "",
        description: "",
        value: "",
        filter: true,
        docTypeFieldId: isValidId(tabTokenTabQuery.data?.tokenTab?.docTypeField),
        tokenType: TokenType.Autocomplete,
        boost: extraParams.boost || boostDefaultValue,
        fuzziness: extraParams.fuziness || fuzzinessDefaultValue,
        valuesQueryType: extraParams.valuesQueryType || valuesQueryTypeDefaultValue,
        globalQueryType: extraParams.globalQueryType || globalQueryTypeDefaultValue,
      }),
      [extraParams, tabTokenTabQuery.data?.tokenTab?.docTypeField],
    ),
    originalValues: originalValues,
    isLoading: tabTokenTabQuery.loading || createOrUpdateTabTokenMutation.loading,
    onSubmit(data) {
      const isExtraParamsType = [TokenType.Text, TokenType.Filter].includes(data.tokenType);

      createOrUpdateTabTokenMutate({
        variables: {
          tokenTabId: tokenTabId !== "new" ? tokenTabId : undefined,
          ...data,
          ...(isExtraParamsType
            ? {
                extraParams: JSON.stringify({
                  globalQueryType: data.globalQueryType,
                  valuesQueryType: data.valuesQueryType,
                  boost: data.boost,
                  fuzziness: data.fuzziness,
                }),
              }
            : {}),
          docTypeFieldId: data?.docTypeFieldId?.id ? data?.docTypeFieldId?.id : null,
        },
      });
    },
    getValidationMessages: fromFieldValidators(
      createOrUpdateTabTokenMutation.data?.tokenTabWithDocTypeField?.fieldValidators,
    ),
  });

  return (
    <ContainerFluid>
      <>
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 2 }}>
          <TitleEntity
            nameEntity="Token Tab"
            description="Create or Edit a Token Tab and add to Tab to create yoy personalized search to perform by tab.
          Choose between differe Token Tabs depending on the search you want to configure."
            id={tokenTabId}
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
            id={tokenTabId}
            pathBack="/token-tabs/"
            setPage={setPage}
            haveConfirmButton={view ? false : true}
            informationSuggestion={[
              {
                content: (
                  <>
                    <TextInput label="Name" {...form.inputProps("name")} />
                    <TextArea label="Description" {...form.inputProps("description")} />
                    <TextInput
                      label="Value"
                      {...form.inputProps("value")}
                      description="Value it must match for this token"
                    />
                    <TooltipDescription informationDescription="Type of Token Tab. Every type implements a different search logic.">
                      <CustomSelect
                        label="Token Type"
                        dict={TokenType}
                        {...form.inputProps("tokenType")}
                        onChange={(tokenType: TokenType) => {
                          form.inputProps("tokenType").onChange(tokenType);
                          if (tokenType !== tokenTypeInitialValue) {
                            form.inputProps("boost").onChange(boostDefaultValue);
                            form.inputProps("fuzziness").onChange(fuzzinessDefaultValue);
                            form.inputProps("valuesQueryType").onChange(valuesQueryTypeDefaultValue);
                            form.inputProps("globalQueryType").onChange(globalQueryTypeDefaultValue);
                          } else {
                            form
                              .inputProps("boost")
                              .onChange(extraParams.boost ? extraParams.boost : boostDefaultValue);
                            form
                              .inputProps("fuzziness")
                              .onChange(extraParams.fuziness ? extraParams.fuziness : fuzzinessDefaultValue);
                            form
                              .inputProps("valuesQueryType")
                              .onChange(
                                extraParams.valuesQueryType ? extraParams.valuesQueryType : valuesQueryTypeDefaultValue,
                              );
                            form
                              .inputProps("globalQueryType")
                              .onChange(
                                extraParams.globalQueryType ? extraParams.globalQueryType : globalQueryTypeDefaultValue,
                              );
                          }
                        }}
                      />
                    </TooltipDescription>
                    <AutocompleteDropdown
                      label="DocType Field"
                      onChange={(val) => form.inputProps("docTypeFieldId").onChange({ id: val.id, name: val.name })}
                      value={
                        !form?.inputProps("docTypeFieldId")?.value?.id
                          ? undefined
                          : {
                              id: form?.inputProps("docTypeFieldId")?.value?.id || "",
                              name: form?.inputProps("docTypeFieldId")?.value?.name || "",
                            }
                      }
                      onClear={() => form.inputProps("docTypeFieldId").onChange(undefined)}
                      disabled={page === 1}
                      useOptions={useDocTypeTokenTab}
                    />
                    {(form.inputProps("tokenType").value === "TEXT" ||
                      form.inputProps("tokenType").value === "FILTER") && (
                      <div>
                        <TextInput label="boost" {...form.inputProps("boost")} disabled={disabled} />
                        <CustomSelect
                          label="valuesQueryType"
                          dict={valuesQueryType}
                          {...form.inputProps("valuesQueryType")}
                          disabled={disabled}
                        />
                        <CustomSelect
                          label="globalQueryType"
                          dict={globalQueryType}
                          {...form.inputProps("globalQueryType")}
                          disabled={disabled}
                        />
                        <CustomSelect
                          label="fuzziness"
                          dict={fuzziness}
                          {...form.inputProps("fuzziness")}
                          disabled={disabled}
                        />
                      </div>
                    )}
                    <BooleanInput label="Filter" {...form.inputProps("filter")} disabled={disabled} />
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
    </ContainerFluid>
  );
}
