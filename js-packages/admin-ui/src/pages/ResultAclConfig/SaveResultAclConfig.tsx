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
import { SettingsSection } from "@pages/AdminSettings/SettingsSection";
import React from "react";
import { useTranslation } from "react-i18next";
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

type RiskWarning = { body: string; confirm(): void };

/** The result ACL configuration of the tenant: a single entity, saved as a whole. */
export function SaveResultAclConfig() {
  const { t } = useTranslation();
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
          title: t("pages.admin-settings.result-acl.saved-title"),
          content: t("pages.admin-settings.result-acl.saved-content"),
          displayType: "success",
        });
      } else {
        toast({
          title: t("common.error"),
          content: combineErrorMessages(data.resultAclConfig?.fieldValidators),
          displayType: "error",
        });
      }
    },
    onError(error) {
      console.error(error);
      toast({
        title: t("common.error"),
        content: error.graphQLErrors?.[0]?.message || t("pages.admin-settings.result-acl.save-error-content"),
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
        body: t("pages.admin-settings.result-acl.enable-warning"),
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
        body: t("pages.admin-settings.result-acl.add-field-warning"),
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
        {t("pages.admin-settings.result-acl.title")}
      </Typography>
      <Typography variant="body1" color="text.secondary">
        {t("pages.admin-settings.result-acl.page-description")}
      </Typography>
    </Box>
  );

  // Without the configuration the form would show the defaults, and saving
  // them would replace it; without the selectable fields none could be added.
  if (configQuery.error || unboundFieldsQuery.error) {
    return (
      <ContainerFluid size="lg" flexColumn>
        {header}
        <Alert severity="error">{t("pages.admin-settings.result-acl.load-error")}</Alert>
      </ContainerFluid>
    );
  }

  return (
    <ContainerFluid size="lg" flexColumn>
      {header}

      <Alert severity="warning">
        <AlertTitle>{t("pages.admin-settings.result-acl.risk-title")}</AlertTitle>
        <Typography variant="body2">{t("pages.admin-settings.result-acl.risk-body")}</Typography>
        <Typography variant="body2" sx={{ mt: 1 }}>
          {t("pages.admin-settings.result-acl.frontend-note")}
        </Typography>
      </Alert>

      <SettingsSection
        icon={<LockOutlinedIcon />}
        title={t("pages.admin-settings.result-acl.mode-title")}
        description={t("pages.admin-settings.result-acl.mode-description")}
      >
        <BooleanInput
          {...enabled}
          label={t("pages.admin-settings.result-acl.enabled-label")}
          onChange={handleEnabledChange}
        />
      </SettingsSection>

      <SettingsSection
        icon={<AdminPanelSettingsOutlinedIcon />}
        title={t("pages.admin-settings.result-acl.super-users-title")}
        description={t("pages.admin-settings.result-acl.super-users-description")}
      >
        <Autocomplete
          defaultChip={superUserRoles.value}
          setChips={superUserRoles.onChange}
          disabled={superUserRoles.disabled}
        />
      </SettingsSection>

      <SettingsSection
        icon={<VisibilityOutlinedIcon />}
        title={t("pages.admin-settings.result-acl.visible-fields-title")}
        description={t("pages.admin-settings.result-acl.visible-fields-description")}
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
          {t("pages.admin-settings.result-acl.doc-type-note")}
        </Typography>
      </SettingsSection>

      {/* The bottom margin keeps Save clear of the floating buttons, as in the entity wizard */}
      <Box sx={{ display: "flex", justifyContent: "flex-end", marginBottom: "70px" }}>
        <Button variant="contained" onClick={() => form.submit()} disabled={!form.canSubmit}>
          {t("common.save")}
        </Button>
      </Box>

      {riskWarning && (
        <ModalConfirm
          type="warning"
          title={t("pages.admin-settings.result-acl.risk-title")}
          body={riskWarning.body}
          labelConfirm={t("common.confirm")}
          actionConfirm={riskWarning.confirm}
          close={closeRiskWarning}
        />
      )}
    </ContainerFluid>
  );
}
