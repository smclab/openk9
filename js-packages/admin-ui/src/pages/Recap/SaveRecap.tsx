import React from "react";
import ReactDOM from "react-dom";
import { Badge, Box, Button, Fade, IconButton, Paper, Typography } from "@mui/material";

import CloseRoundedIcon from "@mui/icons-material/CloseRounded";
import KeyboardArrowDownRoundedIcon from "@mui/icons-material/KeyboardArrowDownRounded";
import SummarizeRoundedIcon from "@mui/icons-material/SummarizeRounded";
import Backdrop from "@mui/material/Backdrop";
import Grow from "@mui/material/Grow";
import RecapDatasource, { areaType } from "@pages/datasources/RecapDatasource";

export type RecapField = {
  key: string;
  label: string;
  value: string | number | boolean | Record<string, any> | Array<any> | null;
  type?: "string" | "number" | "boolean" | "json" | "array";
  isValid?: boolean;
  keyNotView?: string;
};

export type RecapSingleSection = {
  id: string;
  title?: string;
  fields: RecapField[];
  section: {
    sectionId: string;
    sectionLabel: string;
  };
};

export type formType = {
  id: string;
  inputProps<K extends keyof any>(
    field: K,
  ): {
    value: any;
    validationMessages: string[];
  };
};

function looksLikeJsonString(value: any): value is string {
  if (typeof value !== "string") return false;
  const v = value.trim();
  return (v.startsWith("{") && v.endsWith("}")) || (v.startsWith("[") && v.endsWith("]"));
}

function safeJsonParse(value: string): any | null {
  try {
    return JSON.parse(value);
  } catch {
    return null;
  }
}

function detectValueType(value: any): RecapField["type"] {
  if (Array.isArray(value)) return "array";
  if (typeof value === "number") return "number";
  if (typeof value === "boolean") return "boolean";

  if (looksLikeJsonString(value)) {
    const parsed = safeJsonParse(value);
    if (parsed !== null) return Array.isArray(parsed) ? "array" : "json";
  }

  if (typeof value === "object" && value !== null) return "json";
  return "string";
}

function normalizeValue(value: any, type?: RecapField["type"]) {
  if ((type === "json" || type === "array") && typeof value === "string" && looksLikeJsonString(value)) {
    const parsed = safeJsonParse(value);
    if (parsed !== null) return parsed;
  }

  if (type === "array") return value ?? [];
  if (type === "json") return value ?? {};
  return value ?? null;
}

