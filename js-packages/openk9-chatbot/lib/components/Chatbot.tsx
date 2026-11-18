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
import { LanguageProvider } from "./LanguageContext";
import { CloseIcon, LockOutlinedIcon } from "./icons";
import "@fontsource/roboto";
import "@fontsource/titillium-web";
import "./Chatbot.css";

/** which set of default colours the chatbot starts from */
export type ColorScheme = "light" | "dark";

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

/** what the host page can ask of the chatbot through `ref` */
export type ChatbotHandle = {
  /**
   * sends `message` as if the user had typed and submitted it, opening the
   * panel. Returns `false` and sends nothing when the message is empty or
   * whitespace only, when an answer is still streaming, after the chatbot
   * has unmounted, or when `requireLogin` is on and the user is anonymous: in
   * that case the panel opens on the login notice.
   */
  send(message: string): boolean;
};

type ChatbotProps = {
  /**
   * lets the host page drive the chatbot, e.g. a button outside it that asks a
   * question: `chatbotRef.current?.send("...")`. The handle is created once and
   * stays the same for the life of the chatbot, so it can also be kept aside.
   */
  ref?: React.Ref<ChatbotHandle>;
  icon: IconProps;
  title?: React.ReactNode;
  initialMessage?: string;
  nameChatbot?: string;
  /**
   * the default colours: `light` (default) or `dark`. Every colour, the font and
   * the text sizes can then be changed through the `--openk9-chatbot-*` CSS
   * variables, set on the chatbot or on any ancestor, `:root` included.
   */
  colorScheme?: ColorScheme;
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
  /**
   * blocks sending while the user is anonymous: a notice asks them to log in and
   * the typed message stays in the input. The user counts as anonymous when
   * `callbackAuthorization` is missing or returns an empty value at send time.
   * Off by default, so anonymous chat keeps working.
   */
  requireLogin?: boolean;
  /**
   * called by the "Log in" button of the `requireLogin` notice: the login flow
   * belongs to the host page. Without it the notice shows the text only.
   */
  onLoginRequest?: () => void;
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
  ref,
  icon,
  title,
  initialMessage,
  nameChatbot,
  colorScheme,
  language,
  tenant,
  callbackAuthorization,
  useSource,
  numberOfSources,
  aiDisclosure,
  ragMode,
  requireLogin,
  onLoginRequest,
}) => {
  const resolvedNumberOfSources = resolveNumberOfSources(
    numberOfSources,
    useSource,
  );
  return (
    <React.Fragment>
      <LanguageProvider initialLanguage={language}>
        <StructureChatbot
          ref={ref}
          icon={icon}
          title={title}
          initialMessage={initialMessage}
          nameChatbot={nameChatbot}
          colorScheme={colorScheme}
          tenant={tenant}
          callbackAuthorization={callbackAuthorization}
          numberOfSources={resolvedNumberOfSources}
          aiDisclosure={aiDisclosure}
          ragMode={ragMode}
          requireLogin={requireLogin}
          onLoginRequest={onLoginRequest}
        />
      </LanguageProvider>
    </React.Fragment>
  );
};

