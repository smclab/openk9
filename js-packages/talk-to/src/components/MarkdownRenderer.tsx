import { Box, Button, Typography } from "@mui/material";
import BrokenImageOutlinedIcon from "@mui/icons-material/BrokenImageOutlined";
import DescriptionOutlinedIcon from "@mui/icons-material/DescriptionOutlined";
import DownloadIcon from "@mui/icons-material/Download";
import VisibilityIcon from "@mui/icons-material/Visibility";
import React from "react";
import { useTranslation } from "react-i18next";
import type { Components } from "react-markdown";
import { downloadTextFile, fileNameFromUrl, isImageUrl, useDocumentPreview } from "./DocumentPreview";
import { isSafeExternalUrl } from "../../../shared/safe-external-url/safeExternalUrl";
import {
	type CitedSource,
	claimPanel,
	releasePanel,
	sourcesByOrdinal,
} from "../../../shared/inline-citations/inlineCitations";

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
/** The site a source comes from, for the panel; the chip shows its title. */
function hostOf(url?: string): string | undefined {
	if (!url) return undefined;
	try {
		return new URL(url).hostname.replace(/^www\./, "");
	} catch {
		return undefined;
	}
}

/** A chip has to stay inside a line of prose, so the title is cut short. */
function shortTitle(source: CitedSource | undefined, ordinal: number): string {
	const title = source?.title ?? hostOf(source?.url);
	if (!title) return `[${ordinal}]`;
	return title.length > 28 ? `${title.slice(0, 27)}…` : title;
}

/**
 * The citation of one sentence, as a chip naming its source. Several sources
 * on the same sentence become one chip with a count, the way a reader thinks
 * of them: one citation that happens to rest on more than one document.
 *
 * On a corpus where everything comes from one site the domain would say
 * nothing, so the chip carries the document title and leaves the domain to
 * the panel.
 *
 * Sources nothing resolves keep their bare number. The RAG module never
 * suppresses a marker precisely so that it degrades into something a reader
 * can still make sense of.
 */
