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
import { Box, TextField, Button, useTheme } from "@mui/material";
import React from "react";
import { IconProps } from "./Chatbot";
import { Translate } from "./Translate";

export default function Search({
  handleSearch,
  isChatting,
  icon,
  chatbotSearchRef,
}: {
  handleSearch: (message: string) => void;
  cancelAllResponses(): void;
  isChatting: boolean;
  icon: IconProps;
  chatbotSearchRef: React.MutableRefObject<HTMLHeadingElement | null>;
}) {
  const [search, setSearch] = React.useState("");
  const theme = useTheme();
  return (
    <form
      className="openk9-search-form"
      onSubmit={(event) => {
        event.preventDefault();
        handleSearch(search);
        setSearch("");
      }}
      style={{
        display: "flex",
        gap: "15px",
        padding: "12px",
        borderTop: "1px solid #C7C7C7C7",
      }}
    >
      <Box
        className="openk9-search-box"
        display={"flex"}
        width={"100%"}
        gap={1}
      >
        <TextField
          className="openk9-search-input"
          fullWidth
          inputRef={chatbotSearchRef}
          variant="outlined"
          value={search}
          onChange={(event) => setSearch(event.currentTarget.value)}
          placeholder={Translate({ label: "customPlaceholder" })}
          sx={{ width: "100%", background: "white" }}
          inputProps={{
            style: {
              padding: 0,
              paddingInline: "6px",
              height: "29px",
              fontSize: "12px",
              fontWeight: "400",
              lineHeight: "18.25px",
              textAlign: "left",
              borderRadius: theme.shape.borderRadius * 2,
            },
          }}
        />
        <Button
          className="openk9-search-button"
          type="submit"
          variant="contained"
          aria-label={Translate({ label: "startQuestion" })}
          color="primary"
          sx={{
            backgroundColor: theme.palette.primary.main,
            color: theme.palette.primary.contrastText,
            stroke: "white",
            fill: "transparent",
            minWidth: "29px",
            height: "29px",
            padding: 0,
            borderRadius: theme.shape.borderRadius,
          }}
          disabled={search === "" || isChatting}
        >
          {icon.searchIcon}
        </Button>
      </Box>
    </form>
  );
}

