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
