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
import LockOutlinedIcon from "@mui/icons-material/LockOutlined";
import { Button } from "@mui/material";
import React from "react";
import { useTranslation } from "react-i18next";
import { useNavigate } from "react-router-dom";
import { SettingsSection } from "./SettingsSection";

/** Link to the result ACL configuration, which has a page of its own. */
export function ResultAclSection() {
  const { t } = useTranslation();
  const navigate = useNavigate();

  return (
    <SettingsSection
      icon={<LockOutlinedIcon />}
      title={t("pages.admin-settings.result-acl.title")}
      description={t("pages.admin-settings.result-acl.description")}
    >
      <Button variant="outlined" onClick={() => navigate("/admin-settings/result-acl")}>
        {t("pages.admin-settings.result-acl.open")}
      </Button>
    </SettingsSection>
  );
}
