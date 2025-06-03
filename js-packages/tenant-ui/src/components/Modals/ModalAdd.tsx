import { MutationTuple } from "@apollo/client";
import { Autocomplete, Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, TextField } from "@mui/material";
import React from "react";
import { useToast } from "../ToastProvider";

export function ModalAdd<TAssociation, TRemove>({
  id,
  callbackClose,
  title,
  list,
  association,
  remove,
  messageSuccess,
}: {
  id: string | null | undefined;
  callbackClose: () => void;
  messageSuccess: string;
  title?: string;
  list:
    | {
        edges?: Array<{
          node?: {
            id?: string | null;
            name?: string | null;
            description?: string | null;
          } | null;
        } | null> | null;
      }
    | undefined
    | null;
  association: () => MutationTuple<TAssociation, any>;
  remove: () => MutationTuple<TRemove, any>;
}) {
  const [add] = association();
  const [deleteMutation] = remove();
  const [open, setOpen] = React.useState(true);
  const toast = useToast();
  const [items, setItems] = React.useState<Array<{ label: string; value: string }>>([]);

  const handleApply = async () => {
    const removeItems = list?.edges?.filter((remove) => !items.find((item) => item.value === remove?.node?.id));

    if (removeItems && removeItems.length > 0) {
      await Promise.all(
        removeItems.map(async (item) => {
          if (item?.node?.id) {
            await deleteMutation({
              variables: {
                parentId: item.node.id,
                childId: id || "",
              },
            });
          }
        })
      );
    }

    if (id && items.length > 0) {
      await Promise.all(
        items.map(async (item) => {
          await add({
            variables: {
              parentId: item.value,
              childId: id,
            },
            onCompleted: () => {
              toast({
                displayType: "success",
                title: messageSuccess,
                content: "",
              });
              callbackClose();
            },
            onError: (error) => {
              toast({
                displayType: "error",
                title: "Errore",
                content: error.message || "Si è verificato un errore.",
              });
              callbackClose();
            },
          });
        })
      );
    }
    // setIsRecap(true);
  };

  return (
    <Dialog open={open} onClose={callbackClose} fullWidth={true}>
      <DialogTitle>{title || "Association"}</DialogTitle>
      <DialogContent>
        <Autocomplete
          multiple
          options={
            list?.edges?.map((el) => ({
              value: el?.node?.id || "",
              label: el?.node?.name || "",
            })) || []
          }
          getOptionLabel={(option) => option.label}
          value={items}
          onChange={(event, newValue) => setItems(newValue)}
          renderInput={(params) => <TextField {...params} label="Select Items" />}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={callbackClose} color="primary">
          Close
        </Button>
        <Button onClick={handleApply} color="primary">
          Apply
        </Button>
      </DialogActions>
    </Dialog>
  );
}

export function ModalAddSingle({
  id,
  callbackClose,
  title,
  list,
  association,
  messageSuccess,
}: {
  id?: string | null | undefined;
  callbackClose: () => void;
  title?: string;
  messageSuccess: string;
  list: ({ id?: string | null; name?: string | null } | null)[] | null | undefined;
  association: ({
    parentId,
    childId,
    onSuccessCallback,
    onErrorCallback,
  }: {
    parentId: string;
    childId: string;
    onSuccessCallback: () => void;
    onErrorCallback: (error: Error) => void;
  }) => void;
}) {
  // const [open, setOpen] = React.useState(true);
  const [items, setItems] = React.useState<{
    label: string;
    value: string;
  } | null>();
  const toast = useToast();

  const handleApply = () => {
    association({
      parentId: items?.value || "-1",
      childId: id || "-1",
      onSuccessCallback: () => {
        toast({
          displayType: "success",
          title: "Association",
          content: messageSuccess || "",
        });
        callbackClose();
      },
      onErrorCallback: (error) => {
        toast({
          displayType: "error",
          title: "Errore",
          content: error.message || "Si è verificato un errore.",
        });
      },
      // onCompleted: () => {
      //   toast({
      //     displayType: "success",
      //     title: "Association",
      //     content: messageSuccess || "",
      //   });
      //   setOpen(false);
      // },

      // onError: (error) => {
      //   toast({
      //     displayType: "error",
      //     title: "Errore",
      //     content: error.message || "Si è verificato un errore.",
      //   });
      // },
    });
  };

  const createData =
    list?.map((el) => {
      return { value: el?.id || "", label: el?.name || "" };
    }) || [];

  const handleClose = () => {
    callbackClose();
  };

  return (
    <Dialog open onClose={handleClose} fullWidth={true}>
      <DialogTitle>{title ? title : "Association"}</DialogTitle>
      <DialogContent>
        <>
          <Autocomplete
            options={createData}
            getOptionLabel={(option) => option.label}
            value={items}
            onChange={(event, newValue) => setItems(newValue)}
            renderInput={(params) => <TextField {...params} label="Select Items" />}
          />
        </>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose} color="secondary">
          Close
        </Button>
        <Button onClick={handleApply} color="primary">
          Apply
        </Button>
      </DialogActions>
    </Dialog>
  );
}
