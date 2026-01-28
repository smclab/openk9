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
import { Card, Box, Typography, CardContent, useTheme } from "@mui/material";
import { LineChart, XAxis, YAxis, CartesianGrid, Line, Legend, Tooltip as TooltipRecharts } from "recharts";

export function CreateGraphic({
  data,
  width = 600,
  height = 250,
  labelInformationRigth,
  Information,
}: {
  data: any;
  width: number;
  height: number;
  labelInformationRigth: string;
  Information: string;
}) {
  const theme = useTheme();
  const renderCustomTooltip = (props: any) => {
    const { active, payload, label } = props;
    if (active && payload && payload.length) {
      const { name, query } = payload[0].payload; // Dati da mostrare

      return (
        <div
          style={{
            backgroundColor: theme.palette.background.paper,
            color: theme.palette.text.primary,
            padding: "10px",
            borderRadius: "8px",
            border: `1px solid ${theme.palette.divider}`,
          }}
        >
          <Typography variant="body2" style={{ marginBottom: "5px" }}>
            <strong>{label}</strong>
          </Typography>
          <Typography variant="body2">{`Name: ${name}`}</Typography>
          <Typography variant="body2">{`Query: ${query}`}</Typography>
        </div>
      );
    }

    return null;
  };

  return (
    <Card style={{ flex: "1", borderRadius: "10px" }}>
      <Box display="flex" flexDirection="column" width="100%">
        <Box display="flex" alignItems="baseline" justifyContent="space-between">
          <Typography variant="h6" style={{ marginLeft: "20px", marginTop: "10px" }}>
            {Information}
          </Typography>
          <Typography
            variant="subtitle1"
            style={{
              marginRight: "20px",
              color: "#9C0E10",
              cursor: "pointer",
              textDecoration: "underline",
            }}
          >
            {labelInformationRigth}
          </Typography>
        </Box>
        <Typography
          variant="body2"
          style={{
            marginLeft: "20px",
            marginTop: "3px",
            color: "#71717A",
            fontSize: "18px",
            fontWeight: "400",
          }}
        >
          Last 7 days
        </Typography>
      </Box>
      <CardContent style={{ width: "100%" }}>
        <LineChart width={width} height={height} data={data}>
          <XAxis dataKey="name" />
          <YAxis tickCount={11} />
          <CartesianGrid stroke="#eee" strokeDasharray="5 5" />
          <Line type="monotone" dataKey="query" stroke="#C0272B" strokeWidth={2} dot={false} />
          <TooltipRecharts content={renderCustomTooltip} />
          <Legend />
        </LineChart>
      </CardContent>
    </Card>
  );
}

