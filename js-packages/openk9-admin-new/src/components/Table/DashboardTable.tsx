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