export default function Recap({
  recapData,
  setExtraFab,
  forceFullScreen = false,
  actions,
}: {
  recapData: RecapSingleSection[];
  setExtraFab: (fab: React.ReactNode | null) => void;
  forceFullScreen?: boolean;
  actions?: {
    onBack?: () => void;
    onSubmit?: () => void;
    submitLabel?: string;
    backLabel?: string;
  };
}) {
  const [open, setOpen] = React.useState(false);
  const [panelPos, setPanelPos] = React.useState<{ bottom: number; right: number } | null>(null);

  const triggerRef = React.useRef<HTMLDivElement | null>(null);

  const title = recapData[0]?.title ?? recapData[0].section.sectionLabel;
  const totalFields = recapData.reduce((acc, s) => acc + s.fields.length, 0);

  const areas: areaType[] = recapData.map((s) => ({
    title: s.section.sectionLabel,
    fields: s.fields,
  }));

  const handleToggle = () => {
    const el = triggerRef.current;
    if (!el) return;

    const rect = el.getBoundingClientRect();
    const offsetY = 12;

    const right = window.innerWidth - rect.right;
    const bottom = window.innerHeight - rect.top + offsetY;

    setPanelPos({ bottom, right });
    setOpen((prev) => !prev);
  };

  React.useEffect(() => {
    if (forceFullScreen) {
      setOpen(true);
      setExtraFab(null);
      return () => setExtraFab(null);
    }

    const trigger = (
      <Box
        ref={triggerRef}
        onClick={handleToggle}
        sx={{
          px: 2.5,
          py: 1.5,
          bgcolor: "background.paper",
          boxShadow: 6,
          display: "flex",
          alignItems: "center",
          gap: 1.5,
          cursor: "pointer",
        }}
      >
        <Box
          sx={{
            width: 32,
            height: 32,
            borderRadius: "50%",
            bgcolor: "primary.main",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
          }}
        >
          <SummarizeRoundedIcon sx={{ color: "primary.contrastText" }} />
        </Box>

        <Box>
          <Typography fontWeight={600}>{title}</Typography>
        </Box>
      </Box>
    );

    setExtraFab(trigger);
    return () => setExtraFab(null);
  }, [setExtraFab, title, totalFields, open, forceFullScreen]);

  const mainElement = typeof document !== "undefined" ? document.querySelector("main") : null;

  // No scroll when the popup container is fullscreen
  React.useEffect(() => {
    if (!forceFullScreen || !mainElement) return;
    const prevOverflow = mainElement.style.overflow;
    if (open) {
      mainElement.style.overflow = "hidden";
      mainElement.scrollTo({ top: 0, behavior: "smooth" });
    } else {
      mainElement.style.overflow = prevOverflow;
    }
    return () => {
      mainElement.style.overflow = prevOverflow;
    };
  }, [forceFullScreen, mainElement, open]);

  const compactPanel =
    open && panelPos && !forceFullScreen ? (
      <Fade in={open}>
        <Box
          sx={{
            position: "fixed",
            bottom: panelPos.bottom,
            right: panelPos.right,
            zIndex: 1300,
          }}
        >
          <Paper sx={{ width: 440, maxHeight: "70vh", overflow: "hidden" }}>
            <Box
              sx={{
                px: 2,
                py: 1.5,
                borderBottom: "1px solid",
                borderColor: "divider",
                display: "flex",
                justifyContent: "space-between",
              }}
            >
              <Typography fontWeight={600}>{title}</Typography>
              <IconButton size="small" onClick={() => setOpen(false)}>
                <CloseRoundedIcon fontSize="small" />
              </IconButton>
            </Box>

            <Box sx={{ p: 2, overflowY: "auto" }}>
              <RecapDatasource area={areas} />
            </Box>
          </Paper>
        </Box>
      </Fade>
    ) : null;

  const fullScreenPanel =
    open && forceFullScreen ? (
      <>
        <Backdrop
          open={open}
          sx={{ zIndex: 1299 }}
          onClick={() => {
            actions?.onBack?.();
            setOpen(false);
          }}
        />

        <Grow in={open} timeout={{ enter: 260, exit: 180 }} style={{ transformOrigin: "center" }}>
          <Box
            sx={{
              position: "absolute",
              inset: 10,
              zIndex: 1300,
              display: "flex",
              justifyContent: "center",
              alignItems: "center",
            }}
          >
            <Paper
              sx={{
                width: "100%",
                height: "100%",
                overflow: "hidden",
                display: "flex",
                flexDirection: "column",
                boxShadow: 12,
              }}
            >
              <Box
                sx={{
                  px: 2,
                  py: 1.5,
                  borderBottom: "1px solid",
                  borderColor: "divider",
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                }}
              >
                <Typography fontWeight={600}>{title}</Typography>
                <IconButton
                  size="small"
                  onClick={() => {
                    actions?.onBack?.();
                    setOpen(false);
                  }}
                >
                  <CloseRoundedIcon fontSize="small" />
                </IconButton>
              </Box>

              <Box sx={{ p: 2, overflowY: "auto", flex: 1 }}>
                <RecapDatasource area={areas} />
              </Box>

              <Box
                sx={{
                  p: 2,
                  borderTop: "1px solid",
                  borderColor: "divider",
                  display: "flex",
                  justifyContent: "space-between",
                  alignItems: "center",
                  gap: 1,
                }}
              >
                <Button
                  variant="outlined"
                  startIcon={<KeyboardArrowDownRoundedIcon />}
                  onClick={() => {
                    actions?.onBack?.();
                    setOpen(false);
                  }}
                >
                  {actions?.backLabel || "Back"}
                </Button>
                {actions?.onSubmit && (
                  <Button variant="contained" onClick={actions.onSubmit}>
                    {actions.submitLabel || "Create entity"}
                  </Button>
                )}
              </Box>
            </Paper>
          </Box>
        </Grow>
      </>
    ) : null;

  if (forceFullScreen) {
    return fullScreenPanel && mainElement ? ReactDOM.createPortal(fullScreenPanel, mainElement) : fullScreenPanel;
  }

  return <>{compactPanel}</>;
}

export function mappingCardRecap({
  form,
  sections,
  valueOverride,
}: {
  form: formType;
  sections: { label: string; cell: { key: string; label?: string; keyNotView?: string }[] }[];
  valueOverride?: Record<string, any>;
}): RecapSingleSection[] {
  return sections.map((sectionDef) => {
    const fields: RecapField[] = sectionDef.cell.map((element) => {
      const { key, label, keyNotView } = element;
      const input = form.inputProps<any>(key as any);

      const rawValue = valueOverride?.[key] !== undefined ? valueOverride[key] : input.value;

      const type = detectValueType(rawValue);
      let value = normalizeValue(rawValue, type);

      if (keyNotView && type === "json") {
        if (Array.isArray(value)) {
          value = value.map((item: any) => {
            if (item && typeof item === "object" && !Array.isArray(item)) {
              const { [keyNotView]: _omit, ...rest } = item;
              return rest;
            }
            return item;
          });
        } else if (value && typeof value === "object") {
          const { [keyNotView]: _omit, ...rest } = value as Record<string, any>;
          value = rest;
        }
      }

      return {
        key,
        label: label ?? `${key[0].toUpperCase()}${key.slice(1)}`,
        value,
        type,
        isValid: input.validationMessages.length === 0,
        keyNotView: keyNotView,
      };
    });

    return {
      id: `${sectionDef.label}`,
      fields,
      section: {
        sectionId: sectionDef.label,
        sectionLabel: sectionDef.label,
      },
    };
  });
}
