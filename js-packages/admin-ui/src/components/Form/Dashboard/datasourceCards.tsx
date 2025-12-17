import {
  Add as AddIcon,
  Api as ApiIcon,
  Close as CloseIcon,
  Storage as DatabaseIcon,
  Folder as FolderIcon,
  Settings as SettingsIcon,
  Visibility as VisibilityIcon,
  Language as WebIcon,
} from "@mui/icons-material";
import {
  Avatar,
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Divider,
  FormControl,
  IconButton,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
  Typography,
} from "@mui/material";
import { styled, useTheme } from "@mui/material/styles";
import cronstrue from "cronstrue";
import React, { useState } from "react";
import { useNavigate } from "react-router-dom";
import { scrollToTop, themeColor } from "../../../App";
import { useConfirmModal } from "../../../utils/useConfirmModal";

type DatasourceType = "database" | "api" | "filesystem" | "web" | string;
type DatasourceStatus = "active" | "syncing" | "error";

interface Datasource {
  id: number;
  name: string;
  type: string;
  status: DatasourceStatus | string;
  documentsCount: number;
  lastSync: string;
}

interface NewDatasource {
  name: string;
  type: string;
  description: string;
}

const StyledCard = styled(Card)(({ theme }) => ({
  borderRadius: theme.spacing("10px"),
  border: `1px solid ${theme.palette.divider}`,
  transition: "all 0.2s ease-in-out",
  "&:hover": {
    boxShadow: theme.shadows[8],
    transform: "translateY(-2px)",
  },
}));

const StyledDialog = styled(Dialog)(({ theme }) => ({
  "& .MuiPaper-root": {
    borderRadius: theme.spacing(2),
  },
}));

const TypeAvatar = styled(Avatar)<{ bgcolor: string }>(({ theme, bgcolor }) => ({
  width: 40,
  height: 40,
  borderRadius: theme.spacing(1.5),
  backgroundColor: bgcolor,
}));

