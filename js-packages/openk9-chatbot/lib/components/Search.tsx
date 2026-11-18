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
import { IconProps } from "./Chatbot";
import { Translate } from "./Translate";

export default function Search({
  handleSearch,
  isChatting,
  icon,
  chatbotSearchRef,
  describedById,
  notice,
}: {
  /** returns false when the message was not sent, so the input keeps it */
  handleSearch: (message: string) => boolean;
  cancelAllResponses(): void;
  isChatting: boolean;
  icon: IconProps;
  chatbotSearchRef: React.RefObject<HTMLInputElement | null>;
  /** id of the AI disclosure, announced when the input takes focus */
  describedById?: string;
  /** shown above the input, e.g. why the last message was not sent */
  notice?: React.ReactNode;
}) {
  const [search, setSearch] = React.useState("");
  return (
    <form
      className="openk9-search-form"
      onSubmit={(event) => {
        event.preventDefault();
        if (!handleSearch(search)) return;
        setSearch("");
      }}
    >
      {notice}
      <div className="openk9-search-box">
        <div className="openk9-search-input">
          <input
            ref={chatbotSearchRef}
            type="text"
            value={search}
            onChange={(event) => setSearch(event.currentTarget.value)}
            placeholder={Translate({ label: "customPlaceholder" })}
            aria-invalid={false}
            aria-describedby={describedById}
          />
        </div>
        <button
          className="openk9-search-button"
          type="submit"
          aria-label={Translate({ label: "startQuestion" })}
          disabled={search === "" || isChatting}
        >
          {icon.searchIcon}
        </button>
      </div>
    </form>
  );
}

