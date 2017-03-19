-- Table: public.postcode_reference

-- DROP TABLE public.postcode_reference;

CREATE TABLE public.postcode_reference
(
  postcode_no_space character varying(8) NOT NULL,
  postcode character varying(8) NOT NULL,
  lsoa_code character varying(9),
  lsoa_name character varying(255),
  msoa_code character varying(9),
  msoa_name character varying(255),
  ward character varying(9),
  ward_1998 character varying(6),
  ccg character varying(3),
  townsend_score real,
  CONSTRAINT pd_postcode_reference_postcode_no_space PRIMARY KEY (postcode_no_space)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.postcode_reference
  OWNER TO postgres;
