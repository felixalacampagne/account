Attribute VB_Name = "mysql_reconcile"
Option Explicit
' 2025-10-22 17:31

' Settings field names:
'    deletepatterns - marks the column containing strings to delete from the statement description
'    dbnature       - indicates database type for special processing, eg. access
'    dblocation     - text used for the DSN connection
'    accounttype    - format of csv, eg. barclays, keytrade, cbc
'    accountcode    - required when the code is not part of the original statement csv
'    modulepath     - directory containing macros to be updated
'    workbookpath   - column containing directory paths with workbooks for macro updates. All workbooks
'                     in each directory will be updated with macros from modulepath

Public COL_ACCNUM As Integer
Public COL_STATNUM As Integer
Public COL_DATE As Integer
Public COL_DESC As Integer
Public COL_VALUE As Integer

Public Const COL_DBSEQ = 13
Public Const COL_FAILREASON = 14
Public Const COL_DBDESC = 15
Public Const QUITNOW = "QUIT"
Public Const ALLOK = "OK"
'                               Access        MySQL
Dim DBCOL_ID As String        ' "sequence"    id
Dim DBCOL_DATE As String      ' "Date"        transactiondate
Dim DBCOL_TYPE As String      ' "Type"        transactiontype
Dim DBCOL_STMNTREF As String  ' "Stid"        statementref
Dim DB_FALSE As String        ' "false"       0
Dim DB_TRUE As String         ' "true"        1
Dim DB_NUMQUOTE As String     ' ""            single quote
Dim DB_DATEFMT As String

Dim DB_COLQUOTEO As String    ' "["           empty
Dim DB_COLQUOTEC As String    ' "]"           empty
Dim DBCOL_ACCID As String     ' "acc_id"      id
Dim DBCOL_ACCCODE As String   ' "acc_code"    code
Dim DBCOL_ACCCURR As String   ' "acc_curr"    currency

Dim gLastNewID As Long ' ID of the last added record (-1 if none)

Sub reconcileStatement()
Dim db As New ADODB.Connection
Dim acc_id As Long
Dim dblocn As String
Dim failreason As String
Dim dbnature As String

   initCSVColumns

   ' This is pretty ugly but means I don't have to keep commenting/uncommenting when switching between DBs
   dbnature = Range("Settings!dbnature")
   If dbnature = "access" Then
      DBCOL_ID = "sequence"
      DBCOL_DATE = "Date"
      DBCOL_TYPE = "Type"
      DBCOL_STMNTREF = "Stid"
      DB_FALSE = "false"
      DB_TRUE = "true"
      DB_NUMQUOTE = ""
      DB_DATEFMT = "\#dd\/mm\/yyyy\#"
      DB_COLQUOTEO = "["
      DB_COLQUOTEC = "]"
      DBCOL_ACCID = "acc_id"
      DBCOL_ACCCODE = "acc_code"
      DBCOL_ACCCURR = "acc_curr"
   Else
      DBCOL_ID = "id"
      DBCOL_DATE = "transactiondate"
      DBCOL_TYPE = "transactiontype"
      DBCOL_STMNTREF = "statementref"
      DB_FALSE = "0"
      DB_TRUE = "1"
      DB_NUMQUOTE = "'"
      DB_DATEFMT = "'yyyy-mm-dd'"
      DB_COLQUOTEO = ""
      DB_COLQUOTEC = ""
      DBCOL_ACCID = "id"
      DBCOL_ACCCODE = "code"
      DBCOL_ACCCURR = "currency"
   End If
   
   dblocn = Range("Settings!dblocation")
   db.CursorLocation = adUseClient
   db.Open "DSN=" & dblocn
    
   acc_id = getAccountID(db, failreason)
   
   If acc_id > -1 Then
      checkStatement db, acc_id
   Else
      MsgBox "No matching account found in database." & vbCrLf & failreason
   End If
   
   db.Close
