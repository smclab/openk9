import React, { ReactNode } from "react";
import { InformationField } from "./informationField";
import { Box, SxProps, Theme } from "@mui/material";

interface TooltipDescriptionProps {
  children?: ReactNode;
  informationDescription?: string;
  sx?: SxProps<Theme>;
}

export const TooltipDescription: React.FC<TooltipDescriptionProps> = ({ children, informationDescription, sx }) => {
  return (
    <>
      {children ? (
        <Box display={"flex"} flexDirection={"row"} alignItems={"center"} gap={"4px"}>
          {children}
          {informationDescription && <InformationField description={informationDescription} />}
        </Box>
      ) : (
        informationDescription && <InformationField description={informationDescription} />
      )}
    </>
  );
};
