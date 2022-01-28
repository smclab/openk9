import { GenericResultItem } from "@openk9/rest-api";

export type CalendarResultItem = GenericResultItem<{
  calendar: {
    allDay: boolean;
    calendarBookingId: string;
    description: string;
    location: string;
    startTime: string;
    endTime: string;
    title: string;
  };
}>;
