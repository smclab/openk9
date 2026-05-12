import {
  Alert,
  Box,
  Card,
  CardActionArea,
  Chip,
  CircularProgress,
  Radio,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
} from "@mui/material";
import { PreconfigurationsQuery, usePreconfigurationsQuery } from "../../graphql-generated";
import { apiGroupLabel, authSchemeColor, authSchemeLabel, securityConfigDescription, securityConfigLabel, SecurityConfigurationKey, WizardState } from "./types";

type Preconfig = NonNullable<NonNullable<PreconfigurationsQuery["preconfigurations"]>[number]>;

type Props = {
  values: WizardState["step2"];
  onChange: (next: WizardState["step2"]) => void;
};

export function Step2Security({ values, onChange }: Props) {
  const { data, loading, error } = usePreconfigurationsQuery();
  const presets = (data?.preconfigurations ?? []).filter((p): p is Preconfig => !!p);
  const selected = presets.find((p) => p.name === values.securityConfiguration);

  if (loading) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", py: 6 }}>
        <CircularProgress />
      </Box>
    );
  }
  if (error) {
    return <Alert severity="error">Failed to load security presets: {error.message}</Alert>;
  }

  return (
    <Box sx={{ display: "flex", flexDirection: "column", gap: 3 }}>
      <Typography variant="body2" color="text.secondary">
        Select one preset. The table below previews the resulting authorization scheme per API group.
      </Typography>

      <Box sx={{ display: "grid", gridTemplateColumns: "repeat(auto-fill, minmax(280px, 1fr))", gap: 2 }}>
        {presets.map((preset) => {
          const key = preset.name as SecurityConfigurationKey;
          const isSelected = values.securityConfiguration === key;
          return (
            <Card key={key} variant="outlined" sx={{ borderColor: isSelected ? "primary.main" : undefined, borderWidth: isSelected ? 2 : 1 }}>
              <CardActionArea onClick={() => onChange({ securityConfiguration: key })} sx={{ p: 2, alignItems: "flex-start" }}>
                <Box sx={{ display: "flex", alignItems: "flex-start", gap: 1 }}>
                  <Radio checked={isSelected} size="small" sx={{ p: 0, mt: 0.5 }} />
                  <Box>
                    <Typography variant="subtitle2" fontWeight={600}>
                      {securityConfigLabel[key] ?? key}
                    </Typography>
                    <Typography variant="caption" color="text.secondary">
                      {securityConfigDescription[key] ?? ""}
                    </Typography>
                  </Box>
                </Box>
              </CardActionArea>
            </Card>
          );
        })}
      </Box>

      {selected && (
        <TableContainer component={Box} sx={{ border: "1px solid", borderColor: "divider", borderRadius: 1 }}>
          <Table size="small">
            <TableHead>
              <TableRow>
                <TableCell>API Group</TableCell>
                <TableCell>Authorization</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {(selected.configs ?? []).map((c, i) => {
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
      )}

      {/* TODO #1658 follow-up: per-route override requires BE mutation accepting List<Config>. */}
    </Box>
  );
}

export function isStep2Valid(values: WizardState["step2"]): boolean {
  return values.securityConfiguration !== null;
}