const StructureChatbot: React.FC<ChatbotProps> = ({
  ref,
  icon,
  title,
  initialMessage,
  nameChatbot,
  colorScheme = "light",
  tenant = "",
  callbackAuthorization,
  numberOfSources = DEFAULT_NUMBER_OF_SOURCES,
  aiDisclosure,
  ragMode = DEFAULT_RAG_MODE,
  requireLogin = false,
  onLoginRequest,
}) => {
  const [isView, setIsView] = React.useState(false);
  const [loginNoticeOpen, setLoginNoticeOpen] = React.useState(false);
  const [welcomeMessageTime] = React.useState(() =>
    new Date().toLocaleTimeString([], { hour: "2-digit", minute: "2-digit" }),
  );
  const chatbotSearchRef = React.useRef<HTMLInputElement | null>(null);
  const aiDisclosureId = React.useId();
  const resolvedAiDisclosure = resolveAiDisclosure(
    aiDisclosure,
    Translate({ label: "aiDisclosure" }),
  );
  // l'id sta su una sola istanza: `aria-describedby` ne referenza una soltanto e
  // due nodi con lo stesso id non sono validi. Lo porta quella sotto l'input,
  // la piu' vicina al campo; il top solo quando e' l'unica presente
  const describedSlotIsBottom = resolvedAiDisclosure.bottom !== undefined;
  const defaultWelcomeMessage = Translate({ label: "welcomeMessage" });
  const loginRequiredText = Translate({ label: "loginRequired" });
  const loginText = Translate({ label: "login" });
  const closeText = Translate({ label: "close" });

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

  // same test as useGenerateResponse: no token at send time means anonymous.
  // Returns the request, or null when the login notice opens instead
  const startResponse = (message: string) => {
    if (requireLogin && !callbackAuthorization?.()) {
      setLoginNoticeOpen(true);
      return null;
    }
    setLoginNoticeOpen(false);
    return generateResponse(message);
  };

  // `isChatting` si aggiorna solo al render dopo: due `send` nello stesso handler
  // lo vedrebbero entrambe a false. Il ref resta acceso finche' la richiesta
  // partita da `send` non e' finita (a fine stream, su errore o su annullamento)
  const isSending = React.useRef(false);
  // l'handle e' creato una volta sola e legge da qui i valori dell'ultimo render:
  // `generateResponse` cambia a ogni chunk, e un handle ricreato ogni volta
  // resterebbe stantio in mano a chi lo ha salvato
  const latest = React.useRef({ isChatting, startResponse });
  React.useLayoutEffect(() => {
    latest.current = { isChatting, startResponse };
  });
  // un handle tenuto da parte non deve inviare a chatbot smontato: la richiesta
  // partirebbe e nessuno vedrebbe la risposta
  const isMounted = React.useRef(false);
  React.useLayoutEffect(() => {
    isMounted.current = true;
    return () => {
      isMounted.current = false;
    };
  }, []);

  React.useImperativeHandle(
    ref,
    () => ({
      send(message) {
        const { isChatting, startResponse } = latest.current;
        if (
          !isMounted.current ||
          isSending.current ||
          isChatting ||
          message.trim() === ""
        )
          return false;
        setIsView(true);
        const request = startResponse(message);
        if (!request) return false;
        isSending.current = true;
        request.finally(() => {
          isSending.current = false;
        });
        return true;
      },
    }),
    [],
  );

  const handleSearch = (message: string) => startResponse(message) !== null;

  React.useEffect(() => {
    if (messages && messagesEndRef.current) {
      messagesEndRef.current.scrollIntoView({ behavior: "smooth" });
    }
  }, [messages]);

  React.useEffect(() => {
    if (isView && chatbotSearchRef.current) {
      chatbotSearchRef.current.focus();
    }
  }, [isView]);

  return (
    <div
      className="openk9-chatbot-structure"
      data-color-scheme={colorScheme}
      ref={trapRef}
    >
      {isView && (
        <div className="openk9-chatbot-container">
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
            initialMessage={initialMessage ?? defaultWelcomeMessage}
            isGenerateMessage={isGenerateMessage}
            messagesEndRef={messagesEndRef}
            nameChatbot={nameChatbot}
            numberOfSources={numberOfSources}
            colorScheme={colorScheme}
          />
          <Search
            handleSearch={handleSearch}
            cancelAllResponses={cancelAllResponses}
            isChatting={isChatting}
            icon={icon}
            chatbotSearchRef={chatbotSearchRef}
            describedById={aiDisclosureId}
            notice={
              loginNoticeOpen && (
                <div className="openk9-login-notice" role="alert">
                  <div className="openk9-login-notice-icon">
                    <LockOutlinedIcon />
                  </div>
                  <div className="openk9-login-notice-message">
                    {loginRequiredText}
                  </div>
                  <div className="openk9-login-notice-actions">
                    {onLoginRequest && (
                      <button
                        type="button"
                        className="openk9-login-notice-login"
                        onClick={onLoginRequest}
                      >
                        {loginText}
                      </button>
                    )}
                    <button
                      type="button"
                      className="openk9-login-notice-close"
                      aria-label={closeText}
                      onClick={() => setLoginNoticeOpen(false)}
                    >
                      <CloseIcon />
                    </button>
                  </div>
                </div>
              )
            }
          />
          {resolvedAiDisclosure.bottom !== undefined && (
            <AiDisclosure
              id={aiDisclosureId}
              text={resolvedAiDisclosure.bottom}
            />
          )}
        </div>
      )}
      <button
        type="button"
        className="openk9-toggle-chatbot-button"
        aria-label={
          isView
            ? Translate({ label: "closeChatbot" })
            : Translate({ label: "openChatbot" })
        }
        onClick={() => setIsView(!isView)}
      >
        <div className="openk9-toggle-icon-wrapper">
          {isView ? icon.closeIcon : icon.buttonIcon}
        </div>
      </button>
    </div>
  );
};

const ChatbotHeader: React.FC<{
  title?: React.ReactNode;
  icon: IconProps;
  isChatting: boolean;
  resetMessage: () => void;
  setIsView: (isView: boolean) => void;
}> = ({ title, icon, isChatting, resetMessage, setIsView }) => {
  return (
    <div className="openk9-chatbot-header">
      <div className="openk9-logo-icon">{icon.logoIcon}</div>
      <div>{title}</div>
      <div className="openk9-header-buttons">
        <button
          type="button"
          className="openk9-refresh-button"
          disabled={isChatting}
          onClick={resetMessage}
          aria-label={Translate({ label: "clearChat" })}
        >
          {icon.refreshChatIcon}
        </button>

        <button
          type="button"
          className="openk9-close-button"
          onClick={() => setIsView(false)}
          role="button"
          aria-label={Translate({ label: "closeChatbot" })}
        >
          {icon.closeModal}
        </button>
      </div>
    </div>
  );
};

const MessageList: React.FC<{
  messages: Message[];
  icon: IconProps;
  initialMessage: string;
  isGenerateMessage: { id: string; isLoading: boolean } | null;
  messagesEndRef: React.Ref<HTMLDivElement>;
  nameChatbot?: string;
  welcomeMessageTime?: string;
  numberOfSources?: number;
  colorScheme: ColorScheme;
}> = ({
  messages,
  icon,
  initialMessage,
  isGenerateMessage,
  messagesEndRef,
  nameChatbot,
  welcomeMessageTime = "",
  numberOfSources = DEFAULT_NUMBER_OF_SOURCES,
  colorScheme,
}) => (
  <section className="openk9-message-list-container">
    <div className="openk9-message-list">
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
                themeInfo={colorScheme}
              />
              {index === messages.length - 1 && <div ref={messagesEndRef} />}
            </React.Fragment>
          );
        })}
    </div>
  </section>
);

export default Chatbot;
