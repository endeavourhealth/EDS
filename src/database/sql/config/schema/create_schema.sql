
CREATE TABLE postcode_reference (
	postcode_no_space VARCHAR(8) NOT NULL,
	postcode VARCHAR(8) NOT NULL,
	lsoa_code VARCHAR(9),
	lsoa_name VARCHAR(255),
	northing REAL,
	easting REAL,
	ward VARCHAR(9),
	ward_1998 VARCHAR(6), --obsolete, but needed to match townsend scores
	commissioning_region VARCHAR(3),
	ccg VARCHAR(3),
	townsend_score REAL,
	constraint pd_postcode_reference_postcode_no_space primary key (postcode)
 );
