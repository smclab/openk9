import { Box, Button, IconButton, TextField, Tooltip, Typography } from "@mui/material";
import ArrowUpwardIcon from "@mui/icons-material/ArrowUpward";
import StopCircleIcon from "@mui/icons-material/StopCircle";
import AttachFileIcon from "@mui/icons-material/AttachFile";
import BrokenImageOutlinedIcon from "@mui/icons-material/BrokenImageOutlined";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import CloseIcon from "@mui/icons-material/Close";
import ErrorOutlineIcon from "@mui/icons-material/ErrorOutline";
import ImageSearchIcon from "@mui/icons-material/ImageSearch";
import {
	ALLOWED_CONTENT_TYPES,
	ImageQueryError,
	MAX_INPUT_BYTES,
	forgetQueryImage,
	prepareQueryImageCached,
} from "../../../shared/image-query/imageQuery";
import React from "react";
import { useTranslation } from "react-i18next";
import { v4 as uuidv4 } from "uuid";
import UnicodeSpinner from "./utils/UnicodeSpinner";
import { DatasourceSelectMemo } from "./DatasourceSelect";
import { supportsImageQuery, useUser } from "./ChatInfoContext";
import { HandleSearch, QueryImageAttachment } from "./useGenerateResponse";

type ImageAttachment = QueryImageAttachment & {
	status: "pending" | "ready" | "error";
	error?: string;
	width?: number;
	height?: number;
	bytes?: number;
};

// Nasconde visivamente lasciando il testo agli screen reader.
const srOnly = {
	position: "absolute" as const,
	width: 1,
	height: 1,
	padding: 0,
	margin: -1,
	overflow: "hidden",
	clip: "rect(0 0 0 0)",
	whiteSpace: "nowrap" as const,
	border: 0,
};

function formatBytes(bytes: number): string {
	return bytes >= 1024 * 1024 ? `${(bytes / (1024 * 1024)).toFixed(1)} MB` : `${Math.round(bytes / 1024)} KB`;
}

