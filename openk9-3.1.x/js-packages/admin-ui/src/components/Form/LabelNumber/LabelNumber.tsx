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
import { Card, Box, Typography } from "@mui/material";

export function LabelNumber({
  label,
  number,
  unity,
  icon,
}: {
  label: string;
  number: number;
  unity?: string;
  icon?: React.ReactNode;
}) {
  return (
    <Card
      style={{
        maxHeight: "fit-content",
        // maxWidth: "200px",
        flex: "1",
        borderRadius: "10px",
        display: "flex",
        flexDirection: "column",
        justifyContent: "space-between",
        padding: "20px",
      }}
    >
      <Box>
        <Typography variant="subtitle1" style={{ fontWeight: "600" }}>
          {label}
        </Typography>
        <Box display="flex" alignItems="center" justifyContent="space-between" marginTop={2}>
          <Typography
            variant="h4"
            style={{
              color: "#c0272b",
              fontWeight: "800",
              fontFamily: "Nunito Sans",
            }}
          >
            {`${number} ${unity || ""}`}
          </Typography>
          {icon}
        </Box>
      </Box>
    </Card>
  );
}

