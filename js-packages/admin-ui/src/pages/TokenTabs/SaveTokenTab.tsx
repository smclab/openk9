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
import { Box, Button, ClickAwayListener, TextField } from "@mui/material";
import React from "react";
import { useNavigate, useParams } from "react-router-dom";
import { isValidId, useDocTypeTokenTab } from "../../utils/RelationOneToOne";
import { TokenType, useCreateOrUpdateTabTokenMutation, useTabTokenTabQuery } from "../../graphql-generated";
import { useConfirmModal } from "../../utils/useConfirmModal";
import { AutocompleteOptionsList } from "@components/Form/Select/AutocompleteOptionsList";
import Recap, { mappingCardRecap } from "@pages/Recap/SaveRecap";

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

type TokenTypeAutocompleteProps<TokenType> = {
  label?: string;
  value: TokenType;
  dict: Record<string, string>;
  onChange: (value: TokenType) => void;
  disabled?: boolean;
};

function TokenTypeAutocomplete<TokenType extends string>({
  label = "Token Type",
  value,
  dict,
  onChange,
  disabled,
}: TokenTypeAutocompleteProps<TokenType>) {
  const [open, setOpen] = React.useState(false);
  const [highlightedIndex, setHighlightedIndex] = React.useState(0);

  const options = React.useMemo(
    () =>
      Object.entries(dict).map(([val, lab]) => ({
        value: val,
        label: lab,
      })),
    [dict],
  );

  const selectedOption = options.find((o) => o.value === value) ?? null;

  const handleSelect = (option: { value: string; label: string }) => {
    onChange(option.value as TokenType);
  };

  React.useEffect(() => {
    const idx = options.findIndex((o) => o.value === value);
    if (idx >= 0) setHighlightedIndex(idx);
  }, [value, options]);

  return (
    <ClickAwayListener onClickAway={() => setOpen(false)}>
      <Box sx={{ position: "relative" }}>
        <TextField
          label={label}
          fullWidth
          value={selectedOption?.label ?? ""}
          onClick={() => !disabled && setOpen((prev) => !prev)}
          InputProps={{
            readOnly: true,
          }}
          disabled={disabled}
        />

        {open && !disabled && (
          <AutocompleteOptionsList
            options={options}
            highlightedIndex={highlightedIndex}
            loading={false}
            onSelect={(option) => {
              handleSelect(option);
              setOpen(false);
            }}
          />
        )}
      </Box>
    </ClickAwayListener>
  );
}

export function SaveTokenTab({ setExtraFab }: { setExtraFab: (fab: React.ReactNode | null) => void }) {
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
  const isRecap = page === 1;
  const isNew = tokenTabId === "new";
  const disabled = isRecap || view === "view";
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
    onError() {
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

      const { docTypeFieldId, ...cleanData } = data;
      const docTypeId = docTypeFieldId?.id;

      createOrUpdateTabTokenMutate({
        variables: {
          tokenTabId: tokenTabId !== "new" ? tokenTabId : undefined,

          ...cleanData,

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

          ...(data.docTypeFieldId?.id !== "undefined" && data.docTypeFieldId?.id ? { docTypeFieldId: docTypeId } : {}),
        },
      });
    },
    getValidationMessages: fromFieldValidators(
      createOrUpdateTabTokenMutation.data?.tokenTabWithDocTypeField?.fieldValidators,
    ),
  });

  const recapSections = mappingCardRecap({
    form: form as any,
    sections: [
      {
        cell: [
          { key: "name" },
          { key: "description" },
          { key: "value" },
          { key: "tokenType", label: "Token Type" },
          { key: "docTypeFieldId", label: "Document Type Field" },
          ...(form.inputProps("tokenType").value === "FILTER" || form.inputProps("tokenType").value === "TEXT"
            ? [
                { key: "boost" },
                { key: "valuesQueryType", label: "Values Query Type" },
                { key: "globalQueryType", label: "Global Query Type" },
                { key: "fuzziness" },
              ]
            : []),
          { key: "filter" },
        ],
        label: "Recap Token Tab",
      },
    ],
    valueOverride: {
      docTypeFieldId: form.inputProps("docTypeFieldId").value || "",
    },
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
                      <TokenTypeAutocomplete<TokenType>
                        label="Token Type"
                        dict={TokenType}
                        value={form.inputProps("tokenType").value}
                        onChange={(tokenType: TokenType) => {
                          form.inputProps("tokenType").onChange(tokenType);

                          if (tokenType !== tokenTypeInitialValue) {
                            form.inputProps("boost").onChange(boostDefaultValue);
                            form.inputProps("fuzziness").onChange(fuzzinessDefaultValue);
                            form.inputProps("valuesQueryType").onChange(valuesQueryTypeDefaultValue);
                            form.inputProps("globalQueryType").onChange(globalQueryTypeDefaultValue);
                          } else {
                            form.inputProps("boost").onChange(extraParams.boost || boostDefaultValue);
                            form.inputProps("fuzziness").onChange(extraParams.fuziness || fuzzinessDefaultValue);
                            form
                              .inputProps("valuesQueryType")
                              .onChange(extraParams.valuesQueryType || valuesQueryTypeDefaultValue);
                            form
                              .inputProps("globalQueryType")
                              .onChange(extraParams.globalQueryType || globalQueryTypeDefaultValue);
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

