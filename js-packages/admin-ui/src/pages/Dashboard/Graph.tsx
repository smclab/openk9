import TrashIcon from "@mui/icons-material/Delete";
import DocumentIcon from "@mui/icons-material/Description";
import RulerIcon from "@mui/icons-material/Straighten";
import { Box, Card, Typography, useTheme } from "@mui/material";
import DashboardInfoRow from "./DashboardInfoRow";

type detailGraphProps = {
  firstCardNumber: number;
  secondCardNumber: number;
  thirdCardNumber: number;
  firstCardLabel: string;
  secondCardLabel: string;
  thirdCardLabel: string;
  firstCardUnity?: string;
  secondCardUnity?: string;
  thirdCardUnity?: string;
};

export function DetailGraph({
  firstCardLabel,
  secondCardLabel,
  thirdCardLabel,
  firstCardNumber,
  secondCardNumber,
  thirdCardNumber,
  firstCardUnity = "",
  secondCardUnity = "",
  thirdCardUnity = "",
}: detailGraphProps) {
  const theme = useTheme();

  return (
    <Box display="flex" flexDirection="row" gap="14px" flex={1}>
      {/* <DashboardCard title="Document information"> */}
      {/* <Box display="flex" flexDirection="column" gap="14px"> */}
      <Card sx={{ flex: 1 }}>
        <DashboardInfoRow
          icon={
            <DocumentIcon
              sx={{
                color: theme.palette.info.main,
                fontSize: 26,
              }}
            />
          }
          label={firstCardLabel}
          value={
            <Typography
              variant="h6"
              sx={{
                color: theme.palette.info.main,
                fontWeight: 800,
                fontFamily: "Nunito Sans",
                ml: "auto",
                fontSize: "1.3rem",
              }}
            >
              {firstCardNumber}
              <span
                style={{
                  fontSize: "0.95rem",
                  fontWeight: 400,
                  marginLeft: 4,
                  color: theme.palette.text.secondary,
                }}
              >
                {firstCardUnity}
              </span>
            </Typography>
          }
          borderColor={theme.palette.info.main}
        />
      </Card>
      {/* <Divider></Divider> */}
      <Card sx={{ flex: 1 }}>
        <DashboardInfoRow
          icon={
            <TrashIcon
              sx={{
                color: theme.palette.error.main,
                fontSize: 26,
              }}
            />
          }
          label={secondCardLabel}
          value={
            <Typography
              variant="h6"
              sx={{
                color: theme.palette.error.main,
                fontWeight: 800,
                fontFamily: "Nunito Sans",
                ml: "auto",
                fontSize: "1.3rem",
              }}
            >
              {secondCardNumber}
              <span
                style={{
                  fontSize: "0.95rem",
                  fontWeight: 400,
                  marginLeft: 4,
                  color: theme.palette.text.secondary,
                }}
              >
                {secondCardUnity}
              </span>
            </Typography>
          }
          borderColor={theme.palette.error.main}
        />
      </Card>

      {/* <Divider></Divider> */}
      <Card sx={{ flex: 1 }}>
        <DashboardInfoRow
          icon={
            <RulerIcon
              sx={{
                color: theme.palette.success.main,
                fontSize: 26,
              }}
            />
          }
          label={thirdCardLabel}
          value={
            <Typography
              variant="h6"
              sx={{
                color: theme.palette.success.main,
                fontWeight: 800,
                fontFamily: "Nunito Sans",
                ml: "auto",
                fontSize: "1.3rem",
              }}
            >
              {thirdCardNumber}
              <span
                style={{
                  fontSize: "0.95rem",
                  fontWeight: 400,
                  marginLeft: 4,
                  color: theme.palette.text.secondary,
                }}
              >
                {thirdCardUnity}
              </span>
            </Typography>
          }
          borderColor={theme.palette.success.main}
        />
      </Card>
    </Box>
  );
}

export function LabelNumber({
  label,
  number,
  unity,
  icon,
  borderColor,
}: {
  label: string;
  number: number;
  unity?: string;
  icon?: React.ReactNode;
  borderColor?: string;
}) {
  const theme = useTheme();

  return (
    <Box
      display="flex"
      alignItems="center"
      width="100%"
      sx={{
        borderLeft: `4px solid ${borderColor}`,
        pl: 1.5,
        background: theme.palette.mode === "dark" ? "rgba(30,32,36,0.92)" : "rgba(255,255,255,0.92)",
        borderRadius: "6px",
        boxShadow: theme.shadows[1],
        minHeight: 44,
      }}
    >
      <Box>{icon}</Box>
      <Typography
        variant="subtitle2"
        sx={{
          fontWeight: 600,
          flex: 1,
          color: theme.palette.text.primary,
        }}
      >
        {label}
      </Typography>
      <Typography
        variant="h6"
        sx={{
          color: borderColor,
          fontWeight: 800,
          fontFamily: "Nunito Sans",
          ml: "auto",
          transition: "color 0.2s",
          fontSize: "1.3rem",
        }}
      >
        {number}
        <span
          style={{
            fontSize: "0.95rem",
            fontWeight: 400,
            marginLeft: 4,
            color: theme.palette.text.secondary,
          }}
        >
          {unity}
        </span>
      </Typography>
    </Box>
  );
}
