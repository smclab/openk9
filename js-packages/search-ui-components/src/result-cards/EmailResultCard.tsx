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

import React, { useState } from "react";
import { createUseStyles } from "react-jss";
import ClayModal, { useModal } from "@clayui/modal";

import { EmailResultItem } from "@openk9/http-api";
import { ThemeType } from "../theme";
import { EmailIcon } from "../icons/EmailIcon";
import { ResultCard, Highlight } from "../components";

const useStyles = createUseStyles((theme: ThemeType) => ({
  root: {
    "&:focus, &:hover h4": {
      textDecoration: "underline",
    },
  },
  iconArea: {
    margin: theme.spacingUnit,
    marginRight: theme.spacingUnit * 2,
    flexShrink: 0,
    display: "flex",
    justifyContent: "center",
  },
  subject: {
    marginBottom: 2,
  },
  fromTo: {
    display: "flex",
    fontWeight: 300,
    fontSize: 15,
    marginBottom: "0.3rem",
    alignItems: "center",
  },
  fromToBadge: {
    display: "flex",
    alignItems: "center",
    "&+&": { marginLeft: theme.spacingUnit * 3 },
    "& div:first-child": {
      display: "flex",
      alignItems: "center",
      justifyContent: "center",
      fontSize: 12,
      fontWeight: 300,
      textTransform: "uppercase",
      color: "white",
      backgroundColor: theme.digitalLakeMainL3,
      borderRadius: theme.borderRadius,
      padding: [0, theme.spacingUnit],
      marginRight: theme.spacingUnit,
    },
  },
  dialogInfo: {
    marginBottom: theme.spacingUnit * 2,
  },
  body: {
    minHeight: "70vh",
  },
  textArea: {
    fontSize: 14,
  },
}));

export function EmailResultCard({
  data,
  className,
  ...rest
}: {
  data: EmailResultItem;
  onSelect?: () => void;
} & React.AnchorHTMLAttributes<HTMLAnchorElement>): JSX.Element {
  const classes = useStyles();

  const [visible, setVisible] = useState(false);
  const { observer } = useModal({
    onClose: () => setVisible(false),
  });

  return (
    <>
      <ResultCard
        onClick={() => setVisible(true)}
        className={classes.root}
        {...rest}
      >
        <div className={classes.iconArea}>
          <EmailIcon />
        </div>
        <div style={{ minWidth: 0 }}>
          <h4 className={classes.subject}>
            <Highlight
              text={data.source.email.subject}
              highlight={data.highlight["email.subject"]}
            />
          </h4>
          <div className={classes.fromTo}>
            <div className={classes.fromToBadge}>
              <div>from</div>
              <Highlight
                text={data.source.email.from}
                highlight={data.highlight["email.from"]}
              />
            </div>
            <div className={classes.fromToBadge}>
              <div>to</div>
              <Highlight
                text={data.source.email.to}
                highlight={data.highlight["email.to"]}
              />
            </div>
          </div>
          <div className={classes.textArea}>
            <Highlight
              text={data.source.email.body}
              highlight={data.highlight["email.body"]}
            />
          </div>
        </div>
      </ResultCard>

      {visible && (
        <ClayModal observer={observer} size="lg">
          <ClayModal.Header>
            <EmailIcon /> Email
          </ClayModal.Header>
          <ClayModal.Body>
            <div className={classes.dialogInfo}>
              <div>
                <strong>Subject: </strong>
                {data.source.email.subject}
              </div>
              <div>
                <strong>Date: </strong>
                {new Date(data.source.email.date).toLocaleDateString()}
              </div>
              <div>
                <strong>From: </strong>
                {data.source.email.from}
              </div>
              <div>
                <strong>To: </strong>
                {data.source.email.to}
              </div>
              {data.source.email.cc && data.source.email.cc.length > 0 && (
                <div>
                  <strong>CC: </strong>
                  {data.source.email.cc}
                </div>
              )}
            </div>

            <iframe
              frameBorder="0"
              width="100%"
              height="100%"
              srcDoc={data.source.email.htmlBody}
              className={classes.body}
            />
          </ClayModal.Body>
        </ClayModal>
      )}
    </>
  );
}