export default function Search({
	handleSearch,
	cancelAllResponses,
	isChatting,
	canSend = true,
	onUploadFiles,
	isAuthenticated,
	retrieveFromUploadedDocuments,
	onSetRetrieveFromUploadedDocuments,
	selectedDatasourceIds,
	onSetSelectedDatasourceIds,
}: {
	handleSearch: HandleSearch;
	cancelAllResponses(): void;
	isChatting: boolean;
	/** False while the turn would be rejected downstream (chat or bucket info not loaded yet). */
	canSend?: boolean;
	onUploadFiles?: (files: File[]) => Promise<{ ok: boolean }>;
	isAuthenticated?: boolean;
	retrieveFromUploadedDocuments?: boolean;
	onSetRetrieveFromUploadedDocuments?: (value: boolean) => void;
	selectedDatasourceIds: number[];
	onSetSelectedDatasourceIds: (ids: number[]) => void;
}) {
	const [search, setSearch] = React.useState("");
	const { t } = useTranslation();
	const { userInfo } = useUser();

	const [uploading, setUploading] = React.useState(false);
	const [uploadDone, setUploadDone] = React.useState(false);
	const [errors, setErrors] = React.useState<string[]>([]);

	const [attachment, setAttachment] = React.useState<ImageAttachment | null>(null);
	const [thumbBroken, setThumbBroken] = React.useState(false);

	const inputRef = React.useRef<HTMLInputElement | null>(null);
	const imageInputRef = React.useRef<HTMLInputElement | null>(null);
	const imageButtonRef = React.useRef<HTMLButtonElement | null>(null);
	const [lastUploaded, setLastUploaded] = React.useState<string[]>([]);

	const canQueryByImage = supportsImageQuery(userInfo?.retrieveType);

	const allowedTypes = [
		"application/pdf",
		"application/vnd.openxmlformats-officedocument.wordprocessingml.document",
		"application/vnd.openxmlformats-officedocument.presentationml.presentation",
		"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
		"text/html",
		"text/csv",
	];
	const maxSize = 10 * 1024 * 1024;

	function validateFiles(files: File[]): { valid: File[]; errors: string[] } {
		const errs: string[] = [];
		const valid: File[] = [];
		files.forEach((f) => {
			if (f.size > maxSize) {
				errs.push(`${f.name}: ${t("file-too-large", { defaultValue: "File too large (max 10MB)" })}`);
				return;
			}
			if (!allowedTypes.includes(f.type)) {
				errs.push(`${f.name}: ${t("unsupported-file-type", { defaultValue: "Unsupported file type" })}`);
				return;
			}
			valid.push(f);
		});
		return { valid, errors: errs };
	}

	async function handleFilesSelected(filesList: FileList | null) {
		if (!filesList || filesList.length === 0) return;
		const files = Array.from(filesList);
		const { valid, errors } = validateFiles(files);
		setErrors(errors);
		setUploadDone(false);
		if (valid.length > 0 && onUploadFiles) {
			try {
				setUploading(true);
				await onUploadFiles(valid);
				setUploadDone(true);
				onSetRetrieveFromUploadedDocuments?.(true);
				setLastUploaded(valid.map((f) => f.name));
			} catch (e: any) {
				setErrors([(e?.message as string) || "Upload error"]);
			} finally {
				setUploading(false);
			}
		}
	}

	function imageErrorMessage(error: unknown): string {
		const code = error instanceof ImageQueryError ? error.code : "decode-failed";
		switch (code) {
			case "unsupported-type":
				return t("unsupported-file-type", { defaultValue: "Unsupported file type" });
			case "input-too-large":
			case "too-many-pixels":
				return t("image-too-large", {
					defaultValue: "Image too large (max {{size}} MB)",
					size: Math.round(MAX_INPUT_BYTES / (1024 * 1024)),
				});
			default:
				return t("image-not-readable", { defaultValue: "Image could not be read" });
		}
	}

	// Revoke only when the image was not sent: once sent the object URL belongs to the message
	// thumbnail, and revoking it breaks that image.
	function clearAttachment(current: ImageAttachment | null, options: { revoke: boolean }) {
		if (!current) return;
		if (options.revoke) {
			URL.revokeObjectURL(current.previewUrl);
		}
		forgetQueryImage(current.attachmentId);
		setAttachment(null);
		setThumbBroken(false);
	}

	async function handleImageSelected(filesList: FileList | null) {
		const file = filesList?.[0];
		if (!file) return;

		clearAttachment(attachment, { revoke: true });

		const attachmentId = uuidv4();
		const previewUrl = URL.createObjectURL(file);
		const base: QueryImageAttachment = { attachmentId, file, previewUrl, filename: file.name };

		// `accept` only filters the file picker: a dropped file still reaches here, so re-check the type.
		if (!ALLOWED_CONTENT_TYPES.includes(file.type)) {
			setAttachment({
				...base,
				status: "error",
				error: `${t("unsupported-file-type", { defaultValue: "Unsupported file type" })}`,
			});
			return;
		}

		setAttachment({ ...base, status: "pending" });

		try {
			const prepared = await prepareQueryImageCached(attachmentId, file);
			setAttachment((previous) =>
				previous?.attachmentId === attachmentId
					? {
							...previous,
							status: "ready",
							width: prepared.width,
							height: prepared.height,
							bytes: prepared.bytes,
					  }
					: previous,
			);
		} catch (error) {
			setAttachment((previous) =>
				previous?.attachmentId === attachmentId
					? { ...previous, status: "error", error: imageErrorMessage(error) }
					: previous,
			);
		}
	}

	const hasText = search.trim() !== "";
	const imageReady = attachment?.status === "ready";
	// Every new condition must stay inside `!isChatting`: otherwise the Stop button gets disabled
	// mid-stream. An attachment in error deliberately does not block sending valid text.
	const sendDisabled =
		!isChatting && (!canSend || attachment?.status === "pending" || (!hasText && !imageReady));

	const attachmentSummary = !attachment
		? ""
		: attachment.status === "error"
		? `${attachment.filename}: ${attachment.error ?? ""}`
		: attachment.status === "pending"
		? t("preparing-image", { defaultValue: "Preparing image" })
		: t("attached-image", { defaultValue: "Attached image: {{filename}}", filename: attachment.filename });

	const showAdornment = isAuthenticated || canQueryByImage;

	return (
		<Box component="footer" p={2} bgcolor="background.paper" sx={{ borderRadius: "10px" }} zIndex={2} width={"100%"}>
			{(errors.length > 0 || uploadDone || uploading) && (
				<Box sx={{ mb: 1 }}>
					{uploading && (
						<Box sx={{ fontSize: 12 }}>
							<UnicodeSpinner text={`${t("uploading", { defaultValue: "Uploading" })}`} />
						</Box>
					)}
					{uploadDone && (
						<Box sx={{ display: "flex", alignItems: "center", gap: "6px", color: "green", fontSize: 12 }}>
							<CheckCircleOutlineIcon fontSize="small" />
							<span>
								{t("upload-completed", { defaultValue: "Upload completed" })}
								{lastUploaded.length > 0 ? `: ${lastUploaded.join(", ")}` : ""}
							</span>
						</Box>
					)}
					{errors.map((e, i) => (
						<Box key={i} sx={{ display: "flex", alignItems: "center", gap: "6px", color: "#c0272b", fontSize: 12 }}>
							<ErrorOutlineIcon fontSize="small" />
							<span>{e}</span>
						</Box>
					))}
				</Box>
			)}
			<Box sx={{ display: "flex", mb: 1 }}>
				<DatasourceSelectMemo selectedDatasourceIds={selectedDatasourceIds} onChange={onSetSelectedDatasourceIds} />
			</Box>
			<form
				onSubmit={(event) => {
					event.preventDefault();
					if (isChatting) {
						cancelAllResponses();
						return;
					}
					const image = attachment?.status === "ready" ? attachment : undefined;
					// Image and uploaded documents take different retrieval branches, so the image wins.
					const flag = image ? false : retrieveFromUploadedDocuments === true || uploadDone;
					handleSearch(search, flag, image);
					setSearch("");
					clearAttachment(attachment, { revoke: image === undefined });
				}}
				style={{ width: "100%", display: "flex", gap: "15px" }}
			>
				<TextField
					size="small"
					fullWidth
					variant="outlined"
					value={search}
					onChange={(event) => setSearch(event.currentTarget.value)}
					placeholder={t("write-a-message", { defaultValue: "Write a message" })!}
					sx={{
						"& .MuiOutlinedInput-notchedOutline": {
							borderRadius: "10px",
						},
						"&:hover .MuiOutlinedInput-notchedOutline": {
							borderColor: "#c0272b",
						},
					}}
					InputProps={{
						endAdornment: showAdornment && (
							<>
								{/* Document attach: needs upload-files, hence a user and a chat id, hence isAuthenticated. */}
								{isAuthenticated && (
									<>
										<input
											ref={inputRef}
											type="file"
											multiple
											style={{ display: "none" }}
											accept=".pdf,.docx,.pptx,.xlsx,.html,.csv,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.openxmlformats-officedocument.presentationml.presentation,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,text/html,text/csv"
											onChange={(e) => {
												handleFilesSelected(e.currentTarget.files);
												if (inputRef.current) inputRef.current.value = "";
											}}
										/>
										<Tooltip title={t("attach-file-aria-label", { defaultValue: "Attach a file" })}>
											<IconButton
												aria-label={`${t("attach-file-aria-label", { defaultValue: "Attach a file" })}`}
												onClick={() => inputRef.current?.click()}
												size="small"
												sx={{ mr: 0.5 }}
											>
												<AttachFileIcon fontSize="small" />
											</IconButton>
										</Tooltip>
									</>
								)}
								{/* Deliberately not behind isAuthenticated: an image as a question needs no identity or storage. */}
								{canQueryByImage && (
									<>
										<input
											ref={imageInputRef}
											type="file"
											style={{ display: "none" }}
											accept={ALLOWED_CONTENT_TYPES.join(",")}
											onChange={(e) => {
												handleImageSelected(e.currentTarget.files);
												// Re-picking the same file fires no change event unless the input is reset.
												if (imageInputRef.current) imageInputRef.current.value = "";
											}}
										/>
										<Tooltip title={t("search-by-image", { defaultValue: "Search by image" })}>
											<IconButton
												ref={imageButtonRef}
												aria-label={`${t("search-by-image", { defaultValue: "Search by image" })}`}
												onClick={() => imageInputRef.current?.click()}
												size="small"
											>
												<ImageSearchIcon fontSize="small" />
											</IconButton>
										</Tooltip>
									</>
								)}
							</>
						),
					}}
				/>
				<Button
					variant="contained"
					type="submit"
					value="Submit"
					disabled={sendDisabled}
					aria-label={
						isChatting
							? `${t("stop-generating", { defaultValue: "Stop generating" })}`
							: `${t("send-message", { defaultValue: "Send message" })}`
					}
					sx={{ borderRadius: "10px" }}
				>
					{isChatting ? <StopCircleIcon /> : <ArrowUpwardIcon />}
				</Button>
			</form>

			{/* Present from first paint: a node that appears already carrying aria-live is often not announced. */}
			<Box sx={srOnly} role="status" aria-live="polite">
				{attachmentSummary}
			</Box>

			{attachment && (
				<Box
					sx={{
						mt: 1,
						display: "flex",
						alignItems: "flex-start",
						gap: "10px",
						p: "7px 10px",
						border: `1px solid ${attachment.status === "error" ? "#c0272b" : "rgba(0, 0, 0, 0.15)"}`,
						borderRadius: "10px",
					}}
				>
					{attachment.status === "error" || thumbBroken ? (
						<Box
							sx={{
								width: 40,
								height: 40,
								flexShrink: 0,
								borderRadius: "8px",
								backgroundColor: "rgba(0, 0, 0, 0.04)",
								display: "flex",
								alignItems: "center",
								justifyContent: "center",
							}}
						>
							<BrokenImageOutlinedIcon fontSize="small" sx={{ color: "#c0272b" }} />
						</Box>
					) : (
						<Box
							component="img"
							src={attachment.previewUrl}
							// Decorative: the filename is visible text next to it, so alt would repeat it.
							alt=""
							onError={() => setThumbBroken(true)}
							sx={{
								width: 40,
								height: 40,
								flexShrink: 0,
								objectFit: "cover",
								borderRadius: "8px",
								border: "1px solid rgba(0, 0, 0, 0.12)",
							}}
						/>
					)}
					<Box sx={{ flex: 1, minWidth: 0, display: "flex", flexDirection: "column", gap: "2px" }}>
						<Typography variant="body2" fontWeight={600} noWrap title={attachment.filename}>
							{attachment.filename}
						</Typography>
						{attachment.status === "pending" && (
							<Typography variant="caption" color="text.secondary">
								{t("preparing-image", { defaultValue: "Preparing image" })}
							</Typography>
						)}
						{attachment.status === "ready" && attachment.bytes != null && (
							<Typography variant="caption" color="text.secondary">
								{`${attachment.width} × ${attachment.height} · ${formatBytes(attachment.bytes)}`}
							</Typography>
						)}
						{attachment.status === "error" && (
							<Typography
								variant="caption"
								role="alert"
								sx={{ color: "#c0272b", display: "flex", alignItems: "center", gap: "6px" }}
							>
								<ErrorOutlineIcon sx={{ fontSize: 16 }} />
								{attachment.error}
							</Typography>
						)}
					</Box>
					<Tooltip title={t("remove-image", { defaultValue: "Remove image" })}>
						<IconButton
							size="small"
							aria-label={`${t("remove-image", { defaultValue: "Remove image" })}`}
							onClick={() => {
								clearAttachment(attachment, { revoke: true });
								// The row unmounts: without this focus falls to <body> and keyboard users restart.
								imageButtonRef.current?.focus();
							}}
						>
							<CloseIcon fontSize="small" />
						</IconButton>
					</Tooltip>
				</Box>
			)}
		</Box>
	);
}