End Sub
Function getAccountID(db As ADODB.Connection, ByRef reason As String) As Long
Dim rs As ADODB.Recordset
Dim sql As String
Dim ibanacc As String
Dim sheetacc As String
'Dim sheetcur As String
Dim normacc As String
Dim accid As Long
  
   ' Get the IBAN account
   ibanacc = ActiveSheet.Cells(2, COL_ACCNUM)
   '07 Nov 2021 Account numbers now contain spaces
   sheetacc = StrRepl(ibanacc, " ", "")
   sheetacc = StrRepl(sheetacc, "-", "")
   'sheetcur = ActiveSheet.Cells(2, COL_CURRENCY)
   reason = ""
   
   sql = "select " & DBCOL_ACCID & ", " & DBCOL_ACCCODE & ", " & DBCOL_ACCCURR & " from account"
   
   ' Currency not required (and not present in all statements
   'sql = sql & " where " & DBCOL_ACCCURR & " = '" & sheetcur & "'"
   Set rs = New ADODB.Recordset
   rs.Open sql, db, adOpenStatic, adLockPessimistic
   
   rs.MoveFirst
   accid = -1
   
   Do While Not rs.EOF
      normacc = StrRepl(rs(DBCOL_ACCCODE), "-", "")
      normacc = StrRepl(normacc, " ", "")
      Debug.Print rs(DBCOL_ACCID), normacc, rs(DBCOL_ACCCODE), rs(DBCOL_ACCCURR)
      ' Duplicate account codes exist from the pre-EURO days
      If normacc = sheetacc Then
         'If sheetcur = rs(DBCOL_ACCCURR) Then
            accid = rs(DBCOL_ACCID)
            Exit Do
         'End If
      End If
      rs.MoveNext
   Loop
   rs.Close
   If accid = -1 Then
      reason = "Account: " & ibanacc ' & " Currency: " & sheetcur
   End If
   getAccountID = accid
End Function

