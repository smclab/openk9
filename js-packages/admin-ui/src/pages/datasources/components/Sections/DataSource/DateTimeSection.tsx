import React, { useState } from "react";
import { Box, ButtonGroup, Button, Typography, IconButton, Grid } from "@mui/material";
import CronEditor from "./Cron";
import { ConnectionData } from "../../../types";
import { Lock as LockIcon, LockOpen as LockOpenIcon } from "@mui/icons-material";
import { useTheme } from "@mui/material/styles";
import { darken } from "@mui/material/styles";

interface DateTimeSectionProps {
  dataDatasource: ConnectionData;
  setDataDatasource: React.Dispatch<React.SetStateAction<ConnectionData>>;
  disabled?: boolean;
  //   isActive?: boolean;
}

export type CronDataManagement = {
  type: "reindex" | "scheduling" | "purge";
  DayOfMonth: string | null;
  DayOfWeek: string | null;
  Hour: string | null;
  Minute: string | null;
  Month: string | null;
  isEnabled: boolean;
  purge_max_age?: string | null;
};

export const DateTimeSection: React.FC<DateTimeSectionProps> = ({
  dataDatasource,
  setDataDatasource,
  disabled = false,
  //   isActive = false
}) => {
  const [activeSection, setActiveSection] = React.useState<"reindex" | "scheduling" | "purge">("reindex");
  const [expandedLockSection, setExpandedLockSection] = useState<string | null>(null);
  const theme = useTheme();

  const sections = [
    {
      id: "reindex",
      title: "Reindex",
      active: dataDatasource.isCronSectionreindex,
    },
    {
      id: "scheduling",
      title: "Scheduling",
      active: dataDatasource.isCronSectionscheduling,
    },
    {
      id: "purge",
      title: "Purge",
      active: dataDatasource.isCronSectionpurge,
    },
  ];

  const toggleSectionDisabled = (sectionId: string) => {
    const section = sections.find((s) => s.id === sectionId);
    if (section) {
      setDataDatasource((prev) => ({
        ...prev,
        [`isCronSection${sectionId}`]: !prev[`isCronSection${sectionId}`],
      }));
    }
  };

  return (
    <Box display="flex" flexDirection="column" gap={2}>
      <Typography variant="h2">Data Time</Typography>

      <Box sx={{ display: "flex", justifyContent: "center", alignItems: "center" }}>
        <ButtonGroup aria-label="section selector" sx={{ width: "100%" }}>
          {sections.map((section) => {
            const isCurrentActive = activeSection === section.id;
            const isActive = section.active;
            const isLockExpanded = expandedLockSection === section.id;

            return (
              <Button
                size="small"
                key={section.id}
                onClick={() => setActiveSection(section.id as typeof activeSection)}
                onFocus={() => setExpandedLockSection(section.id)}
                onBlur={() => setExpandedLockSection(null)}
                sx={{
                  padding: 0,
                  position: "relative",
                  flex: 1,
                  opacity: isActive ? 1 : 0.5,
                  backgroundColor: isCurrentActive ? theme.palette.primary.main : "transparent",
                  color: isCurrentActive ? "white" : "inherit",
                  display: "grid",
                  gridTemplateRows: "minmax(20px, auto) minmax(20px, auto) minmax(20px, auto)",
                  gridTemplateColumns: "1fr",
                  textAlign: "center",
                  height: "60px",
                  minWidth: 0,
                  "&:hover": {
                    backgroundColor: isCurrentActive ? darken(theme.palette.primary.main, 0.2) : "rgba(0,0,0,0.04)",
                  },
                }}
              >
                <Grid
                  container
                  sx={{
                    justifyContent: "flex-end",
                    gridRow: 1,
                    alignItems: "center",
                    px: 0.5,
                  }}
                >
                  <IconButton
                    size="small"
                    onClick={(e) => {
                      e.stopPropagation();
                      if (!disabled) {
                        toggleSectionDisabled(section.id);
                      }
                    }}
                    onMouseEnter={() => setExpandedLockSection(section.id)}
                    onMouseLeave={() => setExpandedLockSection(null)}
                    disabled={disabled}
                    sx={{
                      color: isActive ? "white" : "error.main",
                      display: "flex",
                      alignItems: "center",
                      gap: isLockExpanded ? 1 : 0,
                      overflow: "hidden",
                      transition: "all 0.5s ease",
                      width: isLockExpanded ? "auto" : "30px",
                      height: "20px",
                      backgroundColor: "#333",
                      borderRadius: "4px",
                      padding: isLockExpanded ? "0 8px" : "0",
                      opacity: disabled ? 0.5 : 1,
                      cursor: disabled ? "not-allowed" : "pointer",
                      ":hover": {
                        backgroundColor: "#333",
                      },
                    }}
                  >
                    {isActive ? (
                      <LockOpenIcon style={{ fontSize: "16px" }} />
                    ) : (
                      <LockIcon style={{ fontSize: "16px" }} />
                    )}
                    <Typography
                      variant="caption"
                      sx={{
                        color: "white",
                        whiteSpace: "nowrap",
                        fontSize: "10px",
                        transition: "opacity 0.5s ease",
                        pointerEvents: isLockExpanded ? "auto" : "none",
                        ml: isLockExpanded ? 0.5 : 0,
                      }}
                    >
                      {isLockExpanded && (isActive ? "Disabilita" : "Abilita")}
                    </Typography>
                  </IconButton>
                </Grid>

                <Typography
                  variant="caption"
                  sx={{
                    gridRow: 2,
                    display: "flex",
                    alignItems: "center",
                    justifyContent: "center",
                    fontSize: "12px",
                    fontWeight: isCurrentActive ? "bold" : "normal",
                  }}
                >
                  {section.title}
                </Typography>

                <div
                  style={{
                    gridRow: 3,
                    backgroundColor: isCurrentActive ? theme.palette.primary.main : "transparent",
                  }}
                />
              </Button>
            );
          })}
        </ButtonGroup>
      </Box>

      {sections.map((section) => (
        <Box
          key={section.id}
          sx={{
            display: activeSection === section.id ? "block" : "none",
          }}
        >
          <CronEditor
            key={`${section.id}-editor`}
            title={section.title}
            dataDatasource={dataDatasource}
            setDataDatasource={setDataDatasource}
            isActive={section.active || false}
            isView={disabled}
            fieldMappings={{
              hourKey: `${section.id}Hour`,
              minuteKey: `${section.id}Minute`,
              monthKey: `${section.id}Month`,
              dayOfMonthKey: `${section.id}Day`,
              dayOfWeekKey: `${section.id}DayOfWeek`,
            }}
          />
        </Box>
      ))}
    </Box>
  );
};
