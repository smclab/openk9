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
import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  IconButton,
  Slide,
  Typography,
} from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";

type PropsModalConfirmRadio = {
  title: string;
  callbackConfirm(): void;
  callbackClose(): void;
  message: string;
};
export function ModalConfirmRadio({ title, callbackConfirm, callbackClose, message }: PropsModalConfirmRadio) {
  return (
    <Dialog
      open
      fullWidth={true}
      onClose={() => callbackClose()}
      aria-describedby="alert-dialog-description"
      TransitionComponent={Slide}
      transitionDuration={500}
      sx={{
        "& .MuiDialog-paper": {
          borderRadius: 4,
          overflow: "hidden",
          boxShadow: "0 10px 20px rgba(0,0,0,0.2)",
        },
      }}
    >
      <Box
        sx={{
          background: "linear-gradient(90deg, #1976D2 0%, #BBDEFB 100%)",
          display: "flex",
          alignItems: "center",
          justifyContent: "space-between",
          padding: "16px",
          borderBottom: "1px solid rgba(255, 255, 255, 0.1)",
        }}
      >
        <Box display="flex" alignItems="center" gap="10px">
          <Typography variant="body2" sx={{ color: "white", fontWeight: "bold", fontSize: "1.25rem" }}>
            {title}
          </Typography>
        </Box>
        <IconButton onClick={() => callbackClose()}>
          <CloseIcon sx={{ fill: "white" }} />
        </IconButton>
      </Box>
      <DialogContent sx={{ py: 2, px: 3 }}>
        <DialogContentText id="alert-dialog-description">
          <Typography variant="body1">{message}</Typography>
        </DialogContentText>
      </DialogContent>
      <DialogActions sx={{ justifyContent: "end", pb: 3 }}>
        <Button
          onClick={() => {
            callbackClose();
          }}
          color="primary"
          variant="outlined"
          sx={{
            borderRadius: "20px",
            padding: "8px 24px",
            textTransform: "none",
            fontWeight: "bold",
            boxShadow: "0 4px 8px rgba(0,0,0,0.2)",
          }}
        >
          Cancel
        </Button>
        <Button
          onClick={() => {
            callbackConfirm();
          }}
          color="primary"
          variant="outlined"
          sx={{
            borderRadius: "20px",
            padding: "8px 24px",
            textTransform: "none",
            fontWeight: "bold",
            boxShadow: "0 4px 8px rgba(0,0,0,0.2)",
          }}
        >
          Ok
        </Button>
      </DialogActions>
    </Dialog>
  );
}

