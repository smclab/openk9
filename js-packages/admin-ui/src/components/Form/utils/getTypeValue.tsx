export const getTypeValue = (jsonData: string | null | undefined) => {
  if (!jsonData) return null;
  try {
    const data = JSON.parse(jsonData);
    if (data.type) {
      return data.type;
    } else {
      return null;
    }
  } catch (error) {
    return null;
  }
};
