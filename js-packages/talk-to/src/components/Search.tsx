import { Box, Button, TextField } from "@mui/material";
import ArrowUpwardIcon from "@mui/icons-material/ArrowUpward";
import StopCircleIcon from "@mui/icons-material/StopCircle";
import React from "react";
import { useTranslation } from "react-i18next";

export default function Search({
	handleSearch,
	cancelAllResponses,
	isChatting,
}: {
	handleSearch: (message: string) => void;
	cancelAllResponses(): void;
	isChatting: boolean;
}) {
	const [search, setSearch] = React.useState("");
	const { t } = useTranslation();

	return (
		<Box component="footer" p={2} bgcolor="background.paper" sx={{ borderRadius: "10px" }} zIndex={2} width={"100%"}>
			<form
				onSubmit={(event) => {
					event.preventDefault();
					if (isChatting) {
						cancelAllResponses();
					} else {
						handleSearch(search);
						setSearch("");
					}
				}}
				style={{ width: "100%", display: "flex", gap: "15px" }}
			>
				<TextField
					size="small"
					fullWidth
					variant="outlined"
					value={search}
					onChange={(event) => setSearch(event.currentTarget.value)}
					placeholder={t("write-a-message")!}
					sx={{
						"& .MuiOutlinedInput-notchedOutline": {
							borderRadius: "10px",
						},
						"&:hover .MuiOutlinedInput-notchedOutline": {
							borderColor: "#c0272b",
						},
					}}
				/>
				<Button
					variant="contained"
					type="submit"
					value="Submit"
					disabled={!isChatting && search === ""}
					sx={{ borderRadius: "10px" }}
				>
					{isChatting ? <StopCircleIcon /> : <ArrowUpwardIcon />}
				</Button>
			</form>
		</Box>
	);
}
