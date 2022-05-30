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

import React, { Suspense, useState } from "react";

import { useRouter } from "next/router";

import { Layout } from "../../../components/Layout";

import { firstOrString } from "../../../components/utils";

export default function TenantSettings() {
  const { query } = useRouter();
  const tenantId = query.tenantId && firstOrString(query.tenantId);

  if (!tenantId) return null;

  return (
    <>
      <Layout
        breadcrumbsPath={[
          { label: "Tenants", path: "/tenants" },
          { label: tenantId },
          { label: "Events", path: `/tenants/${tenantId}/events` },
        ]}
      >
        Event List
      </Layout>
    </>
  );
}
