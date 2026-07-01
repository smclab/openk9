export type HealthUiState = "success" | "down" | "unknown";

export function mapHealthStatus(status: string | undefined | null): HealthUiState {
  if (status === "UP") return "success";
  if (status === "DOWN") return "down";
  return "unknown";
}

export function extractProblemDetails(error: unknown): { title: string; detail?: string } {
  let parsed: unknown = error;
  if (typeof parsed === "string") {
    try {
      parsed = JSON.parse(parsed);
    } catch {
      parsed = { detail: parsed };
    }
  }
  if (parsed && typeof parsed === "object") {
    const p = parsed as { title?: string; message?: string; detail?: string };
    return { title: p.title ?? p.message ?? "Error", detail: p.detail };
  }
  return { title: "Error" };
}
