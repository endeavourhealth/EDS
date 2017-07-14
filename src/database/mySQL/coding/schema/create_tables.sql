DROP TABLE IF EXISTS coding.trm_concept_pc_link;
DROP TABLE IF EXISTS coding.trm_concept;

CREATE TABLE coding.trm_concept
(
    pid bigint NOT NULL PRIMARY KEY,
    code varchar(100) NOT NULL,
    codesystem_pid bigint,
    display varchar(400),
    index_status bigint
);

CREATE INDEX idx_code
  ON coding.trm_concept (code);

CREATE UNIQUE INDEX idx_code_system
  ON coding.trm_concept (code, codesystem_pid);


CREATE TABLE coding.trm_concept_pc_link
(
    pid bigint NOT NULL PRIMARY KEY,
    rel_type integer,
    child_pid bigint NOT NULL,
    codesystem_pid bigint NOT NULL,
    parent_pid bigint NOT NULL
);
