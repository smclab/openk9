import { Box, Typography, Grid, Card, CardContent, Button, Divider } from "@mui/material";
import { useState, useRef, useLayoutEffect } from "react";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import ExpandLessIcon from "@mui/icons-material/ExpandLess";

function RecapDatasource({
  area,
  actions,
}: {
  area: {
    title?: string;
    description?: string;
    fields?: Array<{
      label?: string | null | undefined;
      value?: string | number | boolean | Record<string, any> | null | undefined;
      type?: "string" | "number" | "boolean" | "json" | "array";
    }>;
  }[];
  actions?: { title: string; action: () => void };
}) {
  const [expandedIndexes, setExpandedIndexes] = useState<Set<number>>(new Set());
  const [overflowingCards, setOverflowingCards] = useState<boolean[]>([]);
  const contentRefs = useRef<(HTMLDivElement | null)[]>([]);

  const toggleCard = (idx: number) => {
    setExpandedIndexes((prev) => {
      const newSet = new Set(prev);
      newSet.has(idx) ? newSet.delete(idx) : newSet.add(idx);
      return newSet;
    });
  };

  useLayoutEffect(() => {
    const overflows = contentRefs.current.map((el) => !!el && el.scrollHeight > 300);
    setOverflowingCards(overflows);
  }, [area]);

  return (
    <Box display="flex" flexDirection="column" gap={2}>
      <Grid container spacing={2}>
        {area.map((section, idx) => {
          const isExpanded = expandedIndexes.has(idx);
          const isOverflowing = overflowingCards[idx];

          return (
            <Grid item xs={12} sm={6} md={4} key={idx}>
              <Card
                variant="outlined"
                sx={{
                  height: isExpanded ? "auto" : 300,
                  minHeight: 300,
                  overflow: "hidden",
                  position: "relative",
                  cursor: isOverflowing ? "pointer" : "default",
                  transition: "0.3s",
                }}
                onClick={() => toggleCard(idx)}
              >
                <CardContent ref={(el) => (contentRefs.current[idx] = el)} sx={{ p: 2 }}>
                  {section.title && (
                    <Box display="flex" justifyContent="space-between" alignItems="center" mb={0.5}>
                      <Typography variant="subtitle1" fontWeight={600}>
                        {section.title}
                      </Typography>

                      {isOverflowing && (
                        <Button
                          variant="text"
                          size="small"
                          onClick={(e) => {
                            e.stopPropagation();
                            toggleCard(idx);
                          }}
                          endIcon={isExpanded ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                          sx={{
                            color: "primary.main",
                            fontWeight: 500,
                            textTransform: "none",
                            minWidth: "auto",
                            px: 1,
                          }}
                        >
                          {isExpanded ? "Show Less" : "Show More"}
                        </Button>
                      )}
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
                                <Typography
                                  variant="body2"
                                  color="text.secondary"
                                  sx={{ minWidth: 110, flexShrink: 0, fontWeight: 700 }}
                                >
                                  {key}:
                                </Typography>
                                <Typography variant="body1" fontWeight={400} noWrap>
                                  {displayValue}
                                </Typography>
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
                                        <Typography variant="body1" color="text.secondary" fontWeight={400} noWrap>
                                          {String(value)}
                                        </Typography>
                                      </Box>
                                    ))
                                  ) : (
                                    <Typography variant="body1" fontWeight={400} noWrap>
                                      {String(item)}
                                    </Typography>
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
                          <Typography variant="body1" fontWeight={400} noWrap>
                            {displayValue}
                          </Typography>
                        </Box>
                      );
                    })}
                  </Box>
                </CardContent>
              </Card>
            </Grid>
          );
        })}
      </Grid>

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
