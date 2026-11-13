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
import { useNavigate } from "react-router-dom";

/** Leaves a form for the page it was opened from, or for `listPath` when it was opened from the URL. */
export function useGoBack(listPath: string) {
  const navigate = useNavigate();
  // React Router numbers the session's entries in history.state.idx, starting from 0
  const [firstIdx] = React.useState<number>(() => window.history.state?.idx ?? 0);

  return () => {
    if (firstIdx === 0) navigate(listPath, { replace: true });
    else navigate(firstIdx - 1 - window.history.state.idx);
  };
}
