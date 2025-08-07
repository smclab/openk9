import React from "react";
import {
  Dialog,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
  Typography,
  useTheme,
  Box,
  IconButton,
  Slide,
} from "@mui/material";
import CheckCircleIcon from "@mui/icons-material/CheckCircle";
import CheckCircleOutlineIcon from "@mui/icons-material/CheckCircleOutline";
import SentimentVeryDissatisfiedIcon from "@mui/icons-material/SentimentVeryDissatisfied";
import CloseIcon from "@mui/icons-material/Close";

type ModalDefinition = {
  displayType: "success" | "error" | "warning" | "info";
  title: string;
  content?: React.ReactNode;
  callback?(): void;
};

const ModalContext = React.createContext<(params: ModalDefinition) => void>(null as any);

export function useModal() {
  const showModal = React.useContext(ModalContext);
  return showModal;
}

type ModalProviderProps = { children: React.ReactNode };
export function ModalProvider({ children }: ModalProviderProps) {
  const [modalItems, setModalItems] = React.useState<Array<{ key: string } & ModalDefinition>>([]);

  const addModal = React.useCallback(({ displayType, title, content }: ModalDefinition) => {
    const key = new Date().toJSON();
    setModalItems((modalItems) => [...modalItems, { key, displayType, title, content }]);
  }, []);

  const removeModal = (key: string) => {
    setModalItems((modalItems) => modalItems.filter((item) => item.key !== key));
  };

  const theme = useTheme();
  const colorMap = {
    success: theme.palette.success.main,
    error: theme.palette.error.main,
    warning: theme.palette.warning.main,
    info: theme.palette.info.main,
  };

  const iconMap = {
    success: <CheckCircleOutlineIcon fontSize="large" sx={{ fill: "white" }} />,
    error: <SentimentVeryDissatisfiedIcon fontSize="large" sx={{ fill: "white" }} />,
    warning: <CheckCircleIcon fontSize="large" sx={{ fill: "white" }} />,
    info: <CheckCircleIcon fontSize="large" sx={{ fill: "white" }} />,
  };

  return (
    <ModalContext.Provider value={addModal}>
      {children}
      {modalItems.map(({ key, displayType, title, content, callback }) => (
        <Dialog
          key={key}
          open
          fullWidth
          onClose={() => removeModal(key)}
          aria-describedby="alert-dialog-description"
          TransitionComponent={Slide}
          transitionDuration={500}
          sx={{
            "& .MuiDialog-paper": {
              overflow: "hidden",
              boxShadow: "0 10px 20px rgba(0,0,0,0.2)",
            },
          }}
        >
          <Box bgcolor={theme.palette.success.main} display="flex" justifyContent="flex-end"></Box>
          <Box
            sx={{
              backgroundColor: colorMap[displayType],
              display: "flex",
              alignItems: "center",
              justifyContent: "space-between",
              padding: "16px",
              borderBottom: "1px solid rgba(255, 255, 255, 0.1)",
            }}
          >
            <Box display="flex" alignItems="center" gap="10px">
              {iconMap[displayType]}
              <Typography variant="body2" sx={{ color: "white", fontWeight: "bold", fontSize: "1.25rem" }}>
                {title}
              </Typography>
            </Box>
            <IconButton onClick={() => removeModal(key)}>
              <CloseIcon sx={{ fill: "white" }} />
            </IconButton>
          </Box>
          {content && (
            <DialogContent sx={{ py: 2, px: 3, display: "flex", justifyContent: "center" }}>
              <DialogContentText id="alert-dialog-description" justifyContent="center">
                <Typography variant="body1" sx={{ color: theme.palette.text.primary, textAlign: "center" }}>
                  {content}
                </Typography>
              </DialogContentText>
            </DialogContent>
          )}
          <DialogActions sx={{ justifyContent: "center", pb: 3 }}>
            <Button
              onClick={() => {
                removeModal(key);
                if (callback) callback();
              }}
              color="primary"
              variant="contained"
              sx={{
                padding: "8px 24px",
                textTransform: "none",
                fontWeight: "bold",
                boxShadow: "0 4px 8px rgba(0,0,0,0.2)",
              }}
            >
              Close
            </Button>
          </DialogActions>
        </Dialog>
      ))}
    </ModalContext.Provider>
  );
}
