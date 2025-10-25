-- noinspection SqlNoDataSourceInspectionForFile

insert into account (id,code,description,address,contact,currency,currencyformat,statementref,ranking,swiftbic)
SELECT * FROM CSVREAD('csv/h2/account_h2cols.csv', null, 'charset=UTF-8 fieldSeparator=;');

--ALTER SEQUENCE account_seq RESTART WITH (select max(id)+1 from account);
ALTER TABLE account ALTER COLUMN ID RESTART WITH (SELECT MAX(ID) FROM account) + 1;

insert into transaction (id,accountid,transactiondate,transactiontype,comment,checked,credit,debit,balance,checkedbalance,sortedbalance,statementref)
SELECT id,accountid, cast(parsedatetime(transactiondate, 'yyyy-MM-dd HH:mm:ss') as date) as transactiondate, transactiontype,comment,checked,credit,debit,balance,checkedbalance,sortedbalance,statementref
FROM CSVREAD('csv/h2/transaction_h2cols.csv', null,  'charset=UTF-8 fieldSeparator=;  null=(null)');

-- ALTER SEQUENCE transaction_seq RESTART WITH (select max(id)+1 from transaction);
ALTER TABLE transaction ALTER COLUMN ID RESTART WITH (SELECT MAX(ID) FROM transaction) + 1;

insert into standingorder (id  ,period  ,count  ,paydate      ,entrydate  ,comment,accountid,transactiontype,amount)
SELECT id  ,period  ,count  ,
cast(parsedatetime(paydate, 'yyyy-MM-dd HH:mm:ss') as date) as paydate,
cast(parsedatetime(entrydate, 'yyyy-MM-dd HH:mm:ss') as date) as entrydate,
comment,accountid,transactiontype,amount
FROM CSVREAD('csv/h2/standingorders_h2cols.csv', null,  'charset=UTF-8 fieldSeparator=;');

-- ALTER SEQUENCE standingorder_seq RESTART WITH (select max(id)+1 from standingorder);
ALTER TABLE standingorder ALTER COLUMN ID RESTART WITH (SELECT MAX(ID) FROM standingorder) + 1;

insert into phoneaccount (id  ,accounttype,accountid,code    ,comment,ranking,communication)
SELECT id  ,accounttype, NULLIF(accountid, 0) ,code    ,comment,ranking,communication 
FROM CSVREAD('csv/h2/phoneaccount_h2cols.csv', null, 'charset=UTF-8 fieldSeparator=;');

-- ALTER SEQUENCE phoneaccount_seq RESTART WITH (select max(id)+1 from phoneaccount);
ALTER TABLE phoneaccount ALTER COLUMN ID RESTART WITH (SELECT MAX(ID) FROM phoneaccount) + 1;

insert into phonetransaction (id  ,paydate  ,senderphoneaccountid,recipientphoneaccountid,amount  ,communication,comment,sentdate  ,transactiondate,errorstatus)
SELECT id  ,
cast(parsedatetime(paydate, 'yyyy-MM-dd HH:mm:ss') as date) as paydate,
senderphoneaccountid,recipientphoneaccountid,amount  ,communication,comment,
cast(parsedatetime(sentdate, 'yyyy-MM-dd HH:mm:ss') as date) as sentdate,
cast(parsedatetime(transactiondate, 'yyyy-MM-dd HH:mm:ss') as date) as transactiondate,
errorstatus
FROM CSVREAD('csv/h2/phonetransaction_h2cols.csv', null, 'charset=UTF-8 fieldSeparator=; null=(null)');

-- ALTER SEQUENCE phonetransaction_seq RESTART WITH (select max(id)+1 from phonetransaction);
ALTER TABLE phonetransaction ALTER COLUMN ID RESTART WITH (SELECT MAX(ID) FROM phonetransaction) + 1;

--insert into prefs (id, name,text,numeric)
--SELECT (select nextval('prefs_seq') ) as id, name,text,numeric FROM CSVREAD('csv/h2/prefs_h2cols.csv', null, 'charset=UTF-8 fieldSeparator=; null=(null)');
