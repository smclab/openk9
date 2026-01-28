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
import React from "react";

export function TotalResults({
  totalResult,
  saveTotalResultState,
}: {
  totalResult: number | null;
  saveTotalResultState?: React.Dispatch<React.SetStateAction<number | null>>;
}) {
  if (saveTotalResultState && totalResult!==null) {
    saveTotalResultState(totalResult);
  } else {
    if (saveTotalResultState) {
      saveTotalResultState(0);
    }
  }

  return <div className="openk9-totalResults-container">{totalResult || 0}</div>;
}

