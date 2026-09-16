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
import { Box, IconButton, Link } from "@mui/material";
import { alpha } from "@mui/material/styles";
import CloseRoundedIcon from "@mui/icons-material/Close";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";
import React from "react";
import Chatbot from "../lib/components/Chatbot";
import { Logo } from "./svg/Logo";
import { User } from "./svg/User";
import { TrashIcon } from "./svg/Trash";
import { SearchIcon } from "./svg/SearchSvg";
import { CloseIcon } from "./svg/CloseIcon";
import { defaultThemeK9 } from "./theme";

// da sostituire con l'informativa privacy reale dell'integrazione
const PRIVACY_POLICY_URL = "https://www.example.com/privacy";

/**
 * Informativa AI richiudibile. Chiusa non sparisce: si riduce a una ⓘ che la
 * riapre, così resta sempre raggiungibile dall'utente.
 *
 * Tutto qui dentro è contenuto di frase (`span`, `a`, `button`, `svg`) perché il
 * pannello monta il nodo dentro un `<p>`: un `<div>` — per esempio un `Alert` di
 * MUI — verrebbe estratto dal paragrafo dal parser HTML, rompendo il layout e
 * l'associazione con `aria-describedby`.
 */
function AiDisclosureBanner() {
  const [isOpen, setIsOpen] = React.useState(true);

  if (!isOpen) {
    return (
      <IconButton
        size="small"
        aria-label="Mostra l'informativa sull'uso dell'intelligenza artificiale"
        onClick={() => setIsOpen(true)}
        sx={{ padding: "2px", color: "primary.main" }}
      >
        <InfoOutlinedIcon sx={{ fontSize: 18 }} />
      </IconButton>
    );
  }

  return (
    <Box
      component="span"
      sx={(theme) => ({
        display: "flex",
        alignItems: "flex-start",
        gap: "8px",
        padding: "10px",
        borderRadius: "8px",
        // velo del primario invece di un colore fisso: resta armonioso anche
        // se l'integratore passa un `themeCustom` con un'altra tinta
        backgroundColor: alpha(theme.palette.primary.main, 0.07),
        border: `1px solid ${alpha(theme.palette.primary.main, 0.2)}`,
        // #666 su questo velo resta sopra il 4.5:1 richiesto da WCAG AA
        color: theme.palette.text.secondary,
        fontSize: "11px",
        lineHeight: 1.45,
      })}
    >
      <InfoOutlinedIcon
        sx={{
          fontSize: 18,
          color: "primary.main",
          flexShrink: 0,
          marginTop: "1px",
        }}
      />
      <Box component="span" sx={{ flex: 1 }}>
        Questo assistente virtuale utilizza tecnologie di Intelligenza
        Artificiale. Le risposte sono generate automaticamente e hanno finalità
        esclusivamente informative. È opportuno non inserire dati personali o
        informazioni riservate. Consulta l’
        <Link
          href={PRIVACY_POLICY_URL}
          target="_blank"
          rel="noreferrer"
          sx={{ fontWeight: 700, color: "primary.main" }}
        >
          Informativa Privacy
        </Link>{" "}
        per maggiori dettagli.
      </Box>
      <IconButton
        size="small"
        aria-label="Chiudi l'informativa"
        onClick={() => setIsOpen(false)}
        sx={{ padding: "2px", flexShrink: 0, color: "text.secondary" }}
      >
        <CloseRoundedIcon sx={{ fontSize: 16 }} />
      </IconButton>
    </Box>
  );
}

function App() {
  return (
    <div
      className="openk9-chatbot"
      style={{ position: "absolute", bottom: "20px", right: "20px" }}
    >
      <Chatbot
        initialMessage="Chiedimi pure qualcosa"
        nameChatbot="Openk9"
        tenant="https://k9-frontend.openk9.io"
        // endpoint del RAG: "chat-tool" (default) lascia decidere all'agente se
        // consultare la knowledge base, "chat" passa sempre dal retrieval
        ragMode="chat-tool"
        // lo slot accetta un nodo qualsiasi, quindi l'informativa puo' avere uno
        // stato suo: qui e' un banner richiudibile. Sta in `bottom`, sotto
        // l'input; spostarlo in `top` lo porta sopra la lista dei messaggi
        aiDisclosure={{ bottom: <AiDisclosureBanner /> }}
        icon={{
          buttonIcon: <Logo size={35} color="white" />,
          userIcon: <User />,
          chatbotIcon: <Logo size={25} />,
          refreshChatIcon: <TrashIcon />,
          searchIcon: <SearchIcon />,
          logoIcon: <Logo size={35} />,
          closeIcon: <CloseIcon size="30px" color={"white"} />,
          closeModal: (
            <CloseIcon
              size="18px"
              color={defaultThemeK9.palette.primary.main}
            />
          ),
        }}
        title={
          <h3
            style={{
              color: defaultThemeK9.palette.primary.main,
              fontWeight: 400,
              fontSize: "14px",
              lineHeight: "22px",
            }}
          >
            Chatta con <span style={{ fontWeight: 700 }}>Openk9</span>
          </h3>
        }
      />
    </div>
  );
}

export default App;

