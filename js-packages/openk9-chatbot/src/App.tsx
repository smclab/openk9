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
import Chatbot from "../lib/components/Chatbot";
import { Logo } from "./svg/Logo";
import { User } from "./svg/User";
import { TrashIcon } from "./svg/Trash";
import { SearchIcon } from "./svg/SearchSvg";
import { CloseIcon } from "./svg/CloseIcon";
import { defaultThemeK9 } from "./theme";

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

