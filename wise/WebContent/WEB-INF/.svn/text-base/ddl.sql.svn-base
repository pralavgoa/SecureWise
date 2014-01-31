/* contains all the users uploaded from xml file */
CREATE TABLE invitee
(
  id int(8) NOT NULL auto_increment,
  firstname varchar(64),
  lastname varchar(64),
  salutation varchar(8),
  email varchar(64),
  phone varchar(16),
  pager varchar(16),
  PRIMARY KEY (id),
  INDEX (id)
)TYPE=InnoDB;

/* contains all modules and history of development */
CREATE TABLE module_list
(
  internal_id int(8) NOT NULL auto_increment,
  id int(8) NOT NULL,
  original_name varchar(64) ,
  title varchar(128),
  internal_name varchar(64) ,
  modified timestamp,
  PRIMARY KEY  (internal_id),
  INDEX (id)
) TYPE=InnoDB;

/* tracks sessions for users for a module */
CREATE TABLE module_user_session
(
  id int(8) NOT NULL auto_increment,
  invitee int(8) NOT NULL,
  starttime datetime,
  endtime datetime,
  exit_type char(1) ,
  type varchar(64),
  PRIMARY KEY  (id),
  INDEX (invitee),
  FOREIGN KEY (invitee) REFERENCES invitee(id) ON DELETE CASCADE
) TYPE=InnoDB;

/* records assignments of invitees to one or more modules */
CREATE TABLE module_assignment
(
  module int(8) NOT NULL,
  invitee int(8) NOT NULL,
  assigned timestamp,
  INDEX (invitee),
  INDEX (module),
  FOREIGN KEY (invitee) REFERENCES invitee(id) ON DELETE CASCADE,
  FOREIGN KEY (module) REFERENCES module_list(id) ON DELETE CASCADE
) TYPE=InnoDB;

/* tracks which state user is in */
CREATE TABLE module_user_state
(
  invitee int(8) NOT NULL,
  state int(8),
  entry timestamp,
  INDEX (invitee),
  FOREIGN KEY (invitee) REFERENCES invitee (id) ON DELETE CASCADE
) TYPE=InnoDB;

/* tracks what quiz was taken and how long by user */
CREATE TABLE quiz
(
  id int(8) NOT NULL auto_increment,
  invitee int(8) NOT NULL,
  quiztype varchar(16) ,
  score float(8,5) ,
  starttime datetime,
  endtime datetime,
  PRIMARY KEY  (id),
  INDEX (invitee),
  FOREIGN KEY (invitee) REFERENCES invitee(id) ON DELETE CASCADE
) TYPE=InnoDB;

/* contains an entry for every item in the quiz with final stats  */
CREATE TABLE quiz_item
(
  quiz int(8) NOT NULL,
  question varchar(64) NOT NULL,
  item_index int(2),
  time_submitted datetime,
  full_latency int(8),
  initial_latency int(8),
  clicks int(8),
  answer int(32),
  correct char(1),
  idle_time int(8),
  total_time int(8),
  score float(8,5),
  INDEX (quiz),
  FOREIGN KEY (quiz) REFERENCES quiz(id) ON DELETE CASCADE
) TYPE=InnoDB;

/* records every option that was chosen by the user until submitted */
CREATE TABLE quiz_item_response
(
  quiz int(8) NOT NULL,
  question varchar(64) NOT NULL,
  response int(32),
  latency int(8),
  INDEX (quiz),
  FOREIGN KEY (quiz) REFERENCES quiz(id) ON DELETE CASCADE
) TYPE=InnoDB;

/* contains the randomized assignment for a user */
CREATE TABLE random_assignment
(
  id int(8) NOT NULL auto_increment,
  assignment int(8),
  invitee int(8) NOT NULL,
  assigned timestamp,
  PRIMARY KEY  (id),
  INDEX (invitee),
  FOREIGN KEY (invitee) REFERENCES invitee(id) ON DELETE CASCADE
) TYPE=InnoDB;

/* tracks email to send to users when scheduled */
CREATE TABLE pending
(
  invitee int(8) NOT NULL,
  send_time datetime,
  message varchar(32),
  survey varchar(32),
  completed char,
  completed_time datetime,
  INDEX (invitee),
  FOREIGN KEY (invitee) REFERENCES invitee(id) ON DELETE CASCADE
) TYPE=InnoDB;

/* records how long user spends looking at pages such as sources and outline */
CREATE TABLE page_view
(
  invitee int(8) NOT NULL,
  page varchar(64),
  time varchar(8),
  idle varchar(8),
  hittime timestamp,
  INDEX (invitee),
  FOREIGN KEY (invitee) REFERENCES invitee (id) ON DELETE CASCADE
) TYPE=InnoDB;

