USE bss;

-- add interPathElem to path so interdomain paths can be stored
ALTER TABLE paths ADD interPathElemId INT UNIQUE AFTER pathElemId;
