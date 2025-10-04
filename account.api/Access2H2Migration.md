# Access to H2 migration

Before starting the migration ensure as that the transaction reconciliation using Excel is as up to date
as possible since it may not work with the new H2 database.

The export from UCanaccess is fairly straight forward once the package is downloaded - there is a console.bat, as indicated 
by the documentation, and running it does connect to the database. Exporting the tables is also simple enough;

    export -t account ../../csv/account.csv;
    export -t transaction ../../csv/transaction.csv;
    export -t standingorders ../../csv/standingorders.csv;
    export -t PhoneAccounts ../../csv/phoneaccount.csv;
    export -t PhoneTrans ../../csv/phonetransaction.csv;
    export -t Prefs ../../csv/prefs.csv;

WARNING: The CSV files MUST be loaded into UE in UTF-8 mode - changing to UTF-8 after loading doesn't seem to
work.

Column header update and invalid entry fixing is done by running CSVHeaderFixTest test. The UCanAccess CSV files should
be put into account.api/csv before running CSVHeaderFixTest. The modified files are created with the '_h2cols' suffix.

Run DatabasePopulateTest (need to remove the @Ignore first) to create a new accountH2create DB in account.api/db populated
with the content of the modified CSV files.

The standalone development AccountApplication will run using the db in account.api/db.
