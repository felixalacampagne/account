Attribute VB_Name = "ACC2OLE"
Option Explicit

Sub ExportToWord(cl As Recordset, fldlst() As String, bke As String, fn As String, addntxt As String)
'Array indices are;
'  0 - recordset field name
'  1 - format string
'  2 - column heading
'  3 - column width
'25 Jul 96 Added column widths
Dim wrd As Object

Dim topaste As String, ssep As String, snl As String
Dim strtmp As String
Dim i As Integer, j As Integer
Dim s As String
   
   On Error GoTo ExportToWordErr
   
   ssep = Chr$(2)
   snl = Chr$(10)

   'Create the word object and define the table column separator for
   'use with the texttotable function
   Set wrd = CreateObject("Word.basic")
   wrd.filenewdefault
   wrd.ViewNormal

   j = UBound(fldlst, 2)
   Do
      topaste = ""
      For i = 0 To j
         topaste = topaste & ssep & Format$(cl(fldlst(0, i)), fldlst(1, i))
         'Debug.Print Format$(cl(fldlst(0, i)), fldlst(1, i))
      Next 'i
      topaste = Mid$(topaste, 2) & snl

      wrd.Insert topaste

      If CStr(cl.bookmark) = bke Then
         Exit Do
      End If
      cl.MoveNext
   Loop Until (cl.EOF)

   'Select everything we've just entered and convert it to
   'a table using a specified field separator
   wrd.startofdocument 1
   topaste = wrd.fieldseparator(ssep)
   wrd.texttotable (3)

   'Autofit the columns...
   'TableColumnWidth [.ColumnWidth = number or text]
   '[, .SpaceBetweenCols = number or text]
   '[, .PrevColumn]
   '[, .NextColumn]
   '[, .AutoFit]
   '[, .RulerStyle = number]
   'wrd.tablecolumnwidth , , , , -1, 4

   'Now add the header
   wrd.TableInsertRow 1
   wrd.startofrow
   For i = 0 To j
      s = fldlst(2, i)
      If s = "" Then
         s = fldlst(0, i)
      End If
      wrd.Insert s
      wrd.nextcell
   Next 'i

   'Make headings bold
   wrd.startofdocument 0
   wrd.endofrow 1
   wrd.bold 1
   'and designate them as table headings
   wrd.tableheadings 1

   'Autofit the columns...
   'TableColumnWidth [.ColumnWidth = number or text]
   '[, .SpaceBetweenCols = number or text]
   '[, .PrevColumn]
   '[, .NextColumn]
   '[, .AutoFit]
   '[, .RulerStyle = number]
   wrd.startofdocument 0
   'Select the first column, tablecolumnwidth will be used to
   'automatically select the next column.
   wrd.tableselectcolumn
   For i = 0 To j
      If fldlst(3, i) <> "" Then
         wrd.tablecolumnwidth fldlst(3, i) & " cm", , , -1, , 0
      Else
         'Auto fit the column
         wrd.tablecolumnwidth , , , -1, -1, 0
         'Seems the move to next column doesn't work with
         'the autofit option
         wrd.tablecolumnwidth , , , -1
      End If
   Next 'j

   'Now insert the additional text
   wrd.endofdocument
   wrd.Insert Chr$(13) & addntxt

   'Finally save the file
   wrd.filesaveas (fn)


ExportToWordEnd:
   On Error Resume Next

   Set wrd = Nothing
   Exit Sub

ExportToWordErr:
   Call DisplayErr("ExportToWord", "", Err.Number, Err.Description)
   Resume ExportToWordEnd
   Resume
End Sub

