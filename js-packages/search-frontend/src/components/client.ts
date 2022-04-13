import { ClientAuthenticationState, OpenK9Client } from "@openk9/rest-api";

export const client = OpenK9Client({
  tenant: window.location.origin,
  onAuthenticationStateChange(state) {
    for (const listener of clientAuthenticationChangeListeners) {
      listener(state);
    }
  },
});

export const clientAuthenticationChangeListeners = new Set<
  (state: ClientAuthenticationState) => void
>();
