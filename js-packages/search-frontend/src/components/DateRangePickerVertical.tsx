import { t } from "i18next";
import moment from "moment";
import React from "react";
import { DateRangePicker, SingleDatePicker, SingleDatePickerInput } from "react-dates";


export function DataRangePickerVertical({
  //PROPS
  start,
  end,
}: 
{
  //PROPS TYPE
  start?: any;
  end?: any;
}) {
  const [startDate, setStartDate] = React.useState<any | null>(null);
  const [focusedStartInput, setFocusedStartInput] = React.useState(false);
  const [endDate, setEndDate] = React.useState<any | null>(null);

  const handleFocusChange = (focus: any) => {
    setFocusedStartInput(focus.focused);
  };



  return (
    <div>
      <SingleDatePicker
      date={startDate}
      numberOfMonths={1}
      onDateChange={(startDate) => setStartDate(startDate)}
      focused={focusedStartInput}
      onFocusChange={handleFocusChange}
      hideKeyboardShortcutsPanel
      id="startDate"
       />


    </div>
  );
}
