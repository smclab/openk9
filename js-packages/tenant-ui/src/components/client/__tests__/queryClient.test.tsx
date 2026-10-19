import { renderHook } from "@testing-library/react";
import { beforeEach, describe, expect, it, vi } from "vitest";
import { client } from "../../../openapi-generated/client.gen";
import { getAuthHeader, touchSession } from "../authStore";
import { useRestClient } from "../queryClient";

vi.mock("../authStore", () => ({ getAuthHeader: vi.fn(), touchSession: vi.fn() }));

const fetchMock = vi.fn<typeof fetch>();
// Node's Request rejects relative URLs, the browser resolves them against the page.
client.setConfig({ baseUrl: "http://localhost/api/tenant-manager", fetch: fetchMock });

const json = (status: number, body: unknown) =>
  new Response(JSON.stringify(body), { status, headers: { "Content-Type": "application/json" } });
const api = () => renderHook(() => useRestClient()).result.current.tenantProvisioning;

describe("REST client wiring", () => {
  beforeEach(() => {
    vi.mocked(getAuthHeader).mockReset().mockReturnValue(null);
    vi.mocked(touchSession).mockReset();
    fetchMock.mockReset();
  });

  it("adds the auth header, returns the data and touches the session", async () => {
    vi.mocked(getAuthHeader).mockReturnValue("Basic abc");
    fetchMock.mockResolvedValue(json(200, { id: 7, tenantName: "demo" }));
    await expect(api().createTenant({ tenantName: "demo" } as never)).resolves.toEqual({ id: 7, tenantName: "demo" });
    const request = fetchMock.mock.calls[0][0] as Request;
    expect(request.url).toBe("http://localhost/api/tenant-manager/tenant-manager/tenant");
    expect(request.headers.get("Authorization")).toBe("Basic abc");
    expect(touchSession).toHaveBeenCalledTimes(1);
  });

  it("sends no auth header without credentials", async () => {
    fetchMock.mockResolvedValue(json(200, {}));
    await api().initTenant({ tenantName: "demo" });
    expect((fetchMock.mock.calls[0][0] as Request).headers.has("Authorization")).toBe(false);
  });

  it("keeps a successful result when touchSession throws", async () => {
    vi.mocked(touchSession).mockImplementation(() => {
      throw new Error("quota");
    });
    fetchMock.mockResolvedValue(json(200, { ok: true }));
    await expect(api().createConnector({ tenantName: "demo" })).resolves.toEqual({ ok: true });
  });

  it("rejects with the status text and the server body, without touching the session", async () => {
    fetchMock.mockResolvedValue(json(400, { message: "duplicate tenant" }));
    const error = await api()
      .createTenant({ tenantName: "demo" } as never)
      .catch((e: unknown) => e);
    expect(error).toBeInstanceOf(Error);
    expect(error).toMatchObject({ message: "Bad Request", body: { message: "duplicate tenant" } });
    expect(touchSession).not.toHaveBeenCalled();
  });

  it("falls back to Generic Error for an unlisted status", async () => {
    fetchMock.mockResolvedValue(new Response("teapot", { status: 418 }));
    await expect(api().initTenant({ tenantName: "demo" })).rejects.toMatchObject({ message: "Generic Error", body: "teapot" });
  });

  it("does not wrap twice when the error interceptor is registered again", async () => {
    const [wrap] = client.interceptors.error.fns;
    const id = client.interceptors.error.use(wrap!);
    fetchMock.mockResolvedValue(json(500, { message: "boom" }));
    try {
      await expect(api().initTenant({ tenantName: "demo" })).rejects.toMatchObject({ body: { message: "boom" } });
    } finally {
      client.interceptors.error.eject(id);
    }
  });
});
