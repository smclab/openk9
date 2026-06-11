import React from "react";
import { QueryClient } from "@tanstack/react-query";
import { OpenApiRestClient } from "../../openapi-generated";
import type { ApiRequestOptions } from "../../openapi-generated/core/ApiRequestOptions";
import type { CancelablePromise } from "../../openapi-generated/core/CancelablePromise";
import { FetchHttpRequest } from "../../openapi-generated/core/FetchHttpRequest";
import { getAuthHeader, touchSession } from "./authStore";

class ActivityHttpRequest extends FetchHttpRequest {
  public override request<T>(options: ApiRequestOptions): CancelablePromise<T> {
    const promise = super.request<T>(options);
    promise.then(
      () => touchSession(),
      () => {}
    );
    return promise;
  }
}

export const queryClient = new QueryClient();
const RestClientContext = React.createContext(
  new OpenApiRestClient(
    {
      // The OpenAPI spec produced at build time carries bare paths,
      // so prepend it here.
      BASE: "/api/tenant-manager",
      async HEADERS() {
        const authHeader = getAuthHeader();
        if (authHeader) {
          return { Authorization: authHeader };
        }
        return {} as any;
      },
    },
    ActivityHttpRequest
  )
);
export function useRestClient() {
  return React.useContext(RestClientContext);
}
