import { OpenK9Client } from "@openk9/rest-api";
import { useStore } from "../state";

export const client = OpenK9Client({
  tenant: "",
});

client.addEventListener("authenticationStateChange", (state) => {
  if (state) {
    useStore.getState().setLoginInfo(state.loginInfo, state.userInfo);
  } else {
    useStore.getState().invalidateLogin();
  }
});

(async () => {
  const loginInfo = useStore.getState().loginInfo;
  if (loginInfo) {
    try {
      await client.authenticate(loginInfo);
    } catch (error) {
      console.error(error);
      useStore.getState().invalidateLogin();
    }
  }
})();
