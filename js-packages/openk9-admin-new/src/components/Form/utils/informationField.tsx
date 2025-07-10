import { SxProps, Theme, Tooltip } from "@mui/material";
import InfoIcon from "@mui/icons-material/Info";

export function InformationField({ description, sx }: { description: string; sx?: SxProps<Theme> }) {
  return (
    <Tooltip style={{ marginLeft: "2.5px" }} title={description}>
      <InfoIcon sx={{ cursor: "pointer", fontSize: "20px" }} />
    </Tooltip>
  );
}
