export function formatDate(value: any) {
  return value && dateTimeFormatter.format(new Date(value));
}

const dateTimeFormatter = Intl.DateTimeFormat([], {
  dateStyle: "medium",
  timeStyle: "medium",
});
