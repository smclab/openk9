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
import { Box, Typography } from "@mui/material";
import React from "react";

/**
 * Permanent notice that the conversation is held with an AI system, required by
 * Regulation (EU) 2024/1689 (AI Act) art. 50 §1 for systems interacting directly
 * with people. It is deliberately static text and not an `aria-live` region: it
 * never changes, and a live region would have it re-read at every new message.
 * The input references it through `aria-describedby`, so a screen reader announces
 * it when the field takes focus.
 *
 * `text` is always resolved by the caller, so this component has no way to render
 * nothing: the disclosure is not something an integrator can turn off.
 */
export default function AiDisclosure({
  id,
  text,
}: {
  id: string;
  text: React.ReactNode;
}) {
  return (
    <Box className="openk9-ai-disclosure" sx={{ padding: "0 12px 10px" }}>
      <Typography
        id={id}
        variant="caption"
        component="p"
        // #595959 on white is 7:1, above the 4.5:1 WCAG 2.1 AA threshold, and unlike a
        // palette color it stays legible whatever `themeCustom` an integrator passes.
        sx={{
          margin: 0,
          color: "#595959",
          fontSize: "10px",
          lineHeight: 1.4,
          textAlign: "left",
        }}
      >
        {text}
      </Typography>
    </Box>
  );
}
