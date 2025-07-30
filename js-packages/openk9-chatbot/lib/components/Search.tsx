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
