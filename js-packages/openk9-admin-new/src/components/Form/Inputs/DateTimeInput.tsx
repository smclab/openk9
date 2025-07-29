import { Box, Typography } from "@mui/material";
import { styled } from "styled-components";

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
      <DateTimeInputCustom
        id="datetime-input"
        type="datetime-local"
        value={initialDateTime}
        onChange={handleChange}
        disabled={disabled}
      />
    </Box>
  );
};

export const DateTimeInputCustom = styled.input`
  padding: 8px;
  border-radius: 8px;
  width: fit-content;
  border: 1px solid #80808038;
`;
