export const convertToInputFormat = (isoString: string | null | undefined) => {
  const date = new Date(isoString ?? 0);
  const pad = (value: number) => String(value).padStart(2, "0");
  const day = `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}`;
  const time = `${pad(date.getHours())}:${pad(date.getMinutes())}`;
  return `${day}T${time}`;
};

export function convertToBackEndFormatData(isoString: string) {
  const date = new Date(isoString);
  return date.toISOString();
}