function CitationChip({
	ordinals,
	byOrdinal,
	quoteOf,
}: {
	ordinals: number[];
	byOrdinal: Map<number, CitedSource>;
	quoteOf: (ordinal: number) => string | undefined;
}) {
	const [open, setOpen] = React.useState(false);
	// Closing on the first mouseleave makes the panel vanish while the
	// pointer travels towards it: a diagonal path leaves the chip before it
	// reaches the link. A short grace period keeps it reachable.
	const closing = React.useRef<ReturnType<typeof setTimeout> | undefined>(undefined);
	const closeNow = React.useRef(() => setOpen(false));
	const show = () => {
		if (closing.current) clearTimeout(closing.current);
		// only one panel at a time: the one being left is still inside its
		// grace period, so moving quickly between chips would show both
		claimPanel(closeNow.current);
		setOpen(true);
	};
	const hide = () => {
		if (closing.current) clearTimeout(closing.current);
		closing.current = setTimeout(() => {
			releasePanel(closeNow.current);
			setOpen(false);
		}, 200);
	};
	React.useEffect(() => () => {
		if (closing.current) clearTimeout(closing.current);
		releasePanel(closeNow.current);
	}, []);

	const first = byOrdinal.get(ordinals[0]);
	const extra = ordinals.length - 1;
	const label = shortTitle(first, ordinals[0]);

	return (
		<Box
			component="span"
			sx={{ position: "relative", display: "inline-block" }}
			onMouseEnter={show}
			onMouseLeave={hide}
		>
			<Box
				component="span"
				tabIndex={0}
				onFocus={show}
				onBlur={hide}
				aria-label={label}
				sx={{
					display: "inline-flex",
					alignItems: "baseline",
					gap: "4px",
					ml: "3px",
					px: "6px",
					py: "1px",
					borderRadius: "999px",
					fontSize: "0.72em",
					lineHeight: 1.6,
					cursor: "default",
					color: "text.secondary",
					backgroundColor: "rgba(0, 0, 0, 0.05)",
					"&:hover": { backgroundColor: "rgba(0, 0, 0, 0.10)" },
				}}
			>
				{label}
				{extra > 0 && <Box component="span" sx={{ opacity: 0.7 }}>+{extra}</Box>}
			</Box>

			{open && (
				<Box
					role="tooltip"
					onMouseEnter={show}
					onMouseLeave={hide}
					sx={{
						position: "absolute",
						zIndex: 10,
						top: "100%",
						left: 0,
						width: 320,
						maxWidth: "80vw",
						// padding and not margin: the gap between the chip and the
						// panel has to belong to the panel, or the pointer crossing
						// it leaves both and the panel closes under it
						p: 1,
						borderRadius: "12px",
						backgroundColor: "background.paper",
						boxShadow: 3,
						textAlign: "left",
					}}
				>
					{ordinals.map((ordinal) => {
						const source = byOrdinal.get(ordinal);
						const url = source?.url;
						const quote = quoteOf(ordinal);
						return (
							<Box
								key={ordinal}
								sx={{
									position: "relative",
									p: 0.75,
									borderRadius: "8px",
									"&:hover": { backgroundColor: "rgba(0, 0, 0, 0.04)" },
								}}
							>
								<Typography
									sx={{
										fontSize: "0.75rem",
										fontWeight: 600,
										color: "text.primary",
										opacity: 0.75,
										letterSpacing: "0.02em",
									}}
								>
									{hostOf(url) ?? `[${ordinal}]`}
								</Typography>
								{url && isSafeExternalUrl(url) ? (
									<Box
										component="a"
										href={url}
										target="_blank"
										rel="noreferrer"
										sx={{
											display: "block",
											fontSize: "0.9rem",
											fontWeight: 600,
											// not primary.main: on a light palette it comes out
											// almost white against the panel
											color: "text.primary",
											textDecoration: "underline",
											textUnderlineOffset: "2px",
											"&:hover": { opacity: 0.75 },
											// the whole row opens the source, while the markup
											// stays one anchor: middle click, "copy link" and
											// the keyboard keep working, which an onClick on the
											// container would throw away. The quote below sits
											// above it, so it stays selectable.
											"&::after": {
												content: '""',
												position: "absolute",
												inset: 0,
												borderRadius: "8px",
											},
										}}
									>
										{source?.title ?? url}
									</Box>
								) : (
									<Typography sx={{ fontSize: "0.9rem", fontWeight: 600 }}>
										{source?.title ?? `[${ordinal}]`}
									</Typography>
								)}
								{quote && (
									<Typography
										variant="body2"
										sx={{
											mt: 0.25,
											color: "text.secondary",
											fontStyle: "italic",
											// above the stretched link, so the passage copied
											// from the source can still be selected
											position: "relative",
											zIndex: 1,
										}}
									>
										“{quote}”
									</Typography>
								)}
							</Box>
						);
					})}
				</Box>
			)}
		</Box>
	);
}

/**
 * The components for an answer that carries inline citation markers. The
 * sources come from the `SOURCES` event, which arrives before the first
 * token, so the chips appear as the sentence is written; the quotes come
 * from the `DOCUMENT` events, so they fill the panel once the answer ends.
 */
export function markdownComponentsWithCitations(
	sources: CitedSource[] | undefined,
	quotes?: Map<number, string>,
): Components {
	const byOrdinal = sourcesByOrdinal(sources ?? []);
	const quoteOf = (ordinal: number) => quotes?.get(ordinal);
	return {
		...richMarkdownComponents,
		span: ({ node, children, ...props }) => {
			const raw = (props as Record<string, unknown>)["data-citation-ordinals"];
			if (typeof raw !== "string") return <span {...props}>{children}</span>;
			const ordinals = raw.split(",").map(Number).filter(Number.isInteger);
			if (ordinals.length === 0) return <span {...props}>{children}</span>;
			return (
				<CitationChip ordinals={ordinals} byOrdinal={byOrdinal} quoteOf={quoteOf} />
			);
		},
	};
}

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
				textAlign: style?.textAlign || "left",
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
				textAlign: style?.textAlign || "left",
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