Sub checkStatement(db As ADODB.Connection, accid As Long)
Dim rs As ADODB.Recordset
Dim sql As String
Dim rownum As Long
Dim amtstr As String
Dim stmntamount As Double
Dim credit As Double
Dim debit As Double
Dim amtdate As Date
Dim transsql As String
Dim seqid As Long
Dim notrcncld As String
Dim cntchange As Long
Dim bookmark As Variant
Dim actionstr As String
Dim accidint As Integer
Dim msg As String
Dim dbnullcol As String
Dim dbamtcol As String
Dim dbamtval As Double
Dim first As Boolean
Dim datematch As Boolean
Dim accidsql As String
Dim rowstyle As Integer
Dim i As Integer

   accidsql = DB_NUMQUOTE & accid & DB_NUMQUOTE

   db.BeginTrans
   sql = "select * from transaction where accountid=" & accidsql
   sql = sql & " and checked=" & DB_FALSE
   sql = sql & " order by " & DBCOL_DATE & " asc, " & DBCOL_ID & " asc"
   'mysql gives data type mismatch if id is not in quotes!
   'sql = "select * from transaction where accountid='" & accid & "' and checked=0 order by transactiondate asc, id asc"
   
   Set rs = New ADODB.Recordset
   rs.Open sql, db, adOpenStatic

   ' This is not valid. All items can be checked from previous reconciliation
   ' and new statement can contain new items to be added, eg. interest payments
   ' on savings account.
   If rs.EOF Then
      msg = "No unchecked transaction found for account." & vbCrLf & "All statement items must be added." & vbCrLf & "Continue?"
      i = MsgBox(msg, vbYesNo, "No unchecked transactions")
      If i <> vbYes Then
         rs.Close
         db.RollbackTrans
         Exit Sub
      End If
   End If
   
   With ActiveSheet
      rownum = 2
      ' Stop when an empty value cell is reached as value is a required value
      Do While Trim(.Cells(rownum, COL_VALUE).Text) <> ""
         credit = 0#
         debit = 0#
         amtstr = .Cells(rownum, COL_VALUE).Text
         amtdate = .Cells(rownum, COL_DATE).Value
         amtstr = StrRepl(amtstr, ",", ".")
         stmntamount = Val(amtstr)
         
         If stmntamount < 0 Then
            'debit = stmntamount * -1#
            dbamtval = stmntamount * -1#
            dbamtcol = "debit"
            dbnullcol = "credit"
         Else
            'credit = stmntamount
            dbamtval = stmntamount
            dbamtcol = "credit"
            dbnullcol = "debit"
         End If
         
         datematch = False
         first = False
         seqid = 0
         actionstr = ""
         If Not rs.BOF Then ' BOF only true when there are zero records in recordset
            rs.MoveFirst
         End If
         Do While Not rs.EOF
            If IsNull(rs(dbnullcol)) And Not IsNull(rs(dbamtcol)) Then
               If dbamtval = CDbl(rs(dbamtcol)) Then
                  If Abs(DateDiff("d", rs(DBCOL_DATE), amtdate)) < 3 Then
                     datematch = True
                     first = False
                     seqid = rs(DBCOL_ID)
                     Exit Do
                  ElseIf Not first Then
                     ' mark the first matching value even if the date does not match
                     ' as this will be the record suggested if not date match is found
                     first = True
                     seqid = rs(DBCOL_ID)
                     bookmark = rs.bookmark
                  End If
               End If
            End If
            rs.MoveNext
         Loop

         ' Items in the recordset are being updated - avoiding the already updated
         ' ones should help avoid problems with repetitive equal payments.
         ' There is currently no way to remove the items which have already been checked as part of
         ' the current reconciliation - recordset can't be refreshed during a transaction
         ' and the recordset itself is not editable - guess I will need to make a
         ' copy of the recordset which can be modified...
         ' transsql = transsql & " and checked=0"
         notrcncld = ""
         rowstyle = 1
         If rs.EOF And Not first Then
            Debug.Print "Amount not found: " & stmntamount
            notrcncld = addTransaction(accid, rs, db, .Rows(rownum))
            If notrcncld = ALLOK Then
               rowstyle = 2
               seqid = gLastNewID
            ElseIf notrcncld = QUITNOW Then
               ' Emergency bailout button pressed!
               rs.Close
               db.RollbackTrans
               Exit Sub
            Else
               rowstyle = 0
            End If
            
            .Cells(rownum, COL_FAILREASON) = notrcncld
         ElseIf datematch Then   ' Clean match
            actionstr = rs("comment")
            notrcncld = reconcileTransaction(rs, db, .Rows(rownum))
         Else ' Should mean that first is true and bookmark is set
            rs.bookmark = bookmark
            actionstr = rs("comment")
            notrcncld = "Amount matched with too great date difference"
            notrcncld = reconoraddTransaction(accid, rs, db, .Rows(rownum), notrcncld, actionstr)
            rowstyle = 2
         End If
            
         
         If notrcncld = ALLOK Then
            cntchange = cntchange + 1
            setRowStyle .Rows(rownum), rowstyle
         End If
         
         .Cells(rownum, COL_DBSEQ) = seqid
         .Cells(rownum, COL_FAILREASON) = notrcncld
         
         ' This is misleading if reconoraddTransaction resulted in a new row being added as
         ' the comment is that of the row with the large date difference. Need to refactor
         ' so that the comment indicates that a new row was added
         .Cells(rownum, COL_DBDESC) = actionstr

         rownum = rownum + 1
         
         ' Need to remove the item that has just been checked so that multiple items
         ' with the same amount don't always result in the first one being repeatedly checked
         rs.Close
         rs.Open sql, db, adOpenStatic

      Loop
   End With
   rs.Close
   
   
   If cntchange > 0 Then
      msg = "Changes made: " & cntchange & vbCrLf & vbCrLf & "Commit changes?"
      
      i = MsgBox(msg, vbYesNo, "Commit changes")
      If i <> vbYes Then
         db.RollbackTrans
      Else
         db.CommitTrans
      End If
   Else
      db.RollbackTrans
   End If

    
End Sub

Function addTransaction(accid As Long, rs As ADODB.Recordset, db As ADODB.Connection, arow As Range) As String
Dim msg As String
Dim i As Integer

   msg = "Transaction not present in database:" & vbCrLf
   msg = msg & "Date:    " & arow.Columns(COL_DATE) & vbCrLf
   msg = msg & "Amount:  " & arow.Columns(COL_VALUE) & vbCrLf
   msg = msg & "Description:  " & arow.Columns(COL_DESC) & vbCrLf
   msg = msg & vbCrLf
   msg = msg & "Add to database?"
   i = MsgBox(msg, vbYesNoCancel, "Add missing transaction")
   If i <> vbYes Then
      If i = vbCancel Then
         addTransaction = QUITNOW
      Else
         addTransaction = "Transaction NOT added db at user request"
      End If
      Exit Function
   End If
   
   addTransaction = addtodb(accid, rs, db, arow)
   
End Function

