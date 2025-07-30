import { Box, FormControlLabel, Switch, Typography } from "@mui/material";
import { InformationField } from "../utils/informationField";

export function InputBooleanSimple({
  keyofF,
  description,
  value,
  onChange,
  disabled,
}: {
  keyofF: string;
  description: string;
  value: boolean;
  onChange(event: any): void;
  disabled?: boolean;
}) {
  return (
    <div key={keyofF} style={{ paddingBottom: "18px" }}>
      <Box marginBottom={1} display={"flex"} flexDirection="row" alignItems="center" gap="4px">
        <Typography variant="subtitle1" component="label" htmlFor={keyofF + "label"}>
          {keyofF}
        </Typography>
        {description && <InformationField description={description} />}
      </Box>
      <FormControlLabel
        control={<Switch checked={value} onChange={onChange} id={keyofF} disabled={disabled} />}
        label={keyofF}
      />
    </div>
  );
}
