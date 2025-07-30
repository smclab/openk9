import { Box, Typography } from "@mui/material";
import React from "react";

export function TitleEntity({
  nameEntity,
  description,
  id,
}: {
  nameEntity?: string;
  description?: string;
  id: string;
}) {
  return (
    <React.Fragment>
      <Box>
        <Typography component="h1" variant="h1" fontWeight="600">
          {id === "new" ? `Create new ${nameEntity}` : `Edit ${nameEntity}`}
        </Typography>
        <p>{description}</p>
      </Box>
    </React.Fragment>
  );
}
