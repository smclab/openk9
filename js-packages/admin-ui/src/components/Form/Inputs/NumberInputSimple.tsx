import { Box, SxProps, TextField, Theme, Typography } from "@mui/material";
import { InformationField } from "../utils/informationField";

export function NumberInputSimple({
  label,
  description,
  value,
  onChange,
  disabled,
  isRequired = false,
  setStyles,
}: {
  label: string;
  description?: string;
  isRequired?: boolean;
  value: number;
  disabled?: boolean;
  onChange(event: any): void;
  setStyles?: SxProps<Theme> | undefined;
}) {
  return (
    <Box key={label + "div"} display={"flex"} flexDirection="column" paddingBottom={"20px"} sx={{ ...setStyles }}>
      <Box
        marginBottom={1}
        display={"flex"}
        flexDirection="row"
        alignItems="center"
        gap="4px"
        justifyContent={"space-between"}
      >
        <div style={{ display: "flex", gap: "10px" }}>
          <Typography variant="subtitle1" component="label">
            {label}
          </Typography>
          {isRequired && (
            <Typography color="error" ml={"3px"}>
              *
            </Typography>
          )}
        </div>
        {description && <InformationField description={description} />}
      </Box>
      <TextField
        id={label + "input"}
        disabled={disabled}
        variant="outlined"
        type="number"
        value={value}
        onChange={onChange}
        fullWidth
      />
    </Box>
  );
}
