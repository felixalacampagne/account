Attribute VB_Name = "statement_load"
Option Explicit
' 2025-11-05 17:20

Sub LoadStatement()
Dim statement As String
Dim fd As FileDialog
Dim i As Integer
Dim d As Integer
Dim stmtname As String
Dim stmtpath As String
Dim acctype As String
Dim lastlocn As String
Dim filefmt As String

   On Error Resume Next
   lastlocn = Range("Settings!statementdir")
   acctype = Range("Settings!accounttype")
   On Error GoTo 0

   If acctype <> "beomc" Then
      filefmt = "*.csv"
   Else
      filefmt = "*.xlsx"
   End If

   Set fd = Application.FileDialog(msoFileDialogFilePicker)
   fd.title = "Statement Selection"
   fd.Filters.Add "Statement files", filefmt
   fd.InitialFileName = lastlocn & "\"

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
         stmtpath = Left$(statement, i - 1)
         Exit For
      End If
   Next


   initCSVColumns

   Sheets.Add After:=ActiveWorkbook.Sheets("Settings")
   stmtname = StrRepl(stmtname, "_statement", "")  ' Really only for CBC - should find a better way
   ActiveSheet.Name = Right$(stmtname, 31)

   If acctype = "barclays" Then
      loadBarclays statement, stmtname
   ElseIf acctype = "keytrade" Then
      loadKeytrade statement, stmtname
   ElseIf acctype = "beomc" Then
      loadBeoMC statement, stmtname
   Else
      loadCBC statement, stmtname
   End If

    '07 Nov 2021 insert columns for status and description as statements
    '  may contain more columns and I'd rather have the decription first but not
    '  dont want to overwrite the extra details to be overwritten.
    Cells(1, COL_DBSEQ).EntireColumn.Insert
    Cells(1, COL_DBSEQ).EntireColumn.Insert
    Cells(1, COL_DBSEQ).EntireColumn.Insert
    Cells(1, 1).Select

    If stmtpath <> "" Then
      On Error Resume Next
      Range("Settings!statementdir") = stmtpath
      If Err.Number <> 0 Then
          Range("Settings!A8") = stmtpath
          ActiveWorkbook.Names.Add Name:="statementdir", RefersToR1C1:="=Settings!R8C1"
      End If
      On Error GoTo 0
    End If
End Sub

Sub initCSVColumns()
Dim acctype As String
   acctype = Range("Settings!accounttype")
   If acctype = "barclays" Then
      COL_ACCNUM = 3
      COL_DATE = 2
      COL_DESC = 6
      COL_VALUE = 4
      COL_STATNUM = 1
   ElseIf acctype = "keytrade" Then
      COL_ACCNUM = 8 ' H
      COL_DATE = 2 ' B
      COL_DESC = 5 ' E
      COL_VALUE = 6 ' F
      COL_STATNUM = 1
   ElseIf acctype = "beomc" Then
      COL_ACCNUM = 6 ' F
      COL_DATE = 1 ' A
      COL_DESC = 2 ' B
      COL_VALUE = 5 ' E
      COL_STATNUM = 7 ' G
   Else
      COL_ACCNUM = 1
      COL_STATNUM = 5
      COL_DATE = 6
      COL_DESC = 7
      COL_VALUE = 9
   End If
End Sub

Sub loadCBC(statement As String, stmtname As String)
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

   ' Looks like CBC also needs to be sorted - real statement CSVs are already sorted by date ascending
   ' but the reports generated 'manually' are sorted date descending.
   sortsheet COL_DATE

End Sub

Sub loadBarclays(statement As String, stmtname As String)
Dim rownum As Integer
Dim stmtref As String

   Application.CutCopyMode = False
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
      .TextFilePlatform = 65001
      .TextFileStartRow = 1
      .TextFileParseType = xlDelimited
      .TextFileTextQualifier = xlTextQualifierDoubleQuote
      .TextFileConsecutiveDelimiter = False
      .TextFileTabDelimiter = False
      .TextFileSemicolonDelimiter = False
      .TextFileCommaDelimiter = True
      .TextFileSpaceDelimiter = False
      .TextFileColumnDataTypes = Array(2, 4, 2, 1, 1, 2)
      .TextFileTrailingMinusNumbers = True
      .Refresh BackgroundQuery:=False
   End With

   ' No statement references in the barclays CSV. Use date part of statement name.
   ' For compatibility need to fill the statement column with the value
   stmtref = extractDatepart(stmtname)
   With ActiveSheet
      rownum = 2
      Do While Trim(.Cells(rownum, COL_VALUE).Text) <> ""
         .Cells(rownum, 1) = stmtref
         rownum = rownum + 1
      Loop

   End With

   ' Barclays extracts are sorted the wrong way round which causes
   ' a problem with repeating payments of the same amount, eg. Hello magazine
   sortsheet COL_DATE
End Sub


Sub loadKeytrade(statement As String, stmtname As String)
Dim rownum As Integer
Dim acccode As String

   Application.CutCopyMode = False
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
        .TextFilePlatform = 65001
        .TextFileStartRow = 1
        .TextFileParseType = xlDelimited
        .TextFileTextQualifier = xlTextQualifierDoubleQuote
        .TextFileConsecutiveDelimiter = False
        .TextFileTabDelimiter = False
        .TextFileSemicolonDelimiter = True
        .TextFileCommaDelimiter = False
        .TextFileSpaceDelimiter = False
        .TextFileColumnDataTypes = Array(2, 4, 4, 1, 1, 1, 1)
        .TextFileTrailingMinusNumbers = True
        .Refresh BackgroundQuery:=False
   End With

   ' No account number in keytrade statements.
   ' For compatibility need to fill the account column with a value from the settings page
   acccode = Range("Settings!accountcode")
   With ActiveSheet
      rownum = 2
      Do While Trim(.Cells(rownum, COL_VALUE).Text) <> ""
         .Cells(rownum, COL_ACCNUM) = acccode
         rownum = rownum + 1
      Loop

   End With

   ' Keytrade extracts are sorted the wrong way round which causes
   ' a problem with repeating payments of the same amount, eg. Hello magazine
   sortsheet COL_DATE
