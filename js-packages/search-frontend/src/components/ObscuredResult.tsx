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
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import { faLock } from "@fortawesome/free-solid-svg-icons/faLock";
import React from "react";
import { useTranslation } from "react-i18next";
import { css } from "styled-components";
import { GenericResultItem, useOpenK9Client } from "./client";

type ObscuredResultProps = {
  result: GenericResultItem<Record<string, unknown>>;
};

/**
 * Il template di default di un risultato oscurato (#2489), per ogni tipo di documento:
 * si distingue dai leggibili, mostra solo i campi in chiaro e dice perché non si legge.
 * Non si apre e non ha link: lo garantisce `Result`.
 */
export function ObscuredResult({ result }: ObscuredResultProps) {
  const { t } = useTranslation();
  return (
    <div
      className="openk9-embeddable-search--result-obscured"
      css={css`
        padding: var(--openk9-embeddable-search--spacing-md, 12px)
          var(--openk9-embeddable-search--spacing-lg, 16px);
        background: var(
          --openk9-embeddable-search--obscured-background-color,
          #f8f9fb
        );
        border: 1px dashed
          var(--openk9-embeddable-search--obscured-border-color, #cfd4dc);
        border-radius: var(--openk9-embeddable-search--radius-sm, 8px);
        color: var(--openk9-embeddable-search--obscured-text-color, #475467);
      `}
    >
      <div
        css={css`
          display: flex;
          align-items: center;
          gap: var(--openk9-embeddable-search--spacing-sm, 8px);
          color: var(--openk9-embeddable-search--strong-text-color, #1e1c21);
          font-size: var(--openk9-embeddable-search--font-size-md, 16px);
          font-weight: var(
            --openk9-embeddable-search--font-weight-semibold,
            600
          );
        `}
      >
        <FontAwesomeIcon
          icon={faLock}
          aria-hidden="true"
          css={css`
            color: var(
              --openk9-embeddable-search--obscured-text-color,
              #475467
            );
            font-size: 14px;
          `}
        />
        {t("obscured-result")}
      </div>
      <div
        css={css`
          display: flex;
          flex-wrap: wrap;
          gap: 6px;
          margin-top: var(--openk9-embeddable-search--spacing-sm, 8px);
        `}
      >
        <span
          css={css`
            ${chip}
            background: var(
              --openk9-embeddable-search--obscured-chip-background-color,
              #3f4a5a
            );
            border-color: var(
              --openk9-embeddable-search--obscured-chip-background-color,
              #3f4a5a
            );
            color: white;
          `}
        >
          {t("obscured-result-chip")}
        </span>
        {result.source.documentTypes.map((documentType) => (
          <span key={String(documentType)} css={chip}>
            {String(documentType)}
          </span>
        ))}
      </div>
      <VisibleFields result={result} />
      <ObscuredReason />
    </div>
  );
}

const chip = css`
  display: inline-flex;
  align-items: center;
  padding: 2px var(--openk9-embeddable-search--spacing-sm, 8px);
  border: 1px solid
    var(--openk9-embeddable-search--obscured-border-color, #cfd4dc);
  border-radius: var(--openk9-embeddable-search--radius-pill, 999px);
  background: white;
  font-size: var(--openk9-embeddable-search--font-size-xs, 12px);
  font-weight: var(--openk9-embeddable-search--font-weight-semibold, 600);
  line-height: 1.4;
`;

// i campi che il searcher lascia in ogni risultato oscurato, oltre a quelli in chiaro
const RESERVED_KEYS = new Set(["id", "documentTypes", "obscuredFields"]);

type VisibleField = { label: string; value: string };

/** I campi in chiaro come righe etichetta/valore, nell'ordine in cui arrivano. */
function VisibleFields({ result }: Pick<ObscuredResultProps, "result">) {
  const { i18n } = useTranslation();
  const fields = visibleFields(result.source, i18n.language);
  if (fields.length === 0) return null;
  return (
    <dl
      css={css`
        display: grid;
        grid-template-columns: max-content 1fr;
        align-items: baseline;
        column-gap: var(--openk9-embeddable-search--spacing-xl, 20px);
        row-gap: var(--openk9-embeddable-search--spacing-xs, 4px);
        margin: var(--openk9-embeddable-search--spacing-md, 12px) 0 0;
        font-size: var(--openk9-embeddable-search--font-size-sm, 14px);
      `}
    >
      {fields.map(({ label, value }) => (
        <React.Fragment key={label}>
          <dt
            css={css`
              color: var(
                --openk9-embeddable-search--secondary-text-color,
                #6b7280
              );
              font-size: var(--openk9-embeddable-search--font-size-xs, 12px);
              line-height: 20px;
            `}
          >
            {label}
          </dt>
          <dd
            css={css`
              margin: 0;
              color: var(
                --openk9-embeddable-search--strong-text-color,
                #1e1c21
              );
              line-height: 20px;
              overflow-wrap: anywhere;
            `}
          >
            {value}
          </dd>
        </React.Fragment>
      ))}
    </dl>
  );
}

