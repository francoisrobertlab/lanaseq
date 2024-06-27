-- // add_forgotpassword_table
-- Migration SQL that makes the change goes here.

CREATE TABLE forgot_password (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  user_id bigint(20) NOT NULL,
  request_moment datetime NOT NULL,
  confirm_number varchar(100) NOT NULL,
  used tinyint(1) NOT NULL DEFAULT '0',
  PRIMARY KEY (id),
  CONSTRAINT forgotpasswordUser_ibfk FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- //@UNDO
-- SQL to undo the change goes here.

DROP TABLE forgot_password;
