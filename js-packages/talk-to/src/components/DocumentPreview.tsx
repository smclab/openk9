import { Box, CircularProgress, IconButton, Tooltip, Typography } from "@mui/material";
import CheckIcon from "@mui/icons-material/Check";
import CloseIcon from "@mui/icons-material/Close";
import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import DownloadIcon from "@mui/icons-material/Download";
import OpenInNewIcon from "@mui/icons-material/OpenInNew";
import React from "react";
import { useTranslation } from "react-i18next";
import Markdown from "react-markdown";
import remarkGfm from "remark-gfm";

export type PreviewTarget = {
	title?: string;
	url?: string; // existing file URL (RAG source / image)
	content?: string; // inline generated document
	filename?: string;
	artifactId?: string; // artifact id (message id)
	streaming?: boolean; // still generating
};

type DocumentPreviewContextValue = {
	openPreview: (target: PreviewTarget) => void; // open right panel (manual)
	showArtifactLive: (target: PreviewTarget) => void; // auto open/update while streaming
	closePreview: () => void;
	activeArtifactId: string | null; // artifact currently shown, if any
};

const DocumentPreviewContext = React.createContext<DocumentPreviewContextValue | null>(null);

const IMAGE_EXT = /\.(png|jpe?g|gif|webp|svg|bmp|avif|ico)$/i;

