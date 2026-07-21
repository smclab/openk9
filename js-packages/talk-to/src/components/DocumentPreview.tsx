import { Box, Drawer, IconButton, Tooltip, Typography } from "@mui/material";
import CheckIcon from "@mui/icons-material/Check";
import CloseIcon from "@mui/icons-material/Close";
import ContentCopyIcon from "@mui/icons-material/ContentCopy";
import DownloadIcon from "@mui/icons-material/Download";
import OpenInNewIcon from "@mui/icons-material/OpenInNew";
import React from "react";

export type PreviewTarget = {
	url: string;
	title?: string;
};

type DocumentPreviewContextValue = {
	openPreview: (target: PreviewTarget) => void;
	closePreview: () => void;
};

const DocumentPreviewContext = React.createContext<DocumentPreviewContextValue | null>(null);

const IMAGE_EXT = /\.(png|jpe?g|gif|webp|svg|bmp|avif|ico)$/i;

/** Derive a readable file name from a URL, falling back to the host or a default label. */
export function fileNameFromUrl(url: string, fallback = "documento"): string {
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

/** True when the URL points to a rendered image (by extension). */
export function isImageUrl(url?: string): boolean {
	return !!url && IMAGE_EXT.test(url.split(/[?#]/)[0]);
}

/**
 * Access the shared document-preview drawer. When no provider is mounted the
 * hook degrades gracefully to opening the target in a new browser tab, so the
 * consuming components never break.
 */
export function useDocumentPreview(): DocumentPreviewContextValue {
	const ctx = React.useContext(DocumentPreviewContext);
	if (!ctx) {
		return {
			openPreview: (target) => window.open(target.url, "_blank", "noopener,noreferrer"),
			closePreview: () => {},
		};
	}
	return ctx;
}

/**
 * Provides a single right-side drawer that previews a document or image URL,
 * mirroring the "artifact" experience of modern chat assistants: an inline
 * preview plus copy / download / open-in-new-tab actions. External pages that
 * forbid framing still expose every action in the header, so the user is never
 * stuck on a blank preview.
 */
export function DocumentPreviewProvider({ children }: { children: React.ReactNode }) {
	const [target, setTarget] = React.useState<PreviewTarget | null>(null);
	const [imageError, setImageError] = React.useState(false);
	const [copied, setCopied] = React.useState(false);

	const openPreview = React.useCallback((next: PreviewTarget) => {
		setImageError(false);
		setCopied(false);
		setTarget(next);
	}, []);

	const closePreview = React.useCallback(() => setTarget(null), []);

	const value = React.useMemo(() => ({ openPreview, closePreview }), [openPreview, closePreview]);

	const copyLink = async () => {
		if (!target) return;
		try {
			await navigator.clipboard.writeText(target.url);
			setCopied(true);
			setTimeout(() => setCopied(false), 2000);
		} catch (err) {
			console.error("Errore durante la copia:", err);
		}
	};

	const name = target ? target.title || fileNameFromUrl(target.url) : "";
	const showImage = target ? isImageUrl(target.url) : false;

	return (
		<DocumentPreviewContext.Provider value={value}>
			{children}
			<Drawer
				anchor="right"
				open={!!target}
				onClose={closePreview}
				PaperProps={{
					sx: {
						width: { xs: "100%", sm: "60%", md: "45%", lg: "38%" },
						maxWidth: "760px",
					},
				}}
			>
				{target && (
					<Box display="flex" flexDirection="column" height="100%">
						<Box
							display="flex"
							alignItems="center"
							gap={0.5}
							px={2}
							py={1.5}
							sx={{ borderBottom: "1px solid rgba(0, 0, 0, 0.12)" }}
						>
							<Typography variant="subtitle1" fontWeight={600} noWrap title={name} sx={{ flex: 1, minWidth: 0 }}>
								{name}
							</Typography>
							<Tooltip title={copied ? "Copiato" : "Copia link"}>
								<IconButton size="small" onClick={copyLink} aria-label="Copia link">
									{copied ? <CheckIcon fontSize="small" /> : <ContentCopyIcon fontSize="small" />}
								</IconButton>
							</Tooltip>
							<Tooltip title="Scarica">
								<IconButton
									size="small"
									component="a"
									href={target.url}
									download
									target="_blank"
									rel="noopener noreferrer"
									aria-label="Scarica"
								>
									<DownloadIcon fontSize="small" />
								</IconButton>
							</Tooltip>
							<Tooltip title="Apri in una nuova scheda">
								<IconButton
									size="small"
									component="a"
									href={target.url}
									target="_blank"
									rel="noopener noreferrer"
									aria-label="Apri in una nuova scheda"
								>
									<OpenInNewIcon fontSize="small" />
								</IconButton>
							</Tooltip>
							<Tooltip title="Chiudi">
								<IconButton size="small" onClick={closePreview} aria-label="Chiudi anteprima">
									<CloseIcon fontSize="small" />
								</IconButton>
							</Tooltip>
						</Box>

						<Box flex={1} minHeight={0} sx={{ backgroundColor: "#f5f5f5" }}>
							{showImage && !imageError ? (
								<Box
									height="100%"
									display="flex"
									alignItems="center"
									justifyContent="center"
									p={2}
									sx={{ overflow: "auto" }}
								>
									<img
										src={target.url}
										alt={name}
										onError={() => setImageError(true)}
										style={{ maxWidth: "100%", maxHeight: "100%", objectFit: "contain" }}
									/>
								</Box>
							) : imageError ? (
								<FallbackMessage />
							) : (
								<iframe title={name} src={target.url} style={{ width: "100%", height: "100%", border: "none" }} />
							)}
						</Box>

						{!showImage && (
							<Box px={2} py={1} sx={{ borderTop: "1px solid rgba(0, 0, 0, 0.12)" }}>
								<Typography variant="caption" color="text.secondary">
									Se l'anteprima non viene visualizzata, usa "Apri in una nuova scheda" o "Scarica".
								</Typography>
							</Box>
						)}
					</Box>
				)}
			</Drawer>
		</DocumentPreviewContext.Provider>
	);
}

function FallbackMessage() {
	return (
		<Box height="100%" display="flex" alignItems="center" justifyContent="center" p={3}>
			<Typography variant="body2" color="text.secondary" textAlign="center">
				Anteprima non disponibile. Usa "Apri in una nuova scheda" o "Scarica" nella barra in alto.
			</Typography>
		</Box>
	);
}
