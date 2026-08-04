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
import TranslateIcon from "@mui/icons-material/Translate";
import { IconButton, Menu, MenuItem, Tooltip } from "@mui/material";
import React from "react";
import { useTranslation } from "react-i18next";
import { changeLanguage, SupportedLanguage, supportedLanguages } from "../i18n";

const languageLabels: Record<SupportedLanguage, string> = {
  en: "English",
  it: "Italiano",
};

const LanguageSwitcher: React.FC = () => {
  const { t, i18n } = useTranslation();
  const [anchorEl, setAnchorEl] = React.useState<HTMLElement | null>(null);

  const selectLanguage = (language: SupportedLanguage) => {
    changeLanguage(language);
    setAnchorEl(null);
  };

  return (
    <React.Fragment>
      <Tooltip title={t("app.change-language")}>
        <IconButton
          onClick={(event) => setAnchorEl(event.currentTarget)}
          aria-label={t("app.change-language")}
          sx={{
            borderRadius: "10px",
            "&:hover": {
              backgroundColor: "rgba(0, 0, 0, 0.04)",
            },
          }}
        >
          <TranslateIcon />
        </IconButton>
      </Tooltip>
      <Menu anchorEl={anchorEl} open={Boolean(anchorEl)} onClose={() => setAnchorEl(null)}>
        {supportedLanguages.map((language) => (
          <MenuItem
            key={language}
            selected={i18n.resolvedLanguage === language}
            onClick={() => selectLanguage(language)}
          >
            {languageLabels[language]}
          </MenuItem>
        ))}
      </Menu>
    </React.Fragment>
  );
};

export default LanguageSwitcher;
