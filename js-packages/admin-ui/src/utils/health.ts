import { ApiError } from "../openapi-generated/core/ApiError";
import { Status } from "../openapi-generated/models/Status";

export type HealthUiState = "success" | "down" | "unknown";

export function mapHealthStatus(status: string | undefined | null): HealthUiState {
  if (status === Status.UP) return "success";
  if (status === Status.DOWN) return "down";
  return "unknown";
}

export function extractProblemDetails(error: unknown): { title: string; detail?: string } {
  if (!(error instanceof ApiError)) {
    return { title: "Error" };
  }
  let parsed: any = error.body;
  if (typeof parsed === "string") {
    try {
      parsed = JSON.parse(parsed);
    } catch {
      parsed = { detail: parsed };
    }
  }
  return {
    title: parsed?.title ?? parsed?.message ?? error.message,
    detail: parsed?.detail,
  };
}
