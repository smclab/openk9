import ErrorIcon from "@mui/icons-material/Error";
import ExpandLessIcon from "@mui/icons-material/ExpandLess";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { Box, Card, CardContent, IconButton, Skeleton, Typography, useTheme } from "@mui/material";
import { useState } from "react";
import Markdown from "react-markdown";
import { Logo } from "../Svg/Logo";
import { Message } from "./useGenerateResponse";

export function MessageCard({
	message,
	isGenerateMessage,
}: {
	message: Message;
	isGenerateMessage: {
		isLoading: boolean;
		id: string;
	} | null;
}) {
	const [expanded, setExpanded] = useState(false);

	const handleExpandClick = () => {
		setExpanded(!expanded);
	};

	const sourcesToShow = 4;
	const sources = message.sources || [];
	const visibleSources = sources.slice(0, sourcesToShow);
	const collapsedSources = sources.slice(sourcesToShow);
	const theme = useTheme();

	return (
		<>
			<Box display="flex" alignItems="center" gap={4}>
				<img
					src="https://cdn-icons-png.flaticon.com/512/2919/2919906.png"
					aria-hidden="true"
					alt=""
					height={"50px"}
					width={"50px"}
				/>
				<Typography variant="h6" sx={{ color: "#424242" }}>
					{message.question}
				</Typography>
			</Box>
			<Box display="flex" alignItems="flex-start" gap={4}>
				<span style={{ minWidth: "50px" }}>
					<Logo size={50} />
				</span>
				<div
					style={{
						display: "flex",
						flexDirection: "column",
						width: "100%",
						flex: 1,
					}}
				>
					{message.status !== "ERROR" ? (
						isGenerateMessage?.id === message.id && isGenerateMessage?.isLoading === true ? (
							<>
								<div style={{ display: "flex", flexDirection: "column", gap: "5px" }}>
									<Skeleton variant="rectangular" width="100%" height={35} sx={{ background: "#ffe6e6" }} />
									<Skeleton variant="rectangular" width="100%" height={35} sx={{ background: "#ffe6e6" }} />
									<Skeleton variant="rectangular" width="100%" height={35} sx={{ background: "#ffe6e6" }} />
								</div>
							</>
						) : (
							<Markdown>{message.status}</Markdown>
						)
					) : (
						<Box
							sx={{
								display: "flex",
								alignItems: "stretch",
								borderRadius: "10px",
								overflow: "hidden",
								width: "100%",
							}}
						>
							<Box
								sx={{
									p: 1.5,
									background: "linear-gradient(135deg, rgba(234, 62, 151, 0.1) 0%, rgba(234, 62, 151, 0.19) 100%)",
									border: "2px solid #EA3E971A",
									display: "flex",
									justifyContent: "center",
									alignItems: "center",
									borderTopLeftRadius: "10px",
									borderBottomLeftRadius: "10px",
									minWidth: "48px",
								}}
							>
								<ErrorIcon sx={{ color: "#EA3E97" }} />
							</Box>
							<Box
								sx={{
									p: 1.5,
									border: "2px solid #EA3E971A",
									borderLeft: "none",
									width: "100%",
									display: "flex",
									alignItems: "center",
									fontSize: "0.95rem",
									color: "#333",
									borderTopRightRadius: "10px",
									borderBottomRightRadius: "10px",
									backgroundColor: "#ffffff",
								}}
							>
								{message.answer}
							</Box>
						</Box>
					)}
				</div>
			</Box>
			<Box display="flex" flexWrap="wrap" flexDirection="row" gap={2} mt={2} sx={{ marginLeft: "70px" }}>
				{visibleSources.map((source, index) => (
					<Card
						key={index}
						sx={{ minWidth: 150, cursor: "pointer", maxWidth: "250px" }}
						onClick={() => window.open(source.url, "_blank")}
					>
						<CardContent>
							<Typography variant="body2" component="div" sx={{ color: "black" }}>
								{source.title}
							</Typography>
							<div style={{ display: "flex", gap: "10px", marginTop: "10px" }}>
								<div
									style={{
										padding: "10px",
										borderRadius: "50px",
										background: "#d3d343",
									}}
								></div>
								<Typography variant="body2" sx={{ color: "black" }}>
									{source.source}
								</Typography>
							</div>
						</CardContent>
					</Card>
				))}
				{collapsedSources.length > 0 && (
					<Card
						sx={{ minWidth: 150, cursor: "pointer", minHeight: "120px", maxWidth: "250px", position: "relative" }}
						onClick={handleExpandClick}
					>
						<CardContent>
							<IconButton sx={{ position: "absolute", top: "10px", right: "10px" }} onClick={handleExpandClick}>
								{expanded ? <ExpandLessIcon /> : <ExpandMoreIcon />}
							</IconButton>
							<div style={{ display: "flex", gap: "5px", marginTop: "10px" }}>
								{collapsedSources.map((source, index) => (
									<div
										key={index}
										style={{
											padding: "10px",
											borderRadius: "50px",
											background: "#d3d343",
											width: "20px",
											height: "20px",
										}}
									></div>
								))}
							</div>
							<Typography variant="body2" component="div" sx={{ color: "black", marginTop: "10px" }}>
								{expanded
									? collapsedSources.map((source, index) => (
											<div key={index} style={{ marginBottom: "5px" }}>
												<Typography variant="body2">{source.source}</Typography>
											</div>
									  ))
									: "Visualizza altre fonti"}
							</Typography>
						</CardContent>
					</Card>
				)}
			</Box>
		</>
	);
}

function casualColor(): string {
	return `#${Math.floor(Math.random() * 0xffffff)
		.toString(16)
		.padStart(6, "0")}`;
}

function mappingColors(source: string): string | undefined {
	switch (source.toLowerCase()) {
		case "ansa.it":
			return "blue";
		case "eurosport.it":
			return "red";
		case "ilpost.it":
			return "green";
		case "corriere.it":
			return "yellow";
		default:
			return casualColor();
	}
}
