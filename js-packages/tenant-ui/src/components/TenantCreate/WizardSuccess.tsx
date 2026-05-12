import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import { Box, Button, Paper, Stack, Typography } from "@mui/material";

type Props = {
  tenantId: string;
  tenantName: string;
  onViewDetails: () => void;
  onGoToApiKeys: () => void;
  onCreateAnother: () => void;
};

export function WizardSuccess({ tenantName, onViewDetails, onGoToApiKeys, onCreateAnother }: Props) {
  return (
    <Paper variant="outlined" sx={{ p: 4, textAlign: "center" }}>
      <CheckCircleIcon sx={{ fontSize: 64, color: "success.main", mb: 2 }} />
      <Typography variant="h5" gutterBottom>
        Tenant created
      </Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
        Tenant <strong>{tenantName}</strong> has been provisioned.
      </Typography>
      <Stack direction={{ xs: "column", sm: "row" }} spacing={2} justifyContent="center">
        <Button variant="contained" onClick={onViewDetails}>
          View Tenant Details
        </Button>
        <Button variant="outlined" onClick={onGoToApiKeys}>
          Go to API Keys
        </Button>
        <Button variant="text" onClick={onCreateAnother}>
          Create Another Tenant
        </Button>
      </Stack>
      <Box sx={{ mt: 3 }} />
    </Paper>
  );
}
