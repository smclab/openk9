/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
import { Box, Button, Card, CardContent, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";
import { DataFormCardProps } from "./types";

export default function DataFormCard({
  isVisible,
  onCancel,
  config,
  children,
  onReset,
  onAddField,
}: Omit<DataFormCardProps, "fields" | "options"> & {
  onReset?: () => void;
  onAddField?: () => void;
}) {
  const { t } = useTranslation();
  if (!isVisible) {
    return (
      <Card
        sx={{
          borderRadius: "10px",
          height: "100%",
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          justifyContent: "center",
        }}
      >
        <CardContent sx={{ textAlign: "center" }}>
          <Typography variant="h6" gutterBottom>
            {t("data-form.no-element-selected")}
          </Typography>
          <Typography variant="body2" color="text.secondary" sx={{ maxWidth: 400, mx: "auto", mb: 4 }}>
            {t("data-form.no-element-selected-description", { title: config.title })}
          </Typography>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card sx={{ borderRadius: "10px", height: "100%", overflowY: "auto" }}>
      <CardContent sx={{ display: "flex", flexDirection: "column" }}>
        <Box>
          <Box sx={{ display: "flex", justifyContent: "space-between", mb: 2 }}>
            <Typography variant="h6">{config.title}</Typography>
            <Button variant="outlined" color="inherit" onClick={onCancel}>
              {t("common.cancel")}
            </Button>
          </Box>
          <Typography variant="body2" color="text.secondary" sx={{ mb: 3 }}>
            {config.description}
          </Typography>
          <Box sx={{ overflowY: "auto", maxHeight: "400px", gap: 2, display: "flex", flexDirection: "column" }}>
            <Box
              component="form"
              noValidate
              autoComplete="off"
              display={"grid"}
              gridTemplateColumns={"1fr 1fr"}
              gap={2}
            >
              {children}
            </Box>
          </Box>
        </Box>
        <Box sx={{ gridColumn: "span 2", display: "flex", justifyContent: "flex-end", gap: 2, mt: 2 }}>
          {onReset && <Button onClick={onReset}>{config.resetLabel ?? t("common.reset")}</Button>}
          {config.addLabel && (
            <Button variant="contained" color="primary" onClick={onAddField}>
              {config.addLabel ?? t("common.add")}
            </Button>
          )}
        </Box>
      </CardContent>
    </Card>
  );
}

