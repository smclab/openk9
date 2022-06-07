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
import { gql, useQuery } from "@apollo/client";
import { Virtuoso, TableVirtuoso } from "react-virtuoso";

const dateTimeFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: "medium",
  timeStyle: "medium",
});

export default function IngestionEvents() {
  const { query } = useRouter();
  const tenantId = query.tenantId && firstOrString(query.tenantId);
  const { data: autocompleteOptionsClassName } = useQuery(gql`
    query IngestionEventsAutocompleteOptionsClassName {
      eventOptions {
        className
      }
    }
  `);

  const { data: autocompleteOptionsGroupKey } = useQuery(gql`
    query IngestionEventsAutocompleteOptionsGroupKey {
      eventOptions {
        groupKey
      }
    }
  `);

  const { data: autocompleteOptionsClassPK } = useQuery(gql`
    query IngestionEventsAutocompleteOptionsClassPK {
      eventOptions {
        classPK
      }
    }
  `);

  const { data: autocompleteOptionsType } = useQuery(gql`
    query IngestionEventsAutocompleteOptionsType {
      eventOptions {
        type
      }
    }
  `);
  const [classNameValue, setClassNameValue] = React.useState("");
  const [groupKeyValue, setGroupKeyValue] = React.useState("");
  const [typeValue, setTypeValue] = React.useState("");
  const [classPKValue, setClassPKValue] = React.useState("");

  const { data: eventlist, fetchMore: fetchMoreEvents } = useQuery(
    gql`
      query IngestionEventList(
        $className: String
        $groupKey: String
        $type: String
        $from: Int
      ) {
        event(
          from: $from
          size: 10
          className: $className
          groupKey: $groupKey
          type: $type
        ) {
          className
          created
          groupKey
          id
          size
          type
          version
        }
      }
    `,
    {
      variables: {
        className: classNameValue || undefined,
        groupKey: groupKeyValue || undefined,
        type: typeValue || undefined,
      },
    },
  );

  if (!tenantId) return null;
  const cellStyle: React.CSSProperties = {
    wordBreak: "break-all",
    padding: "8px",
  };

  const cellStyleDate: React.CSSProperties = {
    wordBreak: "break-word",
    whiteSpace: "nowrap",
    padding: "8px",
  };

  const cellStyleNumber: React.CSSProperties = {
    wordBreak: "break-word",
    whiteSpace: "nowrap",
    padding: "8px",
    textAlign: "right",
  };

  const coloumnStyle: React.CSSProperties = {
    padding: "8px",
  };

  return (
    <>
      <Layout
        breadcrumbsPath={[
          { label: "Tenants", path: "/tenants" },
          { label: tenantId },
          { label: "Events", path: `/tenants/${tenantId}/events` },
        ]}
      >
        {eventlist && (
          <TableVirtuoso
            style={{ height: "100%", width: "100%" }}
            data={eventlist.event}
            components={{
              Table: (props) => {
                return (
                  <table style={{ ...props.style, width: "100%" }}>
                    {props.children}
                  </table>
                );
              },
            }}
            fixedHeaderContent={() => (
              <tr
                style={{
                  backgroundColor: "white",
                }}
              >
                <th style={coloumnStyle}>id</th>
                <th style={coloumnStyle}>
                  type
                  <SelectOption
                    value={typeValue}
                    onChange={setTypeValue}
                    options={
                      autocompleteOptionsType?.eventOptions.map(
                        (option: any) => option.type,
                      ) ?? []
                    }
                  />
                </th>
                <th style={coloumnStyle}>
                  className
                  <SelectOption
                    value={classNameValue}
                    onChange={setClassNameValue}
                    options={
                      autocompleteOptionsClassName?.eventOptions.map(
                        (option: any) => option.className,
                      ) ?? []
                    }
                  />
                </th>
                <th style={coloumnStyle}>
                  groupKey
                  <SelectOption
                    value={groupKeyValue}
                    onChange={setGroupKeyValue}
                    options={
                      autocompleteOptionsGroupKey?.eventOptions.map(
                        (option: any) => option.groupKey,
                      ) ?? []
                    }
                  />
                </th>

                <th style={coloumnStyle}>
                  classPK
                  <SelectOption
                    value={classPKValue}
                    onChange={setClassPKValue}
                    options={
                      autocompleteOptionsClassPK?.eventOptions.map(
                        (option: any) => option.classPK,
                      ) ?? []
                    }
                  />
                </th>

                <th style={coloumnStyle}>version</th>
                <th style={coloumnStyle}>size</th>
                <th style={coloumnStyle}>parsingDate</th>
                <th style={coloumnStyle}>created</th>
              </tr>
            )}
            itemContent={(index, event) => (
              <React.Fragment>
                <td style={cellStyle}>{event.id}</td>
                <td style={cellStyle}>{event.type}</td>
                <td style={cellStyle}>{event.className}</td>
                <td style={cellStyle}>{event.groupKey}</td>
                <td style={cellStyleNumber}>{event.version}</td>
                <td style={cellStyleNumber}>{event.size}</td>
                <td style={cellStyleDate}>
                  {dateTimeFormatter.format(new Date(event.created))}
                </td>
              </React.Fragment>
            )}
            endReached={() => {
              fetchMoreEvents({
                variables: {
                  from: eventlist.event.length,
                },
              });
            }}
          />
        )}
      </Layout>
    </>
  );
}
type SelectOptionProps = {
  value: string;
  onChange(value: string): void;
  options: Array<string>;
};
function SelectOption({ value, onChange, options }: SelectOptionProps) {
  return (
    <select
      value={value}
      onBlur={(event) => {
        onChange(event.currentTarget.value);
      }}
    >
      <option value=""></option>
      {options.map((option: any) => {
        return (
          <option key={option.groupKey} value={option.groupKey}>
            {option.groupKey}
          </option>
        );
      })}
    </select>
  );
}
