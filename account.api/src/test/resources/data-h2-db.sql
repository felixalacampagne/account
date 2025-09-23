-- noinspection SqlNoDataSourceInspectionForFile

insert into account (id,code,description,address,contact,currency,currencyformat,statementref,ranking,swiftbic)
SELECT * FROM CSVREAD('src/test/resources/csv/account_h2cols.csv', null, 'charset=UTF-8 fieldSeparator=;');

ALTER SEQUENCE account_seq RESTART WITH (select max(id)+1 from account);

insert into transaction (id,accountid,transactiondate,transactiontype,comment,checked,credit,debit,balance,checkedbalance,sortedbalance,statementref)
SELECT id,accountid, cast(parsedatetime(transactiondate, 'yyyy-MM-dd HH:mm:ss') as date) as transactiondate, transactiontype,comment,checked,credit,debit,balance,checkedbalance,sortedbalance,statementref
FROM CSVREAD('src/test/resources/csv/transaction_h2cols.csv', null,  'charset=UTF-8 fieldSeparator=;  null=(null)');

ALTER SEQUENCE transaction_seq RESTART WITH (select max(id)+1 from transaction);

insert into standingorder (id  ,period  ,count  ,paydate      ,entrydate  ,comment,accountid,transactiontype,amount)
SELECT id  ,period  ,count  ,
cast(parsedatetime(paydate, 'yyyy-MM-dd HH:mm:ss') as date) as paydate,
cast(parsedatetime(entrydate, 'yyyy-MM-dd HH:mm:ss') as date) as entrydate,
comment,accountid,transactiontype,amount
FROM CSVREAD('src/test/resources/csv/standingorders_h2cols.csv', null,  'charset=UTF-8 fieldSeparator=;');

ALTER SEQUENCE standingorder_seq RESTART WITH (select max(id)+1 from standingorder);

insert into phoneaccount (id  ,accounttype,accountid,code    ,comment,ranking,communication)
SELECT id  ,accounttype, NULLIF(accountid, 0) ,code    ,comment,ranking,communication FROM CSVREAD('src/test/resources/csv/phoneaccount_h2cols.csv', null, 'charset=UTF-8 fieldSeparator=;');

ALTER SEQUENCE phoneaccount_seq RESTART WITH (select max(id)+1 from phoneaccount);

insert into phonetransaction (id  ,paydate  ,senderphoneaccountid,recipientphoneaccountid,amount  ,communication,comment,sentdate  ,transactiondate,errorstatus)
SELECT id  ,
cast(parsedatetime(paydate, 'yyyy-MM-dd HH:mm:ss') as date) as paydate,
senderphoneaccountid,recipientphoneaccountid,amount  ,communication,comment,
cast(parsedatetime(sentdate, 'yyyy-MM-dd HH:mm:ss') as date) as sentdate,
cast(parsedatetime(transactiondate, 'yyyy-MM-dd HH:mm:ss') as date) as transactiondate,
errorstatus
FROM CSVREAD('src/test/resources/csv/phonetransaction_h2cols.csv', null, 'charset=UTF-8 fieldSeparator=; null=(null)');

ALTER SEQUENCE phonetransaction_seq RESTART WITH (select max(id)+1 from phonetransaction);

insert into prefs (id, name,text,numeric)
SELECT (select nextval('prefs_seq') ) as id, name,text,numeric FROM CSVREAD('src/test/resources/csv/prefs_h2cols.csv', null, 'charset=UTF-8 fieldSeparator=; null=(null)');
