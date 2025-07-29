import { Box, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";
import SuggestedPrompts from "./SuggestedPrompts";

interface InitialConversationProps {
	handleSearch: (message: string) => void;
}

export const InitialConversation: React.FC<InitialConversationProps> = ({ handleSearch }) => {
	const { t } = useTranslation();
	const suggestedPrompts = [
		"Natural language conversation",
		"Personalized recommendations",
		"Seamless integrations",
		"Resolve a problem",
	];
	return (
		<Box
			bgcolor="white"
			display="flex"
			flexDirection="column"
			justifyContent="center"
			alignItems="center"
			p={2}
			boxSizing="border-box"
		>
			<Box display="flex" alignItems="center" mt={1}>
				<Typography variant="h5">
					<Box component="span" mr={1}>
						{t("welcome-to")}
					</Box>
					Open
				</Typography>
				<Typography variant="h5" fontWeight={700}>
					K9
				</Typography>
			</Box>
			<Typography variant="h5" gutterBottom align="center">
				{t("where-knowledge-has-no-limits")}
			</Typography>
			<Box mt={3}>
				<SuggestedPrompts
					suggestedPrompts={suggestedPrompts}
					handleSuggestedPrompt={(prompt) => handleSearch(prompt)}
				/>
			</Box>
		</Box>
	);
};
