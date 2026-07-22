import { Box, Button, Typography } from "@mui/material";
import BrokenImageOutlinedIcon from "@mui/icons-material/BrokenImageOutlined";
import DescriptionOutlinedIcon from "@mui/icons-material/DescriptionOutlined";
import DownloadIcon from "@mui/icons-material/Download";
import VisibilityIcon from "@mui/icons-material/Visibility";
import React from "react";
import { useTranslation } from "react-i18next";
import type { Components } from "react-markdown";
import { downloadTextFile, fileNameFromUrl, isImageUrl, useDocumentPreview } from "./DocumentPreview";
import { isSafeExternalUrl } from "./utils/safeExternalUrl";

const DOCUMENT_EXT = /\.(pdf|docx?|xlsx?|pptx?|csv|txt|md|json|xml|rtf|odt|ods|odp)$/i;
const DOC_FENCE_EXT = /\.(md|markdown|txt|csv|json|xml|ya?ml|html?)$/i;

function isPreviewableUrl(url?: string): boolean {
	if (!url) return false;
	const clean = url.split(/[?#]/)[0];
	return DOCUMENT_EXT.test(clean) || isImageUrl(clean);
}

function childrenToText(children: React.ReactNode): string {
	return React.Children.toArray(children)
		.filter((child): child is string => typeof child === "string")
		.join("")
		.trim();
}

// inline image: zoom on click, fallback on error
function MarkdownImage({ src, alt, title }: { src?: string; alt?: string; title?: string }) {
	const [error, setError] = React.useState(false);
	const { openPreview } = useDocumentPreview();
	const { t } = useTranslation();

	const fallback = (
		<Box
			component="span"
			sx={{
				display: "inline-flex",
				alignItems: "center",
				gap: 0.5,
				m: 0.25,
				px: 1,
				py: 0.25,
				borderRadius: "8px",
				border: "1px dashed rgba(0, 0, 0, 0.25)",
				color: "text.secondary",
				fontSize: "0.85rem",
				verticalAlign: "middle",
			}}
		>
			<BrokenImageOutlinedIcon sx={{ fontSize: "1rem" }} />
			{alt || t("image-not-available")}
		</Box>
	);

	if (!src || !isSafeExternalUrl(src) || error) {
		return fallback;
	}

	return (
		<img
			src={src}
			alt={alt || ""}
			title={title}
			loading="lazy"
			onError={() => setError(true)}
			onClick={() => openPreview({ url: src, title: alt || title })}
			style={{
				display: "inline-block",
				maxWidth: "100%",
				height: "auto",
				borderRadius: "8px",
				margin: "8px 0",
				cursor: "zoom-in",
				verticalAlign: "middle",
			}}
		/>
	);
}

// doc/image link as a preview chip
function MarkdownLink({ href, children }: { href?: string; children?: React.ReactNode }) {
	const { openPreview } = useDocumentPreview();

	if (!href || !isSafeExternalUrl(href)) return <>{children}</>;

	if (isPreviewableUrl(href)) {
		const label = childrenToText(children) || fileNameFromUrl(href);
		const open = () => openPreview({ url: href, title: childrenToText(children) || undefined });
		return (
			<Box
				component="span"
				role="button"
				tabIndex={0}
				onClick={open}
				onKeyDown={(event) => {
					if (event.key === "Enter" || event.key === " ") {
						event.preventDefault();
						open();
					}
				}}
				title={label}
				sx={{
					display: "inline-flex",
					alignItems: "center",
					gap: 0.5,
					maxWidth: "100%",
					m: 0.25,
					px: 1,
					py: 0.25,
					borderRadius: "8px",
					border: "1px solid rgba(0, 0, 0, 0.15)",
					backgroundColor: "rgba(0, 0, 0, 0.03)",
					color: "#12518f",
					fontSize: "0.85rem",
					cursor: "pointer",
					verticalAlign: "middle",
					"&:hover": { backgroundColor: "rgba(0, 0, 0, 0.07)" },
				}}
			>
				<DescriptionOutlinedIcon sx={{ fontSize: "1rem", flexShrink: 0 }} />
				<Box component="span" sx={{ overflow: "hidden", textOverflow: "ellipsis", whiteSpace: "nowrap" }}>
					{label}
				</Box>
			</Box>
		);
	}

	return (
		<a href={href} target="_blank" rel="noopener noreferrer" style={{ color: "#12518f" }}>
			{children}
		</a>
	);
}

// markdown overrides: tables, images, links (code blocks handled by the caller)
export const richMarkdownComponents: Components = {
	img: ({ node, ...props }) => (
		<MarkdownImage src={props.src as string | undefined} alt={props.alt} title={props.title} />
	),
	a: ({ node, href, children }) => <MarkdownLink href={href}>{children}</MarkdownLink>,
	table: ({ node, children }) => (
		<Box sx={{ overflowX: "auto", my: 1, maxWidth: "100%" }}>
			<Box component="table" sx={{ borderCollapse: "collapse", width: "100%", fontSize: "0.9rem" }}>
				{children}
			</Box>
		</Box>
	),
	th: ({ node, children, style }) => (
		<Box
			component="th"
			style={style}
			sx={{
				border: "1px solid rgba(0, 0, 0, 0.15)",
				p: 1,
				textAlign: (style?.textAlign as any) || "left",
				backgroundColor: "rgba(0, 0, 0, 0.04)",
				fontWeight: 600,
			}}
		>
			{children}
		</Box>
	),
	td: ({ node, children, style }) => (
		<Box
			component="td"
			style={style}
			sx={{
				border: "1px solid rgba(0, 0, 0, 0.15)",
				p: 1,
				textAlign: (style?.textAlign as any) || "left",
				verticalAlign: "top",
			}}
		>
			{children}
		</Box>
	),
};

// whole-answer fenced doc -> artifact (```md or ```name.ext)
export function extractDocumentFromAnswer(
	answer: string,
): { filename: string; content: string; closed: boolean } | null {
	if (!answer) return null;

	// only when the fence is the whole answer (skip inline ```md examples)
	const trimmed = answer.replace(/^\s+/, "");
	if (!trimmed.startsWith("```")) return null;

	const firstNl = trimmed.indexOf("\n");
	if (firstNl < 0) return null; // opening line not complete yet
	const info = trimmed.slice(3, firstNl).trim();

	let explicit: string | null = null;
	if (DOC_FENCE_EXT.test(info)) explicit = sanitizeFilename(info);
	else if (!/^(md|markdown)$/i.test(info)) return null; // other language -> not a document

	// close fence = trailing ```; inner code fences stay in content
	let content = trimmed.slice(firstNl + 1);
	let closed = false;
	const closeMatch = content.match(/\n```[ \t]*\s*$/);
	if (closeMatch) {
		content = content.slice(0, closeMatch.index);
		closed = true;
	} else if (/^```[ \t]*$/.test(content.trim())) {
		content = "";
		closed = true;
	}
	if (!content.trim() && !closed) return null;

	const filename = explicit || filenameFromContent(content, closed);
	return { filename, content, closed };
}

// filename-safe slug
function slugify(text: string): string {
	return text
		.normalize("NFKD")
		.replace(/[̀-ͯ]/g, "")
		.toLowerCase()
		.replace(/[^\p{L}\p{N}]+/gu, "-")
		.replace(/^-+|-+$/g, "")
		.slice(0, 60)
		.replace(/-+$/g, "");
}

// basename only, slugified stem + safe extension
function sanitizeFilename(name: string): string {
	const base = name.split(/[\\/]/).pop() || "";
	const dot = base.lastIndexOf(".");
	const stem = dot > 0 ? base.slice(0, dot) : base;
	const ext = (dot > 0 ? base.slice(dot + 1) : "md").toLowerCase().replace(/[^a-z0-9]/g, "");
	const safeStem = slugify(stem) || "document";
	const safeExt = /^(md|markdown)$/.test(ext) ? "md" : ext || "md";
	return `${safeStem}.${safeExt}`;
}

// name from the doc title/first line; stable while streaming
function filenameFromContent(content: string, closed: boolean): string {
	let match = content.match(/^\s{0,3}#{1,6}\s+(.+?)\s*\r?\n/m);
	let title = match ? match[1] : "";
	if (!title && closed) {
		match = content.match(/^\s{0,3}#{1,6}\s+(.+?)\s*$/m);
		title = match
			? match[1]
			: content
					.split("\n")
					.map((l) => l.trim())
					.find(Boolean) || "";
	}
	if (!title) return "document.md";
	const slug = slugify(title.replace(/[#*`_>[\]()]/g, "").trim());
	return slug ? `${slug}.md` : "document.md";
}

// chat card for a generated document
export function ArtifactCard({
	filename,
	content,
	messageId,
	streaming,
}: {
	filename: string;
	content: string;
	messageId: string;
	streaming: boolean;
}) {
	const { openPreview, showArtifactLive, activeArtifactId } = useDocumentPreview();
	const { t } = useTranslation();

	React.useEffect(() => {
		if (streaming) {
			// auto-open + live update while streaming
			showArtifactLive({ artifactId: messageId, filename, content, streaming: true });
		} else if (activeArtifactId === messageId) {
			// panel already open -> push final state
			showArtifactLive({ artifactId: messageId, filename, content, streaming: false });
		}
		// eslint-disable-next-line react-hooks/exhaustive-deps
	}, [content, streaming]);

	const lines = content ? content.split("\n").length : 0;

	return (
		<Box
			sx={{
				my: 1,
				p: 1.5,
				display: "flex",
				alignItems: "center",
				gap: 1.5,
				maxWidth: "100%",
				border: "1px solid rgba(0, 0, 0, 0.15)",
				borderRadius: "10px",
				backgroundColor: "rgba(0, 0, 0, 0.02)",
			}}
		>
			<DescriptionOutlinedIcon sx={{ color: "#12518f", flexShrink: 0 }} />
			<Box sx={{ flex: 1, minWidth: 0 }}>
				<Typography variant="body2" fontWeight={600} noWrap title={filename}>
					{filename}
				</Typography>
				<Typography variant="caption" color="text.secondary">
					{streaming ? t("artifact-generating") : t("artifact-file-ready", { lines })}
				</Typography>
			</Box>
			<Button
				size="small"
				variant="contained"
				startIcon={<VisibilityIcon />}
				onClick={() => openPreview({ artifactId: messageId, filename, content, streaming })}
				sx={{
					flexShrink: 0,
					borderRadius: "10px",
					boxShadow: "none",
					textTransform: "none",
					backgroundColor: "#12518f",
					color: "#fff",
					"&:hover": { boxShadow: "none", backgroundColor: "#0d3f6f", color: "#fff" },
				}}
			>
				{t("artifact-preview")}
			</Button>
			<Button
				size="small"
				variant="outlined"
				startIcon={<DownloadIcon />}
				disabled={streaming}
				onClick={() => downloadTextFile(filename, content)}
				sx={{
					flexShrink: 0,
					borderRadius: "10px",
					textTransform: "none",
					color: "text.primary",
					borderColor: "rgba(0, 0, 0, 0.23)",
					"&:hover": { color: "#fff", borderColor: "#12518f", backgroundColor: "#12518f" },
					"&.Mui-disabled": { color: "rgba(0, 0, 0, 0.45)", borderColor: "rgba(0, 0, 0, 0.18)" },
				}}
			>
				{t("artifact-download")}
			</Button>
		</Box>
	);
}
