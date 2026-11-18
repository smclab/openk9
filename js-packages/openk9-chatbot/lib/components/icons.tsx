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

// Paths of the Material Icons (Apache License 2.0) the chatbot used to take from
// @mui/icons-material. Decorative: the control that hosts one carries the label.
// Sized at 1em and filled with currentColor, so the CSS of the host decides both.

type IconProps = { className?: string };

function SvgIcon({ path, className }: IconProps & { path: string }) {
  return (
    <svg
      className={
        className
          ? `openk9-chatbot-svg-icon ${className}`
          : "openk9-chatbot-svg-icon"
      }
      focusable="false"
      aria-hidden="true"
      viewBox="0 0 24 24"
    >
      <path d={path} />
    </svg>
  );
}

export function CloseIcon({ className }: IconProps) {
  return (
    <SvgIcon
      className={className}
      path="M19 6.41 17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"
    />
  );
}

export function RefreshIcon({ className }: IconProps) {
  return (
    <SvgIcon
      className={className}
      path="M17.65 6.35C16.2 4.9 14.21 4 12 4c-4.42 0-7.99 3.58-7.99 8s3.57 8 7.99 8c3.73 0 6.84-2.55 7.73-6h-2.08c-.82 2.33-3.04 4-5.65 4-3.31 0-6-2.69-6-6s2.69-6 6-6c1.66 0 3.14.69 4.22 1.78L13 11h7V4z"
    />
  );
}

export function LockOutlinedIcon({ className }: IconProps) {
  return (
    <SvgIcon
      className={className}
      path="M18 8h-1V6c0-2.76-2.24-5-5-5S7 3.24 7 6v2H6c-1.1 0-2 .9-2 2v10c0 1.1.9 2 2 2h12c1.1 0 2-.9 2-2V10c0-1.1-.9-2-2-2M9 6c0-1.66 1.34-3 3-3s3 1.34 3 3v2H9zm9 14H6V10h12zm-6-3c1.1 0 2-.9 2-2s-.9-2-2-2-2 .9-2 2 .9 2 2 2"
    />
  );
}

export function ErrorIcon({ className }: IconProps) {
  return (
    <SvgIcon
      className={className}
      path="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2m1 15h-2v-2h2zm0-4h-2V7h2z"
    />
  );
}

export function VisibilityIcon({ className }: IconProps) {
  return (
    <SvgIcon
      className={className}
      path="M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5M12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5m0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3"
    />
  );
}

export function ContentCopyIcon({ className }: IconProps) {
  return (
    <SvgIcon
      className={className}
      path="M16 1H4c-1.1 0-2 .9-2 2v14h2V3h12zm3 4H8c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h11c1.1 0 2-.9 2-2V7c0-1.1-.9-2-2-2m0 16H8V7h11z"
    />
  );
}

export function CheckIcon({ className }: IconProps) {
  return (
    <SvgIcon
      className={className}
      path="M9 16.17 4.83 12l-1.42 1.41L9 19 21 7l-1.41-1.41z"
    />
  );
}

export function OpenInFullIcon({ className }: IconProps) {
  return (
    <SvgIcon
      className={className}
      path="M21 11V3h-8l3.29 3.29-10 10L3 13v8h8l-3.29-3.29 10-10z"
    />
  );
}
