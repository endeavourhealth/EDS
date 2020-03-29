use staging_barts;

drop table if exists clinical_event;
drop table if exists clinical_event_latest;
drop table if exists clinical_event_target;

create table clinical_event
(
    exchange_id                    char(36)     NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received                    datetime     NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum                bigint       NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    event_id					   bigint		NOT NULL COMMENT 'event id of the clincal event',
    active_ind           		   bool       	NOT NULL COMMENT 'whether an active record or not (deleted), from ACTIVE_IND',
    person_id          			   int	 		NOT NULL COMMENT 'reference to the person Id',
    encounter_id                   int  	    COMMENT 'reference to the encounter Id',
    order_id                       bigint       COMMENT 'Order Id, references ORDER FILE',
    parent_event_id                bigint       COMMENT 'reference to the parent Id',
    event_cd                       varchar(20)  COMMENT 'Coding of the event',
    lookup_event_code			   varchar(255) COMMENT 'the lookup code',
    lookup_event_term			   varchar(255) COMMENT 'the lookup term',
    event_start_dt_tm              datetime   	COMMENT 'start datetime of event',
    event_end_dt_tm                datetime 	COMMENT 'end datetime of the event',
    clinically_significant_dt_tm   datetime     COMMENT 'clinical date of event',
    event_class_cd                 int 	        COMMENT 'the class of the event ie numeric',
    lookup_event_class             varchar(255) COMMENT 'the lookup class of the event ie numeric',
    event_result_status_cd     	   int	 		COMMENT 'the status of the result, ie cancelled, final etc',
    lookup_event_result_status     varchar(255)	COMMENT 'the lookup status of the result, ie cancelled, final etc',
    event_result_txt     		   varchar(255) COMMENT 'textual result',
    event_result_nbr               int	 		COMMENT 'numberical result',
    processed_numeric_result         double	 	COMMENT 'processed numberical result',
    comparator		               varchar(255)	COMMENT 'Comparator',
    event_result_dt				   datetime		COMMENT 'the date of the result',
    normalcy_cd		    		   int	 		COMMENT 'the normalcy code',
    lookup_normalcy_code		   varchar(255)	COMMENT 'the lookup normalcy code',
    normal_range_low_txt		   varchar(255)	COMMENT 'the textual low range',
    normal_range_low_value		   double		COMMENT 'the textual low range value',
    normal_range_high_txt		   varchar(255)	COMMENT 'the textual high range',
    normal_range_high_value		   double		COMMENT 'the textual high range value',
    event_performed_dt_tm		   datetime		COMMENT 'the datetime the event was performed',
    event_performed_prsnl_id	   int	 		COMMENT 'Reference to the person who performed the event',
    event_tag					   varchar(255)	COMMENT 'textual tag of the code',
    event_title_txt		   		   varchar(255)	COMMENT 'the title of the event',
    event_result_units_cd		   int	 		COMMENT 'the code of the units for the result',
    lookup_result_units_code	   varchar(255)	COMMENT 'the lookup code of the units for the result',
    record_status_cd		   	   int 			COMMENT 'the code for the record status',
    lookup_record_status_code	   varchar(255)	COMMENT 'the lookup code for the record status',
    lookup_mrn				   	   varchar(255)	COMMENT 'the lookup mrn for the person',
    audit_json                     mediumtext   null comment 'Used for Audit Purposes',
    CONSTRAINT pk_condition_cds PRIMARY KEY (exchange_id, event_id)
);

CREATE INDEX ix_clinical_event_checksum_helper on clinical_event (event_id, dt_received);

