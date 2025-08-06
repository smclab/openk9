import styled from "@emotion/styled";
import {
  Box,
  Chip,
  CircularProgress,
  IconButton,
  Typography,
  useTheme,
} from "@mui/material";
import Markdown from "react-markdown";
import { Translate } from "./Translate";
import ErrorIcon from "@mui/icons-material/Error";
import { useState } from "react";
import VisibilityIcon from "@mui/icons-material/Visibility";
import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import CheckIcon from "@mui/icons-material/Check";
import OpenInFullIcon from "@mui/icons-material/OpenInFull";

type Theme = "light" | "dark";

type Source = { score?: string; title?: string; url?: string };

export function SingleMessage({
  contentMessage,
  timeMessage,
  isChatbot,
  isLoading = false,
  icon,
  nameChatbot,
  status,
  sources,
  themeInfo = "light",
}: {
  contentMessage: string;
  timeMessage: string;
  isChatbot: boolean;
  isLoading?: boolean;
  icon?: React.ReactNode;
  nameChatbot?: string;
  status?: "END" | "CHUNK" | "ERROR";
  sources?: Source[];
  themeInfo?: Theme;
}) {
  sources = sources || [];
  const theme = useTheme();
  sources && console.log(sources);

  const [copiedSource, setCopiedSource] = useState<string | null>(null);
  // const [showSourcesModal, setShowSourcesModal] = useState(false);
  const [showAllSources, setShowAllSources] = useState(false);
  const [expandedChips, setExpandedChips] = useState<Set<string>>(new Set());
  const maxVisibleSources = 8;
  const visibleSources = sources.slice(0, maxVisibleSources);
  const remainingSources = sources.length - maxVisibleSources;

  const copySource = async (source: any) => {
    try {
      await navigator.clipboard.writeText(source.url);
      setCopiedSource(source.url);
      setTimeout(() => setCopiedSource(null), 2000);
    } catch (err) {
      console.error("Errore durante la copia:", err);
    }
  };

  const toggleChipExpansion = (url: string) => {
    const newSet = new Set(expandedChips);

    if (newSet.has(url)) {
      newSet.delete(url);
      if (newSet.size < sources.length) {
        setShowAllSources(false);
      }
    } else {
      newSet.add(url);
      if (newSet.size === sources.length) {
        setShowAllSources(true);
      }
    }

    setExpandedChips(newSet);
  };

  const getTypeColor = (source: string) => {
    const baseColor = getStableColor(source, themeInfo);

    const hexToRgb = (hex: string) => {
      const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
      return result
        ? {
            r: parseInt(result[1], 16),
            g: parseInt(result[2], 16),
            b: parseInt(result[3], 16),
          }
        : { r: 200, g: 200, b: 200 };
    };

    const rgb = hexToRgb(baseColor);

    const backgroundOpacity = themeInfo === "light" ? 0.1 : 0.2;
    const borderOpacity = themeInfo === "light" ? 0.3 : 0.4;

    const lightBackground = `rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, ${backgroundOpacity})`;
    const lightBorder = `rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, ${borderOpacity})`;

    return {
      backgroundColor: lightBackground,
      color: baseColor,
      borderColor: lightBorder,
    };
  };

  const ariaLabel =
    (isChatbot
      ? nameChatbot + " " + Translate({ label: "sendMessage" })
      : Translate({ label: "youSendMessage" })) + timeMessage;

  return (
    <Box
      className="openk9-single-message-container"
      display={"flex"}
      flexDirection={"column"}
      gap={"5px"}
      width={"100%"}
    >
      {status?.toUpperCase() === "ERROR" ? (
        <Box
          className="openk9-message-box"
          display="flex"
          alignItems="flex-end"
          gap={4}
          sx={{
            gap: "6px",
            backgroundColor: "white",
            flexDirection: isChatbot ? "row" : "row-reverse",
          }}
        >
          {icon && <Box className="openk9-chatbot-icon">{icon}</Box>}
          <Box
            className="openk9-error-message-container"
            sx={{
              display: "flex",
              alignItems: "center",
              background: "#FCEAEA",
              border: "1px solid #D32F2F",
              borderRadius: "10px",
              padding: "12px 16px",
              gap: "10px",
              width: "100%",
            }}
            aria-live="polite"
          >
            <ErrorIcon sx={{ color: "#D32F2F", fontSize: "20px" }} />
            <ParagraphMessage
              className="openk9-paragraph-message"
              $font={theme.typography.fontFamily}
              $color="#D32F2F"
            >
              {contentMessage}
            </ParagraphMessage>
          </Box>
        </Box>
      ) : (
        <Box
          className="openk9-message-box"
          display="flex"
          alignItems="flex-end"
          gap={4}
          sx={{
            gap: "6px",
            backgroundColor: "white",
            flexDirection: isChatbot ? "row" : "row-reverse",
          }}
        >
          {icon && <Box className="openk9-chatbot-icon">{icon}</Box>}
          <Box
            className="openk9-message-content-wrapper"
            sx={{
              display: "flex",
              flexDirection: "column",
              width: ["-webkit-fill-available", "-moz-available", "100%"],
              gap: "6px",
            }}
          >
            <Box
              className="openk9-message-content"
              sx={{
                overflow: "auto",
                display: "flex",
                flexDirection: "column",
                backgroundColor: isChatbot
                  ? theme.palette.background.default
                  : theme.palette.primary.main,
                border: "1px solid",
                borderColor: theme.palette.primary.main,
                paddingInline: "16px",
                color: !isChatbot ? "white" : "black",
                borderRadius: isChatbot
                  ? "12px 12px 12px 2px"
                  : "12px 12px 2px 12px",
              }}
              aria-live="polite"
            >
              <ParagraphMessage
                className="openk9-paragraph-message"
                $isLoading={isLoading}
                $font={theme.typography.fontFamily}
              >
                {isLoading ? (
                  <Box
                    className="openk9-loading-icon-container"
                    sx={{
                      display: "flex",
                      flexDirection: "column",
                      gap: "5px",
                    }}
                  >
                    <CircularProgress
                      className="openk9-loader"
                      disableShrink
                      size={30}
                      sx={{ color: theme.palette.primary.main }}
                    />
                  </Box>
                ) : (
                  <FocusableSection
                    className="openk9-focusable-section"
                    $contraxtFocus={isChatbot ? "black" : "white"}
                    aria-label={ariaLabel}
                    tabIndex={0}
                  >
                    {nameChatbot && (
                      <ParagraphName
                        className="openk9-paragraph-name"
                        $font={theme.typography.fontFamily || ""}
                      >
                        {nameChatbot}
                      </ParagraphName>
                    )}
                    <Markdown>{contentMessage}</Markdown>
                  </FocusableSection>
                )}
              </ParagraphMessage>
              {sources.length > 0 && (
                <Box>
                  <Box
                    display="flex"
                    justifyContent="space-between"
                    alignItems="center"
                    mb={2}
                  >
                    <Typography variant="body2" color="text.secondary">
                      {sources.length} sources
                    </Typography>
                    <Box
                      component="button"
                      onClick={() => {
                        const newState = !showAllSources;
                        setShowAllSources(newState);

                        if (newState) {
                          const allExpanded = new Set(
                            sources.map((s) => s.url || ""),
                          );
                          setExpandedChips(allExpanded);
                        } else {
                          setExpandedChips(new Set());
                        }
                      }}
                      sx={{
                        display: "flex",
                        alignItems: "center",
                        gap: 0.5,
                        color: "#12518f",
                        fontSize: "0.75rem",
                        background: "none",
                        border: "none",
                        cursor: "pointer",
                        "&:hover": { color: "#2782ea" },
                      }}
                    >
                      <VisibilityIcon sx={{ fontSize: "0.75rem" }} />
                      <Typography variant="caption">
                        {showAllSources ? "Hide all" : "Show all"}
                      </Typography>
                    </Box>
                  </Box>

                  <Box display="flex" flexWrap="wrap" gap={1}>
                    {visibleSources.map((source, index) => {
                      const typeColors = getTypeColor(source.url || "");
                      return (
                        <Chip
                          key={source.url || index}
                          style={{
                            flex: expandedChips.has(source.url || "")
                              ? 1
                              : "none",
                          }}
                          label={
                            <Box
                              display="flex"
                              alignItems="center"
                              gap={1}
                              flex={1}
                              sx={{
                                width: expandedChips.has(source.url || "")
                                  ? "auto"
                                  : "170px",
                                overflow: "hidden",
                                textOverflow: expandedChips.has(
                                  source.url || "",
                                )
                                  ? "unset"
                                  : "ellipsis",
                                whiteSpace: "nowrap",
                              }}
                            >
                              <Box
                                sx={{
                                  flex: 1,
                                  width: 8,
                                  height: 8,
                                  flexShrink: 0,
                                  flexGrow: 0,
                                  borderRadius: "50%",
                                  backgroundColor: getStableColor(
                                    source.url,
                                    themeInfo,
                                  ),
                                }}
                              />
                              <Typography
                                flex={1}
                                variant="caption"
                                fontWeight={500}
                                overflow={"hidden"}
                                textOverflow={"ellipsis"}
                              >
                                {source?.title && source.title}
                              </Typography>
                              <Box>
                                <IconButton
                                  size="small"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    copySource(source);
                                  }}
                                  sx={{
                                    p: 0.25,
                                    ml: 0.5,
                                    "&:hover": {
                                      backgroundColor: "rgba(0,0,0,0.05)",
                                    },
                                  }}
                                >
                                  {copiedSource === source.url ? (
                                    <CheckIcon sx={{ fontSize: "0.75rem" }} />
                                  ) : (
                                    <ContentCopyIcon
                                      sx={{ fontSize: "0.75rem" }}
                                    />
                                  )}
                                </IconButton>
                                <IconButton
                                  size="small"
                                  onClick={(e) => {
                                    e.stopPropagation();
                                    toggleChipExpansion(source.url || "");
                                  }}
                                  sx={{
                                    p: 0.25,
                                    "&:hover": {
                                      backgroundColor: "rgba(0,0,0,0.05)",
                                    },
                                  }}
                                >
                                  <OpenInFullIcon
                                    sx={{ fontSize: "0.75rem" }}
                                  />
                                </IconButton>
                              </Box>
                            </Box>
                          }
                          onClick={() => window.open(source.url, "_blank")}
                          sx={{
                            backgroundColor: typeColors.backgroundColor,
                            color: typeColors.color,
                            border: `1px solid ${typeColors.borderColor}`,
                            cursor: "pointer",
                            "&:hover": {
                              backgroundColor: typeColors.backgroundColor,
                              opacity: 0.8,
                            },
                          }}
                          size="small"
                        />
                      );
                    })}

                    {remainingSources > 0 && (
                      <Chip
                        label={
                          <Box display="flex" alignItems="center" gap={0.5}>
                            <OpenInFullIcon sx={{ fontSize: "0.75rem" }} />
                            <Typography variant="caption">
                              +{remainingSources}
                            </Typography>
                          </Box>
                        }
                        // onClick={() => setShowSourcesModal(true)}
                        sx={{
                          backgroundColor:
                            themeInfo === "light" ? "#f5f5f5" : "#2d2d2d",
                          color: themeInfo === "light" ? "#616161" : "#b0b0b0",
                          cursor: "pointer",
                          "&:hover": {
                            backgroundColor:
                              themeInfo === "light" ? "#e0e0e0" : "#404040",
                          },
                        }}
                        size="small"
                      />
                    )}
                  </Box>
                </Box>
              )}
            </Box>
          </Box>
        </Box>
      )}
      <ParagraphTime
        className="openk9-paragraph-time"
        $color={theme.palette.text.secondary}
        $font={theme.typography.body2.font || "Roboto"}
        style={{ alignSelf: !isChatbot ? "start" : "end" }}
      >
        {timeMessage}
      </ParagraphTime>
    </Box>
  );
}

