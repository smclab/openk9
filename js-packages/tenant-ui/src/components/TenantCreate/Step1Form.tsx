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
      <TextField label="Tenant Name" required fullWidth {...input(values, onChange, "tenantName")} helperText="Unique tenant identifier" />
      <TextField label="Virtual Host" required fullWidth {...input(values, onChange, "virtualHost")} helperText="e.g. pikachu.openk9.io" />
      <TextField label="Client ID" required fullWidth {...input(values, onChange, "clientId")} helperText="OAuth2 client identifier" />
      <TextField label="Client Secret" type="password" fullWidth {...input(values, onChange, "clientSecret")} helperText="Optional, leave empty for public clients" />
      <TextField
        label="Issuer URI"
        required
        fullWidth
        {...input(values, onChange, "issuerUri")}
        helperText="OIDC issuer URI"
        sx={{ gridColumn: "1 / -1" }}
      />
    </Box>
  );
}

export function isStep1Valid(values: WizardState["step1"]): boolean {
  return values.tenantName.trim().length > 0 && values.virtualHost.trim().length > 0 && values.clientId.trim().length > 0 && values.issuerUri.trim().length > 0;
}
