import { Box } from "@mui/material";
import BrokenImageOutlinedIcon from "@mui/icons-material/BrokenImageOutlined";
import DescriptionOutlinedIcon from "@mui/icons-material/DescriptionOutlined";
import React from "react";
import type { Components } from "react-markdown";
import { fileNameFromUrl, isImageUrl, useDocumentPreview } from "./DocumentPreview";
import { isSafeExternalUrl } from "./utils/safeExternalUrl";

const DOCUMENT_EXT = /\.(pdf|docx?|xlsx?|pptx?|csv|txt|md|json|xml|rtf|odt|ods|odp)$/i;

/** True when a link points to a previewable document or image file. */
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

/** Inline image with responsive sizing, click-to-zoom and a readable fallback. */
function MarkdownImage({ src, alt, title }: { src?: string; alt?: string; title?: string }) {
	const [error, setError] = React.useState(false);
	const { openPreview } = useDocumentPreview();

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
			{alt || "Immagine non disponibile"}
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

/** Renders a document/image link as an inline chip that opens the preview drawer. */
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

/**
 * Markdown component overrides that add rich rendering of generative answers:
 * GFM tables (styled, horizontally scrollable), inline images with a graceful
 * fallback, and document links surfaced as previewable chips.
 *
 * Meant to be spread into an existing `<Markdown components={...}>` alongside
 * `remarkPlugins={[remarkGfm]}`, so other overrides (e.g. code blocks) are kept.
 * Raw HTML is intentionally not enabled, keeping rendering safe against
 * untrusted answer content.
 */
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
