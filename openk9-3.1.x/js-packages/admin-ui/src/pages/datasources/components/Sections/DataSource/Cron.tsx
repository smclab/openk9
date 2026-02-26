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
import CalendarTodayIcon from "@mui/icons-material/CalendarToday";
import DateRangeIcon from "@mui/icons-material/DateRange";
import EventIcon from "@mui/icons-material/Event";
import TimerIcon from "@mui/icons-material/Timer";
import ViewWeekIcon from "@mui/icons-material/ViewWeek";
import {
  Box,
  Button,
  Card,
  CardContent,
  Chip,
  Divider,
  InputAdornment,
  Slider,
  Stack,
  TextField,
  Tooltip,
  Typography,
  useTheme,
} from "@mui/material";
import cronstrue from "cronstrue";
import React, { useEffect, useState } from "react";
import { useToast } from "../../../../../components/Form/Form/ToastProvider";
import { ConnectionData } from "../../../types";

interface CronValues {
  [key: string]: string;
}

interface Suggestion {
  value: string;
  label: string;
  description: string;
}

type CronFieldType = "Minute" | "Hour" | "DayOfMonth" | "Month" | "DayOfWeek";

interface FieldIcon {
  icon: React.ReactNode;
  tooltip: string;
}

const fieldIcons = (isPurge: boolean): Record<string, { icon: React.ReactNode; tooltip: string }> => {
  return {
    Minute: { icon: <TimerIcon />, tooltip: "Minutes (0-59)" },
    Hour: { icon: <TimerIcon />, tooltip: "Hours (0-23)" },
    DayOfMonth: { icon: <CalendarTodayIcon />, tooltip: "Days of Month (1-31)" },
    Month: { icon: <DateRangeIcon />, tooltip: "Months (1-12)" },
    DayOfWeek: { icon: <ViewWeekIcon />, tooltip: "Days of Week (1-7)" },
    ...(isPurge ? { maxPurgeAge: { icon: <EventIcon />, tooltip: "Max Purge Age" } } : {}),
  };
};

interface SpecificSuggestion {
  value: string | number;
  label: string;
}

const fieldLimits: Record<CronFieldType, { min: number; max: number }> = {
  Minute: { min: 0, max: 59 },
  Hour: { min: 0, max: 23 },
  DayOfMonth: { min: 1, max: 31 },
  Month: { min: 1, max: 12 },
  DayOfWeek: { min: 1, max: 7 },
};

const getDaysInMonth = (month: number, year: number = new Date().getFullYear()): number => {
  // Per febbraio, controlliamo se Ã¨ un anno bisestile
  if (month === 2) {
    return (year % 4 === 0 && year % 100 !== 0) || year % 400 === 0 ? 29 : 28;
  }
  // Per gli altri mesi
  return [4, 6, 9, 11].includes(month) ? 30 : 31;
};

const GridSelector: React.FC<{
  values: number[];
  selectedValue: string;
  onSelect: (value: string) => void;
  columns?: number;
}> = ({ values, selectedValue, onSelect, columns = 10 }) => {
  return (
    <Box
      sx={{
        display: "grid",
        gridTemplateColumns: `repeat(${columns}, 1fr)`,
        gap: 0.5,
        width: "100%",
      }}
    >
      {values.map((value) => (
        <Button
          key={value}
          variant={selectedValue === value.toString() ? "contained" : "outlined"}
          size="small"
          onClick={() => onSelect(value.toString())}
          sx={{
            minWidth: 0,
            p: 0.5,
            height: "28px",
            fontSize: "0.75rem",
          }}
        >
          {value.toString().padStart(2, "0")}
        </Button>
      ))}
    </Box>
  );
};

