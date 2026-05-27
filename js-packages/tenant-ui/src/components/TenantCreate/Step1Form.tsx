import { Box, TextField } from "@mui/material";
import { WizardState } from "./types";

type Props = {
  values: WizardState["step1"];
  onChange: (next: WizardState["step1"]) => void;
};

function input(values: WizardState["step1"], onChange: Props["onChange"], key: keyof WizardState["step1"]) {
  return {
    value: values[key],
    onChange: (e: React.ChangeEvent<HTMLInputElement>) => onChange({ ...values, [key]: e.target.value }),
  };
}

export function Step1Form({ values, onChange }: Props) {
  return (
    <Box
      sx={{
        display: "grid",
        gridTemplateColumns: "1fr 1fr",
        gap: 3,
        "& .MuiTextField-root": {
          "& .MuiOutlinedInput-root": {
            backgroundColor: "background.default",
          },
        },
      }}
    >
      <TextField label="Tenant Name" fullWidth {...input(values, onChange, "tenantName")} helperText="Optional — schema name auto-generated if empty" />
      <TextField label="Virtual Host" required fullWidth {...input(values, onChange, "virtualHost")} helperText="e.g. pikachu.openk9.io" />
      <TextField label="Client ID" fullWidth {...input(values, onChange, "clientId")} helperText="Optional — leave empty for Keycloak auto-managed realm" />
      <TextField
        label="Client Secret"
        type="password"
        fullWidth
        autoComplete="new-password"
        {...input(values, onChange, "clientSecret")}
        helperText="Optional, leave empty for public clients"
      />
      <TextField
        label="Issuer URI"
        fullWidth
        {...input(values, onChange, "issuerUri")}
        helperText="Optional — leave empty for Keycloak auto-managed realm"
        sx={{ gridColumn: "1 / -1" }}
      />
    </Box>
  );
}

export function isStep1Valid(values: WizardState["step1"]): boolean {
  // tenantName is optional (schema name is auto-generated when omitted).
  if (values.virtualHost.trim().length === 0) {
    return false;
  }
  // clientId and issuerUri are optional, but OAuth2Settings needs both: allow
  // either both filled (external IdP) or both empty (Keycloak auto-managed realm).
  const hasClientId = values.clientId.trim().length > 0;
  const hasIssuerUri = values.issuerUri.trim().length > 0;
  return hasClientId === hasIssuerUri;
}
