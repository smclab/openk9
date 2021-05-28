/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation: string; either version 3 of the License, or
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

import ClayForm, { ClayInput } from "@clayui/form";
import clsx from "clsx";
import React from "react";
import { createUseStyles } from "react-jss";

const useStyles = createUseStyles(() => ({
  root: {
    display: "flex",
    flexWrap: "wrap",
    justifyContent: "space-between",
  },
  input: {
    width: "32%",
  },
}));

export type CronInputType = {
  minutesValue: string;
  hoursValue: string;
  daysOfMonthValue: string;
  monthValue: string;
  daysOfWeekValue: string;
  yearValue: string;
};

export function CronInput({
  schedulingValue,
  setSchedulingValue,
}: {
  schedulingValue: CronInputType;
  setSchedulingValue(c: CronInputType): void;
}) {
  const classes = useStyles();

  function handleChangeInputs(event: React.ChangeEvent<HTMLInputElement>) {
    const value = event.target.value;
    setSchedulingValue({
      ...schedulingValue,
      [event.target.name]: value,
    });
  }

  return (
    <div className={classes.root}>
      <ClayForm.Group
        className={clsx("form-group-sm date-field", classes.input)}
      >
        <label htmlFor="dataSourceMinutes">Minutes</label>
        <ClayInput
          id="dataSourceMinutes"
          name="minutesValue"
          placeholder="Insert the minutes"
          onChange={handleChangeInputs}
          value={schedulingValue.minutesValue}
          type="text"
        />
      </ClayForm.Group>

      <ClayForm.Group
        className={clsx("form-group-sm date-field", classes.input)}
      >
        <label htmlFor="dataSourceHours">Hours</label>
        <ClayInput
          id="dataSourceHours"
          name="hoursValue"
          placeholder="Insert the hours"
          onChange={handleChangeInputs}
          value={schedulingValue.hoursValue}
          type="text"
        />
      </ClayForm.Group>

      <ClayForm.Group
        className={clsx("form-group-sm date-field", classes.input)}
      >
        <label htmlFor="dataSourceDaysOfMonth">Days of Month</label>
        <ClayInput
          id="dataSourceDaysOfMonth"
          name="daysOfMonthValue"
          placeholder="Insert the days of month"
          onChange={handleChangeInputs}
          value={schedulingValue.daysOfMonthValue}
          type="text"
        />
      </ClayForm.Group>

      <ClayForm.Group
        className={clsx("form-group-sm date-field", classes.input)}
      >
        <label htmlFor="dataSourceMonth">Month</label>
        <ClayInput
          id="dataSourceMonth"
          name="monthValue"
          placeholder="Insert the month"
          onChange={handleChangeInputs}
          value={schedulingValue.monthValue}
          type="text"
        />
      </ClayForm.Group>

      <ClayForm.Group
        className={clsx("form-group-sm date-field", classes.input)}
      >
        <label htmlFor="dataSourceDaysOfWeek">Days of Week</label>
        <ClayInput
          id="dataSourceDaysOfWeek"
          name="daysOfWeekValue"
          placeholder="Insert the days of week"
          onChange={handleChangeInputs}
          value={schedulingValue.daysOfWeekValue}
          type="text"
        />
      </ClayForm.Group>

      <ClayForm.Group
        className={clsx("form-group-sm date-field", classes.input)}
      >
        <label htmlFor="dataSourceYear">Year</label>
        <ClayInput
          id="dataSourceYear"
          name="yearValue"
          placeholder="Insert the year"
          onChange={handleChangeInputs}
          value={schedulingValue.yearValue}
          type="text"
        />
      </ClayForm.Group>
    </div>
  );
}
