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

import { createUseStyles } from "react-jss";
import ClayAlert from "@clayui/alert";
import { useToast } from "../pages/_app";

const useStyles = createUseStyles(() => ({
  alert: {
    "& .alert-autofit-row": {
      alignItems: "center",
    },
  },
}));

export function Toasts() {
  const classes = useStyles();
  const { toastItems, setToastItems } = useToast();

  return (
    <ClayAlert.ToastContainer>
      {toastItems.map((value) => (
        <ClayAlert
          displayType="success"
          className={classes.alert}
          autoClose={5000}
          key={value.key}
          onClose={() => {
            setToastItems((prevItems) =>
              prevItems.filter((item) => item.key !== value.key),
            );
          }}
        >
          {value.label}
        </ClayAlert>
      ))}
    </ClayAlert.ToastContainer>
  );
}