const SliderTimeSelector: React.FC<{
  value: string;
  onChange: (value: string) => void;
  max: number;
  label: string;
}> = ({ value, onChange, max, label }) => {
  const [sliderValue, setSliderValue] = useState(parseInt(value) || 0);

  const getMarks = () => {
    if (max === 59) {
      return [0, 15, 30, 45, 59].map((v) => ({
        value: v,
        label: v.toString().padStart(2, "0"),
      }));
    } else {
      return [0, 3, 6, 9, 12, 15, 18, 21, 23].map((v) => ({
        value: v,
        label: v.toString().padStart(2, "0"),
      }));
    }
  };

  const displayValue = (val: number) => {
    return val.toString().padStart(2, "0");
  };

  return (
    <Box sx={{ width: "100%", px: 2 }}>
      <Box sx={{ display: "flex", alignItems: "center", gap: 2, mb: 2 }}>
        <TextField
          size="small"
          value={sliderValue.toString().padStart(2, "0")}
          onChange={(e) => {
            let val = parseInt(e.target.value);
            if (!isNaN(val) && val >= 0 && val <= max) {
              setSliderValue(val);
              onChange(val.toString());
            }
          }}
          sx={{ width: 70 }}
          inputProps={{
            min: 0,
            max: 23,
            style: { textAlign: "center" },
          }}
        />
        <Typography>{label}</Typography>
      </Box>
      <Slider
        value={sliderValue}
        onChange={(_, newValue) => {
          setSliderValue(newValue as number);
          onChange((newValue as number).toString());
        }}
        min={0}
        max={max}
        marks={getMarks()}
        valueLabelDisplay="auto"
        valueLabelFormat={displayValue}
      />
    </Box>
  );
};

interface FieldMappings {
  hourKey: keyof CronValues;
  minuteKey: keyof CronValues;
  monthKey: keyof CronValues;
  dayOfMonthKey: keyof CronValues;
  dayOfWeekKey: keyof CronValues;
  maxPurgeAgeKey?: keyof CronValues;
}

interface CronEditorProps {
  title: string;
  dataDatasource: ConnectionData;
  setDataDatasource: React.Dispatch<React.SetStateAction<ConnectionData>>;
  isActive: boolean;
  isView: boolean;
  fieldMappings: FieldMappings;
  // onChangeData: ({ type, DayOfMonth, DayOfWeek, Hour, Minute, Month, isEnabled }: CronDataManagement) => void;
}

