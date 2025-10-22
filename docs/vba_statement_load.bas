Attribute VB_Name = "statement_load"
Option Explicit
' 2025-10-22 17:31

Sub LoadStatement()
Dim statement As String
Dim fd As FileDialog
Dim i As Integer
Dim d As Integer
Dim stmtname As String
Dim acctype As String

   Set fd = Application.FileDialog(msoFileDialogFilePicker)
   fd.title = "Statement Selection"
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
   
   initCSVColumns
   
   acctype = Range("Settings!accounttype")
   If acctype = "barclays" Then
      loadBarclays statement, stmtname
   ElseIf acctype = "keytrade" Then
      loadKeytrade statement, stmtname
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
    stmtname = StrRepl(stmtname, "_statement", "")

    ActiveSheet.Name = Right$(stmtname, 31)

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

   ActiveSheet.Name = Right$(stmtname, 31)


   ' No statement references in the barclays CSV - should use the statement name.
   ' For compatibility need to fill the statement column with the value
   stmtref = Right$(stmtname, 10) ' assumes the name ends with YYYY-MM-DD
   With ActiveSheet
      rownum = 2
      Do While Trim(.Cells(rownum, COL_VALUE).Text) <> ""
         .Cells(rownum, 1) = stmtref
         'If Trim(.Cells(rownum + 1, COL_VALUE).Text) = "" Then
         '   ' Ugly way to ensure rownum points to last filled line which is needed for the sort
         '   Exit Do
         'End If
         rownum = rownum + 1
      Loop

   End With

   ' Barclays extracts are sorted the wrong way round which causes
   ' a problem with repeating payments of the same amount, eg. Hello magazine
   sortsheet COL_DATE
   ' Range("B1").Select
   ' With ActiveSheet
   '   .Sort.SortFields.Clear
   '   .Sort.SortFields.Add2 Key:=Range("B2:B" & rownum), SortOn:=xlSortOnValues, Order:=xlAscending, DataOption:=xlSortNormal
   '   .Sort.Header = xlYes
   '   .Sort.MatchCase = False
   '   .Sort.Orientation = xlTopToBottom
   '   .Sort.SetRange Range("A1:F" & rownum)
   '   .Sort.Apply
   ' End With
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

   ActiveSheet.Name = Right$(stmtname, 31)



   ' No account number in keytrade statements.
   ' For compatibility need to fill the account column with a value from the settings page
   acccode = Range("Settings!accountcode")
   With ActiveSheet
      rownum = 2
      Do While Trim(.Cells(rownum, COL_VALUE).Text) <> ""
         .Cells(rownum, COL_ACCNUM) = acccode
         'If Trim(.Cells(rownum + 1, COL_VALUE).Text) = "" Then
         '   ' Ugly way to ensure rownum points to last filled line which is needed for the sort
         '   Exit Do
         'End If
         rownum = rownum + 1
      Loop

   End With

   ' Keytrade extracts are sorted the wrong way round which causes
   ' a problem with repeating payments of the same amount, eg. Hello magazine
   sortsheet COL_DATE
   ' Range("B1").Select
   ' With ActiveSheet
   ' .Sort.SortFields.Clear
   ' .Sort.SortFields.Add2 Key:=Range("B2:B" & rownum), SortOn:=xlSortOnValues, Order:=xlAscending, DataOption:=xlSortNormal
   ' .Sort.Header = xlYes
   ' .Sort.MatchCase = False
   ' .Sort.Orientation = xlTopToBottom
   ' .Sort.SetRange Range("A1:F" & rownum)
   ' .Sort.Apply
   ' End With
End Sub

Sub sortsheet(sortcol As Integer)
Dim lastrow As Integer
Dim lastcol As Integer

    Cells(1, sortcol).Select
    lastrow = Cells(Rows.Count, sortcol).End(xlUp).row      ' This magic finds the last row
    lastcol = Cells(1, Columns.Count).End(xlToLeft).column  ' This magic finds the last column
    With ActiveSheet
      .Sort.SortFields.Clear
      .Sort.SortFields.Add2 Key:=Range(Cells(2, sortcol), Cells(lastrow, sortcol)), SortOn:=xlSortOnValues, Order:=xlAscending, DataOption:=xlSortNormal
      .Sort.Header = xlYes
      .Sort.MatchCase = False
      .Sort.Orientation = xlTopToBottom
      .Sort.SetRange Range(Cells(1, 1), Cells(lastrow, lastcol))
      .Sort.Apply
    End With
End Sub
