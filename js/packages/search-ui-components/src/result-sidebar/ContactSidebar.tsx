import React from "react";
import { createUseStyles } from "react-jss";
import ClayIcon from "@clayui/icon";
import { format } from "date-fns";
import { ContactResultItem } from "@openk9/http-api";
import { ThemeType } from "../theme";

const useStyles = createUseStyles((theme: ThemeType) => ({
  row: {
    display: "flex",
    alignItems: "center",
  },
  avatar: {
    margin: theme.spacingUnit,
    marginRight: theme.spacingUnit * 3,
    width: 64,
    height: 64,
    fontSize: 64,
    flexShrink: 0,
    display: "flex",
    justifyContent: "center",
    color: theme.digitalLakeMainL2,
  },
}));

export function ContactSidebar({ result }: { result: ContactResultItem }) {
  const classes = useStyles();
  return (
    <>
      <h3 className={classes.row}>
        <div className={classes.avatar}>
          {result.source.user.portrait_preview ? (
            <img src={result.source.user.portrait_preview} />
          ) : (
            <ClayIcon symbol="user" />
          )}
        </div>{" "}
        <div>
          {result.source.user.firstName} {result.source.user.middleName}{" "}
          {result.source.user.lastName}
        </div>
      </h3>
      <div>
        <strong>Screen Name</strong>: {result.source.user.screenName}
      </div>
      <div>
        <strong>Job Title</strong>: {result.source.user.jobTitle}
      </div>
      <div>
        <strong>Job Class</strong>: {result.source.user.jobClass}
      </div>
      <div>
        <strong>Birthday</strong>:{" "}
        {format(result.source.user.birthday, "dd MMMM yyyy, HH:mm")}
      </div>
      <div>
        <strong>Email Address</strong>: {result.source.user.emailAddress}
      </div>
      <div>
        <strong>Facebook</strong>: {result.source.user.facebookSn || "N/A"}
      </div>
      <div>
        <strong>Twitter</strong>: {result.source.user.twitterSn || "N/A"}
      </div>
      <div>
        <strong>Skype</strong>: {result.source.user.skypeSn || "N/A"}
      </div>
      <div>
        <strong>User Id</strong>: {result.source.user.userId}
      </div>
      {result.source.user.employeeNumber && (
        <div>
          <strong>Employee Number</strong>: {result.source.user.employeeNumber}
        </div>
      )}
      {result.source.user.zip && (
        <div>
          <strong>Zip</strong>: {result.source.user.zip}
        </div>
      )}
      {result.source.user.country && (
        <div>
          <strong>Country</strong>: {result.source.user.country}
        </div>
      )}
      {result.source.user.city && (
        <div>
          <strong>City</strong>: {result.source.user.city}
        </div>
      )}
      {result.source.user.phoneNumber && (
        <div>
          <strong>Phone Number</strong>: {result.source.user.phoneNumber}
        </div>
      )}
      {result.source.user.street && (
        <div>
          <strong>Street</strong>: {result.source.user.street}
        </div>
      )}
    </>
  );
}
