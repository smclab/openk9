import {
	Box,
	Button,
	TextField,
	IconButton,
	Popover,
	List,
	ListItemButton,
	ListItemText,
	Typography,
	Divider,
} from "@mui/material";
import ArrowUpwardIcon from "@mui/icons-material/ArrowUpward";
import StopCircleIcon from "@mui/icons-material/StopCircle";
import AttachFileIcon from "@mui/icons-material/AttachFile";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import ErrorOutlineIcon from "@mui/icons-material/ErrorOutline";
import DescriptionIcon from "@mui/icons-material/Description";
import React from "react";
import { useTranslation } from "react-i18next";
import UnicodeSpinner from "./utils/UnicodeSpinner";

export default function Search({
	handleSearch,
	cancelAllResponses,
	isChatting,
	onUploadFiles,
	isAuthenticated,
	retrieveFromUploadedDocuments,
	onSetRetrieveFromUploadedDocuments,
}: {
	handleSearch: (message: string, retrieveFromUploadedDocuments?: boolean) => void;
	cancelAllResponses(): void;
	isChatting: boolean;
	onUploadFiles?: (files: File[]) => Promise<{ ok: boolean }>;
	isAuthenticated?: boolean;
	retrieveFromUploadedDocuments?: boolean;
	onSetRetrieveFromUploadedDocuments?: (value: boolean) => void;
}) {
	const [search, setSearch] = React.useState("");
	const { t } = useTranslation();

	const [uploading, setUploading] = React.useState(false);
	const [uploadDone, setUploadDone] = React.useState(false);
	const [errors, setErrors] = React.useState<string[]>([]);

	const inputRef = React.useRef<HTMLInputElement | null>(null);
	const [anchorEl, setAnchorEl] = React.useState<HTMLElement | null>(null);
	const [lastUploaded, setLastUploaded] = React.useState<string[]>([]);

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

	const handleOpenAttach = (e: React.MouseEvent<HTMLElement>) => {
		if (!isAuthenticated) return;
		setAnchorEl(e.currentTarget);
	};

	const handleCloseAttach = () => setAnchorEl(null);

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
			<form
				onSubmit={(event) => {
					event.preventDefault();
					if (isChatting) {
						cancelAllResponses();
					} else {
						const flag = retrieveFromUploadedDocuments === true || uploadDone;
						handleSearch(search, flag);
						setSearch("");
					}
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
						endAdornment: isAuthenticated && (
							<>
								<input
									ref={inputRef}
									type="file"
									multiple
									style={{ display: "none" }}
									accept=".pdf,.docx,.pptx,.xlsx,.html,.csv,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document,application/vnd.openxmlformats-officedocument.presentationml.presentation,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,text/html,text/csv"
									onChange={(e) => {
										handleCloseAttach();
										handleFilesSelected(e.currentTarget.files);
										if (inputRef.current) inputRef.current.value = "";
									}}
								/>
								<IconButton
									aria-label={`${t("attach-file-aria-label", { defaultValue: "Attach a file" })}`}
									onClick={handleOpenAttach}
									disabled={!isAuthenticated}
									size="small"
									sx={{ mr: 0.5 }}
								>
									<AttachFileIcon fontSize="small" />
								</IconButton>
								<Popover
									open={Boolean(anchorEl)}
									anchorEl={anchorEl}
									onClose={handleCloseAttach}
									anchorOrigin={{ vertical: "top", horizontal: "right" }}
									transformOrigin={{ vertical: "bottom", horizontal: "right" }}
									disableRestoreFocus
								>
									<Box sx={{ px: 2, pt: 1.5, pb: 1 }}>
										<Typography variant="subtitle2" sx={{ textAlign: "center", fontWeight: 600 }}>
											{t("attach-popup-title", { defaultValue: "Upload an attachment" })}
										</Typography>
									</Box>
									<Divider />
									<List dense sx={{ minWidth: 220 }}>
										<ListItemButton
											onClick={() => {
												handleCloseAttach();
												inputRef.current?.click();
											}}
										>
											<DescriptionIcon sx={{ fontSize: 18, mr: 1 }} />
											<ListItemText primary={t("document", { defaultValue: "Document" })} />
										</ListItemButton>
									</List>
								</Popover>
							</>
						),
					}}
				/>
				<Button
					variant="contained"
					type="submit"
					value="Submit"
					disabled={!isChatting && search === ""}
					sx={{ borderRadius: "10px" }}
				>
					{isChatting ? <StopCircleIcon /> : <ArrowUpwardIcon />}
				</Button>
			</form>
		</Box>
	);
}
