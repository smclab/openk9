/**
 * Returns the first element of an array, or a string if it's not an array.
 */
export function firstOrString(arr: string[] | string) {
  if (typeof arr === "string") return arr;
  else return arr[0];
}
