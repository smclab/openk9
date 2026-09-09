import { Typography } from "@mui/material";
import { useTranslation } from "react-i18next";

/**
 * id of the disclosure rendered under the input. The input points at it through
 * `aria-describedby`, so a screen reader announces it when the field takes focus.
 * Only that instance carries the id: the copy on the initial screen is rendered
 * without one, so the document never holds two nodes with the same id.
 */
export const AI_DISCLOSURE_ID = "talk-to-ai-disclosure";

const DEFAULT_AI_DISCLOSURE =
	"You are interacting with an artificial intelligence system. Answers are generated automatically and may contain errors: check important information.";

/**
 * Permanent notice that the conversation is held with an AI system, required by
 * Regulation (EU) 2024/1689 (AI Act) art. 50 §1 for systems that interact directly
 * with people. Static text on purpose and never an `aria-live` region: it does not
 * change, and a live region would have it re-read at every new message.
 */
export default function AiDisclosure({ id }: { id?: string }) {
	const { t } = useTranslation();
	return (
		<Typography
			id={id}
			variant="caption"
			component="p"
			// #595959 on the paper background is 7:1, above the 4.5:1 WCAG 2.1 AA threshold.
			sx={{ margin: 0, color: "#595959", fontSize: "11px", lineHeight: 1.4 }}
		>
			{t("ai-disclosure", { defaultValue: DEFAULT_AI_DISCLOSURE })}
		</Typography>
	);
}
