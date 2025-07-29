import { Box, Typography } from "@mui/material";
import { useTranslation } from "react-i18next";

export const InitialConversation: React.FC = () => {
	const { t } = useTranslation();
	return (
		<Box
			bgcolor="white"
			display="flex"
			flexDirection="column"
			justifyContent="center"
			alignItems="center"
			p={2}
			boxSizing="border-box"
			mt="15%"
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
			{/* <Box mt={3}>
        <Suggestions
          Icon={GroupIcon}
          name="Natural language conversation"
          onAction={() => console.log("Clicked Natural language conversation")}
        />
        <Suggestions
          Icon={GroupIcon}
          name="Knowledge base"
          onAction={() => console.log("Clicked Knowledge base")}
        />
        <Suggestions
          Icon={GroupIcon}
          name="Personalized recommendations"
          onAction={() => console.log("Clicked Personalized recommendations")}
        />
        <Suggestions
          Icon={GroupIcon}
          name="Seamless integrations"
          onAction={() => console.log("Clicked Seamless integrations")}
        />
      </Box> */}
		</Box>
	);
};