const ParagraphTime = styled.p<{ $color?: string; $font?: string }>`
  color: ${(props) => props.$color};
  margin: 0px;
  align-self: end;
  font-size: 10px;
  font-weight: 500;
  line-height: 1.5;
  letter-spacing: 1.2px;
  word-spacing: 1.6px;
  font-family: ${(props) => props.$font};
`;

const ParagraphMessage = styled.div<{
  $color?: string;
  $isLoading?: boolean;
  $font?: string;
  $width?: string;
}>`
  color: ${(props) => props.$color};
  margin: ${(props) => (!props.$isLoading ? "0" : "16px")};
  width: ${(props) => !props.$width};
  font-size: 12px;
  font-weight: 400;
  line-height: 1.5;
  text-align: left;
  font-family: ${(props) => props.$font};
  padding-block: 3px;
  // letter-spacing: 1.44px;
  // word-spacing: 1.92px;
`;

const ParagraphName = styled.p<{ $color?: string; $font?: string }>`
  color: ${(props) => props.$color};
  font-weight: 700;
  size: 10px;
  line-height: 15.21px;
  font-family: ${(props) => props.$font};
  // letter-spacing: 1.44px;
  // word-spacing: 1.92px;
`;

const FocusableSection = styled.section<{ $contraxtFocus: string }>`
  padding-block: 0.2px;

  &:focus {
    outline: 2px solid ${(props) => props.$contraxtFocus};
  }
`;

