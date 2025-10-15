Attribute VB_Name = "MySQLReconcile"
Option Explicit

Const COL_ACCNUM = 1
Const COL_CURRENCY = 4
Const COL_STATNUM = 5
Const COL_DATE = 6
Const COL_DESC = 7
Const COL_VALUE = 9
Const COL_DBSEQ = 13
Const COL_FAILREASON = 14
Const QUITNOW = "QUIT"
Const ALLOK = "OK"
Const COL_DBDESC = 15

Const DBCOL_ID = "sequence"   ' id
Const DBCOL_DATE = "Date"     ' transactiondate
Const DBCOL_TYPE = "Type"     ' transactiontype
Const DBCOL_STMNTREF = "Stid" ' statementref
Const DB_FALSE = "false"      ' 0 for mysql
Const DB_TRUE = "true"        ' 1 for mysql
Const DB_NUMQUOTE = ""        ' single quote for mysql
Const DB_DATEQUOTE = "#"      ' single quote for mysql
Const DB_COLQUOTEO = "["      ' empty for mysql
Const DB_COLQUOTEC = "]"      ' empty for mysql
Const DBCOL_ACCID = "acc_id"
Const DBCOL_ACCCODE = "acc_code"
Const DBCOL_ACCCURR = "acc_curr"
 

Sub reconcileStatement()
Dim db As New ADODB.Connection
Dim acc_id As Long
Dim dblocn As String
Dim failreason As String

   ' Don't really need the worksheet name!!!
   dblocn = Range("Settings!dblocation")

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
Dim sheetcur As String
Dim normacc As String
Dim accid As Long
  
   ' Get the IBAN account
   ibanacc = ActiveSheet.Cells(2, COL_ACCNUM)
   '07 Nov 2021 Account numbers now contain spaces
   sheetacc = StrRepl(ibanacc, " ", "")
   sheetacc = StrRepl(sheetacc, "-", "")
   sheetcur = ActiveSheet.Cells(2, COL_CURRENCY)
   reason = ""
   
   sql = "select " & DBCOL_ACCID & ", " & DBCOL_ACCCODE & ", " & DBCOL_ACCCURR & " from account"
   sql = sql & " where " & DBCOL_ACCCURR & " = '" & sheetcur & "'"
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
         If sheetcur = rs(DBCOL_ACCCURR) Then
            accid = rs(DBCOL_ACCID)
            Exit Do
         End If
      End If
      rs.MoveNext
   Loop
   rs.Close
   If accid = -1 Then
      reason = "Account: " & ibanacc & " Currency: " & sheetcur
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

Dim dbnullcol As String
Dim dbamtcol As String
Dim dbamtval As Double
Dim first As Boolean
Dim datematch As Boolean
Dim accidsql As String
Dim rowstyle As Integer

   accidsql = DB_NUMQUOTE & accid & DB_NUMQUOTE

   db.BeginTrans
   sql = "select * from transaction where accountid=" & accidsql
   sql = sql & " and checked=" & DB_FALSE
   sql = sql & " order by " & DBCOL_DATE & " asc, " & DBCOL_ID & " asc"
   'mysql gives data type mismatch if id is not in quotes!
   'sql = "select * from transaction where accountid='" & accid & "' and checked=0 order by transactiondate asc, id asc"
   
   Set rs = New ADODB.Recordset
   rs.Open sql, db, adOpenStatic, adLockPessimistic

   If rs.EOF Then
      MsgBox "No transactions found for query: " & sql
      rs.Close
      db.RollbackTrans
      Exit Sub
   End If
   
   Dim i As Integer
   For i = 0 To rs.Fields.Count - 1
      Debug.Print rs.Fields(i).Name, rs.Fields(i).Value
   Next
   'Do While Not rs.EOF
   '   Debug.Print rs(DBCOL_ID), rs(DBCOL_DATE), rs("debit"), rs("credit"), rs("comment")
   '   rs.MoveNext
   'Loop
   
   With ActiveSheet
      rownum = 2
      Do While .Cells(rownum, 1).Text <> ""
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
         rs.MoveFirst
         Do While Not rs.EOF
            If IsNull(rs(dbnullcol)) And Not IsNull(rs(dbamtcol)) Then
               If dbamtval = CDbl(rs(dbamtcol)) Then
                  'If Abs(DateDiff("d", rs("transactiondate"), amtdate)) < 3 Then
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
         End If
            
         
         If notrcncld = ALLOK Then
            cntchange = cntchange + 1
            setRowStyle .Rows(rownum), rowstyle
         Else
            notrcncld = reconoraddTransaction(accid, rs, db, .Rows(rownum), notrcncld, actionstr)
            ' There was a matching amount but the date was too different...
            setRowStyle .Rows(rownum), 2
         End If
         
         .Cells(rownum, COL_DBSEQ) = seqid
         .Cells(rownum, COL_FAILREASON) = notrcncld
         
         ' This is misleading if reconoraddTransaction resulted in a new row being added as
         ' the comment is that of the row with the large date difference. Need to refactor
         ' so that the comment indicates that a new row was added
         .Cells(rownum, COL_DBDESC) = actionstr

         rownum = rownum + 1

      Loop
   End With
   rs.Close
   
   
   If cntchange > 0 Then
      ' Do the commit/rollback in async form to allow the changes in the sheet to be examined
      UserForm1.Label1 = "Changes made: " & cntchange & vbCrLf & vbCrLf & "Commit changes?"
      UserForm1.Show False
   End If

    
