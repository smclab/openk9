export const convertToInputFormat = (isoString: any) => {
  const date = new Date(isoString);
  const formattedDate = date.toISOString().slice(0, 16);
  return formattedDate;
};

export function convertToBackEndFormatData(isoString: string) {
  const date = new Date(isoString);
  return date.toISOString();
}
