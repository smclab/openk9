import CheckIcon from "@mui/icons-material/Check";
import { Box, ButtonBase, Card, CardContent, Grid, Typography, useTheme } from "@mui/material";
import { red } from "@mui/material/colors";
import { PluginDriverType, Provisioning } from "../../../../../graphql-generated";

interface PluginDriver {
  __typename?: "PluginDriver";
  id?: string | null;
  name?: string | null;
  description?: string | null;
  type?: PluginDriverType | null;
  provisioning?: Provisioning | null;
}

interface CardsProps {
  systemPluginDrivers: (PluginDriver | null)[] | undefined;
  disabled: boolean;
  activeCardId: string | null;
  setActiveCardId: (id: string | null, name: string | null) => void;
}

export function PluginDriverCards({ systemPluginDrivers, disabled, activeCardId, setActiveCardId }: CardsProps) {
  const handleToggle = (id: string | null | undefined, name: string | null | undefined) => {
    if (!id) return;
    setActiveCardId(activeCardId === id ? null : id, name || null);
  };

  const theme = useTheme();

  const borderColor = theme.palette.mode === "dark" ? "rgba(255, 255, 255, 0.12)" : "rgba(0, 0, 0, 0.12)";

  return (
    <Grid container spacing={3}>
      {systemPluginDrivers?.map((driver) => {
        if (!driver || !driver.id) return null;
        const isActive = activeCardId === driver.id;

        return (
          <Grid item xs={12} sm={6} md={4} key={driver.id}>
            <ButtonBase
              disabled={disabled}
              sx={{ width: "100%", borderRadius: "10px" }}
              onClick={() => handleToggle(driver.id, driver.name)}
            >
              <Card
                sx={{
                  position: "relative",
                  height: "150px",
                  display: "flex",
                  flex: 1,
                  flexDirection: "column",
                  justifyContent: "center",
                  alignItems: "center",
                  transition: "border 0.3s ease",
                  border: isActive ? `2px solid ${red[500]}` : `1px solid ${borderColor}`,
                  boxShadow: isActive ? "0 4px 8px rgba(0, 0, 0, 0.2)" : "none",
                }}
              >
                {isActive && (
                  <Box
                    sx={{
                      position: "absolute",
                      top: 10,
                      right: 10,
                      width: 20,
                      height: 20,
                      borderRadius: "50%",
                      backgroundColor: red[500],
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                    }}
                  >
                    <CheckIcon sx={{ color: "white", fontSize: 16 }} />
                  </Box>
                )}
                <CardContent>
                  <Typography variant="h5" sx={{ opacity: disabled ? "0.3" : "unset" }}>
                    {driver.name || "Unnamed Plugin"}
                  </Typography>
                  <Typography variant="body2" color="textSecondary" sx={{ opacity: disabled ? "0.3" : "unset" }}>
                    {driver.description || "No description available"}
                  </Typography>
                </CardContent>
              </Card>
            </ButtonBase>
          </Grid>
        );
      })}
    </Grid>
  );
}
