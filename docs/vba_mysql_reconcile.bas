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

Dim gDB As Database

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


Sub reconcileStatement()
Dim DaoDBEngine As New DBEngine '(All others are string declarations)
'Dim CurrentDB As Database
Dim acc_id As Long
Dim dblocn As String
Dim failreason As String

   ' Don't really need the worksheet name!!!
   dblocn = Range("Settings!dblocation")
  
   Set gDB = DaoDBEngine.OpenDatabase(dblocn, dbDriverCompleteRequired, False, "ODBC;DATABASE=accountmysql;DSN=" & dblocn)
   
   acc_id = getAccountID(gDB, failreason)
   
   
   If acc_id > -1 Then
      checkStatement gDB, acc_id
   Else
      MsgBox "No matching account found in database." & vbCrLf & failreason
   End If
   
   gDB.Close
End Sub

Sub checkStatement(db As Database, accid As Long)
Dim rs As DAO.Recordset
Dim sql As String
Dim rownum As Long
Dim amtstr As String
Dim stmntamount As Double
Dim credit As double
Dim debit As double
Dim amtdate As Date
Dim transsql As String
Dim seqid As Long
Dim notrcncld As String
Dim cntchange As Long
Dim bookmark As Variant
Dim actionstr As String
Dim accidint As Integer

dim dbnullcol as string
dim dbamtcol as string
dim dbamtval as double
dim first as boolean
dim datematch as boolean

   DBEngine.BeginTrans
   'mysql gives data type mismatch if id is not in quotes!
   sql = "select * from transaction where accountid='" & accid & "' and checked=0 order by transactiondate asc, id asc"
   Set rs = db.OpenRecordset(sql, dbOpenSnapshot)
   If rs.EOF Then
   MsgBox "No transactions found for query: " & sql
      rs.Close
      Exit Sub
   End If

   ' The FindFirst method does not work with MySQL and MySQL ODBC
   ' so forced to use brute force method of searching manually through the recordset for a match.
   ' This is done very crudely with two different searches depending on whether the statement
   ' amount is a credit or a debit.
   With ActiveSheet
      rownum = 2
      Do While .Cells(rownum, 1).Text <> ""
         credit = 0#
         debit = 0#
         amtdate = .Cells(rownum, COL_DATE).Value
         amtstr = .Cells(rownum, COL_VALUE).Text
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
         
         rs.MoveFirst
         Do While Not rs.EOF
            If IsNull(rs(dbnullcol)) And Not IsNull(rs(dbamtcol)) Then
               If dbamtval = CDbl(rs(dbamtcol)) Then
                  If Abs(DateDiff("d", rs("transactiondate"), amtdate)) < 3 Then
                     datematch = true
                     first = false
                     Exit Do
                  elseif not first then
                     ' mark the first matching value even if the date does not match
                     ' as this will be the record suggested if not date match is found
                     first = true
                     bookmark = rs.bookmark
                  end if   
               end if
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
         If rs.EOF and not first Then
            Debug.Print "Amount not found: " & stmntamount
            notrcncld = addTransaction(accid, rs, .Rows(rownum))
            If notrcncld = ALLOK Then
               cntchange = cntchange + 1
               setRowStyle .Rows(rownum), 2
            ElseIf notrcncld = QUITNOW Then
               ' Emergency bailout button pressed!
               DBEngine.Rollback
               rs.Close
               Exit Sub
            Else
               setRowStyle .Rows(rownum), 0
            End If
            
            .Cells(rownum, COL_FAILREASON) = notrcncld
         Else If datematch Then   ' Clean match
            notrcncld = reconcileTransaction(rs, .Rows(rownum))
         Else ' Should mean that first is true and bookmark is set 
            ' Restore pointer to first match to avoid problem in reconoraddTransaction
            rs.bookmark = bookmark
            notrcncld = "Amount matched with too great date difference"
         End If
            
         actionstr = rs("comment")
         If notrcncld = ALLOK Then
            cntchange = cntchange + 1
            setRowStyle .Rows(rownum), 1
         Else
            notrcncld = reconoraddTransaction(accid, rs, .Rows(rownum), notrcncld, actionstr)
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

            
Function addTransaction(accid As Long, rs As Recordset, arow As Range) As String
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
   
   addTransaction = addtodb(accid, rs, arow)
   
