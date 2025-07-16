import styled from "@emotion/styled";
import { Box, CircularProgress, useTheme } from "@mui/material";
import Markdown from "react-markdown";
import { Translate } from "./Translate";
import ErrorIcon from "@mui/icons-material/Error";

export function SingleMessage({
  contentMessage,
  timeMessage,
  isChatbot,
  isLoading = false,
  icon,
  nameChatbot,
  status,
}: {
  contentMessage: string;
  timeMessage: string;
  isChatbot: boolean;
  isLoading?: boolean;
  icon?: React.ReactNode;
  nameChatbot?: string;
  status?: "END" | "CHUNK" | "ERROR";
}) {
  const theme = useTheme();

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

// valutare se lasciare letter spacing e word spacing

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