function generateAccessibleColor(theme: Theme): string {
  const MAX_TRIES = 20;
  const bgColor = theme === "light" ? "#ffffff" : "#000000";

  for (let i = 0; i < MAX_TRIES; i++) {
    const hue = Math.floor(Math.random() * 360);
    const saturation = 80;
    const lightness =
      theme === "light" ? 20 + Math.random() * 30 : 60 + Math.random() * 30;

    const hex = hslToHex(hue, saturation, lightness);
    if (hasGoodContrast(hex, bgColor, 6.0)) {
      return hex;
    }
  }

  return theme === "light" ? "#111111" : "#eeeeee";
}

function hexToRgb(hex: string): { r: number; g: number; b: number } {
  hex = hex.replace(/^#/, "");

  if (hex.length === 3) {
    hex = hex
      .split("")
      .map((c) => c + c)
      .join("");
  }

  const num = parseInt(hex, 16);
  return {
    r: (num >> 16) & 255,
    g: (num >> 8) & 255,
    b: num & 255,
  };
}

function luminance(hex: string): number {
  const rgb = hexToRgb(hex);
  const srgb = [rgb.r, rgb.g, rgb.b].map((c) => {
    const c_ = c / 255;
    return c_ <= 0.03928 ? c_ / 12.92 : Math.pow((c_ + 0.055) / 1.055, 2.4);
  });
  return 0.2126 * srgb[0] + 0.7152 * srgb[1] + 0.0722 * srgb[2];
}

function contrastRatio(fg: string, bg: string): number {
  const L1 = luminance(fg);
  const L2 = luminance(bg);
  return (Math.max(L1, L2) + 0.05) / (Math.min(L1, L2) + 0.05);
}

function hasGoodContrast(fg: string, bg: string, minRatio = 6.0): boolean {
  return contrastRatio(fg, bg) >= minRatio;
}

function hslToHex(h: number, s: number, l: number): string {
  l /= 100;
  const a = (s * Math.min(l, 1 - l)) / 100;
  const f = (n: number) => {
    const k = (n + h / 30) % 12;
    const color = l - a * Math.max(Math.min(k - 3, 9 - k, 1), -1);
    return Math.round(255 * color)
      .toString(16)
      .padStart(2, "0");
  };
  return `#${f(0)}${f(8)}${f(4)}`;
}

const sourceColorMap = new Map<string, { light: string; dark: string }>();

function getStableColor(source: string | undefined, theme: Theme): string {
  if (!source) return theme === "light" ? "#666" : "#999";

  if (!sourceColorMap.has(source)) {
    const mappedColor = mappingColors(source, theme);
    if (mappedColor) {
      sourceColorMap.set(source, { light: mappedColor, dark: mappedColor });
    } else {
      sourceColorMap.set(source, {
        light: generateAccessibleColor("light"),
        dark: generateAccessibleColor("dark"),
      });
    }
  }

  return sourceColorMap.get(source)![theme];
}

function mappingColors(
  source: string | undefined,
  theme: Theme,
): string | undefined {
  switch (source?.toLowerCase()) {
    case "ansa.it":
      return theme === "light" ? "#1976d2" : "#42a5f5";
    case "eurosport.it":
      return theme === "light" ? "#d32f2f" : "#ef5350";
    case "ilpost.it":
      return theme === "light" ? "#388e3c" : "#66bb6a";
    case "corriere.it":
      return theme === "light" ? "#f57c00" : "#ffa726";
    case "techreport.com":
      return theme === "light" ? "#1976d2" : "#42a5f5";
    case "openai.com":
      return theme === "light" ? "#7b1fa2" : "#ab47bc";
    case "meta.ai":
      return theme === "light" ? "#1976d2" : "#42a5f5";
    case "kaggle.com":
      return theme === "light" ? "#f57c00" : "#ffa726";
    case "ethics.ai":
      return theme === "light" ? "#1976d2" : "#42a5f5";
    case "coursera.org":
      return theme === "light" ? "#d32f2f" : "#ef5350";
    case "spotify.com":
      return "#1dd15d";
    case "arxiv.org":
      return theme === "light" ? "#7b1fa2" : "#ab47bc";
    case "github.com":
      return theme === "light" ? "#24292e" : "#f0f6fc";
    case "stackoverflow.com":
      return theme === "light" ? "#f48024" : "#f69c3d";
    case "medium.com":
      return theme === "light" ? "#000000" : "#ffffff";
    case "youtube.com":
      return theme === "light" ? "#ff0000" : "#ff4444";
    case "x.com":
      return theme === "light" ? "#1da1f2" : "#1d9bf0";
    case "linkedin.com":
      return theme === "light" ? "#0077b5" : "#0a66c2";
    case "reddit.com":
      return theme === "light" ? "#ff4500" : "#ff6314";
    case "wikipedia.org":
      return theme === "light" ? "#000000" : "#ffffff";
    default:
      return undefined;
  }
}