End Sub


Function reconoraddTransaction(accid As Long, rs As Recordset, arow As Range, notrcncld As String, actionstr As String) As String
Dim msg As String
Dim amt As String
Dim amtv As Double
Dim i As Integer

   msg = notrcncld & ":" & vbCrLf
   msg = msg & "Date:    " & arow.Columns(COL_DATE) & "(Statement) " & rs("transactiondate") & "(Database)" & vbCrLf
   msg = msg & "Amount:  " & arow.Columns(COL_VALUE) & vbCrLf
   msg = msg & "Statement description:  " & arow.Columns(COL_DESC) & vbCrLf
   msg = msg & "Database description: " & rs("comment") & vbCrLf & vbCrLf
   msg = msg & "Yes=Match, No=Add transaction, Cancel=Do Nothing"

   i = MsgBox(msg, vbYesNoCancel, "Match or Add Transaction")
   Select Case i
   Case vbYes
        reconoraddTransaction = reconcileTransaction(rs, arow)
   Case vbNo
        actionstr = "Transaction added to db"
        reconoraddTransaction = addtodb(accid, rs, arow)
   Case Else
        actionstr = "NO MATCH for transaction"
        reconoraddTransaction = notrcncld
   End Select

End Function

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

   ' Monumentally more complex than it needs to be since columns have changed for mysql and different
   ' types of quoting are required for different fields
   inssql = "insert into transaction (accountid, " & DBCOL_STMNTREF & ", checked, " & DB_COLQUOTEO & DBCOL_DATE & DB_COLQUOTEC & ", comment, " & DBCOL_TYPE & ", " & amtcol
   inssql = inssql & ") values ("
   inssql = inssql & DB_NUMQUOTE & accid & DB_NUMQUOTE & ", "
   inssql = inssql & "'" & arow.Columns(COL_STATNUM) & "'" & ", "
   inssql = inssql & DB_TRUE & ", "
   inssql = inssql & DB_DATEQUOTE & arow.Columns(COL_DATE) & DB_DATEQUOTE & ", "
   inssql = inssql & "'" & Left$(arow.Columns(COL_DESC), rs("comment").DefinedSize) & "', "
   inssql = inssql & "'STMNT', "
   inssql = inssql & DB_NUMQUOTE & amtval & DB_NUMQUOTE
   inssql = inssql & ")"
   
   Debug.Print "addtodb: " & inssql
   db.Execute inssql
   
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


Sub LoadStatement()
'
' LoadStatement Macro
'
Dim statement As String

Dim fd As FileDialog
Dim i As Integer
Dim d As Integer
Dim stmtname As String

   Set fd = Application.FileDialog(msoFileDialogFilePicker)
   fd.title = "CBC Statement Selection"
   fd.Filters.Add "Statement files", "*.csv"
   fd.FilterIndex = 1
   If fd.Show <> -1 Then
      Exit Sub
   End If
   
   statement = fd.SelectedItems.Item(1)
   d = Len(statement)
   stmtname = statement
   For i = d To 1 Step -1
      If Mid$(statement, i, 1) = "." Then
         d = i
      ElseIf Mid$(statement, i, 1) = "\" Or Mid$(statement, i, 1) = "/" Then
         stmtname = Mid$(statement, i + 1, d - i - 1)
         Exit For
      End If
   Next
   
    With ActiveSheet.QueryTables.Add(Connection:="TEXT;" & statement, Destination:=Range("$A$1"))
        .Name = stmtname
        .FieldNames = True
        .RowNumbers = False
        .FillAdjacentFormulas = False
        .PreserveFormatting = True
        .RefreshOnFileOpen = False
        .RefreshStyle = xlInsertDeleteCells
        .SavePassword = False
        .SaveData = True
        .AdjustColumnWidth = True
        .RefreshPeriod = 0
        .TextFilePromptOnRefresh = False
        .TextFilePlatform = 437
        .TextFileStartRow = 1
        .TextFileParseType = xlDelimited
        .TextFileTextQualifier = xlTextQualifierDoubleQuote
        .TextFileConsecutiveDelimiter = False
        .TextFileTabDelimiter = False
        .TextFileSemicolonDelimiter = True
        .TextFileCommaDelimiter = False
        .TextFileSpaceDelimiter = False
        .TextFileColumnDataTypes = Array(1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1)
        .TextFileTrailingMinusNumbers = True
        .Refresh BackgroundQuery:=False
    End With
    stmtname = StrRepl(stmtname, "_statement", "")
    
    ActiveSheet.Name = Right$(stmtname, 31)
    
    '07 Nov 2021 Crude column insertion for status and description as statements
    '  now contain more columns and I'd rather have the decription first but not
    '  dont want the extra details to be overwritten... and I've no clue how to do any
    '  of this anymore so had to rely on recording a macro, which always results in
    '  very strange code
    Columns(COL_DBSEQ).Select
    Selection.Insert Shift:=xlToRight, CopyOrigin:=xlFormatFromLeftOrAbove
    Selection.Insert Shift:=xlToRight, CopyOrigin:=xlFormatFromLeftOrAbove
    Selection.Insert Shift:=xlToRight, CopyOrigin:=xlFormatFromLeftOrAbove
    Cells(1, 1).Select