export function visibleFields(
  source: Record<string, unknown>,
  language: string,
): VisibleField[] {
  const fields = new Map<string, VisibleField>();
  const visit = (value: unknown, path: string) => {
    if (value === null || value === undefined || value === "") return;
    if (Array.isArray(value)) {
      if (value.some((item) => typeof item === "object" && item !== null)) {
        value.forEach((item) => visit(item, path));
      } else {
        add(path, value.map(String).join(", "));
      }
      return;
    }
    if (typeof value === "object") {
      Object.entries(value).forEach(([key, child]) =>
        visit(child, `${path}.${key}`),
      );
      return;
    }
    add(path, String(value));
  };
  const add = (path: string, value: string) => {
    // un padre I18N vale per tutte le varianti: una riga sola, la prima che arriva
    const field = path.replace(/\.i18n\.[^.]+$|\.base$/, "");
    if (fields.has(field)) return;
    const name = field.split(".").pop() ?? field;
    fields.set(field, {
      label: toLabel(name),
      value: /date/i.test(name) ? formatDate(value, language) : value,
    });
  };
  Object.entries(source).forEach(([key, value]) => {
    if (!RESERVED_KEYS.has(key)) visit(value, key);
  });
  return [...fields.values()];
}

// il nome del campo, da `lastModifiedDate` a "Last modified date": la response non ha le etichette
function toLabel(name: string) {
  const words = name
    .replace(/([a-z0-9])([A-Z])/g, "$1 $2")
    .replace(/[_-]+/g, " ")
    .toLowerCase();
  return words.charAt(0).toUpperCase() + words.slice(1);
}

function formatDate(value: string, language: string) {
  const date = new Date(/^\d+$/.test(value) ? Number(value) : value);
  return Number.isNaN(date.getTime())
    ? value
    : date.toLocaleDateString(language);
}

// null finché non si sa, e senza OAuth2, dove il login lo gestisce chi integra
function useAuthenticated() {
  const client = useOpenK9Client();
  const [authenticated, setAuthenticated] = React.useState<boolean | null>(
    null,
  );
  React.useEffect(() => {
    let isCurrent = true;
    client.authInit?.then((ready) => {
      if (isCurrent) setAuthenticated(ready);
    });
    return () => {
      isCurrent = false;
    };
  }, [client]);
  return authenticated;
}

function ObscuredReason() {
  const { t } = useTranslation();
  const client = useOpenK9Client();
  // chi non ha fatto login lo può vedere accedendo, con lo stesso login della testata
  const canLogin = useAuthenticated() === false;
  return (
    <div
      css={css`
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        gap: var(--openk9-embeddable-search--spacing-sm, 8px);
        margin-top: var(--openk9-embeddable-search--spacing-md, 12px);
        font-size: var(--openk9-embeddable-search--font-size-xs, 12px);
      `}
    >
      <span>
        {t(canLogin ? "obscured-result-login" : "obscured-result-hint")}
      </span>
      {canLogin && (
        <button
          type="button"
          onClick={() => client.authenticate({})}
          css={css`
            padding: var(--openk9-embeddable-search--spacing-xs, 4px)
              var(--openk9-embeddable-search--spacing-md, 12px);
            border: 1px solid
              var(
                --openk9-embeddable-search--primary-subtle-background-color,
                #f9edee
              );
            border-radius: var(--openk9-embeddable-search--radius-pill, 999px);
            background: var(
              --openk9-embeddable-search--primary-subtle-background-color,
              #f9edee
            );
            color: var(
              --openk9-embeddable-search--secondary-active-color,
              #c0272b
            );
            font-size: var(--openk9-embeddable-search--font-size-xs, 12px);
            font-weight: var(
              --openk9-embeddable-search--font-weight-semibold,
              600
            );
            cursor: pointer;
          `}
        >
          {t("login")}
        </button>
      )}
    </div>
  );
}
