/*
* Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
*
* This program is free software: you can redistribute it and/or modify
* it under the terms of the GNU Affero General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU Affero General Public License for more details.
*
* You should have received a copy of the GNU Affero General Public License
* along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/
import { NamePath, useSideNavigation } from "@components/sideNavigationContext";
import AddIcon from "@mui/icons-material/Add";
import {
  Box,
  Button,
  Checkbox,
  List,
  ListItemIcon,
  ListItemText,
  Paper,
  Skeleton,
  SxProps,
  Theme,
  Typography,
} from "@mui/material";
import React from "react";
import { useNavigate } from "react-router-dom";
import { associateType } from "utils";
import { ModalConfirm } from "../Modals";

type ListProps = {
  unassociated: associateType[] | undefined;
  isLoading: boolean;
  associated: associateType[] | undefined;
};

export function MultiAssociationCustomQuery<Q>({
  sx,
  list,
  disabled,
  isLoading,
  titleAssociation,
  onSelect,
  isRecap,
  createPath,
}: {
  sx?: SxProps<Theme>;
  list: ListProps;
  disabled: boolean;
  isLoading?: boolean;
  isRecap: boolean;
  titleAssociation?: string;
  createPath?: { entity: NamePath; path: string };
  onSelect({ items, isAdd }: { items: Array<{ value: string; label: string }>; isAdd: boolean }): void;
}) {
  const [selectedInUse, setSelectedInUse] = React.useState<{ label: string; value: string }[]>([]);
  const [selectedAvailable, setSelectedAvailable] = React.useState<{ label: string; value: string }[]>([]);
  const [inUseItems, setInUseItems] = React.useState<{ label: string; value: string }[]>([]);
  const [availableItems, setAvailableItems] = React.useState<{ label: string; value: string }[]>([]);
  const [isViewModal, setIsViewModal] = React.useState(false);
  const { changaSideNavigation } = useSideNavigation();
  const navigate = useNavigate();

  React.useEffect(() => {
    const associatedItems =
      list.associated?.map((item) => ({
        value: item.value || "",
        label: item.label || "",
      })) || [];
    setInUseItems(associatedItems);
  }, [list.associated]);

  React.useEffect(() => {
    const unassociatedItems =
      list.unassociated?.map((item) => ({
        value: item.value || "",
        label: item.label || "",
      })) || [];
    setAvailableItems(unassociatedItems);
  }, [list.unassociated]);

  function handleItemsChange({ isAdded }: { isAdded: boolean }) {
    if (!isAdded) {
      const updatedAvailableItems = availableItems.filter(
        (item) => !selectedAvailable.some((sel) => sel.value === item.value),
      );
      const newInUseItems = [...inUseItems, ...selectedAvailable];

      setAvailableItems(updatedAvailableItems);
      setInUseItems(newInUseItems);
      onSelect({ items: selectedAvailable, isAdd: true });
    } else {
      const updatedInUseItems = inUseItems.filter((item) => !selectedInUse.some((sel) => sel.value === item.value));
      const newAvailableItems = [...availableItems, ...selectedInUse];

      setInUseItems(updatedInUseItems);
      setAvailableItems(newAvailableItems);
      onSelect({ items: selectedInUse, isAdd: false });
    }
    setSelectedInUse([]);
    setSelectedAvailable([]);
  }

  const toggleSelection = (
    item: { label: string; value: string },
    setSelected: React.Dispatch<React.SetStateAction<{ label: string; value: string }[]>>,
  ) => {
    setSelected((prev) =>
      prev.some((selectedItem) => selectedItem.value === item.value)
        ? prev.filter((selectedItem) => selectedItem.value !== item.value)
        : [...prev, item],
    );
  };

  const renderList = (
    title: string,
    items: { label: string; value: string }[],
    selected: { label: string; value: string }[],
    onToggle: (item: { label: string; value: string }) => void,
    label: string,
    createPath?: { entity: NamePath; path: string },
  ) => (
    <>
      <Box
        display={"flex"}
        flexDirection={"row"}
        justifyContent={"space-between"}
        alignItems={"center"}
        sx={{ marginBottom: 1 }}
      >
        <Typography variant="body2" sx={{ fontWeight: 400 }}>
          {label}
        </Typography>
        {createPath && (
          <Button
            onClick={() => {
              setIsViewModal(true);
            }}
          >
            <AddIcon />
          </Button>
        )}
      </Box>
      {isViewModal && (
        <ModalConfirm
          title="Confirm to leave from this page?"
          body="Are you sure you want to leave this page? This action is irreversible and all associated data will be lost."
          type="info"
          labelConfirm="Confirm"
          actionConfirm={() => {
            if (createPath?.path && createPath.entity) {
              window.scrollTo({ top: 0, behavior: "smooth" });
              changaSideNavigation(createPath.entity);
              navigate(createPath.path);
            }
            setIsViewModal(false);
          }}
          close={() => {
            setIsViewModal(false);
          }}
        />
      )}
      {isLoading ? (
        <Paper
          variant="outlined"
          sx={{ minWidth: 200, height: 230, display: "flex", flexDirection: "column", gap: 1, padding: 2 }}
        >
          {Array.from({ length: 5 }).map((_, index) => (
            <Skeleton key={index} variant="rectangular" width="100%" height={32} />
          ))}
        </Paper>
      ) : (
        <Paper variant="outlined" sx={{ minWidth: 200, height: 230, overflow: "auto" }}>
          <List
            dense
            component="div"
            role="list"
            aria-label={title}
            sx={{ background: isLoading ? "blue" : "trasparent" }}
          >
            {items.map((item) => (
              <Box
                key={item.value}
                role="listitem"
                sx={{
                  display: "flex",
                  alignItems: "center",
                  padding: "8px",
                  cursor: "pointer",
                }}
                onClick={() => onToggle(item)}
              >
                <ListItemIcon>
                  <Checkbox
                    checked={selected.some((sel) => sel.value === item.value)}
                    tabIndex={-1}
                    disableRipple
                    disabled={disabled}
                  />
                </ListItemIcon>
                <ListItemText primary={item.label} />
              </Box>
            ))}
          </List>
        </Paper>
      )}
    </>
  );

  return (
    <Box>
      {titleAssociation && (
        <Typography variant="body1" fontWeight={600}>
          {titleAssociation}
        </Typography>
      )}
      <Box
        display="flex"
        justifyContent="center"
        alignItems="center"
        gap={2}
        sx={{ marginBottom: "20px", width: "fit-content", ...sx }}
      >
        <Box sx={{ minWidth: isRecap ? "300px" : "unset" }}>
          {renderList(
            "In Use",
            inUseItems,
            selectedInUse,
            (item) => {
              return !disabled && toggleSelection(item, setSelectedInUse);
            },
            !isRecap ? "Associated Items" : "",
          )}
        </Box>
        {!isRecap && (
          <>
            <Box display="flex" flexDirection="column" alignItems="center" gap={1}>
              <Button
                variant="outlined"
                size="small"
                onClick={() => handleItemsChange({ isAdded: true })}
                disabled={selectedInUse.length === 0 || disabled}
                aria-label="move selected to available"
              >
                &gt;
              </Button>
              <Button
                variant="outlined"
                size="small"
                onClick={() => handleItemsChange({ isAdded: false })}
                disabled={selectedAvailable.length === 0 || disabled}
                aria-label="move selected to in use"
              >
                &lt;
              </Button>
            </Box>
            <Box>
              {renderList(
                "Available",
                availableItems,
                selectedAvailable,
                (item) => !disabled && toggleSelection(item, setSelectedAvailable),
                "Unassociated Items",
                createPath,
              )}
            </Box>
          </>
        )}
      </Box>
    </Box>
  );
}

