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

import React, {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { useRouter } from "next/router";
import { ThemeProvider as ThemeProviderReact18Fix } from "react-jss";
import { ClayIconSpriteContext } from "@clayui/icon";
import "@clayui/css/lib/css/atlas.css";
import "@clayui/css/lib/css/base.css";
import "../styles.css";
import { Toasts } from "../components/Toasts";
import { isServer, useLoginRedirect } from "../state";
import { defaultTheme } from "../components/theme";
import useSWR from "swr";

const ThemeProvider = ThemeProviderReact18Fix as any;

const ToastContext = createContext<{
  pushToast(label: string): void;
  toastItems: { label: string; key: string }[];
  setToastItems: React.Dispatch<
    React.SetStateAction<
      {
        label: string;
        key: string;
      }[]
    >
  >;
} | null>(null);

export function useToast() {
  const ctxValue = useContext(ToastContext);
  if (!ctxValue) {
    throw new Error(
      "Error! You are trying to use Toast without its context! <Layout/> provides that.",
    );
  }

  return ctxValue;
}

export default function MyApp({ Component, pageProps }: any) {
  useSWR("@openk9/search-frontend", () => import("@openk9/search-frontend"));

  useEffect(() => {
    const style = document.getElementById("server-side-styles");
    if (style && style.parentNode) {
      style.parentNode.removeChild(style);
    }
  });

  const [toastItems, setToastItems] = useState<
    { label: string; key: string }[]
  >([]);

  const toastContextValue = useMemo(
    () => ({
      pushToast(label: string) {
        setToastItems((ts) => [
          ...ts,
          { label, key: Math.random().toFixed(5) },
        ]);
      },
      toastItems,
      setToastItems,
    }),
    [toastItems],
  );

  const { basePath } = useRouter();

  useLoginRedirect();

  return (
    <ToastContext.Provider value={toastContextValue}>
      <ThemeProvider theme={defaultTheme}>
        <ClayIconSpriteContext.Provider value={basePath + "/icons.svg"}>
          <Component {...pageProps} />
          {!isServer && <Toasts />}
        </ClayIconSpriteContext.Provider>
      </ThemeProvider>
    </ToastContext.Provider>
  );
}
