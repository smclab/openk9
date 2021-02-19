import { useEffect } from "react";
import { useRouter } from "next/router";
import { ThemeProvider } from "react-jss";
import { defaultTheme } from "@openk9/search-ui-components";

import "@clayui/css/lib/css/base.css";
import "../styles.css";
import { ClayIconSpriteContext } from "@clayui/icon";

export default function MyApp({ Component, pageProps }) {
  useEffect(() => {
    const style = document.getElementById("server-side-styles");
    if (style) {
      style.parentNode.removeChild(style);
    }
  });

  const { basePath } = useRouter();
  return (
    <ThemeProvider theme={defaultTheme}>
      <ClayIconSpriteContext.Provider value={basePath + "/icons.svg"}>
        <Component {...pageProps} />
      </ClayIconSpriteContext.Provider>
    </ThemeProvider>
  );
}
