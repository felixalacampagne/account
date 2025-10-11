-- noinspection SqlNoDataSourceInspectionForFile

-- https://dev.mysql.com/doc/refman/8.4/en/load-data.html
LOAD DATA LOCAL INFILE  
'csv/account_h2.csv'
INTO TABLE account  
FIELDS TERMINATED BY ';' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(ID,RANKING,ADDRESS,CODE,CONTACT,CURRENCY,CURRENCYFORMAT,DESCRIPTION,STATEMENTREF,SWIFTBIC);

-- TRUE/FALSE in csv must be manually converted to 1/0 values 
-- null fields in csv must be manually set to null
LOAD DATA LOCAL INFILE  
'csv/transaction_h2.csv'
INTO TABLE transaction  
FIELDS TERMINATED BY ';' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(BALANCE,@vchk,CHECKEDBALANCE,@vcred,@vdeb,SORTEDBALANCE,TRANSACTIONDATE,ACCOUNTID,ID,COMMENT,STATEMENTREF,TRANSACTIONTYPE)
set 
CHECKED = (@vchk = 'TRUE'),
CREDIT = NULLIF(@vcred,'') ,
DEBIT = NULLIF(@vdeb,'')
;

-- rows with null account id are not imported (I think). null account id is expressed as ;;
-- empty field is interpreted as empty string so fails when the field is a number, ie. accountid
-- so accountid must be explicitly set to NULL if the field is empty
LOAD DATA LOCAL INFILE  
'csv/phoneaccount_h2.csv'
INTO TABLE phoneaccount  
FIELDS TERMINATED BY ';' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(RANKING,@vaccid,ID,ACCOUNTTYPE,CODE,COMMENT,COMMUNICATION)
set
ACCOUNTID = NULLIF(@vaccid,'')
;

LOAD DATA LOCAL INFILE  
'csv/phonetransaction_h2.csv'
INTO TABLE phonetransaction  
FIELDS TERMINATED BY ';' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(PAYDATE,SENTDATE,TRANSACTIONDATE,ID,RECIPIENTPHONEACCOUNTID,SENDERPHONEACCOUNTID,AMOUNT,COMMENT,COMMUNICATION,ERRORSTATUS);

LOAD DATA LOCAL INFILE  
'csv/standingorder_h2.csv'
INTO TABLE standingorder  
FIELDS TERMINATED BY ';' 
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
IGNORE 1 LINES
(AMOUNT,ENTRYDATE,PAYDATE,ACCOUNTID,COUNT,ID,COMMENT,PERIOD,TRANSACTIONTYPE);
