import { Chip } from "@mui/material";
import { ApiKeyDisplayStatus } from "./labels";

const config: Record<ApiKeyDisplayStatus, { label: string; color: "success" | "warning" | "error" }> = {
  ACTIVE: { label: "Active", color: "success" },
  EXPIRED: { label: "Expired", color: "warning" },
  REVOKED: { label: "Revoked", color: "error" },
};

export function StatusBadge({ status }: { status: ApiKeyDisplayStatus }) {
  const { label, color } = config[status];
  return <Chip size="small" label={label} color={color} variant="filled" />;
}
