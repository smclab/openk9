import { Box, Typography } from "@mui/material";
import React from "react";
import type { Components } from "react-markdown";
import { type CitedSource, claimPanel, releasePanel, sourcesByOrdinal } from "./inlineCitations";

const ALLOWED_PROTOCOLS = ["http:", "https:", "mailto:"];

/**
 * Only the protocols a citation link may use. A source url comes from the
 * index, so it is not ours to trust: a "javascript:" one rendered as an
 * anchor would run on click.
 */
function isSafeExternalUrl(url: string | undefined | null): boolean {
	if (!url) return false;
	try {
		return ALLOWED_PROTOCOLS.includes(new URL(url, window.location.href).protocol);
	} catch {
		return false;
	}
}

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
	React.useEffect(
		() => () => {
			if (closing.current) clearTimeout(closing.current);
			releasePanel(closeNow.current);
		},
		[],
	);

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
				{extra > 0 && (
					<Box component="span" sx={{ opacity: 0.7 }}>
						+{extra}
					</Box>
				)}
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
		span: ({ node, children, ...props }) => {
			const raw = (props as Record<string, unknown>)["data-citation-ordinals"];
			if (typeof raw !== "string") return <span {...props}>{children}</span>;
			const ordinals = raw.split(",").map(Number).filter(Number.isInteger);
			if (ordinals.length === 0) return <span {...props}>{children}</span>;
			return <CitationChip ordinals={ordinals} byOrdinal={byOrdinal} quoteOf={quoteOf} />;
		},
	};
}