End Function

Function addtodb(accid As Long, rs As Recordset, arow As Range) As String
Dim amt As String
Dim amtv As Double
Dim inssql As String
amt = Trim$(arow.Columns(COL_VALUE))
   amt = StrRepl(amt, ",", ".")
   amtv = Val(amt)
   
   Dim amtcol As String
   Dim amtval As Double
   
   'rs.AddNew
   If amtv < 0 Then
      'rs("debit") = amtv * -1
      amtcol = "debit"
      amtval = amtv * -1
   Else
      'rs("credit") = amtv
      amtcol = "credit"
      amtval = amtv
   End If
   'rs("accountid") = accid
   'rs("statementref") = arow.Columns(COL_STATNUM)
   'rs("checked") = True
   'rs("transactiondate") = arow.Columns(COL_DATE)
   'rs("comment") = Left$(arow.Columns(COL_DESC), rs("comment").Size)
   'rs("type") = "STMNT"
   
   'rs.Update
   
   inssql = "insert into transaction (accountid, statementref, checked, transactiondate, comment, transactiontype, " & amtcol
   inssql = inssql & ") values ("
   inssql = inssql & "'" & accid & "', "
   inssql = inssql & "'" & arow.Columns(COL_STATNUM) & "', "
   inssql = inssql & "1, "
   inssql = inssql & "'" & arow.Columns(COL_DATE) & "', "
   inssql = inssql & "'" & Left$(arow.Columns(COL_DESC), rs("comment").Size) & "', "
   inssql = inssql & "'STMNT', "
   inssql = inssql & "'" & amtval & "'"
   inssql = inssql & ")"
   
   Debug.Print "addtodb: insert sql: " & inssql
   gDB.Execute inssql
   
   addtodb = ALLOK
End Function

Function reconcileTransaction(rs As Recordset, arow As Range) As String
Dim stmntid As String
Dim updsql As String

   reconcileTransaction = ALLOK
   stmntid = arow.Columns(COL_STATNUM).Text
   If "" & rs("statementref") = "" Then
      
      updsql = "update transaction set statementref='" & stmntid & "', checked=1 where id='" & rs("id") & "'"
      Debug.Print "reconcileTransaction: " & updsql
      gDB.Execute updsql
      
      'rs.Edit
      'rs("statementref") = stmntid
      'rs("checked") = True
      'rs.Update
   ElseIf rs("statementref") = stmntid Then
      updsql = "update transaction set checked=1 where id='" & rs("id") & "'"
      Debug.Print 'reconcileTransaction: ' & updsql
      gDB.Execute updsql
      'rs.Edit
      'rs("checked") = True
      'rs.Update
   Else
      reconcileTransaction = "Statement id '" & rs("statementref") & "' already present"
   End If
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

Function getAccountID(db As Database, ByRef reason As String) As Long
Dim rs As Recordset
Dim sql As String
Dim ibanacc As String
Dim sheetacc As String
Dim sheetcur As String
Dim normacc As String
Dim accid As Long

   
   ' Get the IBAN account
   ibanacc = ActiveSheet.Cells(2, COL_ACCNUM)
   '07 Nov 2021 Account numbers now contain spaces
   ibanacc = StrRepl(ibanacc, " ", "")
   sheetacc = ibanacc ' Mid$(ibanacc, 5)
   sheetcur = ActiveSheet.Cells(2, COL_CURRENCY)
   reason = ""
   
   sql = "select id, code, currency from account"
   Set rs = db.OpenRecordset(sql, 4) '4-snapshot
   rs.MoveFirst
   accid = -1
   
   Do While Not rs.EOF
      normacc = StrRepl(rs("code"), "-", "")
      normacc = StrRepl(normacc, " ", "")
      Debug.Print rs("id"), normacc, rs("code"), rs("currency")
      ' Duplicate account codes exist from the pre-EURO days
      If normacc = sheetacc Then
         If sheetcur = rs("currency") Then
            accid = rs("id")
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



