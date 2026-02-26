import { Grid, ButtonBase, Paper, Box, Typography } from "@mui/material";
import AutoAwesomeIcon from "@mui/icons-material/AutoAwesome"; // MUI icon simile a Sparkles

const SuggestedPrompts = ({
	suggestedPrompts,
	handleSuggestedPrompt,
}: {
	suggestedPrompts: Array<string>;
	handleSuggestedPrompt: (prompt: string) => void;
}) => {
	return (
		<Grid container spacing={2} mt={6}>
			{suggestedPrompts.map((prompt: any, index: any) => (
				<Grid item xs={12} md={6} key={index}>
					<ButtonBase onClick={() => handleSuggestedPrompt(prompt)} sx={{ width: "100%", textAlign: "left" }}>
						<Paper
							elevation={1}
							sx={{
								boxShadow: "none",
								width: "100%",
								p: 2,
								border: "1px solid",
								borderColor: "grey.200",
								borderRadius: "10px",
								transition: "all 0.2s",
								"&:hover": {
									borderColor: "error.light",
									boxShadow: 3,
								},
							}}
						>
							<Box display="flex" alignItems="flex-start" gap={2}>
								<Box
									sx={{
										width: 32,
										height: 32,
										bgcolor: "grey.100",
										borderRadius: "10px",
										display: "flex",
										alignItems: "center",
										justifyContent: "center",
										transition: "background-color 0.2s",
										".MuiButtonBase-root:hover &": {
											bgcolor: "error.lighter",
										},
									}}
								>
									<AutoAwesomeIcon
										sx={{
											fontSize: 18,
											color: "grey.600",
											".MuiButtonBase-root:hover &": {
												color: "error.main",
											},
										}}
									/>
								</Box>
								<Box>
									<Typography variant="body2" fontWeight={500} color="text.primary">
										{prompt}
									</Typography>
									<Typography variant="caption" color="text.secondary" mt={0.5}>
										Click here to start
									</Typography>
								</Box>
							</Box>
						</Paper>
					</ButtonBase>
				</Grid>
			))}
		</Grid>
	);
};

export default SuggestedPrompts;