Function addtodb(accid As Long, rs As ADODB.Recordset, db As ADODB.Connection, arow As Range) As String
Dim amt As String
Dim amtv As Double
Dim inssql As String
Dim sqldate As String
   amt = Trim$(arow.Columns(COL_VALUE))
   amt = StrRepl(amt, ",", ".")
   amtv = Val(amt)
   
   Dim amtcol As String
   Dim amtval As Double
   
   If amtv < 0 Then
      amtcol = "debit"
      amtval = amtv * -1
   Else
      amtcol = "credit"
      amtval = amtv
   End If

   ' #DD/MM/YYYY# works for access
   ' 'DD/MM/YYYY' does NOT work for mysql
   ' 'YYYY-MM-DD' is OK for MySQL.
   ' Would be simpler to use a date format for mysql and access instead of DATEQUOTEL/R
   sqldate = Format(arow.Columns(COL_DATE), DB_DATEFMT)

   ' Monumentally more complex than it needs to be since columns have changed for mysql and different
   ' types of quoting are required for different fields
   inssql = "insert into transaction (accountid, " & DBCOL_STMNTREF & ", checked, " & DB_COLQUOTEO & DBCOL_DATE & DB_COLQUOTEC & ", comment, " & DBCOL_TYPE & ", " & amtcol
   inssql = inssql & ") values ("
   inssql = inssql & DB_NUMQUOTE & accid & DB_NUMQUOTE & ", "
   inssql = inssql & "'" & arow.Columns(COL_STATNUM) & "'" & ", "
   inssql = inssql & DB_TRUE & ", "
   inssql = inssql & sqldate & ", "
   inssql = inssql & "'" & Left$(StrSanitize(arow.Columns(COL_DESC)), rs("comment").DefinedSize) & "', "
   inssql = inssql & "'STMNT', "
   inssql = inssql & DB_NUMQUOTE & amtval & DB_NUMQUOTE
   inssql = inssql & ")"
   
   Debug.Print "addtodb: " & inssql
   gLastNewID = -1
   db.Execute inssql
   
   ' This is supposed to return the ID of the added record. It works for Access DB and the command does not give error on mysql
   ' Would require re-write of entire code to get the new id returned so use a global value
   Dim rsid As New ADODB.Recordset
   rsid.Open "select @@Identity", db
   gLastNewID = rsid.Fields(0)
   Debug.Print "addtodb: added record id: " & gLastNewID
   rsid.Close
   
   addtodb = ALLOK
End Function

Function reconcileTransaction(rs As ADODB.Recordset, db As ADODB.Connection, arow As Range) As String
Dim stmntid As String
Dim updsql As String

   reconcileTransaction = ALLOK
   stmntid = arow.Columns(COL_STATNUM).Text
   If "" & rs(DBCOL_STMNTREF) = "" Then
      
      updsql = "update transaction set " & DBCOL_STMNTREF & "='" & stmntid & "', checked=" & DB_TRUE
      updsql = updsql & " where " & DBCOL_ID & "=" & DB_NUMQUOTE & rs(DBCOL_ID) & DB_NUMQUOTE
      Debug.Print "reconcileTransaction: " & updsql
      db.Execute updsql
      
      'rs.Edit
      'rs("statementref") = stmntid
      'rs("checked") = True
      'rs.Update
   ElseIf rs(DBCOL_STMNTREF) = stmntid Then
      updsql = "update transaction set checked=" & DB_TRUE & " where " & DBCOL_ID & "=" & DB_NUMQUOTE & rs(DBCOL_ID) & DB_NUMQUOTE
      Debug.Print "reconcileTransaction: " & updsql
      db.Execute updsql
      'rs.Edit
      'rs("checked") = True
      'rs.Update
   Else
      reconcileTransaction = "Statement id '" & rs(DBCOL_STMNTREF) & "' already present"
   End If
End Function

Function reconoraddTransaction(accid As Long, rs As ADODB.Recordset, db As ADODB.Connection, arow As Range, notrcncld As String, actionstr As String) As String
Dim msg As String
Dim amt As String
Dim amtv As Double
Dim i As Integer

   msg = notrcncld & ":" & vbCrLf
   msg = msg & "Date:    " & arow.Columns(COL_DATE) & "(Statement) " & rs(DBCOL_DATE) & "(Database)" & vbCrLf
   msg = msg & "Amount:  " & arow.Columns(COL_VALUE) & vbCrLf
   msg = msg & "Statement description:  " & arow.Columns(COL_DESC) & vbCrLf
   msg = msg & "Database description: " & rs("comment") & vbCrLf & vbCrLf
   msg = msg & "Yes=Match, No=Add transaction, Cancel=Do Nothing"

   i = MsgBox(msg, vbYesNoCancel, "Match or Add Transaction")
   Select Case i
   Case vbYes
        reconoraddTransaction = reconcileTransaction(rs, db, arow)
   Case vbNo
        actionstr = "Transaction added to db"
        reconoraddTransaction = addtodb(accid, rs, db, arow)
   Case Else
        actionstr = "NO MATCH for transaction"
        reconoraddTransaction = notrcncld
   End Select

