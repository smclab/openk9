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

// Suggest a virtual host as <tenantName>.<base domain>, where the base domain
// is the last two labels of the host serving the wizard (e.g. openk9.localhost).
export function deriveVirtualHost(tenantName: string, hostname: string): string {
  const name = tenantName.trim();
  if (!name) {
    return "";
  }
  return `${name}.${hostname.split(".").slice(-2).join(".")}`;
}

export function Step1Form({ values, onChange }: Props) {
  const baseHostname = window.location.hostname;
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
      <TextField
        label="Tenant Name"
        fullWidth
        value={values.tenantName}
        onChange={(e) => {
          const tenantName = e.target.value;
          // Keep auto-filling virtualHost until the user diverges from the suggestion.
          const tracksSuggestion = values.virtualHost === "" || values.virtualHost === deriveVirtualHost(values.tenantName, baseHostname);
          onChange({
            ...values,
            tenantName,
            virtualHost: tracksSuggestion ? deriveVirtualHost(tenantName, baseHostname) : values.virtualHost,
          });
        }}
        helperText="Optional — tenant name auto-generated if empty. Also fills Virtual Host."
      />
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
