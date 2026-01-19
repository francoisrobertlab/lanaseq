-- // add messages table
-- Migration SQL that makes the change goes here.

create table message (
    id bigint(20) not null AUTO_INCREMENT,
    owner_id bigint(20) not null,
    message varchar(255) not null,
    color varchar(255) default null,
    unread tinyint not null default 0,
    date DATETIME not null,
    primary key (id),
    constraint message_owner_ibfk foreign key (owner_id) references users (id) on delete cascade on update cascade
);


-- //@UNDO
-- SQL to undo the change goes here.

drop table message;
