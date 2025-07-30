import { Box, TextField, Typography } from "@mui/material";
import { InformationField } from "../utils/informationField";

export function NumberInputSimple({
  label,
  description,
  value,
  onChange,
  disabled,
  isRequired = false,
}: {
  label: string;
  description?: string;
  isRequired?: boolean;
  value: number;
  disabled?: boolean;
  onChange(event: any): void;
}) {
  return (
    <Box key={label + "div"} display={"flex"} flexDirection="column" paddingBottom={"20px"}>
      <Box marginBottom={1} display={"flex"} flexDirection="row" alignItems="center" gap="4px">
        <Typography variant="subtitle1" component="label">
          {label}
        </Typography>
        {isRequired && (
          <Typography color="error" ml={"3px"}>
            *
          </Typography>
        )}
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
