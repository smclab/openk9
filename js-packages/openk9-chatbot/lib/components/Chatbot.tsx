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
import { SingleMessage, type SourcesVariant } from "./SingleMessage";
import { loadSavedConversation, saveConversation } from "./conversationStore";
import { useFocusTrap } from "./useFocusTrap";
import { Translate } from "./Translate";
import { LanguageProvider } from "./LanguageContext";
import { CloseIcon, LockOutlinedIcon, RefreshIcon } from "./icons";
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
  /**
   * opens the panel and moves the focus to its input, sending nothing. On a
   * panel that `persistConversation` reopened and the user has not entered
   * yet, it moves the focus to the input and makes the panel modal. No effect
   * when the panel is already open otherwise, or after the chatbot has
   * unmounted.
   */
  open(): void;
  /**
   * closes the panel, keeping the conversation for when it opens again. No
   * effect when the panel is already closed or after the chatbot has unmounted.
   */
  close(): void;
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
   * how the sources under an answer look: `chips` (default) are the coloured
   * chips with the copy and expand buttons, `list` is a list of links that
   * open in a new tab, coloured by the CSS variables.
   */
  sourcesVariant?: SourcesVariant;
  /** `false` hides the time under every message, the welcome one included */
  showTimestamps?: boolean;
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
  /**
   * shows the round button that opens and closes the panel. On by default; with
   * `false` the panel opens only from the host page, through `open()` or
   * `send()` on `ref`, and closes with its own close button, Esc or `close()`.
   */
  showLauncher?: boolean;
  /** replaces the placeholder of the input. Defaults to the translated one */
  placeholder?: string;
  /**
   * keeps the conversation and the panel state across the pages of the same
   * tab, in `sessionStorage` under a key per `tenant`: the concluded messages
   * for an anonymous user, the chat id only for an authenticated one (the
   * messages come back from the backend). A panel left open opens again on the
   * next page without taking the focus; on screens narrower than 576px, where
   * the panel fills the screen, it comes back closed. Off by default.
   */
  persistConversation?: boolean;
};

/** the two positions the disclosure can occupy inside the panel */
type AiDisclosureSlots = {
  /** above the message list, right under the header */
  top?: React.ReactNode;
  /** under the input, where the notice sits by default */
  bottom?: React.ReactNode;
};

const DEFAULT_NUMBER_OF_SOURCES = 8;

