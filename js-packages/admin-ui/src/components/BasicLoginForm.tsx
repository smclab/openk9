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
import React, { useState } from "react";
import { Box, Button, TextField, Typography, Paper } from "@mui/material";
import { Logo } from "./common/Logo";

type BasicLoginFormProps = {
  title: string;
  onLogin: (token: string) => void;
};

export function BasicLoginForm({ title, onLogin }: BasicLoginFormProps) {
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (username && password) {
      onLogin(btoa(`${username}:${password}`));
    }
  };

  return (
    <Box
      display="flex"
      justifyContent="center"
      alignItems="center"
      minHeight="100vh"
      sx={{ backgroundColor: "background.default" }}
    >
      <Paper
        elevation={0}
        sx={{
          padding: 4,
          display: "flex",
          flexDirection: "column",
          alignItems: "center",
          gap: 2,
          width: 380,
          border: 1,
          borderColor: "divider",
        }}
      >
        <Box display="flex" alignItems="center" gap={1} mb={1}>
          <Logo size={40} />
          <Typography variant="h5" fontWeight={700} color="text.primary">
            Open
          </Typography>
          <Typography variant="h4" fontWeight={700} color="text.primary">
            K9
          </Typography>
        </Box>
        <Typography variant="h6" align="center" fontWeight="bold" color="text.primary">
          {title}
        </Typography>
        <form onSubmit={handleSubmit} style={{ display: "flex", flexDirection: "column", gap: "16px", width: "100%" }}>
          <TextField
            label="Username"
            variant="outlined"
            size="small"
            fullWidth
            value={username}
            onChange={(e) => setUsername(e.target.value)}
            required
          />
          <TextField
            label="Password"
            type="password"
            variant="outlined"
            size="small"
            fullWidth
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
          />
          <Button type="submit" variant="contained" color="primary" size="large">
            Login
          </Button>
        </form>
      </Paper>
    </Box>
  );
}
