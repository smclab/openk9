import {
  Dialog,
  Slide,
  Box,
  Typography,
  IconButton,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
  useTheme,
  Breakpoint,
} from "@mui/material";
import React, { ReactNode } from "react";
import CloseIcon from "@mui/icons-material/Close";

export function ModalConfirm({
  actionConfirm,
  labelConfirm,
  title,
  body,
  close,
  children,
  maxWidth,
  fullWidth = false,
  type = "info",
  confirmationWord,
}: {
  actionConfirm(): void;
  labelConfirm: string;
  title: string;
  body?: string;
  children?: ReactNode;
  close(): void;
  fullWidth?: boolean;
  maxWidth?: Breakpoint;
  type?: "success" | "info" | "error" | "warning";
  confirmationWord?: string;
}) {
  const [open, setOpen] = React.useState(true);
  const [inputValue, setInputValue] = React.useState("");
  const theme = useTheme();

  const colorMap = {
    success: theme.palette.success.main,
    error: theme.palette.error.main,
    warning: theme.palette.warning.main,
    info: theme.palette.info.main,
  };
  const handleClose = () => {
    setOpen(false);
    close();
  };

  return (
    <Dialog
      open={open}
      onClose={handleClose}
      TransitionComponent={Slide}
      transitionDuration={500}
      fullWidth={fullWidth}
      maxWidth={maxWidth}
      sx={{
        "& .MuiDialog-paper": {
          overflow: "hidden",
          boxShadow: "0 10px 20px rgba(0,0,0,0.2)",
        },
      }}
    >
      <Box
        sx={{
          background: colorMap[type],
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          padding: "16px",
          borderBottom: "1px solid rgba(255, 255, 255, 0.1)",
        }}
      >
        <Typography variant="h6" sx={{ color: "white", fontWeight: "bold", fontSize: "1.25rem" }}>
          {title}
        </Typography>
        <IconButton onClick={handleClose}>
          <CloseIcon sx={{ fill: "white" }} />
        </IconButton>
      </Box>
      <DialogContent sx={{ padding: "20px 16px" }}>
        <DialogContentText id="alert-dialog-description">
          <Typography variant="body1" sx={{ color: theme.palette.text.primary }}>
            {body}
            {children}
          </Typography>
        </DialogContentText>
        {confirmationWord && (
          <Box
            sx={{
              mt: 2,
              p: 2,
              backgroundColor: theme.palette.background.default,
              borderRadius: "8px",
              border: `1px solid ${theme.palette.divider}`,
            }}
          >
            <Typography variant="body2" sx={{ mb: 1 }}>
              Please type <strong>{confirmationWord}</strong> to confirm:
            </Typography>
            <input
              type="text"
              value={inputValue}
              onChange={(e) => setInputValue(e.target.value)}
              style={{
                width: "100%",
                padding: "8px",
                border: `1px solid ${theme.palette.divider}`,
                borderRadius: "4px",
                backgroundColor: theme.palette.background.paper,
                color: theme.palette.text.primary,
              }}
            />
          </Box>
        )}
      </DialogContent>
      <DialogActions sx={{ display: "flex", justifyContent: "center", pb: 3 }}>
        <Button
          onClick={handleClose}
          color="primary"
          variant="outlined"
          sx={{
            padding: "8px 24px",
            textTransform: "none",
            fontWeight: "bold",
          }}
        >
          Cancel
        </Button>
        <Button
          onClick={() => {
            actionConfirm();
            handleClose();
          }}
          color="primary"
          variant="contained"
          disabled={confirmationWord ? inputValue !== confirmationWord : false} // Disable logic
          sx={{
            padding: "8px 24px",
            textTransform: "none",
            fontWeight: "bold",
            boxShadow: "0 4px 8px rgba(0,0,0,0.2)",
          }}
        >
          {labelConfirm}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
