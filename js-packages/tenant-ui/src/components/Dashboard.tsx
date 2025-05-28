import React from "react";
import { Link } from "react-router-dom";
import { getUserProfile } from "./authentication";
import { BrandLogo } from "./BrandLogo";
import Card from "@mui/material/Card";
import CardContent from "@mui/material/CardContent";
import Typography from "@mui/material/Typography";
import Box from "@mui/material/Box";
import List from "@mui/material/List";
import ListItem from "@mui/material/ListItem";
import ListItemIcon from "@mui/material/ListItemIcon";
import ListItemText from "@mui/material/ListItemText";
import Paper from "@mui/material/Paper";
import PublicIcon from "@mui/icons-material/Public";
import ArchiveIcon from "@mui/icons-material/Archive";
import BusinessIcon from "@mui/icons-material/Business";
import MailOutlineIcon from "@mui/icons-material/MailOutline";
import Container from "@mui/material/Container";

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
        <WizardList />
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

function WizardList() {
  return (
    <Paper elevation={2} sx={{ maxWidth: 420, borderRadius: 2, flex: "1 1 0", p: 0 }}>
      <Box sx={{ bgcolor: "white", pt: 2.5, borderTopLeftRadius: 2, borderTopRightRadius: 2 }}>
        <Typography variant="h6" sx={{ pl: 2 }}>
          <Link to="wizards" style={{ color: "inherit", textDecoration: "none", cursor: "pointer" }}>
            Connect your stuff
          </Link>
        </Typography>
      </Box>
      <List>
        <WizardListItem
          icon={<PublicIcon sx={{ color: "#9C0E10" }} />}
          to="wizards/web-crawler"
          title="Web Crawler"
          description="A web crawler that indexes a web-site"
          firstElement={true}
        />
        <WizardListItem
          icon={<ArchiveIcon sx={{ color: "#9C0E10" }} />}
          to="wizards/database"
          title="Database"
          description="Index a database query"
        />
        <WizardListItem
          icon={<BusinessIcon sx={{ color: "#9C0E10" }} />}
          to="wizards/sitemap"
          title="Site Map"
          description="Index a site-map xml file"
        />
        <WizardListItem
          icon={<MailOutlineIcon sx={{ color: "#9C0E10" }} />}
          to="wizards/server-email"
          title="Email Server"
          description="Index emails"
          lastElement={true}
        />
      </List>
    </Paper>
  );
}

type WizardListItemProps = {
  icon: React.ReactNode;
  to: string;
  title: string;
  description: string;
  firstElement?: boolean;
  lastElement?: boolean;
};
function WizardListItem({ icon, to, title, description, firstElement, lastElement }: WizardListItemProps) {
  return (
    <ListItem
      alignItems="flex-start"
      sx={{
        borderTop: firstElement ? "none" : undefined,
        mt: firstElement ? "-8px" : undefined,
        borderBottomLeftRadius: lastElement ? 2 : 0,
        borderBottomRightRadius: lastElement ? 2 : 0,
        bgcolor: "background.paper",
      }}
    >
      <ListItemIcon>
        <Box
          sx={{
            bgcolor: "var(--openk9-embeddable-dashboard--secondary-color, #f5f5f5)",
            p: 1,
            display: "flex",
            borderRadius: "50%",
            color: "#9C0E10",
          }}
        >
          {icon}
        </Box>
      </ListItemIcon>
      <ListItemText
        primary={
          <Link to={to} style={{ color: "inherit", textDecoration: "none" }}>
            {title}
          </Link>
        }
        secondary={description}
      />
    </ListItem>
  );
}
