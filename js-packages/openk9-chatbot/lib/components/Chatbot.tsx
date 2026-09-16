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
import {
  Box,
  Button,
  IconButton,
  Theme,
  ThemeProvider,
  useTheme,
} from "@mui/material";
import React from "react";
import AiDisclosure from "./AiDisclosure";
import Search from "./Search";
import useGenerateResponse, {
  DEFAULT_RAG_MODE,
  Message,
  RagMode,
} from "./useGenerateResponse";
import { SingleMessage } from "./SingleMessage";
import { useFocusTrap } from "./useFocusTrap";
import { Translate } from "./Translate";
import { defaultThemeK9 } from "../../src/theme";
import { LanguageProvider } from "./LanguageContext";
import "@fontsource/roboto";
import "@fontsource/titillium-web";

export type IconProps = {
  buttonIcon?: React.ReactNode;
  userIcon?: React.ReactNode;
  chatbotIcon?: React.ReactNode;
  refreshChatIcon?: React.ReactNode;
  closeIcon?: React.ReactNode;
  searchIcon?: React.ReactNode;
  logoIcon?: React.ReactNode;
  closeModal?: React.ReactNode;
};

type ChatbotProps = {
  icon: IconProps;
  title?: React.ReactNode;
  initialMessage?: string;
  nameChatbot?: string;
  themeCustom?: Theme;
  language?: string;
  tenant?: string;
  callbackAuthorization?: () => string | undefined | null;
  /**
   * @deprecated use `numberOfSources` instead (`numberOfSources: 0` hides the
   * sources, equivalent to `useSource: false`).
   */
  useSource?: boolean;
  /**
   * how many sources to display under each answer. `0` hides the sources block
   * entirely. When omitted, falls back to `useSource` (deprecated) or `8`.
   */
  numberOfSources?: number;
  /**
   * where the AI interaction disclosure goes and what it says. Both slots are
   * optional and each takes a node: `top` renders above the message list,
   * `bottom` under the input. Filling one slot only moves the notice there.
   * When neither carries anything the translated default for the active
   * language lands under the input: the notice can be moved and reworded,
   * never disabled.
   */
  aiDisclosure?: AiDisclosureSlots;
  /**
   * which RAG endpoint answers the conversation: `chat-tool` (default) lets the
   * agent decide whether to consult the knowledge base, `chat` always goes
   * through retrieval. Both take the same request body and emit the same
   * stream events.
   */
  ragMode?: RagMode;
};

/** the two positions the disclosure can occupy inside the panel */
type AiDisclosureSlots = {
  /** above the message list, right under the header */
  top?: React.ReactNode;
  /** under the input, where the notice sits by default */
  bottom?: React.ReactNode;
};

const DEFAULT_NUMBER_OF_SOURCES = 8;

/** resolves the effective sources limit honoring the deprecated `useSource` flag */
function resolveNumberOfSources(
  numberOfSources: number | undefined,
  useSource: boolean | undefined,
): number {
  if (numberOfSources !== undefined) return numberOfSources;
  if (useSource === false) return 0;
  return DEFAULT_NUMBER_OF_SOURCES;
}

/** a slot carries nothing when it is missing, null, or a blank string */
function aiDisclosureSlotContent(
  slot: React.ReactNode | undefined,
): React.ReactNode | undefined {
  if (slot === undefined || slot === null) return undefined;
  if (typeof slot === "string" && slot.trim() === "") return undefined;
  return slot;
}

/**
 * resolves which slots render and with what. Filling one slot only moves the
 * notice there; when neither carries anything the translated default lands under
 * the input, so no prop value or combination can take the disclosure out of the
 * DOM — position and wording are customizable, presence is not.
 */
function resolveAiDisclosure(
  aiDisclosure: AiDisclosureSlots | undefined,
  defaultText: string,
): AiDisclosureSlots {
  const top = aiDisclosureSlotContent(aiDisclosure?.top);
  const bottom = aiDisclosureSlotContent(aiDisclosure?.bottom);
  if (top === undefined && bottom === undefined) return { bottom: defaultText };
  return { top, bottom };
}

const Chatbot: React.FC<ChatbotProps> = ({
  icon,
  title,
  initialMessage = "Benvenuti su Openk9, hai dei dubbi? non esitare a chiedere",
  nameChatbot,
  themeCustom,
  language,
  tenant,
  callbackAuthorization,
  useSource,
  numberOfSources,
  aiDisclosure,
  ragMode,
}) => {
  const resolvedNumberOfSources = resolveNumberOfSources(
    numberOfSources,
    useSource,
  );
  return (
    <React.Fragment>
      <ThemeProvider theme={themeCustom || defaultThemeK9}>
        <LanguageProvider initialLanguage={language}>
          <StructureChatbot
            icon={icon}
            title={title}
            initialMessage={initialMessage}
            nameChatbot={nameChatbot}
            tenant={tenant}
            callbackAuthorization={callbackAuthorization}
            numberOfSources={resolvedNumberOfSources}
            aiDisclosure={aiDisclosure}
            ragMode={ragMode}
          />
        </LanguageProvider>
      </ThemeProvider>
    </React.Fragment>
  );
};