const DatasourcesSection = ({ datasourcesData }: { datasourcesData: any }) => {
  const [datasources, setDatasources] = useState<Datasource[]>([]);
  React.useEffect(() => {
    if (datasourcesData?.length > 0) {
      setDatasources(datasourcesData);
    }
  }, [datasourcesData]);
  const navigate = useNavigate();

  const initialStateEditMessage = (type: "Edit" | "Create" = "Edit") => ({
    title: `${type} Datasource`,
    body: `Are you sure you want to ${type.toLocaleLowerCase()} this datasource?`,
    labelConfirm: `${type}`,
  });
  type modalMessageType = { title: string; body: string; labelConfirm: string };
  const [modalMessage, setModalMessage] = useState<modalMessageType>(initialStateEditMessage);
  const { openConfirmModal, ConfirmModal } = useConfirmModal(modalMessage);

  const handleEditOrCreateClick = async ({
    type,
    datasourceId,
  }: {
    type: "Edit" | "Create";
    datasourceId?: string;
  }) => {
    setModalMessage(initialStateEditMessage(type));
    const confirmed = await openConfirmModal();
    // setActiveTab("datasource");
    const createOrUpdate =
      type === "Edit"
        ? `/data-source/${datasourceId}/mode/edit/landingTab/datasource`
        : `/data-source/new/mode/create/landingTab/datasource`;
    confirmed && navigate(createOrUpdate);
    confirmed && scrollToTop();
  };

  const [showCreateForm, setShowCreateForm] = useState(false);
  const [newDatasource, setNewDatasource] = useState<NewDatasource>({
    name: "",
    type: "database",
    description: "",
  });

  const getReadableCronDescription = (cronExpression: string): string => {
    try {
      const fullCronExpression = `${cronExpression}`;
      return cronstrue.toString(fullCronExpression);
    } catch (error) {
      return "Invalid cron expression";
    }
  };

  const getTypeConfig = (type: DatasourceType) => {
    const configs: Record<DatasourceType, { icon: React.ReactNode; color: any; bgcolor: string }> = {
      database: {
        icon: <DatabaseIcon />,
        color: "primary",
        bgcolor: "#e3f2fd",
      },
      api: {
        icon: <ApiIcon />,
        color: "secondary",
        bgcolor: "#f3e5f5",
      },
      filesystem: {
        icon: <FolderIcon />,
        color: "warning",
        bgcolor: "#fff3e0",
      },
      web: {
        icon: <WebIcon />,
        color: "info",
        bgcolor: "#e0f2f1",
      },
    };
    return configs[type] || configs.database;
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case "active":
        return "success";
      case "syncing":
        return "warning";
      case "error":
        return "error";
      default:
        return "default";
    }
  };

  const handleCreateDatasource = () => {
    if (newDatasource.name.trim()) {
      const newId = Math.max(...datasources.map((d) => d.id)) + 1;
      const newItem: Datasource = {
        id: newId,
        name: newDatasource.name,
        type: newDatasource.type,
        status: "active",
        documentsCount: 0,
        lastSync: "Just now",
      };
      setDatasources([...datasources, newItem]);
      setNewDatasource({ name: "", type: "database", description: "" });
      setShowCreateForm(false);
    }
  };

  const EmptyState: React.FC = () => (
    <StyledCard>
      <CardContent sx={{ textAlign: "center", py: 8 }}>
        <TypeAvatar bgcolor="#f5f5f5" sx={{ mx: "auto", mb: 2, width: 64, height: 64 }}>
          <DatabaseIcon sx={{ fontSize: 32, color: "#9e9e9e" }} />
        </TypeAvatar>
        <Typography variant="h6" gutterBottom sx={{ fontWeight: 600 }}>
          No Datasources Configured
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mb: 3, maxWidth: 300, mx: "auto" }}>
          Start by creating your first datasource to index and search your data.
        </Typography>
      </CardContent>
    </StyledCard>
  );

  const theme = useTheme();
  const isDarkMode = theme.palette.mode;

  const DatasourceCard: React.FC<{ datasource: any }> = ({ datasource }) => {
    const typeConfig: {
      icon: React.ReactElement;
      color: "success" | "warning" | "error" | "default";
      bgcolor: string;
    } = {
      icon: <DatabaseIcon />,
      color: getStatusColor(datasource?.schedulable ? "active" : "syncing"),
      bgcolor: isDarkMode === "dark" ? "#e3f2fd" : "#86ccff",
    };

    return (
      <StyledCard>
        <CardContent
          style={{
            padding: "10px",
            ...(isDarkMode === "dark" && {
              backgroundColor: themeColor.dark.secondary,
            }),
          }}
        >
          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: "auto 1fr auto",
              gridTemplateRows: "auto auto",
              alignItems: "start",
            }}
          >
            <Box sx={{ gridRow: "1 / 3", alignSelf: "center" }}>
              <TypeAvatar bgcolor={typeConfig.bgcolor} style={{ marginRight: "10px" }}>
                {typeConfig.icon}
              </TypeAvatar>
            </Box>
            <Typography
              variant="subtitle1"
              sx={{
                fontWeight: 600,
                overflow: "hidden",
                textOverflow: "ellipsis",
                fontSize: "0.95rem",
                gridColumn: "2",
                gridRow: "1",
              }}
            >
              {datasource.name}
            </Typography>
            <Box sx={{ gridColumn: "3", gridRow: "1" }}>
              <IconButton
                size="small"
                onClick={() => handleEditOrCreateClick({ datasourceId: datasource.id, type: "Edit" })}
              >
                <SettingsIcon fontSize="small" />
              </IconButton>
            </Box>

            <Box sx={{ gridColumn: "2 / 4", gridRow: "2" }}>
              <Divider sx={{ mb: 1 }} />
              <Stack>
                <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 500 }}>
                    Status
                  </Typography>
                  <Chip
                    label={datasource?.schedulable ? "active" : "idle"}
                    size="small"
                    color={getStatusColor(datasource?.schedulable ? "active" : "syncing")}
                    variant="filled"
                    sx={{
                      height: 20,
                      fontSize: "0.7rem",
                      fontWeight: 500,
                      textTransform: "capitalize",
                    }}
                  />
                </Box>
                <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center" }}>
                  <Typography variant="caption" color="text.secondary" sx={{ fontWeight: 500 }}>
                    Sync Scheduled: {getReadableCronDescription(datasource?.scheduling)}
                  </Typography>
                  <Typography
                    variant="caption"
                    sx={{
                      fontWeight: 500,
                      color: "text.primary",
                      overflow: "hidden",
                      textOverflow: "ellipsis",
                      maxWidth: 100,
                    }}
                  >
                    {datasource.lastSync}
                  </Typography>
                </Box>
              </Stack>
            </Box>
          </Box>
        </CardContent>
      </StyledCard>
    );
  };

  return (
    <>
      <Card
        sx={{
          width: "100%",
          boxShadow: "none",
          padding: "16px 14px",
        }}
      >
        {/* Header */}
        <Box sx={{ display: "flex", justifyContent: "space-between", alignItems: "center", mb: 3 }}>
          <Box>
            <Typography variant="h4" sx={{ fontWeight: 600, color: "text.primary", mb: 0.5 }}>
              Recent Datasources
            </Typography>
            <Typography variant="body2" color="text.secondary">
              Last created datasources
            </Typography>
          </Box>
          <Box display={"flex"} gap={"10px"} flexWrap={"wrap"} justifyContent={"end"}>
            <Button
              variant="contained"
              startIcon={<VisibilityIcon />}
              onClick={async () => {
                const view = {
                  title: `View all Datasources`,
                  body: `Are you sure you want to view all datasources?`,
                  labelConfirm: `View`,
                };
                setModalMessage(view);
                const confirmed = await openConfirmModal();
                confirmed && navigate("/data-sources");
                scrollToTop();
              }}
              sx={{ borderRadius: "10px" }}
            >
              {"View Datasources"}
            </Button>
            <Button
              variant="contained"
              startIcon={<AddIcon />}
              onClick={() => handleEditOrCreateClick({ type: "Create" })}
              sx={{ borderRadius: "10px" }}
            >
              {datasources.length === 0 ? "Create First Datasource" : "New Datasource"}
            </Button>
          </Box>
        </Box>

        {/* Content */}
        {datasources.length === 0 ? (
          <EmptyState />
        ) : (
          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: "repeat(auto-fill, minmax(250px, 1fr))",
              gap: 2,
              px: 0,
            }}
          >
            {datasources.map((datasource) => (
              <Box key={datasource.id} sx={{ width: "100%" }}>
                <DatasourceCard datasource={datasource} />
              </Box>
            ))}
          </Box>
        )}

        <StyledDialog open={showCreateForm} onClose={() => setShowCreateForm(false)} maxWidth="sm" fullWidth>
          <DialogTitle
            sx={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              pb: 1,
            }}
          >
            <Typography variant="h6" sx={{ fontWeight: 600 }}>
              New Datasource
            </Typography>
            <IconButton onClick={() => setShowCreateForm(false)} size="small" sx={{ color: "text.secondary" }}>
              <CloseIcon />
            </IconButton>
          </DialogTitle>

          <DialogContent sx={{ pt: 2 }}>
            <Stack spacing={3}>
              <TextField
                fullWidth
                label="Name"
                value={newDatasource.name}
                onChange={(e) => setNewDatasource({ ...newDatasource, name: e.target.value })}
                placeholder="Datasource name"
                variant="outlined"
                sx={{
                  "& .MuiOutlinedInput-root": {
                    borderRadius: 2,
                    "&:hover fieldset": {
                      borderColor: "#ef4444",
                    },
                    "&.Mui-focused fieldset": {
                      borderColor: "#ef4444",
                    },
                  },
                }}
              />

              <FormControl fullWidth>
                <InputLabel>Type</InputLabel>
                <Select
                  value={newDatasource.type}
                  label="Type"
                  onChange={(e) => setNewDatasource({ ...newDatasource, type: e.target.value })}
                  sx={{
                    borderRadius: 2,
                    "&:hover .MuiOutlinedInput-notchedOutline": {
                      borderColor: "#ef4444",
                    },
                    "&.Mui-focused .MuiOutlinedInput-notchedOutline": {
                      borderColor: "#ef4444",
                    },
                  }}
                >
                  <MenuItem value="database">Database</MenuItem>
                  <MenuItem value="api">REST API</MenuItem>
                  <MenuItem value="filesystem">File System</MenuItem>
                  <MenuItem value="web">Web Scraping</MenuItem>
                </Select>
              </FormControl>

              <TextField
                fullWidth
                label="Description"
                value={newDatasource.description}
                onChange={(e) => setNewDatasource({ ...newDatasource, description: e.target.value })}
                placeholder="Optional description..."
                multiline
                rows={3}
                variant="outlined"
                sx={{
                  "& .MuiOutlinedInput-root": {
                    borderRadius: 2,
                    "&:hover fieldset": {
                      borderColor: "#ef4444",
                    },
                    "&.Mui-focused fieldset": {
                      borderColor: "#ef4444",
                    },
                  },
                }}
              />
            </Stack>
          </DialogContent>

          <DialogActions sx={{ p: 3, pt: 2 }}>
            <Button
              onClick={() => setShowCreateForm(false)}
              variant="outlined"
              sx={{
                borderRadius: 2,
                textTransform: "none",
                flex: 1,
                py: 1.5,
                borderColor: "#d1d5db",
                color: "text.secondary",
                "&:hover": {
                  borderColor: "#9ca3af",
                  bgcolor: "#f9fafb",
                },
              }}
            >
              Cancel
            </Button>
            <Button
              onClick={handleCreateDatasource}
              variant="contained"
              disabled={!newDatasource.name.trim()}
              sx={{
                bgcolor: "#ef4444",
                "&:hover": { bgcolor: "#dc2626" },
                borderRadius: 2,
                textTransform: "none",
                flex: 1,
                py: 1.5,
                "&:disabled": {
                  bgcolor: "#e5e7eb",
                  color: "#9ca3af",
                },
              }}
            >
              Create
            </Button>
          </DialogActions>
        </StyledDialog>
      </Card>
      <ConfirmModal />
    </>
  );
};

export default DatasourcesSection;
