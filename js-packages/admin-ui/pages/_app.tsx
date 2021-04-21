/*
 * Copyright (c) 2020-present SMC Treviso s.r.l. All rights reserved.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import { useEffect } from "react";
import { useRouter } from "next/router";
import { ThemeProvider } from "react-jss";
import {
  defaultTheme,
  loadPluginDepsIntoGlobal,
} from "@openk9/search-ui-components";

import "@clayui/css/lib/css/atlas.css";
import "../styles.css";
import { ClayIconSpriteContext } from "@clayui/icon";

loadPluginDepsIntoGlobal();

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
