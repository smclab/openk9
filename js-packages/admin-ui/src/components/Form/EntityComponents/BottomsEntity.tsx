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
import { Button } from "@mui/material";

export function BottomsEntity({
  controll,
  actionSave,
  actionBack,
  submit,
}: {
  controll: boolean;
  actionSave(): void;
  actionBack(): void;
  submit: boolean;
}) {
  return (
    <div
      style={{
        display: "flex",
        flexWrap: "wrap",
        justifyContent: "space-between",
      }}
    >
      <Button
        className="btn btn-secondary"
        variant="contained"
        type="button"
        onClick={() => {
          actionBack();
        }}
      >
        BACK
      </Button>
      {submit && (
        <Button
          className="btn btn-danger"
          variant="contained"
          type="button"
          disabled={!controll}
          onClick={() => {
            actionSave();
          }}
        >
          {"SAVE AND CONTINUE"}
        </Button>
      )}
    </div>
  );
}
