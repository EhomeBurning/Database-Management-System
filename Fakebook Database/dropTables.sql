--DROP TRIGGER REJECT_DUPLICATE_FRIENDSHIP;
DROP TRIGGER city_trigger;
DROP TRIGGER program_trigger;
DROP TRIGGER friendship_trigger;

DROP SEQUENCE city_sequence;
DROP SEQUENCE program_sequence;

ALTER TABLE ALBUMS
  DROP CONSTRAINT COVER_CONSTRAINT;


DROP TABLE FRIENDS;
DROP TABLE USER_CURRENT_CITY;
DROP TABLE USER_HOMETOWN_CITY;
DROP TABLE EDUCATION;
DROP TABLE MESSAGE;
DROP TABLE PARTICIPANTS;
DROP TABLE TAGS;
DROP TABLE USER_EVENTS;

DROP TABLE CITIES;
DROP TABLE PROGRAMS;
DROP TABLE PHOTOS;
DROP TABLE ALBUMS;
DROP TABLE USERS;