export function fileNameFromUrl(url: string, fallback = "document"): string {
	try {
		const parsed = new URL(url, window.location.href);
		const last = parsed.pathname.split("/").filter(Boolean).pop();
		return last ? decodeURIComponent(last) : parsed.hostname || fallback;
	} catch {
		const clean = (url || "").split(/[?#]/)[0];
		const last = clean.split("/").filter(Boolean).pop();
		return last || fallback;
	}
}

export function isImageUrl(url?: string): boolean {
	return !!url && IMAGE_EXT.test(url.split(/[?#]/)[0]);
}

// client-side download of text content
export function downloadTextFile(filename: string, content: string) {
	const ext = filename.split(".").pop()?.toLowerCase();
	const mime =
		ext === "csv"
			? "text/csv"
			: ext === "json"
			? "application/json"
			: ext === "html" || ext === "htm"
			? "text/html"
			: "text/markdown";
	const blob = new Blob([content], { type: `${mime};charset=utf-8` });
	const url = URL.createObjectURL(blob);
	const a = document.createElement("a");
	a.href = url;
	a.download = filename;
	document.body.appendChild(a);
	a.click();
	document.body.removeChild(a);
	URL.revokeObjectURL(url);
}

// shared preview panel; no-op without a provider
export function useDocumentPreview(): DocumentPreviewContextValue {
	const ctx = React.useContext(DocumentPreviewContext);
	if (!ctx) {
		return {
			openPreview: (target) => {
				if (target.url) window.open(target.url, "_blank", "noopener,noreferrer");
				else if (target.content != null) downloadTextFile(target.filename || "documento.md", target.content);
			},
			showArtifactLive: () => {},
			closePreview: () => {},
			activeArtifactId: null,
		};
	}
	return ctx;
}

// chat + side preview panel (split layout, not an overlay)
export function DocumentPreviewProvider({ children }: { children: React.ReactNode }) {
	const [target, setTarget] = React.useState<PreviewTarget | null>(null);
	const [imageError, setImageError] = React.useState(false);
	const [copied, setCopied] = React.useState(false);
	const dismissedRef = React.useRef<string | null>(null);
	const identityRef = React.useRef<string | null>(null);

	const applyTarget = React.useCallback((next: PreviewTarget) => {
		const identity = next.artifactId || next.url || null;
		if (identity !== identityRef.current) {
			identityRef.current = identity;
			setImageError(false);
			setCopied(false);
		}
		setTarget(next);
	}, []);

	const openPreview = React.useCallback(
		(next: PreviewTarget) => {
			if (next.artifactId) dismissedRef.current = null; // manual open clears any dismissal
			applyTarget(next);
		},
		[applyTarget],
	);

	const showArtifactLive = React.useCallback(
		(next: PreviewTarget) => {
			if (next.artifactId && dismissedRef.current === next.artifactId) return; // user closed it
			applyTarget(next);
		},
		[applyTarget],
	);

	const closePreview = React.useCallback(() => {
		setTarget((prev) => {
			if (prev?.artifactId) dismissedRef.current = prev.artifactId;
			identityRef.current = null;
			return null;
		});
	}, []);

	const value = React.useMemo(
		() => ({ openPreview, showArtifactLive, closePreview, activeArtifactId: target?.artifactId ?? null }),
		[openPreview, showArtifactLive, closePreview, target?.artifactId],
	);

	return (
		<DocumentPreviewContext.Provider value={value}>
			<Box sx={{ display: "flex", width: "100%", height: "100vh", overflow: "hidden" }}>
				<Box sx={{ flex: 1, minWidth: 0, height: "100%", overflow: "hidden" }}>{children}</Box>
				{target && (
					<PreviewPanel
						target={target}
						copied={copied}
						imageError={imageError}
						onImageError={() => setImageError(true)}
						onCopy={async () => {
							try {
								await navigator.clipboard.writeText(target.content != null ? target.content : target.url || "");
								setCopied(true);
								setTimeout(() => setCopied(false), 2000);
							} catch (err) {
								console.error("Errore durante la copia:", err);
							}
						}}
						onClose={closePreview}
					/>
				)}
			</Box>
		</DocumentPreviewContext.Provider>
	);
}

function PreviewPanel({
	target,
	copied,
	imageError,
	onImageError,
	onCopy,
	onClose,
}: {
	target: PreviewTarget;
	copied: boolean;
	imageError: boolean;
	onImageError: () => void;
	onCopy: () => void;
	onClose: () => void;
}) {
	const { t } = useTranslation();
	const isContent = target.content != null;
	const isStreaming = !!target.streaming;
	const name = target.filename || target.title || (target.url ? fileNameFromUrl(target.url) : "document");
	const showImage = !isContent && isImageUrl(target.url);
	const bodyRef = React.useRef<HTMLDivElement | null>(null);

	// keep scrolled to bottom while streaming
	React.useEffect(() => {
		if (isContent && isStreaming && bodyRef.current) {
			bodyRef.current.scrollTop = bodyRef.current.scrollHeight;
		}
	}, [target.content, isContent, isStreaming]);

	return (
		<Box
			sx={{
				width: { xs: "100%", sm: "55%", md: "45%", lg: "40%" },
				maxWidth: "820px",
				height: "100vh",
				flexShrink: 0,
				display: "flex",
				flexDirection: "column",
				borderLeft: "1px solid rgba(0, 0, 0, 0.12)",
				backgroundColor: isContent ? "#ffffff" : "#f5f5f5",
			}}
		>
			<Box
				display="flex"
				alignItems="center"
				gap={0.5}
				px={2}
				py={1.5}
				sx={{ borderBottom: "1px solid rgba(0, 0, 0, 0.12)", flexShrink: 0 }}
			>
				{isStreaming && <CircularProgress size={16} sx={{ mr: 0.5 }} />}
				<Typography variant="subtitle1" fontWeight={600} noWrap title={name} sx={{ flex: 1, minWidth: 0 }}>
					{name}
				</Typography>
				<Tooltip title={copied ? t("preview-copied") : isContent ? t("preview-copy-content") : t("preview-copy-link")}>
					<IconButton size="small" onClick={onCopy} aria-label={t("preview-copy") ?? ""}>
						{copied ? <CheckIcon fontSize="small" /> : <ContentCopyIcon fontSize="small" />}
					</IconButton>
				</Tooltip>
				<Tooltip title={isStreaming ? t("preview-generating") : t("preview-download")}>
					<span>
						{isContent ? (
							<IconButton
								size="small"
								disabled={isStreaming}
								onClick={() => downloadTextFile(name, target.content || "")}
								aria-label={t("preview-download") ?? ""}
							>
								<DownloadIcon fontSize="small" />
							</IconButton>
						) : (
							<IconButton
								size="small"
								component="a"
								href={target.url}
								download
								target="_blank"
								rel="noopener noreferrer"
								aria-label={t("preview-download") ?? ""}
							>
								<DownloadIcon fontSize="small" />
							</IconButton>
						)}
					</span>
				</Tooltip>
				{!isContent && (
					<Tooltip title={t("preview-open-new-tab")}>
						<IconButton
							size="small"
							component="a"
							href={target.url}
							target="_blank"
							rel="noopener noreferrer"
							aria-label={t("preview-open-new-tab") ?? ""}
						>
							<OpenInNewIcon fontSize="small" />
						</IconButton>
					</Tooltip>
				)}
				<Tooltip title={t("preview-close")}>
					<IconButton size="small" onClick={onClose} aria-label={t("preview-close-aria") ?? ""}>
						<CloseIcon fontSize="small" />
					</IconButton>
				</Tooltip>
			</Box>

			<Box ref={bodyRef} sx={{ flex: 1, minHeight: 0, overflow: "auto" }}>
				{isContent ? (
					<Box
						sx={{
							p: 2,
							fontSize: "0.9rem",
							"& table": { borderCollapse: "collapse", width: "100%", my: 1 },
							"& th, & td": { border: "1px solid rgba(0,0,0,0.15)", p: 1, textAlign: "left" },
							"& th": { backgroundColor: "rgba(0,0,0,0.04)" },
							"& img": { maxWidth: "100%", height: "auto" },
							"& pre": { overflowX: "auto", background: "#f5f5f5", p: 1.5, borderRadius: "8px" },
							"& code": { wordBreak: "break-word" },
						}}
					>
						<Markdown remarkPlugins={[remarkGfm]}>{target.content || ""}</Markdown>
						{isStreaming && (
							<Typography variant="caption" color="text.secondary" sx={{ display: "block", mt: 1 }}>
								▍ {t("preview-generating")}
							</Typography>
						)}
					</Box>
				) : showImage && !imageError ? (
					<Box height="100%" display="flex" alignItems="center" justifyContent="center" p={2}>
						<img
							src={target.url}
							alt={name}
							onError={onImageError}
							style={{ maxWidth: "100%", maxHeight: "100%", objectFit: "contain" }}
						/>
					</Box>
				) : imageError ? (
					<FallbackMessage />
				) : (
					<iframe title={name} src={target.url} style={{ width: "100%", height: "100%", border: "none" }} />
				)}
			</Box>

			{!isContent && !showImage && (
				<Box px={2} py={1} sx={{ borderTop: "1px solid rgba(0, 0, 0, 0.12)", flexShrink: 0 }}>
					<Typography variant="caption" color="text.secondary">
						{t("preview-fallback-caption")}
					</Typography>
				</Box>
			)}
		</Box>
	);
}

function FallbackMessage() {
	const { t } = useTranslation();
	return (
		<Box height="100%" display="flex" alignItems="center" justifyContent="center" p={3}>
			<Typography variant="body2" color="text.secondary" textAlign="center">
				{t("preview-fallback-message")}
			</Typography>
		</Box>
	);
}