/** the panel floats beside the page from 576px up; narrower, it fills the screen */
function isWideScreen() {
  return (
    typeof window.matchMedia !== "function" ||
    window.matchMedia("(min-width: 576px)").matches
  );
}

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
  sourcesVariant,
  showTimestamps,
  aiDisclosure,
  ragMode,
  requireLogin,
  onLoginRequest,
  showLauncher,
  placeholder,
  persistConversation,
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
          sourcesVariant={sourcesVariant}
          showTimestamps={showTimestamps}
          aiDisclosure={aiDisclosure}
          ragMode={ragMode}
          requireLogin={requireLogin}
          onLoginRequest={onLoginRequest}
          showLauncher={showLauncher}
          placeholder={placeholder}
          persistConversation={persistConversation}
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
  sourcesVariant = "chips",
  showTimestamps = true,
  aiDisclosure,
  ragMode = DEFAULT_RAG_MODE,
  requireLogin = false,
  onLoginRequest,
  showLauncher = true,
  placeholder,
  persistConversation = false,
}) => {
  // letto una volta, al montaggio: e' quanto ha lasciato la pagina precedente
  // della stessa scheda. Stesso test di useGenerateResponse: senza token
  // l'utente e' anonimo. Una conversazione dell'altro tipo di utente (login o
  // logout tra le due pagine, token non ancora pronto) non si riprende; il
  // pannello si'. Sotto i 576px il pannello occupa lo schermo: si riprende chiuso
  const [saved] = React.useState(() => {
    const conversation = persistConversation
      ? loadSavedConversation(tenant)
      : null;
    if (!conversation) return null;
    const isAuthenticated = Boolean(callbackAuthorization?.());
    const hasChatId = "chatId" in conversation;
    const isOtherUser = isAuthenticated !== hasChatId;
    return {
      open: conversation.open && isWideScreen(),
      startedAt: conversation.startedAt,
      chatId:
        isAuthenticated && "chatId" in conversation
          ? conversation.chatId
          : null,
      messages:
        !isAuthenticated && "messages" in conversation
          ? conversation.messages
          : [],
      other: isOtherUser ? conversation : null,
    };
  });
  const [isView, setIsView] = React.useState(saved?.open ?? false);
  // il pannello riaperto al cambio pagina si vede ma non e' ancora modale: non
  // prende il focus e non trattiene Tab finche' l'utente non ci entra
  const [isPassive, setIsPassive] = React.useState(saved?.open ?? false);
  const [loginNoticeOpen, setLoginNoticeOpen] = React.useState(false);
  const [startedAt] = React.useState(
    () => saved?.startedAt ?? new Date().toISOString(),
  );
  const welcomeMessageTime = new Date(startedAt).toLocaleTimeString([], {
    hour: "2-digit",
    minute: "2-digit",
  });
  // la chat dell'utente autenticato ancora da ricaricare dal backend
  const [pendingChatId, setPendingChatId] = React.useState(
    saved?.chatId ?? null,
  );
  // la conversazione dell'altro tipo di utente resta salvata com'e' finche'
  // questa pagina non ne inizia o ne svuota una
  const [otherUserConversation, setOtherUserConversation] = React.useState(
    saved?.other ?? null,
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
  const openChatbotText = Translate({ label: "openChatbot" });
  const closeChatbotText = Translate({ label: "closeChatbot" });
  const titleId = React.useId();

  const {
    messages,
    generateResponse,
    cancelAllResponses,
    isChatting,
    isLoading: isGenerateMessage,
    resetMessage,
    getChatId,
    restoreChat,
  } = useGenerateResponse({
    initialMessages: saved?.messages ?? [],
    tenant,
    callbackAuthorization,
    ragMode,
  });
  const [trapRef] = useFocusTrap(isView && !isPassive);
  const messagesEndRef = React.useRef<HTMLDivElement | null>(null);

  // same test as useGenerateResponse: no token at send time means anonymous.
  // Returns the request, or null when the login notice opens instead
  const startResponse = (message: string) => {
    if (requireLogin && !callbackAuthorization?.()) {
      setLoginNoticeOpen(true);
      return null;
    }
    setLoginNoticeOpen(false);
    // una domanda inviata prima che la chat salvata arrivi ne apre una nuova
    setPendingChatId(null);
    setOtherUserConversation(null);
    return generateResponse(message);
  };

  const resetConversation = () => {
    setPendingChatId(null);
    setOtherUserConversation(null);
    resetMessage();
  };

  // in errore (401, 404, rete) o senza messaggi si riparte da una conversazione
  // vuota, senza avvisi; il salvataggio sotto perde il chatId
  const restoreSavedChat = React.useEffectEvent(
    (chatId: string, signal: AbortSignal) => restoreChat(chatId, signal),
  );
  React.useEffect(() => {
    if (pendingChatId === null) return;
    const controller = new AbortController();
    restoreSavedChat(pendingChatId, controller.signal)
      .catch(() => undefined)
      .finally(() => {
        if (!controller.signal.aborted) setPendingChatId(null);
      });
    return () => controller.abort();
  }, [pendingChatId]);

  // finche' la chat salvata non e' arrivata il suo chatId resta nel salvataggio,
  // cosi' un nuovo cambio pagina non la perde
  React.useEffect(() => {
    if (!persistConversation) return;
    if (otherUserConversation !== null) {
      saveConversation(tenant, { ...otherUserConversation, open: isView });
      return;
    }
    const chatId = getChatId() ?? pendingChatId;
    saveConversation(
      tenant,
      chatId !== null
        ? { open: isView, startedAt, chatId }
        : { open: isView, startedAt, messages },
    );
  }, [
    persistConversation,
    tenant,
    isView,
    startedAt,
    messages,
    pendingChatId,
    otherUserConversation,
    getChatId,
  ]);

  // chi aveva il focus prima dell'apertura: il pulsante tondo o quello della
  // pagina. Alla chiusura il focus ci torna solo se stava dentro il pannello,
  // che smontando lo perderebbe; dopo il commit, perche' su mobile il pulsante
  // tondo e' nascosto finche' il pannello e' aperto. Se chi apriva non c'e' piu'
  // o era il body (Safari e Firefox su macOS non danno il focus al pulsante
  // cliccato) il focus va al pulsante tondo, quando c'e'
  const opener = React.useRef<HTMLElement | null>(null);
  const focusAfterClose = React.useRef<HTMLElement | null>(null);
  const launcherRef = React.useRef<HTMLButtonElement | null>(null);
  // sul pannello riaperto al cambio pagina l'apertura lo rende modale e porta
  // il focus all'input, come un'apertura normale
  const openPanel = () => {
    if (!trapRef.current || isPassive) {
      opener.current =
        document.activeElement instanceof HTMLElement
          ? document.activeElement
          : null;
    }
    if (isPassive) chatbotSearchRef.current?.focus();
    setIsPassive(false);
    setIsView(true);
  };
  // l'utente entra nel pannello riaperto con un clic o con Tab
  const enterPanel = () => setIsPassive(false);
  const closePanel = () => {
    const previous = opener.current;
    const canReturn =
      previous !== null && previous !== document.body && previous.isConnected;
    focusAfterClose.current = trapRef.current?.contains(document.activeElement)
      ? canReturn
        ? previous
        : launcherRef.current
      : null;
    setIsView(false);
  };

  // `isChatting` si aggiorna solo al render dopo: due `send` nello stesso handler
  // lo vedrebbero entrambe a false. Il ref resta acceso finche' la richiesta
  // partita da `send` non e' finita (a fine stream, su errore o su annullamento)
  const isSending = React.useRef(false);
  // l'handle e' creato una volta sola e legge da qui i valori dell'ultimo render:
  // `generateResponse` cambia a ogni chunk, e un handle ricreato ogni volta
  // resterebbe stantio in mano a chi lo ha salvato
  const latest = React.useRef({
    isChatting,
    startResponse,
    openPanel,
    closePanel,
  });
  React.useLayoutEffect(() => {
    latest.current = { isChatting, startResponse, openPanel, closePanel };
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
        const { isChatting, startResponse, openPanel } = latest.current;
        if (
          !isMounted.current ||
          isSending.current ||
          isChatting ||
          message.trim() === ""
        )
          return false;
        openPanel();
        const request = startResponse(message);
        if (!request) return false;
        isSending.current = true;
        request.finally(() => {
          isSending.current = false;
        });
        return true;
      },
      open() {
        if (!isMounted.current) return;
        latest.current.openPanel();
      },
      close() {
        if (!isMounted.current) return;
        latest.current.closePanel();
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

  // il pannello riaperto al cambio pagina e' gia' visibile al montaggio, ma il
  // focus resta alla pagina
  const focusInputOnOpen = React.useEffectEvent(() => {
    if (!isPassive) chatbotSearchRef.current?.focus();
  });
  React.useEffect(() => {
    if (isView) focusInputOnOpen();
  }, [isView]);

  // se intanto il focus l'ha preso qualcun altro (la modale di login della
  // pagina, aperta da `onLoginRequest`) resta li'. Un opener ancora nel DOM ma
  // nascosto (un menu richiuso) non lo accetta: allora va al pulsante tondo
  React.useEffect(() => {
    if (isView) return;
    const target = focusAfterClose.current;
    focusAfterClose.current = null;
    if (!target) return;
    const active = document.activeElement;
    if (active !== null && active !== document.body) return;
    target.focus();
    if (document.activeElement !== target) launcherRef.current?.focus();
  }, [isView]);

  return (
    <div className="openk9-chatbot-structure" data-color-scheme={colorScheme}>
      {isView && (
        // il focus trap copre solo il pannello: con `aria-modal` il resto della
        // pagina, pulsante tondo compreso, e' fuori dalla finestra. Il pannello
        // riaperto al cambio pagina diventa modale quando l'utente ci entra
        <div
          className="openk9-chatbot-container"
          ref={trapRef}
          role="dialog"
          aria-modal={isPassive ? undefined : "true"}
          onFocus={enterPanel}
          onPointerDown={enterPanel}
          aria-labelledby={title ? titleId : undefined}
          aria-label={title ? undefined : nameChatbot || "Chatbot"}
          onKeyDown={(event) => {
            // 229: Safari chiude la composizione prima del keydown, con
            // `isComposing` gia' a false
            if (
              event.key !== "Escape" ||
              event.nativeEvent.isComposing ||
              event.keyCode === 229
            )
              return;
            closePanel();
            // l'Esc e' del chatbot: un listener della pagina (il menu che contiene
            // il pulsante che lo ha aperto) non deve chiudersi anche lui
            event.stopPropagation();
          }}
        >
          <ChatbotHeader
            title={title}
            titleId={titleId}
            icon={icon}
            isChatting={isChatting}
            resetMessage={resetConversation}
            onClose={closePanel}
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
            sourcesVariant={sourcesVariant}
            showTimestamps={showTimestamps}
            colorScheme={colorScheme}
          />
          <Search
            handleSearch={handleSearch}
            cancelAllResponses={cancelAllResponses}
            isChatting={isChatting}
            icon={icon}
            chatbotSearchRef={chatbotSearchRef}
            describedById={aiDisclosureId}
            placeholder={placeholder}
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
      {showLauncher && (
        <button
          ref={launcherRef}
          type="button"
          className="openk9-toggle-chatbot-button"
          aria-label={isView ? closeChatbotText : openChatbotText}
          onClick={isView ? closePanel : openPanel}
        >
          <div className="openk9-toggle-icon-wrapper">
            {isView ? icon.closeIcon : icon.buttonIcon}
          </div>
        </button>
      )}
    </div>
  );
};

const ChatbotHeader: React.FC<{
  title?: React.ReactNode;
  titleId: string;
  icon: IconProps;
  isChatting: boolean;
  resetMessage: () => void;
  onClose: () => void;
}> = ({ title, titleId, icon, isChatting, resetMessage, onClose }) => {
  return (
    <div className="openk9-chatbot-header">
      <div className="openk9-logo-icon">{icon.logoIcon}</div>
      <div id={titleId}>{title}</div>
      <div className="openk9-header-buttons">
        <button
          type="button"
          className="openk9-refresh-button"
          disabled={isChatting}
          onClick={resetMessage}
          aria-label={Translate({ label: "clearChat" })}
        >
          {icon.refreshChatIcon ?? <RefreshIcon />}
        </button>

        <button
          type="button"
          className="openk9-close-button"
          onClick={onClose}
          role="button"
          aria-label={Translate({ label: "closeChatbot" })}
        >
          {icon.closeModal ?? <CloseIcon />}
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
  sourcesVariant: SourcesVariant;
  showTimestamps: boolean;
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
  sourcesVariant,
  showTimestamps,
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
        showTimestamps={showTimestamps}
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
                showTimestamps={showTimestamps}
              />
              <SingleMessage
                contentMessage={message.answer}
                status={message.status}
                sources={message?.sources}
                numberOfSources={numberOfSources}
                sourcesVariant={sourcesVariant}
                showTimestamps={showTimestamps}
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