create table clinical_event_latest
(
    exchange_id                    char(36)     NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    dt_received                    datetime     NOT NULL COMMENT 'date time this record was received into Discovery',
    record_checksum                bigint       NOT NULL COMMENT 'checksum of the columns below to easily spot duplicates',
    event_id					   bigint		NOT NULL COMMENT 'event id of the clincal event',
    active_ind           		   bool       	NOT NULL COMMENT 'whether an active record or not (deleted), from ACTIVE_IND',
    person_id          			   int	 		NOT NULL COMMENT 'reference to the person Id',
    encounter_id                   int  	    COMMENT 'reference to the encounter Id',
    order_id                       bigint       COMMENT 'Order Id, references ORDER FILE',
    parent_event_id                bigint      COMMENT 'reference to the parent Id',
    event_cd                       varchar(20)  COMMENT 'Coding of the event',
    lookup_event_code			   varchar(255) COMMENT 'the lookup code',
    lookup_event_term			   varchar(255) COMMENT 'the lookup term',
    event_start_dt_tm              datetime   	COMMENT 'start datetime of event',
    event_end_dt_tm                datetime 	COMMENT 'end datetime of the event',
    clinically_significant_dt_tm   datetime     COMMENT 'clinical date of event',
    event_class_cd                 int 	        COMMENT 'the class of the event ie numeric',
    lookup_event_class             varchar(255) COMMENT 'the lookup class of the event ie numeric',
    event_result_status_cd     	   int	 		COMMENT 'the status of the result, ie cancelled, final etc',
    lookup_event_result_status     varchar(255)	COMMENT 'the lookup status of the result, ie cancelled, final etc',
    event_result_txt     		   varchar(255) COMMENT 'textual result',
    event_result_nbr               int	 		COMMENT 'numberical result',
    processed_numeric_result         double	 	COMMENT 'processed numberical result',
    comparator		               varchar(255)	COMMENT 'Comparator',
    event_result_dt				   datetime		COMMENT 'the date of the result',
    normalcy_cd		    		   int	 		COMMENT 'the normalcy code',
    lookup_normalcy_code		   varchar(255)	COMMENT 'the lookup normalcy code',
    normal_range_low_txt		   varchar(255)	COMMENT 'the textual low range',
    normal_range_low_value		   double		COMMENT 'the textual low range value',
    normal_range_high_txt		   varchar(255)	COMMENT 'the textual high range',
    normal_range_high_value		   double 		COMMENT 'the textual high range value',
    event_performed_dt_tm		   datetime		COMMENT 'the datetime the event was performed',
    event_performed_prsnl_id	   int	 		COMMENT 'Reference to the person who performed the event',
    event_tag					   varchar(255)	COMMENT 'textual tag of the code',
    event_title_txt		   		   varchar(255)	COMMENT 'the title of the event',
    event_result_units_cd		   int	 		COMMENT 'the code of the units for the result',
    lookup_result_units_code	   varchar(255)	COMMENT 'the lookup code of the units for the result',
    record_status_cd		   	   int 			COMMENT 'the code for the record status',
    lookup_record_status_code	   varchar(255)	COMMENT 'the lookup code for the record status',
    lookup_mrn				   	   varchar(255)	COMMENT 'the lookup mrn for the person',
    audit_json                     mediumtext   null comment 'Used for Audit Purposes',
    CONSTRAINT pk_condition_cds PRIMARY KEY (exchange_id, event_id)
);

create table clinical_event_target
(
    exchange_id                    char(36)     NOT NULL COMMENT 'links to audit.exchange table (but on a different server)',
    unique_id                  	   varchar(255) NOT NULL COMMENT ' unique ID derived from source IDs ',
    is_delete           		   bool       	NOT NULL COMMENT 'whether an active record or not (deleted), from ACTIVE_IND',
    event_id					   bigint		NOT NULL COMMENT 'event id of the clincal event',
    person_id          			   int	 		NOT NULL COMMENT 'reference to the person Id',
    encounter_id                   int  	    COMMENT 'reference to the encounter Id',
    order_id                       bigint       COMMENT 'Order Id, references ORDER FILE',
    parent_event_id                bigint      COMMENT 'reference to the parent Id',
    lookup_event_code			   varchar(255) COMMENT 'the lookup code',
    lookup_event_term			   varchar(255) COMMENT 'the lookup term',
    clinically_significant_dt_tm   datetime     COMMENT 'clinical date of event',
    processed_numeric_result         double	 	COMMENT 'processed numberical result',
    comparator		               varchar(255)	COMMENT 'Comparator',
    normalcy_cd		    		   int	 		COMMENT 'the normalcy code',
    lookup_normalcy_code		   varchar(255)	COMMENT 'the lookup normalcy code',
    normal_range_low_value		   double		COMMENT 'the textual low range value',
    normal_range_high_value		   double 		COMMENT 'the textual high range value',
    event_performed_dt_tm		   datetime		COMMENT 'the datetime the event was performed',
    event_performed_prsnl_id	   int	 		COMMENT 'Reference to the person who performed the event',
    event_title_txt		   		   varchar(255)	COMMENT 'the title of the event',
    lookup_result_units_code	   varchar(255)	COMMENT 'the lookup code of the units for the result',
    lookup_record_status_code	   varchar(255)	COMMENT 'the lookup code for the record status',
    lookup_mrn				   	   varchar(255)	COMMENT 'the lookup mrn for the person',
    audit_json                     mediumtext   null comment 'Used for Audit Purposes',
    is_confidential            	   bool COMMENT 'if this procedure should be confidential or not',
    event_result_txt varchar(255) COMMENT 'free text result string',
    CONSTRAINT pk_condition_cds PRIMARY KEY (exchange_id, event_id)
);