const CronEditor: React.FC<CronEditorProps> = ({
  title,
  dataDatasource,
  setDataDatasource,
  isActive,
  fieldMappings,
  isView,
  // onChangeData,
}) => {
  const getCronFromType = () => {
    const defaultCronValues = {
      reindex: "0 0 1 * * ?",
      scheduling: "0 */30 * ? * * *",
      purge: "0 0 1 * * ?",
    };

    const type = title.toLowerCase() as keyof typeof defaultCronValues;
    const cronString =
      type === "reindex"
        ? dataDatasource?.reindexing || defaultCronValues.reindex
        : type === "scheduling"
        ? dataDatasource?.scheduling || defaultCronValues.scheduling
        : dataDatasource?.purging || defaultCronValues.purge;

    const [seconds, minute, hour, dayOfMonth, month, dayOfWeek] = cronString.split(" ");
    setCronValues((prev) => ({
      ...prev,
      Minute: minute,
      Hour: hour,
      DayOfMonth: dayOfMonth,
      Month: month,
      DayOfWeek: dayOfWeek,
      ...(type === "purge" ? { maxPurgeAge: dataDatasource?.purgeMaxAge || "2d" } : {}),
    }));

    return cronString;
  };

  const [cronExpression, setCronExpression] = useState<string>("");
  const [cronValues, setCronValues] = useState<CronValues>({
    // Second: "0",
    Minute: "",
    Hour: "",
    DayOfMonth: "",
    Month: "",
    DayOfWeek: "",
  });

  const [selectedField, setSelectedField] = useState<CronFieldType | "maxPurgeAge">("Minute");

  const [fieldValue, setFieldValue] = useState<string>("*");

  const suggestions: Record<CronFieldType | "maxPurgeAge", Suggestion[]> = {
    Minute: [
      { value: "*", label: "Every minute", description: "Run every minute" },
      { value: "*/5", label: "Every 5 min", description: "Run every 5 minutes (0, 5, 10, ...)" },
      { value: "*/15", label: "Every 15 min", description: "Run every 15 minutes (0, 15, 30, 45)" },
      { value: "*/30", label: "Every 30 min", description: "Run every 30 minutes (0, 30)" },
      { value: "0", label: "At minute 0", description: "Run at the start of every hour" },
      { value: "0,30", label: "Minutes 0 and 30", description: "Run at minutes 0 and 30 of every hour" },
    ],
    Hour: [
      { value: "*", label: "Every hour", description: "Run every hour" },
      { value: "*/2", label: "Every 2 hours", description: "Run every 2 hours" },
      { value: "*/6", label: "Every 6 hours", description: "Run every 6 hours (0, 6, 12, 18)" },
      { value: "9-17", label: "Working hours", description: "Run during working hours (9-17)" },
      { value: "0", label: "At midnight", description: "Run at midnight (00:00)" },
      { value: "12", label: "At noon", description: "Run at noon (12:00)" },
    ],
    DayOfMonth: [
      { value: "*", label: "Every day", description: "Run every day of the month" },
      { value: "1", label: "1st of month", description: "Run on the first day of every month" },
      { value: "15", label: "15th of month", description: "Run on the 15th day of every month" },
      { value: "L", label: "Last day", description: "Run on the last day of every month" },
      { value: "1-5", label: "Days 1-5", description: "Run on the first 5 days of the month" },
    ],
    Month: [
      { value: "*", label: "Every month", description: "Run every month" },
      { value: "1,4,7,10", label: "Quarterly", description: "Run every three months (Jan, Apr, Jul, Oct)" },
      { value: "1-6", label: "First half", description: "Run from January to June" },
      { value: "7-12", label: "Second half", description: "Run from July to December" },
    ],
    DayOfWeek: [
      { value: "*", label: "Every day", description: "Run every day of the week" },
      { value: "2-6", label: "Mon-Fri", description: "Run from Monday to Friday" },
      { value: "1,7", label: "Weekend", description: "Run on Saturday and Sunday" },
      { value: "2", label: "Monday", description: "Run only on Monday" },
    ],
    maxPurgeAge: [
      { value: "1d", label: "1 days", description: "Purge data older than 1 day" },
      { value: "2d", label: "2 days", description: "Purge data older than 2 days" },
      { value: "3d", label: "3 days", description: "Purge data older than 3 days" },
      { value: "7d", label: "1 week", description: "Purge data older than 7 days, 1 week" },
      { value: "15d", label: "15 days", description: "Purge data older than 15 days" },
      { value: "30d", label: "30 days", description: "Purge data older than 30 days" },
    ],
  };

  const fieldLabels: Record<CronFieldType, string> = {
    Minute: "Minutes (0-59)",
    Hour: "Hours (0-23)",
    DayOfMonth: "Days of Month (1-31)",
    Month: "Months (1-12)",
    DayOfWeek: "Days of Week (1-7)",
    ...(title.toLowerCase() === "purge"
      ? {
          maxPurgeAge: "Max Purge Age",
        }
      : {}),
  };

  useEffect(() => {
    setFieldValue(cronValues[selectedField]);
  }, [selectedField, cronValues]);

  const handleFieldChange = (field: string, value: string) => {
    setCronValues((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const validateFieldValue = (value: string): boolean => {
    if (
      value === "*" ||
      value.includes(",") ||
      value.includes("-") ||
      value.includes("/") ||
      value.includes("?") ||
      selectedField === "maxPurgeAge"
    ) {
      return true;
    }

    const numValue = parseInt(value);
    const limits = fieldLimits[selectedField as CronFieldType];
    return !isNaN(numValue) && numValue >= limits.min && numValue <= limits.max;
  };

  const applyValue = () => {
    setCronValues((prev) => ({
      ...prev,
      [selectedField]: fieldValue,
    }));
  };

  const handleKeyDown = (event: React.KeyboardEvent) => {
    if (event.key === "Enter") {
      applyValue();
    }
  };

  const applySuggestion = (value: string) => {
    setFieldValue(value);
    setCronValues((prev) => ({
      ...prev,
      [selectedField]: value,
    }));
  };

  const getReadableCronDescription = (): string => {
    try {
      const fullCronExpression = `${cronExpression}`;
      return cronstrue.toString(fullCronExpression);
    } catch (error) {
      return "Invalid cron expression";
    }
  };

  useEffect(() => {
    if (cronValues.Month !== "*" && cronValues.DayOfMonth !== "*") {
      const selectedMonth = parseInt(cronValues.Month);
      const selectedDay = parseInt(cronValues.DayOfMonth);
      const maxDays = getDaysInMonth(selectedMonth);

      if (selectedDay > maxDays) {
        setCronValues((prev) => ({
          ...prev,
          DayOfMonth: "*",
        }));
      }
    }
  }, [cronValues.Month]);

  const renderSpecificSelector = () => {
    switch (selectedField) {
      case "Minute":
        return (
          <Box>
            <Typography variant="subtitle2" gutterBottom>
              Select minute:
            </Typography>
            <SliderTimeSelector value={fieldValue} onChange={applySuggestion} max={59} label="minutes" />
          </Box>
        );
      case "Hour":
        return (
          <Box>
            <Typography variant="subtitle2" gutterBottom>
              Select hour:
            </Typography>
            <SliderTimeSelector value={fieldValue} onChange={applySuggestion} max={23} label="hours" />
          </Box>
        );
      case "DayOfMonth":
        return (
          <Box>
            <Typography variant="subtitle2" gutterBottom>
              Select day:
            </Typography>
            <GridSelector
              values={Array.from(
                { length: getDaysInMonth(cronValues.Month !== "*" ? parseInt(cronValues.Month) : 31) },
                (_, i) => i + 1,
              )}
              selectedValue={fieldValue}
              onSelect={applySuggestion}
              columns={7}
            />
          </Box>
        );
      case "Month":
        return (
          <Box>
            <Typography variant="subtitle2" gutterBottom>
              Select month:
            </Typography>
            <GridSelector
              values={[1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12]}
              selectedValue={fieldValue}
              onSelect={applySuggestion}
              columns={4}
            />
          </Box>
        );
      case "DayOfWeek":
        return (
          <Box>
            <Typography variant="subtitle2" gutterBottom>
              Select day of the week:
            </Typography>
            <GridSelector
              values={[1, 2, 3, 4, 5, 6, 7]} // 1 = Sunday, 7 = Saturday
              selectedValue={fieldValue}
              onSelect={applySuggestion}
              columns={7}
            />
          </Box>
        );
    }
  };
  const theme = useTheme();
  const isDark = theme.palette.mode === "dark";

  useEffect(() => {
    if (dataDatasource) {
      const newCronString = getCronFromType();
      setCronExpression(newCronString);
    }
  }, [dataDatasource?.reindexing, dataDatasource?.scheduling, dataDatasource?.purging]);

  const showToast = useToast();

  const validateCronExpression = (cronExp: string): boolean => {
    try {
      // Verifica se l'espressione cron Ã¨ valida usando cronstrue
      cronstrue.toString(cronExp);
      return true;
    } catch (error) {
      return false;
    }
  };

  const handleSave = () => {
    // Check if both DayOfMonth and DayOfWeek are specified
    if (
      cronValues.DayOfMonth !== "*" &&
      cronValues.DayOfWeek !== "*" &&
      cronValues.DayOfMonth !== "?" &&
      cronValues.DayOfWeek !== "?"
    ) {
      showToast({
        displayType: "error",
        title: "Invalid Cron Expression",
        content: "Cannot specify both Day of Month and Day of Week. Please specify only one.",
      });
      return;
    }

    // Determine the adjusted values based on which field is specified
    const { adjustedDayOfMonth, adjustedDayOfWeek } = (() => {
      if (cronValues.DayOfWeek !== "*" && cronValues.DayOfWeek !== "?") {
        return {
          adjustedDayOfMonth: "?",
          adjustedDayOfWeek: cronValues.DayOfWeek,
        };
      }
      if (cronValues.DayOfMonth !== "*" && cronValues.DayOfMonth !== "?") {
        return {
          adjustedDayOfMonth: cronValues.DayOfMonth,
          adjustedDayOfWeek: "?",
        };
      }
      return {
        adjustedDayOfMonth: cronValues.DayOfMonth,
        adjustedDayOfWeek: cronValues.DayOfWeek,
      };
    })();

    // Add leading '0' for seconds when creating the cron expression
    const newCronExpression = `0 ${cronValues.Minute} ${cronValues.Hour} ${adjustedDayOfMonth} ${cronValues.Month} ${adjustedDayOfWeek}`;

    if (!validateCronExpression(newCronExpression)) {
      showToast({
        displayType: "error",
        title: "Invalid Cron Expression",
        content: "Please check your cron expression values and try again.",
      });
      return;
    }

    setCronExpression(newCronExpression);

    setDataDatasource((prev) => ({
      ...prev,
      scheduling: title.toLowerCase() === "scheduling" ? newCronExpression : prev.scheduling,
      reindexing: title.toLowerCase() === "reindex" ? newCronExpression : prev.reindexing,
      purging: title.toLowerCase() === "purge" ? newCronExpression : prev.purging,
      ...(title.toLowerCase() === "purge" ? { purgeMaxAge: cronValues.maxPurgeAge } : {}),
    }));

    showToast({
      displayType: "success",
      title: "Success",
      content: `${title} configuration has been saved successfully.`,
    });
  };

  return (
    <Card>
      <CardContent>
        <Typography variant="h5" gutterBottom>
          {title} - Task Scheduler
        </Typography>

        <Box sx={{ mt: 3, mb: 4, opacity: isView || isActive ? 1 : 0.6 }}>
          <Box sx={{ display: "flex", alignItems: "flex-start", gap: 1 }}>
            <Stack direction="row" spacing={1}>
              {Object.entries(fieldIcons(title.toLowerCase() === "purge")).map(([field, { icon, tooltip }]) => (
                <Tooltip key={field} title={tooltip}>
                  <Button
                    variant={selectedField === field ? "contained" : "outlined"}
                    size="small"
                    onClick={() => setSelectedField(field as CronFieldType)}
                    disabled={isView || !isActive}
                    sx={{
                      minWidth: "auto",
                      height: "40px",
                      transition: "all 0.3s ease",
                      width: selectedField === field ? "auto" : "40px",
                      padding: selectedField === field ? "6px 16px" : "8px",
                      "& .MuiButton-startIcon": {
                        margin: selectedField === field ? "0 8px 0 0" : 0,
                        transition: "margin 0.3s ease",
                      },
                      "& .MuiSvgIcon-root": {
                        fontSize: "20px",
                      },
                      whiteSpace: "nowrap",
                      overflow: "hidden",
                    }}
                    startIcon={icon}
                  >
                    {selectedField === field && fieldLabels[field as CronFieldType]}
                  </Button>
                </Tooltip>
              ))}
            </Stack>

            <Box flex={1} sx={{ display: "flex", alignItems: "center", gap: 2 }}>
              <TextField
                value={fieldValue}
                onChange={(event) => {
                  const newValue = event.target?.value;
                  if (newValue === "" || validateFieldValue(newValue)) {
                    setFieldValue(newValue);
                    handleFieldChange(selectedField, newValue);
                  }
                }}
                onKeyDown={handleKeyDown}
                size="small"
                fullWidth
                disabled={isView || !isActive}
                InputProps={{
                  startAdornment: (
                    <InputAdornment position="start">
                      {fieldIcons(title.toLowerCase() === "purge")[selectedField].icon}
                    </InputAdornment>
                  ),
                }}
                sx={{ flex: 1 }}
              />
            </Box>
          </Box>

          <Box sx={{ mt: 2 }}>
            <Stack direction="row" spacing={2}>
              <Box flex={1}>
                <Typography variant="subtitle2" gutterBottom>
                  Generic suggestions:
                </Typography>
                <Stack direction="row" spacing={1} sx={{ flexWrap: "wrap", gap: 1 }}>
                  {suggestions[selectedField].map((suggestion) => (
                    <Tooltip key={suggestion.value} title={suggestion.description} arrow placement="top">
                      <Chip
                        label={suggestion.label}
                        onClick={() => applySuggestion(suggestion.value)}
                        variant="outlined"
                        color={fieldValue === suggestion.value ? "primary" : "default"}
                        size="small"
                        disabled={isView || !isActive}
                      />
                    </Tooltip>
                  ))}
                </Stack>
              </Box>

              <Box flex={1}>
                <Typography variant="subtitle2" gutterBottom>
                  {title.toLowerCase() === "purge" ? "" : "Specific values:"}
                </Typography>
                <Box
                  sx={{
                    opacity: !isView && isActive ? 1 : 0.7,
                    pointerEvents: !isView && isActive ? "auto" : "none",
                  }}
                >
                  {renderSpecificSelector()}
                </Box>
              </Box>
            </Stack>
          </Box>
        </Box>

        <Box sx={{ display: "flex", alignItems: "center", gap: 2, mb: 3 }}>
          <Typography variant="subtitle1" sx={{ opacity: isView || isActive ? 1 : 0.7 }}>
            Cron Expression:
          </Typography>
          <Box
            sx={{
              display: "flex",
              flexWrap: "wrap",
              gap: 1,
              flexGrow: 1,
              p: 1,
              bgcolor: isDark ? "rgba(255, 255, 255, 0.05)" : "grey.100",
              border: isDark ? "1px solid rgba(255, 255, 255, 0.12)" : "1px solid rgba(0, 0, 0, 0.12)",
              borderRadius: 1,
            }}
          >
            {Object.entries(cronValues).map(([key, value], index) => (
              <Chip
                key={key}
                label={`${value}`}
                color={selectedField === key ? "primary" : "default"}
                onClick={() => setSelectedField(key as CronFieldType)}
                size="small"
                variant={selectedField === key ? "filled" : "outlined"}
                disabled={isView || !isActive}
              />
            ))}
          </Box>
        </Box>

        <Box
          sx={{
            p: 2,
            bgcolor: isDark ? "rgba(255, 255, 255, 0.05)" : "grey.100",
            border: isDark ? "1px solid rgba(255, 255, 255, 0.12)" : "1px solid rgba(0, 0, 0, 0.12)",
            borderRadius: 1,
            mb: 3,
          }}
        >
          <Typography variant="subtitle1" gutterBottom>
            Complete Cron Expression:
          </Typography>
          <Typography variant="body1" sx={{ fontFamily: "monospace", fontSize: "1.1rem" }}>
            {cronExpression}{" "}
          </Typography>
          <Typography variant="body1" sx={{ fontFamily: "monospace", fontSize: "1.1rem", mt: 2, mb: 2 }}>
            {title.toLowerCase() === "purge" && cronValues.maxPurgeAge && `Max Purge Age: ${cronValues.maxPurgeAge}`}
          </Typography>
          <Divider />
          <Typography variant="subtitle1" sx={{ mt: 2 }} gutterBottom>
            In simple words:
          </Typography>
          <Typography variant="body1">{getReadableCronDescription()}</Typography>
        </Box>

        <Box sx={{ display: "flex", justifyContent: "flex-end", mt: 3, gap: 2 }}>
          <Button disabled={isView} variant="contained" color="primary" onClick={handleSave}>
            SAVE
          </Button>
        </Box>
      </CardContent>
    </Card>
  );
};

export default CronEditor;

