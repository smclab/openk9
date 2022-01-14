import { GenericResultItem } from "@openk9/rest-api";

export type UserResultItem = GenericResultItem<{
  user: {
    screenName: string;
    emailAddress: string;
    coperturaGeografica: string;
    phoneNumber: string;
    jobTitle: string;
    jobClass: string;
    fullName: string;
    birthday: string;
    facebookSn: string;
    twitterSn: string;
    skypeSn: string;
    userId: string;
  };
}>;
