DROP TABLE IF EXISTS trm_concept;
DROP TABLE IF EXISTS trm_concept_pc_link;

-- Table: public.trm_concept

-- DROP TABLE public.trm_concept;

CREATE TABLE public.trm_concept
(
  pid bigint NOT NULL,
  code character varying(100) NOT NULL,
  codesystem_pid bigint,
  display character varying(400),
  index_status bigint,
  CONSTRAINT trm_concept_pkey PRIMARY KEY (pid)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.trm_concept
  OWNER TO postgres;
GRANT ALL ON TABLE public.trm_concept TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE public.trm_concept TO endeavour;

-- Index: public.idx_code

-- DROP INDEX public.idx_code;

CREATE INDEX idx_code
  ON public.trm_concept
  USING btree
  (code COLLATE pg_catalog."default");

-- Index: public.idx_code_system

-- DROP INDEX public.idx_code_system;

CREATE UNIQUE INDEX idx_code_system
  ON public.trm_concept
  USING btree
  (code COLLATE pg_catalog."default", codesystem_pid);

-- Table: public.trm_concept_pc_link

-- DROP TABLE public.trm_concept_pc_link;

CREATE TABLE public.trm_concept_pc_link
(
  pid bigint NOT NULL,
  rel_type integer,
  child_pid bigint NOT NULL,
  codesystem_pid bigint NOT NULL,
  parent_pid bigint NOT NULL,
  CONSTRAINT trm_concept_pc_link_pkey PRIMARY KEY (pid)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.trm_concept_pc_link
  OWNER TO postgres;
GRANT ALL ON TABLE public.trm_concept_pc_link TO postgres;
GRANT SELECT, UPDATE, INSERT, DELETE ON TABLE public.trm_concept_pc_link TO endeavour;
