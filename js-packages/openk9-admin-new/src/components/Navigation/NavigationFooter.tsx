import { Box, Button } from "@mui/material";
import LogoutIcon from "@mui/icons-material/Logout";
import { keycloak } from "../authentication";

export function NavigationFooter() {
  return (
    <Button variant="outlined" onClick={() => keycloak.logout()}>
      <LogoutIcon />
    </Button>
  );
}
