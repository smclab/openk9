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
import { Typography, TableHead, TableRow, TableCell, TableBody, Table as TableMaterial } from "@mui/material";
import React from "react";
import { Link } from "react-router-dom";

export function DashBoardTable() {
  return (
    <React.Fragment>
      <Typography
        variant="h6"
        style={{
          marginLeft: "10px",
        }}
      >
        Recent Ingestion Activities:
      </Typography>
      <TableMaterial>
        <TableHead style={{ background: "white" }}>
          <TableRow>
            <TableCell>Request Id</TableCell>
            <TableCell>Created</TableCell>
            <TableCell>Crawl type</TableCell>
            <TableCell>Domains</TableCell>
            <TableCell>Status</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          <TableRow>
            <TableCell>
              <Link to="#1">FirstId</Link>
            </TableCell>
            <TableCell>22/03/2022</TableCell>
            <TableCell>String</TableCell>
            <TableCell>Openk9.prova.it</TableCell>
            <TableCell>Running</TableCell>
          </TableRow>
          <TableRow>
            <TableCell>
              <Link to="#1">SecondId</Link>
            </TableCell>
            <TableCell>25/03/2022</TableCell>
            <TableCell>Int</TableCell>
            <TableCell>Openk9.prova.it</TableCell>
            <TableCell>Running</TableCell>
          </TableRow>
          <TableRow>
            <TableCell>
              <Link to="#1">aszsfesees</Link>
            </TableCell>
            <TableCell>22/11/2022</TableCell>
            <TableCell>String</TableCell>
            <TableCell>Openk9.prova.it</TableCell>
            <TableCell>Running</TableCell>
          </TableRow>
        </TableBody>
      </TableMaterial>
    </React.Fragment>
  );
}

