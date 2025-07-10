import { Box, styled } from "@mui/material";

interface BoxAreaProps {
  isActive: boolean;
}

export const BoxArea = styled(Box)<BoxAreaProps>`
  padding: 35px;
  width: 100%;
  border-radius: 20px;
  border: ${(props) => (!props?.isActive ? "2px solid #80808038" : "2px solid #ff00002e")};
`; 