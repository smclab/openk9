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
