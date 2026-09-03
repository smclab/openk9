/*
 * Copyright (c) 2021-present SMC Treviso s.r.l. All rights reserved.
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
import { StrictMode } from "react";
import { createRoot } from "react-dom/client";
import { App, openk9 } from "./App";

async function bootstrap() {
  // the widget instance built by App already starts the OAuth2 initialization
  // with its own tenant: awaiting that promise avoids a second, tenant-less
  // init here. It is null when the demo runs with OAuth2 disabled.
  await openk9.client.authInit;
  createRoot(document.getElementById("root")!).render(
    <StrictMode>
      <App />
    </StrictMode>,
  );
}

void bootstrap();
