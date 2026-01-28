/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
import { Box, Button, Card, CardContent, Divider, Tooltip, Typography } from "@mui/material";
import { useRef, useState } from "react";

export type areaType = {
  title?: string;
  description?: string;
  fields?: Array<{
    label?: string | null | undefined;
    value?: string | number | boolean | Record<string, any> | null | undefined;
    type?: "string" | "number" | "boolean" | "json" | "array";
    jsonView?: boolean;
    divider?: boolean;
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
              minWidth: forceFullScreen ? 0 : 274,
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
              {area.length > 1 && section.title && (
                <>
                  <Box display="flex" justifyContent="space-between" alignItems="center" mb={0.5}>
                    <Typography variant="subtitle1" fontWeight={600}>
                      {section.title}
                    </Typography>
                  </Box>
                  <Divider sx={{ mb: 1 }} />
                </>
              )}
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

                  if (field.jsonView) {
                    return (
                      <Box key={index} display="flex" flexDirection="column">
                        <Typography
                          variant="body2"
                          color="text.secondary"
                          sx={{ minWidth: 110, flexShrink: 0, fontWeight: 700, mb: 0.5 }}
                        >
                          {field.label}:
                        </Typography>
                        <JsonFieldBox value={displayValue} />
                      </Box>
                    );
                  }

                  if (field.divider) {
                    return <Divider />;
                  }

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

function escapeHtml(str: string): string {
  return str.replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

function highlightJson(json: string): string {
  const escaped = escapeHtml(json);
  // strings
  let result = escaped.replace(/(".*?")/g, '<span class="json-string">$1</span>');
  // keys
  result = result.replace(/<span class="json-string">(".*?")<\/span>(\s*:)/g, '<span class="json-key">$1</span>$2');
  // boolean
  result = result.replace(/\b(true|false)\b/g, '<span class="json-boolean">$1</span>');
  // null
  result = result.replace(/\b(null)\b/g, '<span class="json-null">$1</span>');
  // numbers
  result = result.replace(/\b(-?(0x)?\d+(\.\d+)?)\b/g, '<span class="json-number">$1</span>');
  return result;
}

function JsonFieldBox({ value }: { value: string }) {
  let pretty = value;

  try {
    pretty = JSON.stringify(JSON.parse(value), null, 2);
  } catch {}
  return (
    <Box
      component="pre"
      sx={(theme) => {
        const isDark = theme.palette.mode === "dark";

        const colors = {
          key: isDark ? "#9cdcfe" : "#0451a5",
          string: isDark ? "#ce9178" : "#a31515",
          boolean: isDark ? "#569cd6" : "#0000ff",
          null: isDark ? "#569cd6" : "#0000ff",
          number: isDark ? "#b5cea8" : "#098658",
        };

        return {
          m: 0,
          p: 1.5,
          borderRadius: 1,
          fontFamily: 'Menlo, Consolas, "Fira Code", monospace',
          fontSize: 12,
          overflowX: "auto",
          whiteSpace: "pre",
          bgcolor: isDark ? "#1e1e1e" : "#f5f5f5",
          border: `1px solid ${isDark ? "#303030" : "#e0e0e0"}`,
          "& .json-key": { color: colors.key },
          "& .json-string": { color: colors.string },
          "& .json-boolean": { color: colors.boolean },
          "& .json-null": { color: colors.null },
          "& .json-number": { color: colors.number },
        };
      }}
      dangerouslySetInnerHTML={{ __html: highlightJson(pretty) }}
    />
  );
}
export default RecapDatasource;

