import BlockIcon from "@mui/icons-material/Block";
import VisibilityIcon from "@mui/icons-material/Visibility";
import { IconButton, Skeleton, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, Tooltip, Typography } from "@mui/material";
import { GetApiKeysQuery } from "../../../graphql-generated";
import { apiGroupLabel, deriveDisplayStatus } from "./labels";
import { StatusBadge } from "./statusBadge";

type ApiKey = NonNullable<NonNullable<GetApiKeysQuery["apiKeys"]>[number]>;

type Props = {
  loading: boolean;
  rows: ApiKey[];
  onView: (row: ApiKey) => void;
  onRevoke: (row: ApiKey) => void;
};

function formatDate(value: string | null | undefined) {
  if (!value) return "—";
  return new Date(value).toLocaleDateString();
}

export function ApiKeyTable({ loading, rows, onView, onRevoke }: Props) {
  return (
    <TableContainer>
      <Table size="small">
        <TableHead>
          <TableRow>
            <TableCell>Name</TableCell>
            <TableCell>API Group</TableCell>
            <TableCell>Status</TableCell>
            <TableCell>Created</TableCell>
            <TableCell>Expires</TableCell>
            <TableCell align="right">Actions</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {loading
            ? Array.from({ length: 4 }).map((_, i) => (
                <TableRow key={i}>
                  <TableCell colSpan={6}>
                    <Skeleton variant="text" />
                  </TableCell>
                </TableRow>
              ))
            : rows.map((row) => {
                const status = deriveDisplayStatus(row.status, row.expirationDate);
                const revokeDisabled = status === "REVOKED";
                return (
                  <TableRow key={row.id} hover>
                    <TableCell>
                      <Typography variant="body2" fontWeight={500}>
                        {row.name ?? "—"}
                      </Typography>
                    </TableCell>
                    <TableCell>{row.apiGroup ? apiGroupLabel[row.apiGroup] ?? row.apiGroup : "—"}</TableCell>
                    <TableCell>
                      <StatusBadge status={status} />
                    </TableCell>
                    <TableCell>{formatDate(row.createDate)}</TableCell>
                    <TableCell>{formatDate(row.expirationDate)}</TableCell>
                    <TableCell align="right">
                      <Tooltip title="View details">
                        <IconButton size="small" onClick={() => onView(row)}>
                          <VisibilityIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                      <Tooltip title={revokeDisabled ? "Already revoked" : "Revoke"}>
                        <span>
                          <IconButton size="small" color="error" disabled={revokeDisabled} onClick={() => onRevoke(row)}>
                            <BlockIcon fontSize="small" />
                          </IconButton>
                        </span>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                );
              })}
        </TableBody>
      </Table>
    </TableContainer>
  );
}
