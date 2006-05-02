USE oscars;

-- reservations-related functions

CREATE FUNCTION makeTag
( startTime INT(11), login TEXT, id INT(11) )
RETURNS TEXT
RETURN CONCAT(
          ( SELECT abbrev from domains WHERE local=1 ),
          '-', login, '-',
          ( SELECT DATE( (SELECT from_unixtime(startTime)))),
          '-', id);
