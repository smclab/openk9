import { Button, Checkbox, ListItemText, Menu, MenuItem } from "@mui/material";
import SourceOutlinedIcon from "@mui/icons-material/SourceOutlined";
import React from "react";
import { useQuery } from "react-query";
import { useTranslation } from "react-i18next";
import { OpenK9Client } from "./client";

type Datasource = { id: number; name: string };

const DatasourceSelect = ({
	selectedDatasourceIds,
	onChange,
}: {
	selectedDatasourceIds: number[];
	onChange: (ids: number[]) => void;
}) => {
	const { t } = useTranslation();
	const [anchorEl, setAnchorEl] = React.useState<null | HTMLElement>(null);

	const client = OpenK9Client();

	const { data: datasources = [] } = useQuery<Datasource[]>({
		queryKey: ["datasources"],
		queryFn: client.getDatasources,
		staleTime: Infinity,
		cacheTime: Infinity,
	});

	const handleClick = (event: React.MouseEvent<HTMLElement>) => {
		setAnchorEl(event.currentTarget);
	};

	const handleClose = () => {
		setAnchorEl(null);
	};

	const toggleDatasource = (id: number) => {
		onChange(
			selectedDatasourceIds.includes(id)
				? selectedDatasourceIds.filter((selectedId) => selectedId !== id)
				: [...selectedDatasourceIds, id],
		);
	};

	if (datasources.length === 0) {
		return null;
	}

	const selectedCount = selectedDatasourceIds.length;
	const label =
		selectedCount === 0
			? t("all-sources", { defaultValue: "All sources" })
			: selectedCount === 1
			? t("one-source", { defaultValue: "1 source" })
			: t("selected-sources", { count: selectedCount, defaultValue: "{{count}} sources" });

	return (
		<>
			<Button
				onClick={handleClick}
				startIcon={<SourceOutlinedIcon />}
				size="small"
				variant="outlined"
				sx={{
					color: "text.primary",
					minWidth: "auto",
					px: 1,
					py: 0.5,
					fontSize: "0.875rem",
					position: "relative",
					zIndex: 2,
					borderRadius: "10px",
					borderColor: "rgba(0, 0, 0, 0.12)",
					"&:hover": {
						borderColor: "primary.main",
						backgroundColor: "#C0272B",
					},
				}}
			>
				{label}
			</Button>
			<Menu
				anchorEl={anchorEl}
				open={Boolean(anchorEl)}
				onClose={handleClose}
				slotProps={{
					paper: {
						sx: {
							boxShadow: "none",
							borderRadius: "10px",
							border: "1px solid rgba(0, 0, 0, 0.25)",
							maxHeight: 320,
						},
					},
				}}
				anchorOrigin={{
					vertical: "top",
					horizontal: "left",
				}}
				transformOrigin={{
					vertical: "bottom",
					horizontal: "left",
				}}
			>
				{datasources.map((datasource) => {
					const isSelected = selectedDatasourceIds.includes(datasource.id);
					return (
						<MenuItem
							key={datasource.id}
							onClick={() => toggleDatasource(datasource.id)}
							dense
							selected={isSelected}
							role="menuitemcheckbox"
							aria-checked={isSelected}
						>
							<Checkbox
								checked={isSelected}
								size="small"
								sx={{ p: 0.5, mr: 1, "&.Mui-checked": { color: "secondary.contrastText" } }}
								tabIndex={-1}
								aria-hidden
							/>
							<ListItemText
								primary={datasource.name}
								primaryTypographyProps={{ fontWeight: isSelected ? 600 : 400 }}
							/>
						</MenuItem>
					);
				})}
			</Menu>
		</>
	);
};

export const DatasourceSelectMemo = React.memo(DatasourceSelect);
