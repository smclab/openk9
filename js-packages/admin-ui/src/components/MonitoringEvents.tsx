import React from "react";
import { gql } from "@apollo/client";
import ClayLayout from "@clayui/layout";
import { EventSortable, useMonitoringEventsQuery, useMonitoringEventDataQuery } from "../graphql-generated";
import ClayIcon from "@clayui/icon";
import ClayDropDown from "@clayui/drop-down";
import ClayButton, { ClayButtonWithIcon } from "@clayui/button";
import ClayModal, { useModal } from "@clayui/modal";
import { TableVirtuoso } from "react-virtuoso";

gql`
  query MonitoringEvents($field: EventSortable, $ordering: String) {
    event(sortBy: $field, sortType: $ordering, from: 0, size: 10) {
      id
      className
      created
      groupKey
      size
      type
      version
      classPK
      parsingDate
    }
  }
`;

gql`
  query MonitoringEventData($id: String) {
    eventData(id: $id)
  }
`;

type Ordering = "DESC" | "ASC";

export function MonitoringEvents() {
  const [fieldOrder, setFieldOrder] = React.useState<{ field: EventSortable; ordering: Ordering }>({
    field: EventSortable.Created,
    ordering: "ASC",
  });
  const monitoringEventsQuery = useMonitoringEventsQuery({
    variables: { field: fieldOrder.field, ordering: fieldOrder.ordering },
  });

  const tableHeading = (field: EventSortable, nameHeading: string, width: string) => {
    return (
      <th style={{ width }}>
        <ClayDropDown
          trigger={
            <ClayButton displayType="unstyled">
              {nameHeading}
              {fieldOrder.field === field && fieldOrder.ordering === "ASC" && <ClayIcon className="c-ml-sm-3" symbol="angle-down" />}
              {fieldOrder.field === field && fieldOrder.ordering === "DESC" && <ClayIcon className="c-ml-sm-3" symbol="angle-up" />}
            </ClayButton>
          }
        >
          <ClayDropDown.ItemList>
            <ClayDropDown.Group header="ORDER BY">
              <ClayDropDown.Item>
                <div className="custom-control custom-radio">
                  <label>
                    <input
                      className="custom-control-input"
                      type="radio"
                      checked={field === fieldOrder.field && fieldOrder.ordering === "ASC"}
                      onChange={() => {
                        setFieldOrder({ field, ordering: "ASC" });
                      }}
                    />
                    <span className="custom-control-label">
                      <span className="custom-control-label-text">Ascending</span>
                    </span>
                  </label>
                </div>
              </ClayDropDown.Item>
              <ClayDropDown.Item>
                <div className="custom-control custom-radio">
                  <label>
                    <input
                      className="custom-control-input"
                      type="radio"
                      checked={field === fieldOrder.field && fieldOrder.ordering === "DESC"}
                      onChange={() => {
                        setFieldOrder({ field, ordering: "DESC" });
                      }}
                    />
                    <span className="custom-control-label">
                      <span className="custom-control-label-text">Descending</span>
                    </span>
                  </label>
                </div>
              </ClayDropDown.Item>
            </ClayDropDown.Group>
            {false && (
              <ClayDropDown.Group header="FILTER BY">
                <ClayDropDown.Item>
                  <div className="custom-control custom-checkbox">
                    <label>
                      <input checked={true} className="custom-control-input" type="checkbox" />
                      <span className="custom-control-label">
                        <span className="custom-control-label-text">Ascending</span>
                      </span>
                    </label>
                  </div>
                </ClayDropDown.Item>
                <ClayDropDown.Item>
                  <div className="custom-control custom-checkbox">
                    <label>
                      <input checked={true} className="custom-control-input" type="checkbox" />
                      <span className="custom-control-label">
                        <span className="custom-control-label-text">Descending</span>
                      </span>
                    </label>
                  </div>
                </ClayDropDown.Item>
              </ClayDropDown.Group>
            )}
          </ClayDropDown.ItemList>
        </ClayDropDown>
      </th>
    );
  };

  return (
    <ClayLayout.ContainerFluid view>
      <TableVirtuoso
        style={{ height: "80vh" }}
        data={monitoringEventsQuery.data?.event as any}
        components={{
          Table: (props) => <table {...props} className="table table-list" style={{ ...props.style, tableLayout: "fixed" }} />,
        }}
        fixedHeaderContent={() => {
          return (
            <tr>
              <th style={{ width: "15%" }}>
                <ClayButton displayType="unstyled">Id</ClayButton>
              </th>
              {tableHeading(EventSortable.ClassName, "ClassName", "10%")}
              {tableHeading(EventSortable.Type, "Type", "10%")}
              {tableHeading(EventSortable.Version, "Version", "10%")}
              {tableHeading(EventSortable.GroupKey, "GroupKey", "10%")}
              {tableHeading(EventSortable.ClassPk, "ClassPK", "10%")}
              {tableHeading(EventSortable.ParsingDate, "Parsing", "10%")}
              {tableHeading(EventSortable.Created, "Created", "15%")}
              {tableHeading(EventSortable.Size, "Size", "5%")}
              <th style={{ width: "5%" }}>Data</th>
            </tr>
          );
        }}
        itemContent={(index, event) => {
          return (
            <React.Fragment>
              <td>{event?.id}</td>
              <td>{event?.className}</td>
              <td>{event?.type}</td>
              <td>{event?.version}</td>
              <td>{event?.groupKey}</td>
              <td>{event?.classPK}</td>
              <td>{event?.parsingDate}</td>
              <td>{event?.created && dateFormatter.format(new Date(event.created))}</td>
              <td>{event?.size}</td>
              <td>{event?.id && <EventData id={event.id} />}</td>
            </React.Fragment>
          );
        }}
      />
    </ClayLayout.ContainerFluid>
  );
}

const dateFormatter = new Intl.DateTimeFormat(undefined, {
  dateStyle: "medium",
  timeStyle: "medium",
});

type EventDataProps = { id: string };
function EventData({ id }: EventDataProps) {
  const { observer, onOpenChange, open } = useModal();
  const monitoringEventDataQuery = useMonitoringEventDataQuery({
    variables: { id },
  });
  return (
    <>
      {open && (
        <ClayModal observer={observer} size="lg">
          <ClayModal.Header>{"Event Data"}</ClayModal.Header>
          <ClayModal.Body>
            <pre>{JSON.stringify(JSON.parse(monitoringEventDataQuery.data?.eventData ?? "{}"), null, 2)}</pre>
          </ClayModal.Body>
        </ClayModal>
      )}
      <ClayButtonWithIcon aria-label="" symbol="info-panel-open" onClick={() => onOpenChange(true)} />
    </>
  );
}
