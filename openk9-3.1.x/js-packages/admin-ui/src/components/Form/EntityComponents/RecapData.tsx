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
import { Stack, Box, Typography, TextField, Paper, Button } from "@mui/material";
import React from "react";
import { useNavigate } from "react-router-dom";
import { ContainerFluid } from "../Containers";

export function RecapData({
  form,
  Data,
  page,
  setPage,
  submit,
  pathBack,
  preSubmit,
  associateOneToOne,
  multiAssociation,
  isCreate = true,
}: {
  form: any;
  page: number;
  isCreate?: boolean;
  submit: boolean;
  pathBack: string;
  preSubmit?: React.ReactNode;
  multiAssociation:
    | {
        labelName: string;
        items: {
          label: string;
          value: string;
        }[][];
        setItems: React.Dispatch<
          React.SetStateAction<
            {
              label: string;
              value: string;
            }[][]
          >
        >;
        selectValue?:
          | {
              value: string;
              label: string;
            }[]
          | undefined;
        onchange?: React.Dispatch<React.SetStateAction<never[]>> | undefined;
        level: number;
      }[]
    | undefined;
  associateOneToOne:
    | {
        labelName: string;
        multiAssociation: boolean;
        options?:
          | {
              value: string | null | undefined;
              label: string | null | undefined;
            }[]
          | undefined;
        defaultSelect?:
          | {
              value: string | null | undefined;
              label: string | null | undefined;
            }
          | undefined;
        onchange?:
          | React.Dispatch<
              React.SetStateAction<{
                value: string | null | undefined;
                label: string | null | undefined;
              }>
            >
          | undefined;
        level: number;
      }[]
    | undefined;
  setPage: React.Dispatch<React.SetStateAction<number>>;
  Data: Array<{
    content?: React.ReactElement;
    page?: number;
    validation?: boolean;
  }>;
}) {
  const navigate = useNavigate();
  const allData = Data.map((dat) => dat.content);
  const [viewPreSubmit, setViewPreSubmit] = React.useState(false);

  const disableChildren = (element: React.ReactNode): React.ReactNode => {
    if (!React.isValidElement(element)) return element;

    return React.cloneElement(
      element as any,
      { disabled: true, readOnly: true },
      element.props.children ? React.Children.map(element.props.children, disableChildren) : element.props.children,
    );
  };

  return (
    <React.Fragment>
      <Stack spacing={2} sx={{ alignItems: "flex-start" }}>
        {viewPreSubmit && preSubmit}
        {React.Children.map(allData, (child, index) => {
          if (!React.isValidElement(child)) return null;
          return disableChildren(child);
        })}
        {associateOneToOne?.map((associate, index) => (
          <Box key={"association-one-to-one" + index} pb={2}>
            <Typography variant="subtitle1">{associate.labelName}</Typography>
            <TextField variant="outlined" value={associate.defaultSelect?.label || ""} disabled fullWidth />
          </Box>
        ))}
        {multiAssociation?.map((associate, index) => (
          <Box key={"multi-association-" + index} pb={2}>
            <Typography variant="subtitle1">{associate.labelName}</Typography>
            <Paper
              variant="outlined"
              sx={{
                borderColor: "#f1f2f5",
                backgroundColor: "#f1f2f5",
                color: "#a7a9bc",
                p: 2,
                display: "flex",
                flexDirection: "column",
                gap: 2,
              }}
            >
              {associate?.items[0]?.map((item, index) => (
                <Typography key={"multi-associate" + index}>{item.label}</Typography>
              ))}
            </Paper>
          </Box>
        ))}
        <ContainerFluid size="md">
          <Box
            sx={{
              display: "flex",
              flexWrap: "wrap",
              justifyContent: "space-between",
            }}
            style={{ marginBlock: "16px" }}
          >
            <Button
              variant="outlined"
              color="primary"
              onClick={() => {
                if (page === 0) {
                  navigate(pathBack, { replace: true });
                } else {
                  setPage((p) => p - 1);
                }
              }}
            >
              BACK
            </Button>
            {submit && (
              <Button
                variant="contained"
                color="primary"
                onClick={() => {
                  if (preSubmit) {
                    setViewPreSubmit(true);
                  } else {
                    form.submit();
                  }
                }}
              >
                {isCreate ? "Create entity" : "Update entity"}
              </Button>
            )}
          </Box>
        </ContainerFluid>
      </Stack>
    </React.Fragment>
  );
}