End Sub




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

Sub btnCBCSetReportPDF_Click()
getCBCReportDates "Request PDF report"
End Sub

Sub btnCBCSetReportCSV_Click()
getCBCReportDates "Request CSV file"
End Sub
Sub getCBCReportDates(report As String)
Dim objIE As Object
Dim rowsdate As Integer
Dim rowedate As Integer
Dim coldate As Integer
Dim strsdate As String
Dim stredate As String
Dim dt As Date
Dim incby As Integer

   rowsdate = 4
   rowedate = 5
   coldate = 2
    With ActiveSheet
         dt = DateValue(.Cells(rowsdate, coldate).Text)
         strsdate = Format(dt, "dd-mm-yyyy")
         
         dt = DateValue(.Cells(rowedate, coldate).Text)
         stredate = Format(dt, "dd-mm-yyyy")
    End With


Dim objParent As Object
Dim objForms As Object, objForm As Object
Dim objInputElement As Object
Dim objOption As Object
Dim objDocument As Object
Dim objEvent As Object
Dim lngRow As Integer
Dim strComment As String
Dim appname As String
Dim i As Integer

'transactionSelectiongroup.transactionDateSelectioncategory
'transactionSelectiongroup.transactiondategroup.fromdatePick
'transactionSelectiongroup.transactiondategroup.uptodatePick


Set objIE = GetIEApp
'Make sure an IE object was hooked
If TypeName(objIE) = "Nothing" Then
  MsgBox "Could not hook Internet Explorer object", vbCritical, "GetFields() Error"
  GoTo Clean_Up
End If

   appname = objIE.LocationName
   
   ' This is less than ideal but using Sendkeys is the only way I have found which
   ' successfully gets the page to recognize that something has been typed. None of the
   ' methods which are supposed to trigger user input events work
   On Error Resume Next
   AppActivate appname
   
   
   Set objDocument = objIE.Document
    Set objParent = objIE.Document.All
    With objParent

      Set objInputElement = objParent.Tags("INPUT").Item("transactionSelectiongroup.transactionDateSelectioncategory")
      'objInputElement.Item("id_120").Checked = True
      i = 0
      
      ' Must be an easier way than this to set the value but everything I tried so far gives object doesn't support ...
      ' For what it's worth the inputelement is a HTMLInputElement
      Do
         Set objOption = objInputElement.Item(i)
         If Not objOption Is Nothing Then
            If objOption.Value = "date" Then
               objOption.Checked = True
               Exit Do
            End If
            i = i + 1
         End If
      Loop While Not objOption Is Nothing
      
      'objInputElement.Item("date").Checked = True
      'objParent.Item("transactionSelectiongroup.transactionDateSelectioncategory").Checked = True
      Set objInputElement = objParent.Item("transactionSelectiongroup.transactiondategroup.fromdatePick")
      objInputElement.Focus
      objInputElement.Value = strsdate
      SendKeys "{TAB}"
      Set objEvent = objDocument.createEventObject
      objInputElement.fireevent "onkeyup", objEvent
      
      Set objInputElement = objParent.Item("transactionSelectiongroup.transactiondategroup.uptodatePick")
      objInputElement.Click
      objInputElement.Focus
      objInputElement.Value = stredate
      objInputElement.Blur
      Set objEvent = objDocument.createEventObject
      objInputElement.fireevent "onkeyup", objEvent
      SendKeys "{TAB}"
    'agf:validate'
      ' have to somehow find the a containing '<span>Request PDF report</span>' and
      ' <span>Request CSV file</span>
    
      ' This seems to work although need to find a way to get the code to recognise that
      ' data has been entered into the date fields....
      For Each objInputElement In objParent.Tags("A")
         Debug.Print "'" & Trim$(objInputElement.Text) & "'"
         If objInputElement.Text Like "*" & report & "*" Then
            objInputElement.Focus
            objInputElement.Click
            Exit For
         End If
      Next
    End With

Clean_Up:
Set objInputElement = Nothing
Set objForm = Nothing
Set objForms = Nothing
Set objIE = Nothing
End Sub





