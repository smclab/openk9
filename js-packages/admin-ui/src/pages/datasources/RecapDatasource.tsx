import { Box, Button, Card, CardContent, Divider, Tooltip, Typography } from "@mui/material";
import { useRef, useState } from "react";

export type areaType = {
  title?: string;
  description?: string;
  fields?: Array<{
    label?: string | null | undefined;
    value?: string | number | boolean | Record<string, any> | null | undefined;
    type?: "string" | "number" | "boolean" | "json" | "array";
  }>;
};

function RecapDatasource({
  area,
  actions,
  forceFullScreen = false,
}: {
  area: areaType[];
  actions?: { title: string; action: () => void };
  forceFullScreen?: boolean;
}) {
  const [expandedIndexes, setExpandedIndexes] = useState<Set<number>>(new Set());
  const contentRefs = useRef<(HTMLDivElement | null)[]>([]);

  const toggleCard = (idx: number) => {
    setExpandedIndexes((prev) => {
      const newSet = new Set(prev);
      newSet.has(idx) ? newSet.delete(idx) : newSet.add(idx);
      return newSet;
    });
  };

  return (
    <Box
      display={forceFullScreen ? "grid" : "flex"}
      flexDirection={forceFullScreen ? undefined : "row"}
      gap={forceFullScreen ? 3 : 2}
      width={forceFullScreen ? "100%" : undefined}
      gridTemplateColumns={forceFullScreen ? { xs: "1fr", sm: "1fr 1fr", md: "1fr 1fr 1fr" } : undefined}
      alignItems={forceFullScreen ? "stretch" : undefined}
    >
      {area.map((section, idx) => {
        const isExpanded = expandedIndexes.has(idx);

        return (
          <Card
            variant="outlined"
            sx={{
              width: forceFullScreen ? "100%" : 440,
              maxWidth: forceFullScreen ? "100%" : 440,
              minWidth: forceFullScreen ? 0 : 200,
              minHeight: 297,
              height: isExpanded || forceFullScreen ? "auto" : 297,
              overflow: "auto",
              position: "relative",
              transition: "0.3s",
              boxShadow: forceFullScreen ? 4 : undefined,
              mb: 0,
              display: "flex",
              flexDirection: "column",
            }}
            onClick={() => toggleCard(idx)}
          >
            <CardContent ref={(el) => (contentRefs.current[idx] = el)} sx={{ p: forceFullScreen ? 3 : 2, flex: 1 }}>
              {section.title && (
                <Box display="flex" justifyContent="space-between" alignItems="center" mb={0.5}>
                  <Typography variant="subtitle1" fontWeight={600}>
                    {section.title}
                  </Typography>
                </Box>
              )}
              <Divider sx={{ mb: 1 }} />
              {section.description && (
                <Typography variant="body2" color="text.secondary" display="block" mb={1}>
                  {section.description}
                </Typography>
              )}

              <Box display="flex" flexDirection="column" gap={0.75}>
                {section.fields?.flatMap((field, index) => {
                  if (field.type === "json") {
                    let parsed: Record<string, any> | null = null;

                    if (typeof field.value === "string") {
                      try {
                        parsed = JSON.parse(field.value);
                      } catch {
                        parsed = null;
                      }
                    } else if (typeof field.value === "object" && field.value !== null) {
                      parsed = field.value;
                    }

                    if (parsed) {
                      return Object.entries(parsed).map(([key, val], subIdx) => {
                        let displayValue: string;

                        if (Array.isArray(val)) {
                          displayValue = val.length > 0 ? JSON.stringify(val) : "-";
                        } else if (
                          val === null ||
                          val === "" ||
                          (typeof val === "object" && Object.keys(val).length === 0)
                        ) {
                          displayValue = "-";
                        } else if (typeof val === "boolean") {
                          displayValue = val ? "Yes" : "No";
                        } else {
                          displayValue = String(val);
                        }

                        return (
                          <Box
                            key={`${index}-${subIdx}`}
                            display="flex"
                            justifyContent={"space-between"}
                            alignItems="center"
                          >
                            {key === "title" ? (
                              <Typography
                                variant="body2"
                                color="text.secondary"
                                sx={{ minWidth: 110, flexShrink: 0, fontWeight: 700 }}
                              >
                                {displayValue}:
                              </Typography>
                            ) : (
                              <>
                                <Typography
                                  variant="body2"
                                  color="text.secondary"
                                  sx={{ minWidth: 110, flexShrink: 0, fontWeight: 700 }}
                                >
                                  {key}:
                                </Typography>
                                <Tooltip title={displayValue} placement="top" arrow>
                                  <Typography variant="body1" fontWeight={400} noWrap sx={{ cursor: "help" }}>
                                    {displayValue}
                                  </Typography>
                                </Tooltip>
                              </>
                            )}
                          </Box>
                        );
                      });
                    }
                  }

                  if (field.type === "array") {
                    return (
                      <Box key={index} display="flex" gap={0} flexDirection={"column"}>
                        <Typography
                          variant="body2"
                          color="text.secondary"
                          sx={{ minWidth: 110, flexShrink: 0, fontWeight: 700, mb: 0.5 }}
                        >
                          {field.label}:
                        </Typography>
                        {field.value &&
                          Array.isArray(field.value) &&
                          field.value.map((item, itemIndex) => (
                            <Box display="flex" flexDirection="column" key={itemIndex} mb={1}>
                              {typeof item === "object" && item !== null ? (
                                Object.entries(item).map(([key, value], index) => (
                                  <Box display="flex" justifyContent={"space-between"} key={key}>
                                    <Typography
                                      variant="body2"
                                      color="text.secondary"
                                      sx={{
                                        minWidth: 110,
                                        flexShrink: 0,
                                        fontWeight: 500,
                                      }}
                                    >
                                      {key} :
                                    </Typography>
                                    <Tooltip title={String(value)} placement="top" arrow>
                                      <Typography
                                        variant="body1"
                                        color="text.secondary"
                                        fontWeight={400}
                                        noWrap
                                        sx={{ cursor: "help" }}
                                      >
                                        {String(value)}
                                      </Typography>
                                    </Tooltip>
                                  </Box>
                                ))
                              ) : (
                                <Tooltip title={String(item)} placement="top" arrow>
                                  <Typography variant="body1" fontWeight={400} noWrap sx={{ cursor: "help" }}>
                                    {String(item)}
                                  </Typography>
                                </Tooltip>
                              )}
                            </Box>
                          ))}
                      </Box>
                    );
                  }

                  const displayValue =
                    field.value === null || field.value === undefined || field.value === ""
                      ? "-"
                      : typeof field.value === "boolean"
                      ? field.value
                        ? "Yes"
                        : "No"
                      : String(field.value);

                  return (
                    <Box key={index} display="flex" justifyContent={"space-between"} alignContent={"center"}>
                      <Typography
                        variant="body2"
                        color="text.secondary"
                        sx={{ minWidth: 110, flexShrink: 0, fontWeight: 700 }}
                      >
                        {field.label}:
                      </Typography>
                      <Tooltip title={displayValue} placement="top" arrow>
                        <Typography variant="body1" fontWeight={400} noWrap sx={{ cursor: "help" }}>
                          {displayValue}
                        </Typography>
                      </Tooltip>
                    </Box>
                  );
                })}
              </Box>
            </CardContent>
          </Card>
        );
      })}

      {actions && (
        <>
          <Divider />
          <Box display="flex" justifyContent="flex-end" mt={1}>
            <Button variant="contained" size="small" onClick={actions.action}>
              {actions.title}
            </Button>
          </Box>
        </>
      )}
    </Box>
  );
}

export default RecapDatasource;
