-- noinspection SqlNoDataSourceInspectionForFile

LOAD DATA LOCAL INFILE
'csv/account_h2cols.csv'
INTO TABLE account
FIELDS TERMINATED BY ';'
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
(id,code,description,address,contact,currency,currencyformat,statementref,ranking,swiftbic);

UPDATE account_seq SET next_val=(select max(id)+1 from account);

LOAD DATA LOCAL INFILE
'csv/transaction_h2cols.csv'
INTO TABLE transaction
FIELDS TERMINATED BY ';'
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
(id,accountid,transactiondate,transactiontype,comment,checked,credit,debit,balance,checkedbalance,sortedbalance,statementref);

UPDATE transaction_seq SET next_val=(select max(id)+1 from transaction);

LOAD DATA LOCAL INFILE
'csv/phoneaccount_h2cols.csv'
INTO TABLE phoneaccount
FIELDS TERMINATED BY ';'
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
(id,accounttype,accountid,code,comment,ranking,communication,@dummy,@dummy,@dummy);

UPDATE phoneaccount_seq SET next_val=(select max(id)+1 from phoneaccount);


LOAD DATA LOCAL INFILE
'csv/phonetransaction_h2cols.csv'
INTO TABLE phonetransaction
FIELDS TERMINATED BY ';'
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
(id,paydate,senderphoneaccountid,recipientphoneaccountid,amount,communication,comment,sentdate,transactiondate,errorstatus);

UPDATE phonetransaction_seq SET next_val=(select max(id)+1 from phonetransaction);

LOAD DATA LOCAL INFILE
'csv/standingorders_h2cols.csv'
INTO TABLE standingorder
FIELDS TERMINATED BY ';'
ENCLOSED BY '"'
LINES TERMINATED BY '\n'
(id,period,count,paydate,entrydate,comment,accountid,transactiontype,amount);

UPDATE standingorder_seq SET next_val=(select max(id)+1 from standingorder);

