import Box from "@mui/material/Box";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Container from "@mui/material/Container";
import Typography from "@mui/material/Typography";
import React from "react";
import { getUserProfile } from "./authentication";
import { BrandLogo } from "./BrandLogo";

export function bytesToMegabytes(bytes: number): number {
  const megabytes = bytes / (1024 * 1024);
  return parseFloat(megabytes.toFixed(4));
}

export function DashBoard() {
  const [user, setUser] = React.useState<string | undefined>();
  React.useEffect(() => {
    getUserProfile().then((data) => {
      setUser(JSON.parse(JSON.stringify(data))?.name);
    });
  }, []);

  return (
    <Container maxWidth="lg">
      <Box sx={{ display: "flex", gap: 3, mt: 3 }}>
        <Presentation user={user || ""} />
      </Box>
    </Container>
  );
}

function Presentation({ user }: { user: string }) {
  return (
    <Card sx={{ ml: 1, maxHeight: 350, borderRadius: 2, maxWidth: 400, position: "relative", flex: "1 1 0" }}>
      <CardContent>
        <Box sx={{ position: "absolute", right: 0, bottom: 0, zIndex: 0 }}>
          <BrandLogo colorFill={"#bd61612e"} width={270} height={220} viewBox="0 0 75 73" />
        </Box>
        <Typography variant="h5" component="div" sx={{ m: 2, fontWeight: "bold", mt: 2, ml: 2, position: "relative", zIndex: 1 }}>
          {`Welcome ${user || "no name"}`}
        </Typography>
        <Typography variant="body1" sx={{ m: 2, position: "relative", zIndex: 1 }}>
          OpenK9 is a complete Cognitive Enterprise Search solution that fits all your needs. Powerful, Modern and Flexible, it employs
          Machine Learning to enrich your data and give the best experience possible.
        </Typography>
      </CardContent>
    </Card>
  );
}