End Function


Sub newTextSheet()
    Sheets(3).Select
    Sheets.Add
    Cells.Select
    
    ' F--king idiot Excel formats strings containing only numbers as exponents
    ' EVEN when told to format the cell as text. There is no way to format a cell as text
    ' there is only a format for numbers - and it's been like this for decades judging by the
    ' number of Google responses
    Selection.NumberFormat = "@"
    
    Range("A1").Select

End Sub

Function StrSanitize(strtoclean As String) As String
Dim rownum As Integer
Dim column As Integer
Dim sanistr As String
Dim delstr As String
'Range("Settings!deletepatterns").Row
sanistr = Trim(strtoclean)
On Error GoTo funcend ' in case 'deletepatterns' has not been configured
rownum = Range("Settings!deletepatterns").row + 1 ' Selection.Row + 1
column = Range("Settings!deletepatterns").column

With Worksheets("Settings")
   Do While .Cells(rownum, column).Text <> ""
      delstr = .Cells(rownum, column).Text
      sanistr = StrRepl(sanistr, delstr, "")
      rownum = rownum + 1
   Loop
End With

funcend:
sanistr = StrRepl(sanistr, "'", "") ' Make it safe to include in SQL string

' Hidden method on Application: removes leading/trailing space AND condenses multispaces into single space!!
sanistr = Application.Trim(sanistr)

StrSanitize = sanistr

End Function

Function StrRepl(ByVal Body As String, orig As String, repl As String, Optional cmp As VbCompareMethod = vbBinaryCompare) As String
'04 Aug 98  Skips the whole of the replacment string
Dim o As Long
Dim curpos As Long
Dim lorig As Long
Dim lrepl As Long
   curpos = 1
   lorig = Len(orig)
   lrepl = Len(repl)
   Do
      o = InStr(curpos, Body, orig, cmp)
      If o > 0 Then
         Body = Left$(Body, o - 1) & repl & Mid$(Body, o + lorig)
         curpos = o + lrepl
      End If
   Loop While o > 0

   StrRepl = Body
End Function

Sub setRowStyle(arow As Range, mode As Integer)
    With arow.Interior
        .Pattern = xlSolid
        .PatternColorIndex = xlAutomatic
        Select Case mode
        Case 0
            .ThemeColor = xlThemeColorAccent2
            .TintAndShade = 0.399975585192419
        Case 1
            .ThemeColor = xlThemeColorAccent3
            .TintAndShade = 0.399975585192419
        Case 2
            .ThemeColor = xlThemeColorAccent6
            .TintAndShade = 0.399975585192419
        End Select
         
        .PatternTintAndShade = 0
    End With
End Sub

Sub btnIncMonth2_Click()
Dim rowsdate As Integer
Dim rowedate As Integer
Dim coldate As Integer
Dim sdate As Date
Dim edate As Date
Dim str As String
Dim incby As Integer

   rowsdate = 4

   coldate = 2
   incby = 1
    
    
    With ActiveSheet
         str = .Cells(rowsdate, coldate).Text
         sdate = DateValue(str)
         sdate = DateAdd("m", incby, sdate)
         .Cells(rowsdate, coldate) = sdate
    End With
End Sub

Sub btnIncMonth_Click()
Dim rowsdate As Integer
Dim rowedate As Integer
Dim coldate As Integer
Dim sdate As Date
Dim edate As Date
Dim str As String
Dim incby As Integer

   rowsdate = 4
   rowedate = 5
   coldate = 2
   incby = 0
    
    
    With ActiveSheet
         str = .Cells(rowedate, coldate).Text
         If str <> "" Then
            incby = 1
         End If
         str = .Cells(rowsdate, coldate).Text
         
         sdate = DateValue(str)
         sdate = DateAdd("m", incby, sdate)
         edate = DateAdd("m", 1, sdate)
         edate = DateAdd("d", -1, edate)
         
         .Cells(rowsdate, coldate) = sdate
         .Cells(rowedate, coldate) = edate
         
    End With
End Sub



