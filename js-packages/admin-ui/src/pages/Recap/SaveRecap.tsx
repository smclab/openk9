import { Badge, Box, Fade, IconButton, Paper, Typography } from "@mui/material";
import RecapDatasource, { areaType } from "@pages/datasources/RecapDatasource";
import React from "react";
import CloseRoundedIcon from "@mui/icons-material/CloseRounded";
import SummarizeRoundedIcon from "@mui/icons-material/SummarizeRounded";

export type RecapField = {
  key: string;
  label: string;
  value: string | number | boolean | Record<string, any> | Array<any> | null;
  type?: "string" | "number" | "boolean" | "json" | "array";
  isValid?: boolean;
};

export type RecapSingleSection = {
  id: string;
  fields: RecapField[];
  section: { sectionId: string; sectionLabel: string };
  tabId?: string;
  isRequired?: boolean;
  callbackNext?: () => void;
  callbackBack?: () => void;
};

export type formType = {
  id: string;
  value: any;
  disabled: boolean;
  inputProps<K extends keyof any>(
    field: K,
  ): {
    id: string;
    value: any[K];
    onChange: (value: any[K]) => void;
    disabled: boolean;
    validationMessages: string[];
    map<M>(
      mapValue: (value: any[K]) => M,
      mapOnChange: (value: M) => any[K],
    ): {
      id: string;
      value: M;
      onChange: (value: M) => void;
      disabled: boolean;
      validationMessages: string[];
    };
  };
};

type AssociationItem = {
  label: string;
  value: string | number;
};

function isAssociationItem(value: any): value is AssociationItem {
  return typeof value === "object" && value !== null && "label" in value && "value" in value;
}

function detectValueType(value: any): RecapField["type"] {
  if (Array.isArray(value)) return "array";
  if (isAssociationItem(value)) return "json";
  if (typeof value === "number") return "number";
  if (typeof value === "boolean") return "boolean";
  if (typeof value === "object" && value !== null) return "json";
  return "string";
}

function normalizeValue(value: any, type: RecapField["type"]) {
  if (type === "array") return value ?? [];
  if (type === "json") return value ?? {};
  return value ?? null;
}

export default function Recap({ recapData }: { recapData: RecapSingleSection[] }) {
  const [open, setOpen] = React.useState(false);

  const title = React.useMemo(() => {
    if (!recapData || recapData.length === 0) return "Recap";
    return recapData[0].section.sectionLabel || "Recap";
  }, [recapData]);

  const totalFields = React.useMemo(
    () => recapData.reduce((acc, section) => acc + section.fields.length, 0),
    [recapData],
  );

  const handleToggle = () => {
    if (!recapData || recapData.length === 0) return;
    setOpen((prev) => !prev);
  };

  const areas: areaType[] = React.useMemo(
    () =>
      recapData.map((section) => ({
        title: section.section.sectionLabel || "",
        fields: section.fields,
      })),
    [recapData],
  );

  if (!recapData || recapData.length === 0) return null;

  return (
    <>
      <Box
        onClick={handleToggle}
        sx={{
          position: "fixed",
          bottom: 24,
          right: 24,
          zIndex: 1300,
          borderRadius: 9999,
          px: 2.5,
          py: 1.5,
          bgcolor: "background.paper",
          boxShadow: 6,
          display: "flex",
          alignItems: "center",
          gap: 1.5,
          cursor: "pointer",
          minWidth: 160,
          maxWidth: 260,
          overflow: "hidden",
        }}
      >
        <Box
          sx={{
            borderRadius: 9999,
            bgcolor: "primary.main",
            display: "flex",
            alignItems: "center",
            justifyContent: "center",
            width: 32,
            height: 32,
          }}
        >
          <SummarizeRoundedIcon sx={{ fontSize: 20, color: "primary.contrastText" }} />
        </Box>
        <Box sx={{ flex: 1, minWidth: 0 }}>
          <Typography variant="body2" noWrap sx={{ fontWeight: 600 }}>
            {title}
          </Typography>
          <Typography variant="caption" color="text.secondary" noWrap>
            {recapData.length} sezioni · {totalFields} campi
          </Typography>
        </Box>
        <Badge color="primary" variant="dot" overlap="circular" invisible={!totalFields} />
      </Box>

      <Fade in={open}>
        <Box
          sx={{
            position: "fixed",
            bottom: 88,
            right: 24,
            zIndex: 1300,
          }}
        >
          <Paper
            elevation={8}
            sx={{
              width: 440,
              maxWidth: "90vw",
              maxHeight: "70vh",
              borderRadius: 4,
              overflow: "hidden",
              display: "flex",
              flexDirection: "column",
            }}
          >
            <Box
              sx={{
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                px: 2,
                py: 1.5,
                borderBottom: "1px solid",
                borderColor: "divider",
                bgcolor: "background.default",
              }}
            >
              <Box>
                <Typography variant="subtitle1" sx={{ fontWeight: 600 }}>
                  {title}
                </Typography>
                <Typography variant="caption" color="text.secondary">
                  Recap
                </Typography>
              </Box>
              <IconButton size="small" onClick={() => setOpen(false)}>
                <CloseRoundedIcon fontSize="small" />
              </IconButton>
            </Box>

            <Box
              sx={{
                p: 2,
                overflowY: "auto",
              }}
            >
              <RecapDatasource area={areas} />
            </Box>
          </Paper>
        </Box>
      </Fade>
    </>
  );
}

export function mappingCardRecap({
  form,
  sections,
}: {
  form: formType;
  sections: { label: string; keys: string[] }[];
}): RecapSingleSection[] {
  return sections.map((sectionDef) => {
    const fields: RecapField[] = sectionDef.keys.map((key) => {
      const input = form.inputProps<any>(key as any);
      const detectedType = detectValueType(input.value);
      const normalized = normalizeValue(input.value, detectedType);

      return {
        key,
        label: key,
        value: normalized,
        type: detectedType,
        isValid: input.validationMessages.length === 0,
      };
    });

    return {
      id: `${form.id}-${sectionDef.label}`,
      fields,
      section: {
        sectionId: form.id,
        sectionLabel: sectionDef.label,
      },
      callbackBack: () => {},
    };
  });
}
