import { Box, Typography } from "@mui/material";

export const DateTimeInput = ({
  initialDateTime,
  disabled,
  setDateTime,
}: {
  initialDateTime: any;
  disabled: boolean;
  setDateTime: any;
}) => {
  const handleChange = (event: any) => {
    setDateTime(event.target.value);
  };

  return (
    <Box display={"flex"} flexDirection={"column"} gap={"10px"}>
      <Typography variant="body1">Select Date and Time:</Typography>
      <input
        id="datetime-input"
        type="datetime-local"
        value={initialDateTime}
        onChange={handleChange}
        disabled={disabled}
      />
    </Box>
  );
};
