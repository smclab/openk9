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
  fromFieldValidators,
  ModalConfirm,
  MultiAssociationCustomQuery,
  useForm,
  useToast,
} from "@components/Form";
import Autocomplete from "@components/Form/Form/AutoComplete";
import AdminPanelSettingsOutlinedIcon from "@mui/icons-material/AdminPanelSettingsOutlined";
import LockOutlinedIcon from "@mui/icons-material/LockOutlined";
import VisibilityOutlinedIcon from "@mui/icons-material/VisibilityOutlined";
import { Alert, AlertTitle, Box, Button, Typography } from "@mui/material";
import React from "react";
import {
  useCreateOrUpdateResultAclConfigMutation,
  useResultAclConfigQuery,
  useUnboundDocTypeFieldByResultAclConfigQuery,
} from "../../graphql-generated";
import {
  FieldOption,
  ResultAclConfigValues,
  toFieldOptions,
  toResultAclConfigDto,
  toSelectableFieldOptions,
} from "./resultAclConfigForm";
import { SettingsSection } from "./SettingsSection";

const RISK_TITLE = "Risk of exfiltration by inference";

type RiskWarning = { body: string; confirm(): void };

/** The result ACL configuration of the tenant: a single entity, saved as a whole. */
export function SaveResultAclConfig() {
  const toast = useToast();
  const [riskWarning, setRiskWarning] = React.useState<RiskWarning | null>(null);
  const [fieldsKey, setFieldsKey] = React.useState(0);

  const configQuery = useResultAclConfigQuery({ fetchPolicy: "network-only" });
  const unboundFieldsQuery = useUnboundDocTypeFieldByResultAclConfigQuery({ fetchPolicy: "network-only" });

  const [mutate, mutation] = useCreateOrUpdateResultAclConfigMutation({
    refetchQueries: ["ResultAclConfig", "UnboundDocTypeFieldByResultAclConfig"],
    onCompleted(data) {
      if (data.resultAclConfig?.entity) {
        toast({
          title: "Configuration saved",
          content: "The obscured results configuration has been saved.",
          displayType: "success",
        });
      } else {
        toast({
          title: "Error",
          content: combineErrorMessages(data.resultAclConfig?.fieldValidators),
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.error(error);
      toast({
        title: "Error",
        content: error.graphQLErrors?.[0]?.message || "Unable to save the obscured results configuration.",
        displayType: "error",
      });
    },
  });

  const config = configQuery.data?.resultAclConfig;

  const form = useForm<ResultAclConfigValues>({
    // Memoized: the fields association reads its list from here, so a new
    // identity on every render would re-seed the association lists.
    initialValues: React.useMemo(() => ({ enabled: false, superUserRoles: [], visibleFields: [] }), []),
    originalValues: React.useMemo(
      () =>
        config
          ? {
              enabled: config.enabled,
              superUserRoles: (config.superUserRoles ?? []).filter((role): role is string => !!role),
              visibleFields: toFieldOptions(config.visibleFields),
            }
          : undefined,
      [config],
    ),
    isLoading: configQuery.loading || mutation.loading,
    onSubmit(values) {
      mutate({ variables: { resultAclConfigDTO: toResultAclConfigDto(values) } });
    },
    getValidationMessages: fromFieldValidators(mutation.data?.resultAclConfig?.fieldValidators),
  });

  const enabled = form.inputProps("enabled");
  const superUserRoles = form.inputProps("superUserRoles");
  const visibleFields = form.inputProps("visibleFields");

  const selectableFields = React.useMemo(
    () =>
      toSelectableFieldOptions(unboundFieldsQuery.data?.unboundDocTypeFieldByResultAclConfig, config?.visibleFields),
    [unboundFieldsQuery.data, config],
  );
  const unassociatedFields = React.useMemo(
    () => selectableFields.filter((option) => !visibleFields.value.some((field) => field.value === option.value)),
    [selectableFields, visibleFields.value],
  );

  const handleEnabledChange = (value: boolean) => {
    if (value) {
      setRiskWarning({
        body: "With the mode on, in the text searches that ask for it the documents a user cannot read appear among their results with only the visible fields. By repeating searches the user can infer part of the content they cannot see.",
        confirm: () => enabled.onChange(true),
      });
    } else {
      enabled.onChange(false);
    }
  };

  const handleFieldsSelect = ({ items, isAdd }: { items: FieldOption[]; isAdd: boolean }) => {
    const currentFields = visibleFields.value;
    if (isAdd) {
      setRiskWarning({
        body: "The value of the fields you add will be visible to every user who finds the document in a text search, even if they cannot read it. Add only fields that do not reveal confidential content. The risk of inference remains: by repeating searches a user can infer part of the content from the fields that are not visible too.",
        confirm: () =>
          visibleFields.onChange([
            ...currentFields,
            ...items.filter((item) => !currentFields.some((field) => field.value === item.value)),
          ]),
      });
    } else {
      visibleFields.onChange(currentFields.filter((field) => !items.some((item) => item.value === field.value)));
    }
  };

  const closeRiskWarning = () => {
    setRiskWarning(null);
    // The association moves the items before calling onSelect: remounting it
    // drops an addition the administrator did not confirm.
    setFieldsKey((key) => key + 1);
  };

  if (configQuery.loading) return null;

  const header = (
    <Box>
      <Typography component="h1" variant="h1" fontWeight="600">
        ACL: obscured results
      </Typography>
      <Typography variant="body1" color="text.secondary">
        With the mode on, in the text searches that ask for it ACLs no longer exclude documents: a user without
        permission sees the document among the results, but only with its visible fields. Knn and hybrid search and the
        chat stay filtered by ACLs, and the filter values, with their counts, come only from the documents the user can
        read.
      </Typography>
    </Box>
  );

  // Without the configuration the form would show the defaults, and saving
  // them would replace it; without the selectable fields none could be added.
  if (configQuery.error || unboundFieldsQuery.error) {
    return (
      <ContainerFluid size="lg" flexColumn>
        {header}
        <Alert severity="error">
          Unable to read the obscured results configuration or the selectable fields. Reload the page: until they are
          read, the page does not allow saving.
        </Alert>
      </ContainerFluid>
    );
  }

  return (
    <ContainerFluid size="lg" flexColumn>
      {header}

      <Alert severity="warning">
        <AlertTitle>{RISK_TITLE}</AlertTitle>
        <Typography variant="body2">
          In text search an obscured document answers like a readable one: the searched text, the applied filters,
          sorting and score also use the fields that are not visible. By repeating searches and watching which documents
          appear, and in which order, a user can infer part of the content they cannot see. For each obscured result the
          response also lists the names of the removed fields, not their values.
        </Typography>
        <Typography variant="body2" sx={{ mt: 1 }}>
          The search frontend asks for obscured results only if whoever embeds it turns on the dedicated option. The
          option does not protect the documents: with the mode on, any user who calls the search API directly can ask
          for them.
        </Typography>
      </Alert>

      <SettingsSection
        icon={<LockOutlinedIcon />}
        title="Mode"
        description="In text search ACLs decide what is visible of each result, instead of excluding it."
      >
        <BooleanInput {...enabled} label="Turn on obscured results" onChange={handleEnabledChange} />
      </SettingsSection>

      <SettingsSection
        icon={<AdminPanelSettingsOutlinedIcon />}
        title="Super-user roles"
        description="Users with one of these roles in their access token see in full every result of the text searches that ask for the mode, and the filter values, with their counts, come from all documents. In knn and hybrid search and in the chat the ACL filter applies to them too. The extra roles of the request do not count."
      >
        <Autocomplete
          defaultChip={superUserRoles.value}
          setChips={superUserRoles.onChange}
          disabled={superUserRoles.disabled}
        />
      </SettingsSection>

      <SettingsSection
        icon={<VisibilityOutlinedIcon />}
        title="Visible fields"
        description="Visible even in obscured results, which always show the id and the document type as well. Excluded fields, OBJECT fields, NESTED fields with sub-fields, variants of i18n fields and multi-fields cannot be selected."
      >
        <MultiAssociationCustomQuery
          key={fieldsKey}
          list={{
            unassociated: unassociatedFields,
            associated: visibleFields.value,
            isLoading: unboundFieldsQuery.loading,
          }}
          isLoading={unboundFieldsQuery.loading}
          disabled={visibleFields.disabled}
          isRecap={false}
          onSelect={handleFieldsSelect}
        />
        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
          A document type with a visible field cannot be deleted: remove the field from here first.
        </Typography>
      </SettingsSection>

      {/* The bottom margin keeps Save clear of the floating buttons, as in the entity wizard */}
      <Box sx={{ display: "flex", justifyContent: "flex-end", marginBottom: "70px" }}>
        <Button variant="contained" onClick={() => form.submit()} disabled={!form.canSubmit}>
          Save
        </Button>
      </Box>

      {riskWarning && (
        <ModalConfirm
          type="warning"
          title={RISK_TITLE}
          body={riskWarning.body}
          labelConfirm="Confirm"
          actionConfirm={riskWarning.confirm}
          close={closeRiskWarning}
        />
      )}
    </ContainerFluid>
  );
}