End Sub

' Statement is a multi-sheet Excel file. Not sure where the 'active' statement
' is located, it could be the 'Next statement' sheet which contains one set
' of transactions, or it could be on the 'Previous statements' sheet which
' contains multiple sets of transactions. When I want to do the reconcile, ie.
' after I've received notifiction that the statement for the month is due,
' the transactions for the statement to be paid could already be considered
' as 'Previous statements'. Thus, even if I could extract the transactions
' programmatically, I don't actually know where to get them from. Therefore
' the first version requires that the transactions have been manually
' copied to the clipboard and loading consists of pasting into the
' the current sheet. The name of the current sheet must be already set to
' the value to use as the statement reference.
' Eventually it might be possible to load the transactions directly from
' the downloaded spreadsheet... need to wait for a new statement to arrive...
Sub loadBeoMC(statement As String, stmtname As String)
Dim stmtref As String
Dim acccode As String
Dim rownum As Integer


Dim wbTarget As Workbook
Dim startrow As Integer
Dim endrow As Integer
Dim chkcol As Integer
Dim actSheet As Worksheet
Dim actBook As Workbook
Dim actSheetName As String
Dim celval As String
Const stmnthead As String = "Statement on : "
   stmtref = extractDatepart(stmtname) ' Overridden below by date from statement
   acccode = Range("Settings!accountcode")
   chkcol = COL_VALUE


   Set actBook = ActiveWorkbook
   actSheetName = ActiveWorkbook.ActiveSheet.Name


   Set wbTarget = Workbooks.Open(statement)

   startrow = 5

   ' Still to be confirmed where the real latest 'to-be-paid' statement is located
   With wbTarget.Sheets("Previous statements")

      celval = .Cells(startrow, 1).Text    ' Statement on....
      If Left$(celval, Len(stmnthead)) <> stmnthead Then
         Err.Raise vbObjectError + 513, "Unrecognised Beo MC statement:" & vbCrLf & "Statement does not start with '" & stmnthead & "'"
         Exit Sub
      End If
      stmtref = extractDatepart(celval)

      endrow = startrow + 1
      Do While (Trim(.Cells(endrow, chkcol).Text) <> "") And (Left$(.Cells(endrow, 1).Text, Len(stmnthead)) <> stmnthead)
         endrow = endrow + 1
      Loop
      If Left$(.Cells(endrow, 1).Text, Len(stmnthead)) = stmnthead Then
         endrow = endrow - 1
      End If
      .Rows("" & startrow & ":" & endrow).Copy actBook.Sheets(actSheetName).Cells(1, 1)
   End With
   wbTarget.Close


   ' Statement ref is a date formatted using '/' as separator but I think I prefer to use '-'
   stmtref = StrRepl(stmtref, "/", "-")

   With actBook.Sheets(actSheetName)

      rownum = 2
      Do While Trim(.Cells(rownum, chkcol).Text) <> ""
         .Cells(rownum, COL_ACCNUM) = acccode
         .Cells(rownum, COL_STATNUM).NumberFormat = "@"  ' magic to force Excel display string without corrupting it
         .Cells(rownum, COL_STATNUM).Value = stmtref
         rownum = rownum + 1
      Loop
      .Cells.Select
      .Cells.EntireColumn.AutoFit
      .Columns("A:A").ClearFormats
      .Columns("A:A").NumberFormat = "dd/mm/yyyy;@"
   End With
   ActiveSheet.Name = Right$(stmtname, 31)
   sortsheet COL_DATE
End Sub

Sub sortsheet(sortcol As Integer)
Dim lastrow As Integer
Dim lastcol As Integer

    Cells(1, sortcol).Select
    lastrow = Cells(Rows.Count, sortcol).End(xlUp).row      ' This magic finds the last row

    ' Some statments do not have headers for all rows.
    ' Assume all statements have a header row.
    ' Thus row 2 is a full row (or empty for an empty statement)
    lastcol = Cells(2, Columns.Count).End(xlToLeft).column  ' This magic finds the last column

    ' WARNING: Beomc date column inexplicably requires 'xlSortTextAsNumbers'
    ' whereas xlSortNormal is OK for other statement types
    With ActiveSheet
      .Sort.SortFields.Clear
      .Sort.SortFields.Add2 Key:=Range(Cells(2, sortcol), Cells(lastrow, sortcol)), SortOn:=xlSortOnValues, Order:=xlAscending, DataOption:=xlSortTextAsNumbers
      .Sort.Header = xlYes
      .Sort.MatchCase = False
      .Sort.Orientation = xlTopToBottom
      .Sort.SetRange Range(Cells(1, 1), Cells(lastrow, lastcol))
      .Sort.Apply
    End With
End Sub

Function extractDatepart(origvalue As String) As String
Dim datepart As String
Dim i As Integer

   i = Len(origvalue)
   Do While i > 0
      ' Can't put this in the while expression because all parts are evaluated even when
      ' the first part of the AND is false so i=0 is used in the Mid$ which is invalid!
      If Not (IsNumeric(Mid$(origvalue, i, 1)) Or Mid$(origvalue, i, 1) = "-" Or Mid$(origvalue, i, 1) = "/") Then
         Exit Do
      End If
      i = i - 1
   Loop
   datepart = Mid$(origvalue, i + 1)
   extractDatepart = datepart
End Function

