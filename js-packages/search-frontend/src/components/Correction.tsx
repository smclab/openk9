import { useRange } from "./useRange";

type CorrectionFunction = {
  information: (
    correctedQuery: string,
    errorQuery: string,
    confirm: () => void,
  ) => React.ReactNode;
  setSearch: (newSearch: string) => void;
};
export default function Correction({
  information,
  setSearch,
}: CorrectionFunction) {
  const { correction } = useRange();
  if (!correction || !correction.autocorrectionText) return null;
  return information(
    correction?.autocorrectionText || "",
    correction?.originalText || "",
    () => {
      setSearch(correction?.autocorrectionText || "");
    },
  );
}
