
* consent_response records the response to a consent form */
CREATE TABLE consent_response
(
  invitee int(8) NOT NULL,
  answer char(1),
  viewdate timestamp,
  consent varchar(64) NOT NULL default '',
  INDEX (invitee),
  FOREIGN KEY (invitee) REFERENCES invitee(id) ON DELETE CASCADE
) TYPE=InnoDB;

/* consent_hits records when a user looks at the consent form */
CREATE TABLE consent_hits
(
  invitee int(8) NOT NULL,
  viewdate timestamp,
  consent varchar(64) NOT NULL default '',
  INDEX (invitee),
  FOREIGN KEY (invitee) REFERENCES invitee(id) ON DELETE CASCADE
) TYPE=InnoDB;

/* decline_reason records why a user declines the consent form */
CREATE TABLE consent_decline
(
  invitee int(8) NOT NULL,
  reason  TEXT,
  declinedate timestamp,
  consent varchar(64) NOT NULL default '',
  INDEX (invitee),
  FOREIGN KEY (invitee) REFERENCES invitee(id) ON DELETE CASCADE
) TYPE=InnoDB;
