-- Use this to import the cerner_code_value_ref.csv file into your local DB

LOAD DATA LOCAL INFILE 'C:/path/to/the/csv/file/cerner_code_value_ref.csv' 
INTO TABLE publisher_transform.cerner_code_value_ref 
FIELDS TERMINATED BY ',' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 ROWS
(CODE_VALUE_CD, @DATE, ACTIVE_IND, CODE_DESC_TXT, CODE_DISP_TXT, CODE_MEANING_TXT, CODE_SET_NBR, CODE_SET_DESC_TXT, ALIAS_NHS_CD_ALIAS
)
set date = STR_TO_DATE(@DATE, '%d/%m/%Y'),
 service_id= 'put-service-uuid-here';