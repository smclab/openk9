import CheckIcon from "@mui/icons-material/Check";
import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import ErrorIcon from "@mui/icons-material/Error";
import OpenInFullIcon from "@mui/icons-material/OpenInFull";
import VisibilityIcon from "@mui/icons-material/Visibility";
import { Box, Chip, IconButton, Skeleton, Typography } from "@mui/material";
import { useState } from "react";
import { useTranslation } from "react-i18next";
import Markdown from "react-markdown";
import { Logo } from "../Svg/Logo";
import { Message } from "./useGenerateResponse";

type Theme = "light" | "dark";

export function MessageCard({
	message,
	isGenerateMessage,
	theme = "light",
}: {
	message: Message;
	isGenerateMessage: {
		isLoading: boolean;
		id: string;
	} | null;
	theme?: Theme;
}) {
	const [copiedSource, setCopiedSource] = useState<string | null>(null);
	const [showSourcesModal, setShowSourcesModal] = useState(false);
	const [showAllSources, setShowAllSources] = useState(false);
	const [expandedChips, setExpandedChips] = useState<Set<string>>(new Set());
	const { t } = useTranslation();

	const sources = message.sources || [];
	const maxVisibleSources = 8;
	const visibleSources = sources.slice(0, maxVisibleSources);
	const remainingSources = sources.length - maxVisibleSources;

	const copySource = async (source: any) => {
		try {
			await navigator.clipboard.writeText(source.url);
			setCopiedSource(source.url);
			setTimeout(() => setCopiedSource(null), 2000);
		} catch (err) {
			console.error("Errore durante la copia:", err);
		}
	};

	const toggleChipExpansion = (url: string) => {
		const newSet = new Set(expandedChips);

		if (newSet.has(url)) {
			newSet.delete(url);
			if (newSet.size < sources.length) {
				setShowAllSources(false);
			}
		} else {
			newSet.add(url);
			if (newSet.size === sources.length) {
				setShowAllSources(true);
			}
		}

		setExpandedChips(newSet);
	};

	const getTypeColor = (source: string) => {
		const baseColor = getStableColor(source, theme);

		const hexToRgb = (hex: string) => {
			const result = /^#?([a-f\d]{2})([a-f\d]{2})([a-f\d]{2})$/i.exec(hex);
			return result
				? {
						r: parseInt(result[1], 16),
						g: parseInt(result[2], 16),
						b: parseInt(result[3], 16),
				  }
				: { r: 200, g: 200, b: 200 };
		};

		const rgb = hexToRgb(baseColor);

		const backgroundOpacity = theme === "light" ? 0.1 : 0.2;
		const borderOpacity = theme === "light" ? 0.3 : 0.4;

		const lightBackground = `rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, ${backgroundOpacity})`;
		const lightBorder = `rgba(${rgb.r}, ${rgb.g}, ${rgb.b}, ${borderOpacity})`;

		return {
			backgroundColor: lightBackground,
			color: baseColor,
			borderColor: lightBorder,
		};
	};

	const PreBlock = (props: any) => {
		const [copied, setCopied] = useState(false);
		const child: any = Array.isArray(props.children) ? props.children?.[0] : props.children;
		const className: string = child?.props?.className || "";
		const languageMatch = /language-([a-z0-9+#]+)/i.exec(className);
		const language = languageMatch?.[1] || "txt";
		const badgeFor = (lang: string) => {
			switch ((lang || "").toLowerCase()) {
				case "ts":
				case "tsx":
					return {
						label: "TS",
						bg: theme === "light" ? "#e6f0ff" : "#133a66",
						color: theme === "light" ? "#2f6feb" : "#7aa7ff",
					};
				case "js":
				case "jsx":
					return {
						label: "JS",
						bg: theme === "light" ? "#fff9db" : "#5a4d00",
						color: theme === "light" ? "#a68b00" : "#ffd866",
					};
				case "py":
				case "python":
					return {
						label: "PY",
						bg: theme === "light" ? "#eaf5ff" : "#0f2d4d",
						color: theme === "light" ? "#0b78d1" : "#6cb8ff",
					};
				case "json":
					return {
						label: "JSON",
						bg: theme === "light" ? "#f0f0f0" : "#343434",
						color: theme === "light" ? "#555" : "#ccc",
					};
				case "html":
					return {
						label: "HTML",
						bg: theme === "light" ? "#ffe8e1" : "#4d2a23",
						color: theme === "light" ? "#e34c26" : "#ffab91",
					};
				case "css":
					return {
						label: "CSS",
						bg: theme === "light" ? "#e7f3ff" : "#203a5a",
						color: theme === "light" ? "#2965f1" : "#90caf9",
					};
				case "sql":
					return {
						label: "SQL",
						bg: theme === "light" ? "#e8f5e9" : "#1b3a2a",
						color: theme === "light" ? "#2e7d32" : "#a5d6a7",
					};
				case "bash":
				case "sh":
					return {
						label: "BASH",
						bg: theme === "light" ? "#edf7ed" : "#1f2a1f",
						color: theme === "light" ? "#2e7d32" : "#b2fab4",
					};
				case "yaml":
				case "yml":
					return {
						label: "YAML",
						bg: theme === "light" ? "#fff3e0" : "#4a3722",
						color: theme === "light" ? "#ef6c00" : "#ffcc80",
					};
				default:
					return {
						label: (lang || "").toUpperCase(),
						bg: theme === "light" ? "transparent" : "transparent",
						color: theme === "light" ? "#666" : "#bbb",
					};
			}
		};
		const badge = badgeFor(language);
		const rawChildren = child?.props?.children;
		const codeText = Array.isArray(rawChildren) ? rawChildren.join("") : String(rawChildren ?? "");

		const copy = async () => {
			try {
				await navigator.clipboard.writeText(codeText);
				setCopied(true);
				setTimeout(() => setCopied(false), 1500);
			} catch (e) {}
		};

		return (
			<div
				style={{
					position: "relative",
					width: "100%",
					maxWidth: "100%",
					whiteSpace: "pre-wrap",
					borderRadius: "8px",
					padding: "12px",
					backgroundColor: theme === "light" ? "#f5f5f5" : "#1e1e1e",
				}}
			>
				<div style={{ position: "absolute" }}>
					<span
						style={{
							display: "inline-block",
							padding: "2px 8px",
							borderRadius: 6,
							background: badge.bg,
							color: badge.color,
							fontSize: 11,
							fontWeight: 600,
							textTransform: "uppercase",
						}}
					>
						{badge.label || ""}
					</span>
				</div>
				<div style={{ position: "sticky", top: 6, zIndex: 1, display: "flex", justifyContent: "flex-end" }}>
					<button
						onClick={copy}
						style={{
							display: "inline-flex",
							alignItems: "center",
							gap: 6,
							fontSize: 12,
							border: "1px solid rgba(0,0,0,0.12)",
							borderRadius: 6,
							padding: "4px 8px",
							background: theme === "light" ? "#fff" : "#2b2b2b",
							color: theme === "light" ? "#333" : "#ddd",
							cursor: "pointer",
							boxShadow: "0 1px 2px rgba(0,0,0,0.06)",
							transition: "background 0.15s ease",
						}}
						aria-label={copied ? t("copied") || "Copied" : t("copy-code") || "Copy code"}
					>
						{copied ? <CheckIcon style={{ fontSize: 16 }} /> : <ContentCopyIcon style={{ fontSize: 16 }} />}
						<span>{copied ? t("copied") || "Copied" : t("copy-code") || "Copy code"}</span>
					</button>
				</div>
				<div style={{ margin: "8px 0" }} />
				<pre
					style={{
						margin: 0,
						position: "relative",
						display: "block",
						width: "100%",
						maxWidth: "100%",
						overflowX: "auto",
						whiteSpace: "pre-wrap",
						borderRadius: "10px",
						padding: 12,
						backgroundColor: theme === "light" ? "#f5f5f5" : "#1e1e1e",
					}}
					{...props}
				/>
			</div>
		);
	};

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
				<Box
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
							<Markdown
								components={{
									pre: (props: any) => <PreBlock {...props} />,
									code: (props: any) => {
										const { inline, style, children, ...rest } = props || {};
										return (
											<code
												style={{
													whiteSpace: inline ? "normal" : "inherit",
													wordBreak: inline ? "break-word" : "inherit",
													...(style || {}),
												}}
												{...rest}
											>
												{children}
											</code>
										);
									},
								}}
							>
								{message.answer}
							</Markdown>
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
				</Box>
			</Box>

			{sources.length > 0 && (
				<Box ml={9}>
					<Box display="flex" justifyContent="space-between" alignItems="center" mb={2}>
						<Typography variant="body2" color="text.secondary">
							{sources.length} sources
						</Typography>
						<Box
							component="button"
							onClick={() => {
								const newState = !showAllSources;
								setShowAllSources(newState);

								if (newState) {
									const allExpanded = new Set(sources.map((s) => s.url || ""));
									setExpandedChips(allExpanded);
								} else {
									setExpandedChips(new Set());
								}
							}}
							sx={{
								display: "flex",
								alignItems: "center",
								gap: 0.5,
								color: "#12518f",
								fontSize: "0.75rem",
								background: "none",
								border: "none",
								cursor: "pointer",
								"&:hover": { color: "#2782ea" },
							}}
						>
							<VisibilityIcon sx={{ fontSize: "0.75rem" }} />
							<Typography variant="caption">{showAllSources ? "Hide all" : "Show all"}</Typography>
						</Box>
					</Box>

					<Box display="flex" flexWrap="wrap" gap={1}>
						{visibleSources.map((source, index) => {
							const typeColors = getTypeColor(source.source || source.url || "");
							return (
								<Chip
									key={source.url || index}
									label={
										<Box
											display="flex"
											alignItems="center"
											gap={1}
											sx={{
												width: expandedChips.has(source.url || "") ? "auto" : "170px",
												overflow: "hidden",
												textOverflow: expandedChips.has(source.url || "") ? "unset" : "ellipsis",
												whiteSpace: "nowrap",
											}}
										>
											<Box
												sx={{
													width: 8,
													height: 8,
													flexShrink: 0,
													flexGrow: 0,
													borderRadius: "50%",
													backgroundColor: getStableColor(source.source || source.url, theme),
												}}
											/>
											<Typography variant="caption" fontWeight={500} overflow={"hidden"} textOverflow={"ellipsis"}>
												{source.title ||
													source.source ||
													(source.filename && source?.filename + source?.file_extension)}
											</Typography>
											{!source?.filename && !source?.file_extension && (
												<IconButton
													size="small"
													onClick={(e) => {
														e.stopPropagation();
														copySource(source);
													}}
													sx={{
														p: 0.25,
														ml: 0.5,
														"&:hover": { backgroundColor: "rgba(0,0,0,0.05)" },
													}}
												>
													{copiedSource === source.url ? (
														<CheckIcon sx={{ fontSize: "0.75rem" }} />
													) : (
														<ContentCopyIcon sx={{ fontSize: "0.75rem" }} />
													)}
												</IconButton>
											)}
											<IconButton
												size="small"
												onClick={(e) => {
													e.stopPropagation();
													toggleChipExpansion(source.url || "");
												}}
												sx={{
													p: 0.25,
													"&:hover": { backgroundColor: "rgba(0,0,0,0.05)" },
												}}
											>
												<OpenInFullIcon sx={{ fontSize: "0.75rem" }} />
											</IconButton>
										</Box>
									}
									onClick={() => !source?.filename && !source?.file_extension && window.open(source.url, "_blank")}
									sx={{
										backgroundColor: typeColors.backgroundColor,
										color: typeColors.color,
										border: `1px solid ${typeColors.borderColor}`,
										cursor: (!source?.filename && !source?.file_extension && "pointer") || "default",
										"&:hover": {
											backgroundColor: typeColors.backgroundColor,
											opacity: 0.8,
										},
									}}
									size="small"
								/>
							);
						})}

						{remainingSources > 0 && (
							<Chip
								label={
									<Box display="flex" alignItems="center" gap={0.5}>
										<OpenInFullIcon sx={{ fontSize: "0.75rem" }} />
										<Typography variant="caption">+{remainingSources}</Typography>
									</Box>
								}
								onClick={() => setShowSourcesModal(true)}
								sx={{
									backgroundColor: theme === "light" ? "#f5f5f5" : "#2d2d2d",
									color: theme === "light" ? "#616161" : "#b0b0b0",
									cursor: "pointer",
									"&:hover": {
										backgroundColor: theme === "light" ? "#e0e0e0" : "#404040",
									},
								}}
								size="small"
							/>
						)}
					</Box>
				</Box>
			)}
		</>
	);
}

function generateAccessibleColor(theme: Theme): string {
	const MAX_TRIES = 20;
	const bgColor = theme === "light" ? "#ffffff" : "#000000";

	for (let i = 0; i < MAX_TRIES; i++) {
		const hue = Math.floor(Math.random() * 360);
		const saturation = 80;
		const lightness = theme === "light" ? 20 + Math.random() * 30 : 60 + Math.random() * 30;

		const hex = hslToHex(hue, saturation, lightness);
		if (hasGoodContrast(hex, bgColor, 6.0)) {
			return hex;
		}
	}

	return theme === "light" ? "#111111" : "#eeeeee";
}

function hexToRgb(hex: string): { r: number; g: number; b: number } {
	hex = hex.replace(/^#/, "");

	if (hex.length === 3) {
		hex = hex
			.split("")
			.map((c) => c + c)
			.join("");
	}

	const num = parseInt(hex, 16);
	return {
		r: (num >> 16) & 255,
		g: (num >> 8) & 255,
		b: num & 255,
	};
}

function luminance(hex: string): number {
	const rgb = hexToRgb(hex);
	const srgb = [rgb.r, rgb.g, rgb.b].map((c) => {
		const c_ = c / 255;
		return c_ <= 0.03928 ? c_ / 12.92 : Math.pow((c_ + 0.055) / 1.055, 2.4);
	});
	return 0.2126 * srgb[0] + 0.7152 * srgb[1] + 0.0722 * srgb[2];
}

function contrastRatio(fg: string, bg: string): number {
	const L1 = luminance(fg);
	const L2 = luminance(bg);
	return (Math.max(L1, L2) + 0.05) / (Math.min(L1, L2) + 0.05);
}

function hasGoodContrast(fg: string, bg: string, minRatio = 6.0): boolean {
	return contrastRatio(fg, bg) >= minRatio;
}

function hslToHex(h: number, s: number, l: number): string {
	l /= 100;
	const a = (s * Math.min(l, 1 - l)) / 100;
	const f = (n: number) => {
		const k = (n + h / 30) % 12;
		const color = l - a * Math.max(Math.min(k - 3, 9 - k, 1), -1);
		return Math.round(255 * color)
			.toString(16)
			.padStart(2, "0");
	};
	return `#${f(0)}${f(8)}${f(4)}`;
}

const sourceColorMap = new Map<string, { light: string; dark: string }>();

function getStableColor(source: string | undefined, theme: Theme): string {
	if (!source) return theme === "light" ? "#666" : "#999";

	if (!sourceColorMap.has(source)) {
		const mappedColor = mappingColors(source, theme);
		if (mappedColor) {
			sourceColorMap.set(source, { light: mappedColor, dark: mappedColor });
		} else {
			sourceColorMap.set(source, {
				light: generateAccessibleColor("light"),
				dark: generateAccessibleColor("dark"),
			});
		}
	}

	return sourceColorMap.get(source)![theme];
}

function mappingColors(source: string | undefined, theme: Theme): string | undefined {
	switch (source?.toLowerCase()) {
		case "ansa.it":
			return theme === "light" ? "#1976d2" : "#42a5f5";
		case "eurosport.it":
			return theme === "light" ? "#d32f2f" : "#ef5350";
		case "ilpost.it":
			return theme === "light" ? "#388e3c" : "#66bb6a";
		case "corriere.it":
			return theme === "light" ? "#f57c00" : "#ffa726";
		case "techreport.com":
			return theme === "light" ? "#1976d2" : "#42a5f5";
		case "openai.com":
			return theme === "light" ? "#7b1fa2" : "#ab47bc";
		case "meta.ai":
			return theme === "light" ? "#1976d2" : "#42a5f5";
		case "kaggle.com":
			return theme === "light" ? "#f57c00" : "#ffa726";
		case "ethics.ai":
			return theme === "light" ? "#1976d2" : "#42a5f5";
		case "coursera.org":
			return theme === "light" ? "#d32f2f" : "#ef5350";
		case "spotify.com":
			return "#1dd15d";
		case "arxiv.org":
			return theme === "light" ? "#7b1fa2" : "#ab47bc";
		case "github.com":
			return theme === "light" ? "#24292e" : "#f0f6fc";
		case "stackoverflow.com":
			return theme === "light" ? "#f48024" : "#f69c3d";
		case "medium.com":
			return theme === "light" ? "#000000" : "#ffffff";
		case "youtube.com":
			return theme === "light" ? "#ff0000" : "#ff4444";
		case "x.com":
			return theme === "light" ? "#1da1f2" : "#1d9bf0";
		case "linkedin.com":
			return theme === "light" ? "#0077b5" : "#0a66c2";
		case "reddit.com":
			return theme === "light" ? "#ff4500" : "#ff6314";
		case "wikipedia.org":
			return theme === "light" ? "#000000" : "#ffffff";
		default:
			return undefined;
	}
}
