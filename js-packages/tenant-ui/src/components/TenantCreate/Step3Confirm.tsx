import { Box, Chip, Divider, Paper, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Typography } from "@mui/material";
import { usePreconfigurationsQuery } from "../../graphql-generated";
import { apiGroupLabel, authSchemeColor, authSchemeLabel, securityConfigLabel, WizardState } from "./types";

type Props = {
  values: WizardState;
};

function Field({ label, value }: { label: string; value: string | null | undefined }) {
  return (
    <Box>
      <Typography variant="caption" color="text.secondary">
        {label}
      </Typography>
      <Typography variant="body1" sx={{ wordBreak: "break-all" }}>
        {value || "—"}
      </Typography>
    </Box>
  );
}

export function Step3Confirm({ values }: Props) {
  const { data } = usePreconfigurationsQuery({
    fetchPolicy: "cache-first",
    nextFetchPolicy: "cache-only",
  });
  const selected = (data?.preconfigurations ?? []).find((p) => p?.name === values.step2.securityConfiguration);

  return (
    <Stack spacing={3}>
      <Paper variant="outlined" sx={{ p: 3 }}>
        <Typography variant="subtitle1" gutterBottom>
          Gateway Configuration
        </Typography>
        <Divider sx={{ mb: 2 }} />
        <Box sx={{ display: "grid", gridTemplateColumns: "1fr 1fr", gap: 2 }}>
          <Field label="Tenant Name" value={values.step1.tenantName} />
          <Field label="Virtual Host" value={values.step1.virtualHost} />
          <Field label="Client ID" value={values.step1.clientId} />
          <Field label="Client Secret" value={values.step1.clientSecret ? "••••••••" : "(empty)"} />
          <Box sx={{ gridColumn: "1 / -1" }}>
            <Field label="Issuer URI" value={values.step1.issuerUri} />
          </Box>
        </Box>
      </Paper>

      <Paper variant="outlined" sx={{ p: 3 }}>
        <Box sx={{ display: "flex", alignItems: "center", justifyContent: "space-between", mb: 1 }}>
          <Typography variant="subtitle1">Route Protection</Typography>
          {values.step2.securityConfiguration && (
            <Chip label={securityConfigLabel[values.step2.securityConfiguration] ?? values.step2.securityConfiguration} color="primary" size="small" />
          )}
        </Box>
        <Divider sx={{ mb: 2 }} />
        <TableContainer>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>API Group</TableCell>
                <TableCell>Authorization</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {(selected?.configs ?? []).map((c, i) => {
                if (!c) return null;
                const groupKey = c.apiGroup as string;
                const schemeKey = c.authScheme as string;
                return (
                  <TableRow key={`${groupKey}-${i}`}>
                    <TableCell>{apiGroupLabel[groupKey] ?? groupKey}</TableCell>
                    <TableCell>
                      <Chip size="small" label={authSchemeLabel[schemeKey] ?? schemeKey} color={authSchemeColor[schemeKey] ?? "default"} />
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </TableContainer>
      </Paper>
    </Stack>
  );
}