const StructureChatbot: React.FC<ChatbotProps> = ({
  icon,
  title,
  initialMessage = "Benvenuti su Openk9, hai dei dubbi? non esitare a chiedere",
  nameChatbot,
  tenant = "",
  callbackAuthorization,
  numberOfSources = DEFAULT_NUMBER_OF_SOURCES,
  aiDisclosure,
  ragMode = DEFAULT_RAG_MODE,
}) => {
  const [isView, setIsView] = React.useState(false);
  const [welcomeMessageTime, setWelcomeMessageTime] = React.useState("");
  const chatbotSearchRef = React.useRef<HTMLInputElement | null>(null);
  const theme = useTheme();
  const aiDisclosureId = React.useId();
  const resolvedAiDisclosure = resolveAiDisclosure(
    aiDisclosure,
    Translate({ label: "aiDisclosure" }),
  );
  // l'id sta su una sola istanza: `aria-describedby` ne referenza una soltanto e
  // due nodi con lo stesso id non sono validi. Lo porta quella sotto l'input,
  // la piu' vicina al campo; il top solo quando e' l'unica presente
  const describedSlotIsBottom = resolvedAiDisclosure.bottom !== undefined;

  const {
    messages,
    generateResponse,
    cancelAllResponses,
    isChatting,
    isLoading: isGenerateMessage,
    resetMessage,
  } = useGenerateResponse({
    initialMessages: [],
    tenant,
    callbackAuthorization,
    ragMode,
  });
  const [trapRef] = useFocusTrap(isView);
  const messagesEndRef = React.useRef<HTMLDivElement | null>(null);

  React.useEffect(() => {
    if (messages && messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [messages]);

  React.useEffect(() => {
    const currentTime = new Date();
    setWelcomeMessageTime(
      currentTime.toLocaleTimeString([], {
        hour: "2-digit",
        minute: "2-digit",
      }),
    );
  }, []);

  React.useEffect(() => {
    if (isView && chatbotSearchRef.current) {
      chatbotSearchRef.current.focus();
    }
  }, [isView]);

  return (
    <Box
      className="openk9-chatbot-structure"
      ref={trapRef}
      sx={{
        display: "flex",
        justifyContent: "flex-start",
        alignItems: "flex-end",
        bgcolor: "trasparent",
        flexDirection: "column",
        gap: "12px",
      }}
    >
      {isView && (
        <Box
          className="openk9-chatbot-container"
          sx={{
            position: { xs: "fixed", sm: "unset" },
            top: { xs: "0", sm: "unset" },
            left: { xs: "0", sm: "unset" },
            display: "flex",
            flexDirection: "column",
            border: {
              xs: "unset",
              sm: `1px solid ${theme.palette.divider}`,
            },
            borderRadius: { xs: "unset", sm: theme.shape.borderRadius * 2 },
            backgroundColor: theme.palette.primary.contrastText,
            height: { xs: "100vh", sm: "460px" },
            width: { xs: "100vw", sm: "365px" },
            boxShadow: theme.shadows[3],
            zIndex: 2,
          }}
        >
          <ChatbotHeader
            title={title}
            icon={icon}
            isChatting={isChatting}
            resetMessage={resetMessage}
            setIsView={setIsView}
          />
          {resolvedAiDisclosure.top !== undefined && (
            <AiDisclosure
              id={describedSlotIsBottom ? undefined : aiDisclosureId}
              text={resolvedAiDisclosure.top}
            />
          )}
          <MessageList
            messages={messages}
            icon={icon}
            welcomeMessageTime={welcomeMessageTime}
            initialMessage={initialMessage}
            isGenerateMessage={isGenerateMessage}
            messagesEndRef={messagesEndRef}
            nameChatbot={nameChatbot}
            numberOfSources={numberOfSources}
          />
          <Search
            handleSearch={generateResponse}
            cancelAllResponses={cancelAllResponses}
            isChatting={isChatting}
            icon={icon}
            chatbotSearchRef={chatbotSearchRef}
            describedById={aiDisclosureId}
          />
          {resolvedAiDisclosure.bottom !== undefined && (
            <AiDisclosure
              id={aiDisclosureId}
              text={resolvedAiDisclosure.bottom}
            />
          )}
        </Box>
      )}
      <Button
        className="openk9-toggle-chatbot-button"
        aria-label={
          isView
            ? Translate({ label: "closeChatbot" })
            : Translate({ label: "openChatbot" })
        }
        sx={{
          borderRadius: "50%",
          color: "white",
          display: { xs: isView ? "none" : "unset", sm: "flex" },
          minWidth: "56px",
          width: "56px",
          height: "56px",
          boxShadow: theme.shadows[3],
          background: theme.palette.primary.main,
          padding: 0,
          "&:hover": {
            background: theme.palette.primary.light,
            boxShadow: theme.shadows[3],
          },
        }}
        onClick={() => setIsView(!isView)}
      >
        <Box
          className="openk9-toggle-icon-wrapper"
          sx={{
            justifyContent: "center",
            display: "flex",
            height: "100%",
            width: "100%",
            alignItems: "center",
          }}
        >
          {isView ? icon.closeIcon : icon.buttonIcon}
        </Box>
      </Button>
    </Box>
  );
};

const ChatbotHeader: React.FC<{
  title?: React.ReactNode;
  icon: IconProps;
  isChatting: boolean;
  resetMessage: () => void;
  setIsView: (isView: boolean) => void;
}> = ({ title, icon, isChatting, resetMessage, setIsView }) => {
  const theme = useTheme();

  return (
    <Box
      className="openk9-chatbot-header"
      sx={{
        display: "flex",
        alignItems: "center",
        borderBottom: `1px solid ${theme.palette.divider}`,
        pb: theme.spacing(2),
        mb: theme.spacing(2),
        gap: theme.spacing(2),
        padding: "12px",
        paddingBottom: "0px",
        marginBottom: "12px",
        justifyContent: "space-between",
      }}
    >
      <Box
        className="openk9-logo-icon"
        sx={{
          minWidth: 0,
          padding: 0,
        }}
      >
        {icon.logoIcon}
      </Box>
      <div>{title}</div>
      <Box
        className="openk9-header-buttons"
        display={"flex"}
        gap={"5px"}
        alignItems={"center"}
      >
        <Button
          className="openk9-refresh-button"
          disabled={isChatting}
          onClick={resetMessage}
          aria-label={Translate({ label: "clearChat" })}
          sx={{
            minWidth: 0,
            padding: 0,
            height: "24px",
            width: "24px",
          }}
        >
          {icon.refreshChatIcon}
        </Button>

        <IconButton
          className="openk9-close-button"
          onClick={() => setIsView(false)}
          role="button"
          size="small"
          sx={{ display: { xs: "block", sm: "none" } }}
          aria-label={Translate({ label: "closeChatbot" })}
        >
          {icon.closeModal}
        </IconButton>
      </Box>
    </Box>
  );
};

const MessageList: React.FC<{
  messages: Message[];
  icon: IconProps;
  initialMessage: string;
  isGenerateMessage: { id: string; isLoading: boolean } | null;
  messagesEndRef: React.RefObject<HTMLDivElement>;
  nameChatbot?: string;
  welcomeMessageTime?: string;
  numberOfSources?: number;
}> = ({
  messages,
  icon,
  initialMessage,
  isGenerateMessage,
  messagesEndRef,
  nameChatbot,
  welcomeMessageTime = "",
  numberOfSources = DEFAULT_NUMBER_OF_SOURCES,
}) => (
  <Box
    className="openk9-message-list-container"
    component="section"
    sx={{
      flex: 1,
      display: "flex",
      flexDirection: "column",
      overflow: "hidden",
    }}
  >
    <Box
      className="openk9-message-list"
      sx={{
        flex: 1,
        overflowY: "auto",
        paddingInline: "16px",
        gap: "12px",
        display: "flex",
        flexDirection: "column",
        alignItems: "flex-start",
        bgcolor: "transparent",
      }}
    >
      <SingleMessage
        contentMessage={initialMessage}
        isChatbot={true}
        timeMessage={welcomeMessageTime}
        icon={icon.chatbotIcon}
        isLoading={false}
        nameChatbot={nameChatbot}
      />
      {messages.length > 0 &&
        messages.map((message, index) => {
          const sendTime = message?.sendTime
            ? new Date(message?.sendTime).toLocaleTimeString([], {
                hour: "2-digit",
                minute: "2-digit",
              })
            : "";
          const responseTime = message?.responseTime
            ? new Date(message?.responseTime).toLocaleTimeString([], {
                hour: "2-digit",
                minute: "2-digit",
              })
            : message?.responseTime || "";
          return (
            <React.Fragment key={index}>
              <SingleMessage
                contentMessage={message.question}
                isChatbot={false}
                timeMessage={sendTime}
                icon={icon.userIcon}
                isLoading={false}
              />
              <SingleMessage
                contentMessage={message.answer}
                status={message.status}
                sources={message?.sources}
                numberOfSources={numberOfSources}
                isChatbot={true}
                timeMessage={responseTime}
                icon={icon.chatbotIcon}
                isLoading={
                  isGenerateMessage?.id === message.id &&
                  isGenerateMessage?.isLoading
                }
                nameChatbot={nameChatbot}
              />
              {index === messages.length - 1 && <div ref={messagesEndRef} />}
            </React.Fragment>
          );
        })}
    </Box>
  </Box>
);

export default Chatbot;
