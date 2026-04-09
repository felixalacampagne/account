Attribute VB_Name = "Module1"
Const TASKSHEET = "Task_input"
Const NORMALTESTCOLOR = 16777215 'Normal balck/white cell - can be changed
Const GOODTESTCOLOR = 13561798 ' Good colour (Green)
Const QT_AC_FILES = "\\172.24.51.70\Alliance Lite\files\emission\"
'Const DEV_AC_FILES = "\\behw2218\AC_FILES" ' Windows 7 DEV machine
Const DEV_AC_FILES = "C:\Program Files\SWIFT\Alliance Lite\files" ' local machine


Const CAMSSTART = "B" ' 2   ' Column for classical morning start
Const CAMEND = "C"    ' 3   ' Column for classical morning end (lunchtime)
Const CPMSTART = "D"  ' 4   ' Column for classical afternoon start (end lunchtime)
Const CPMEND = "E"    ' 5   ' Column for classical afternoon end (classical EOD)
Const CREQUIRED = "K" ' 11  ' Column for required hours, ie. normally 37.50
Const CREMAIN = "L"   ' 12  ' Column for remaining time to work in week

Const CXSTART = "M"   ' 13  ' Column for start Extra time
Const CXEND = "N"     ' 14  ' Column for end Extra time
Const CXMULTI = "O"   ' 15  ' Column for multiple Extra time periods as SHH:MM-EHH:MM
Const CXSUM = "F"     ' 6   ' Column for total Extra time which is added to the standard time

Type DaySummary
   date As Date
   sumestimate As Double
   sumactual As Double
   isset As Boolean
End Type


Sub DailyTaskActuals()
Dim i As Integer
Dim rowno As Integer
Dim compdate As Date
Dim curdaysum As DaySummary
Dim usr As String
Dim msg As String

' find tasks which match the weekid string which is assumed
' to be formatted PRODUCT_RELEASE_ITERATION aka col2_col3_col4
' Col 04 - weekid
' Col 05 - process step
' Col 08 - Estimated Hours
' Col 09 - Estimated Date
' Col 10 - Actual Hours
' Col 11 - completion date
' col 12 - performed by user
' col 13 - planned by user
' Assume a max of 2000 rows

   If ActiveSheet.Name = TASKSHEET Then
      rowidx = Selection.row
      If Not IsDate(Cells(rowidx, 11).Value) Then
         If Not IsDate(Cells(rowidx, 9).Value) Then
            MsgBox "Select a row with a valid planned or completion date"
            Exit Sub
         End If
         curdaysum.date = DateValue(Cells(rowidx, 9).Value)
      Else
        curdaysum.date = DateValue(Cells(rowidx, 11).Value)
      End If

   usr = getUserID
   For rowno = 2 To 2000
      If usr = Cells(rowno, 12) Then
         If IsDate(Cells(rowno, 11).Value) Then
            compdate = DateValue(Cells(rowno, 11).Value)
            If compdate = curdaysum.date Then
      
                  curdaysum.sumactual = curdaysum.sumactual + Val(Cells(rowno, 10))
            End If
         End If
      End If
      If usr = Cells(rowno, 13) And (LCase$(Cells(rowno, 5)) <> "unplanned") Then
         If IsDate(Cells(rowno, 9).Value) Then
            compdate = DateValue(Cells(rowno, 9).Value)
            If compdate = curdaysum.date Then
               curdaysum.sumestimate = curdaysum.sumestimate + Val(Cells(rowno, 8))
            End If
         End If
      End If
   Next
   msg = "Date: " & curdaysum.date & vbCrLf & "Estimated hours: " & curdaysum.sumestimate & vbCrLf & "Actual hours: " & curdaysum.sumactual
   MsgBox msg, , "Actuals for " & usr
   Else
      MsgBox "Select a row with a valid planned or completion date on the " & TASKSHEET & " sheet."
   End If
   
   
   
End Sub

Function getUserID() As String
   Select Case Application.UserName
   Case "carmstro": getUserID = "Chris"
   End Select
End Function


Function getCurrentWeekRow() As Integer
Dim week As Integer
Dim rowoffset As Integer

week = DatePart("ww", Now)  ' week number

rowoffset = ((week) * 7)
getCurrentWeekRow = rowoffset - 1
End Function

Function getCurrentDayRow() As Integer
Dim row As Integer
Dim week As Integer
Dim rowoffset As Integer

row = DatePart("w", Now)    ' day of week

rowoffset = getCurrentWeekRow()
row = row + rowoffset - 2 ' Mon is day 2, rowoffset is Mon

getCurrentDayRow = row
End Function


Sub setcellbkg(row As Integer, col As Integer, green As Integer)
' This is a hack to change the colour of the cell from a 'user defined function', ie. one called
' as a formula
If green = 1 Then
    Cells(row, col).Interior.Color = rgb(196, 215, 155)      ' #C4D79B 60% green
Else
    Cells(row, col).Interior.Color = rgb(242, 220, 219) ' #F2DCDB 80% red, use .ColorIndex = xlNone for no fill
End If
End Sub


' A number of times the time cells have been accidentally typed into which erases the
' content and there is no undo in excel, ie. the original values are lost.
' To prevent this I tried to use protection on the time cells however this prevents VBA from
' entering a new value into the empty cells.
'
' Next 'Data Validation' was applied to the time cells. This allows VBA to insert the
' new time and it allows a time to be deleted which is helpful for when something causes a
' delay in the change of work state. Unfortunately it is now difficult to manually
' enter a time, eg. from the monitor file.
'
' One solution might be to manage the protection from VBA, eg. unprotect, modify, re-protect.
' This might be the best way as it is fairly simple to disable protection and then reapply it.
' Another solution is to provide some sort of dialog which could be used to programmatically
' update a cell so the data validation does not need to be removed and reapplied.
'
' Protecton is modified using something like:
' ThisWorkbook.Worksheets("Sheet1").Unprotect Password:="Password
' ThisWorkbook.Worksheets("Sheet1").Protect Password:="Password", UserInterfaceOnly:=True
' second one keeps the protection but allows VBA to update, the setting is lost when the sheet is closed.
' Reapply the protection:
' ThisWorkbook.Worksheets("Sheet1").Protect Password:="Password"
' Using "Password" makes it easy to disable protection when manual updates are required.
'


Sub InsertNow()

Dim i As Integer
Dim col As Integer
Dim rowoffset As Integer

Dim row As Integer
Dim week As Integer
Dim xtrabrk As Integer

row = DatePart("w", Now)
week = DatePart("ww", Now)

rowoffset = getCurrentWeekRow() - 1
Cells(rowoffset, "A").Value = week  'Week number


If Cells(rowoffset, "B").Text = "" Then
    ' row-2 adjust date to the Monday of the current week: Monday=2
    Cells(rowoffset, "B").Value = (Now - (row - 2))
    Cells(rowoffset, "B").NumberFormat = "dd/mm/yyyy;@"
   
    'Range("I48:L52").Select
    ' Include homeworking time (column F) in the totals. Amount of time must be added manually.
    ' 15 Jul 2024 Include the new calculations which are temporarily in the F column for comparison with the old calculation
    '             Once the calculations are confirmed to match the new calculations can be moved to colH
    Range("H13:L17").Select  ' First week of year is usually short so use the second as the reference
    Selection.Copy
    Cells(rowoffset + 1, "H").Select
    ActiveSheet.Paste
    ' 15 Jul 2024 also want the cols with the new remaining time calculations (Q & R). These will replace
    '             col J & L once the calculations are confirmed to match
    Range("Q13:R17").Select
    Selection.Copy
    Cells(rowoffset + 1, "Q").Select
    ActiveSheet.Paste
    
    ActiveSheet.Shapes("btnNow").Top = ActiveSheet.Cells(rowoffset + 7, "G").Top '15 * rowoffset
    ActiveSheet.Shapes("btnNow").Left = ActiveSheet.Cells(rowoffset, "G").Left + (ActiveSheet.Cells(rowoffset, "H").Left - ActiveSheet.Cells(rowoffset, "G").Left) / 2
    
    ' So it happened that the second week of 2024 had a holiday with the result that
    ' the hours to work for every new week was being set to the reduced value.
    ' Since the required number of hours per week is never going
    ' to change (at least as far as I'm concerned!!) lets ensure it is set to
    ' a suitable default each time.
    ' Hours per week is rowoffset+1, 11 from the week number/date row
    Cells(rowoffset + 1, CREQUIRED).Value = "37.50"
End If


row = getCurrentDayRow() 'row = row + rowoffset - 1

If Cells(row, "A").Text = "" Then
   Cells(row, "A").Value = Format$(Now, "ddd")
End If



' Things are a little more complex with "Working from home"
' Sometimes there is a work, then a journey to work, work, lunch, work
' Or work,lunch,work,break,work
' never so far but you never know; work,break,work,lunch,work,break,work
' Having reseved columns for all the permutations is too messy. Currently I
' have been using col6 as an additional time column and manually calculating
' the addition time by using cols 13 and 14 to manually track the start and stop times.
' I will automate the general idea of the additional time column calculated from 13 and 14.
' When a third (4th etc.) break is required the times in already in 13 and 14 will be
' appended ranges already in row 15 (the comment row is moved to 16)
xtrabrk = 1
For i = 2 To 5
    If Cells(row, i).Text = "" Then
        Cells(row, i).Value = Now
        Cells(row, i).NumberFormat = "hh:mm"
        xtrabrk = 0
        Exit For
    End If
Next

If xtrabrk > 0 Then
    ' Use the extra break processing
    If (Cells(row, CXSTART).Text <> "") And (Cells(row, CXEND).Text <> "") Then
        ' add last range to the summary column
        If Cells(row, CXMULTI).Text <> "" Then
            Cells(row, CXMULTI).Value = Cells(row, CXMULTI).Text & ", "
        End If
        Cells(row, CXMULTI).Value = Cells(row, CXMULTI).Text & Cells(row, CXSTART).Text & "-" & Cells(row, CXEND).Text
        Cells(row, CXMULTI).WrapText = False
        Cells(row, CXSTART).Value = ""
        Cells(row, CXEND).Value = ""
    End If
    
    If Cells(row, CXSTART).Text = "" Then
        Cells(row, CXSTART).Value = Now
        Cells(row, CXSTART).NumberFormat = "hh:mm"
    Else
        Cells(row, CXEND).Value = Now
        Cells(row, CXEND).NumberFormat = "hh:mm"
        
        ' Add to the extra time column
        Cells(row, CXSUM).Value = Cells(row, CXSUM) + Cells(row, CXEND).Value - Cells(row, CXSTART).Value
        Cells(row, CXSUM).NumberFormat = "hh:mm"
    End If
    
End If
BackupActiveWorkbook
End Sub

Function ZeroSec(adate As Date) As Date
Dim nosec As Date
    ' More incomprehensible MS shirt - time and date are handled completely independantly so no way to simply set seconds to 0
    nosec = adate - (Second(adate) / 60 / 60 / 24)
    'Debug.Print adate, "=>", nosec
    ZeroSec = nosec
End Function

Function timeStr(adate As Date) As String
    timeStr = Format$(adate, "hh:mm:ss")
End Function

Function elapsedMinutes(date2 As Date, date1 As Date) As Integer
' Performs the difference calculation as it is performed using the cell formulas
'Dim hdiff As Integer
'Dim mdiff As Integer
'Dim cfmins As Integer
    'hdiff = hour(date2 - date1) * 60
    'mdiff = Minute(date2 - date1)
    'cfmins = hdiff + mdiff

Dim ddmins As Integer

    
    ' Using integer division (backslash), which rounds down, the results are the same as
    ' using the cell formula algorithm. Using decimal division, which rounds to even (I think),
    ' the results are sometimes +1. Using DateDiff with minutes gives same result as using decimal division.
    ' For consistency with all the previous years will use seconds and integer division algorithm.
    ddmins = DateDiff("s", date1, date2) \ 60
    
    'If (cfmins <> ddmins) Then
    '    Debug.Print "Elapsed min discrepency: " & timeStr(date1) & " - " & timeStr(date2) & ": cellf:" & cfmins & " datdf:" & ddmins & " (hdif:" & hdiff & " mdif:" & mdiff & ")"
    'End If
    elapsedMinutes = ddmins
End Function



Function calctime(ByVal times As Range) As Date
' Uses DateDiff for calculating the differences in minutes.

Dim rownum As Integer
Dim elapsedmins As Integer
Dim elminasdate As Date
    Application.Volatile

    With Application.Caller
        rownum = times.row
        
        If (Cells(rownum, CAMSSTART) <> "") Then
            If (Cells(rownum, CAMEND) = "") Then
                elapsedmins = elapsedMinutes(Now, Cells(rownum, CAMSSTART))
            Else
                elapsedmins = elapsedMinutes(Cells(rownum, CAMEND), Cells(rownum, CAMSSTART))
                If ((Cells(rownum, CPMSTART) <> "") And (Cells(rownum, CPMEND) = "")) Then
                    elapsedmins = elapsedmins + elapsedMinutes(Now, Cells(rownum, CPMSTART))
                Else
                    elapsedmins = elapsedmins + elapsedMinutes(Cells(rownum, CPMEND), Cells(rownum, CPMSTART))
                End If
            End If
        End If
        elapsedmins = elapsedmins + elapsedMinutes(Cells(rownum, CXSUM), 0)
        
        ' Calculate xtra time up to now if xtratime start is set and xtratime end is not set.
        ' If both xtra time start and end are set then it is already included in xtra sum
        If ((Cells(rownum, CXSTART) <> "") And (Cells(rownum, CXEND) = "")) Then
            elapsedmins = elapsedmins + elapsedMinutes(Now, Cells(rownum, CXSTART))
        End If
        
        ' Could use (elapsedmins / 1440) where 1440 = 60 * 24 but I prefer to 'officially'
        ' convert the minutes into a date.
        ' WARNING: a period of more than 24hours will give an error but should never be a problem
        elminasdate = TimeSerial((elapsedmins \ 60), (elapsedmins Mod 60), 0) '(elapsedmins / 1440) ' 60 * 24 = 1440
    End With
    calctime = elminasdate
End Function

Function remaintime(ByVal requiredhours As Range, ByVal workedtime As Range, ByVal remaincell As Range)
'Application.Volatile

Dim wtime As Double
Dim rtime As Double
Dim rqhours As Double
Dim strtime As String
Dim hour As Integer
Dim min As Integer
    
    Application.Volatile
    ' WARNING: Excel cannot handle a -ve remaining time so I a workaround would be to change the cell
    ' color. This can't be done with a native formula function so I figured I'd use a VBA so-called
    ' user-defined-function. Some hours later I now find that changing the color is blocked for some
    ' reason unknown reason. So this was a waste of time

    ' replaces formula
    ' = IF( (Q192)>K188/24, "-",""  ) &    TEXT(ABS(K188/24-(Q192)),"[hh]:mm")

    'Debug.Print workedtime.row, workedtime.Column, (Cells(workedtime.row, workedtime.Column).Value * 24#)
    rqhours = Cells(requiredhours.row, requiredhours.Column).Value
    wtime = Cells(workedtime.row, workedtime.Column).Value * 24#
    rtime = rqhours - wtime
    'Debug.Print workedtime.row, workedtime.Column, "workedhours=" & wtime, "requiredhours=" & rqhours, "remainhours=" & rtime

    If (rtime < 0) Then
        rtime = Abs(rtime)
        strtime = "-"
        Parent.Evaluate "setcellbkg(" & remaincell.row & "," & remaincell.Column & ", 1)"
    Else
        Parent.Evaluate "setcellbkg(" & remaincell.row & "," & remaincell.Column & ", 0)"
    End If

    ' FORKING HELL - time format does not handle hour values greater than 24!
    ' and can't return a date value to be formatted using [hh]:mm because excel does not handle -ve dates
    ' ... talk about rock & hard place - this product has been around for decades and it is STILL a piece of shirt!
    hour = Int(rtime)
    min = (rtime - hour) * 60
    
    strtime = strtime & Format(hour, "00") & ":" & Format(min, "00")
    remaintime = strtime
End Function

Function calctimeOldCellFormulaVersion(ByVal times As Range) As Date
' This is the old version with both the cell forumla and minute based calculations which was used
' to verify that the calculated times were the saem.
' The times are calculated in a number of ways which give slightly different results
' 1) normal VB date arithmetic is done using dates with (truncated) zero seconds
' 2) calculations is done as it was using the cell formulas where the total differen in minutes is derived
'    by summing the hours differences as minutes and the minutes differences. This tends to result
'    in slightly lower total times due to differences in truncation
' 3) as for (1) but perform the difference on the 'raw' date and then truncate the seconds. This
'    gives results which are more or less the same as for (2)
' 4) use DateDiff for calculating the differences in minutes. This is now the chosen option.
' value from (3) is returned as it seems to be the simplest

Dim rownum As Integer
Dim elapsedtime As Date
Dim elapsedmins As Integer
Dim sdate As Date
Dim edate As Date
Dim elminasdate As Date
    Application.Volatile

    With Application.Caller
        rownum = times.row
        
        If (Cells(rownum, CAMSSTART) <> "") Then
            If (Cells(rownum, CAMEND) = "") Then
                elapsedtime = ZeroSec(Now - Cells(rownum, CAMSSTART))
                elapsedmins = elapsedMinutes(Now, Cells(rownum, CAMSSTART))
            Else
                elapsedtime = ZeroSec(Cells(rownum, CAMEND) - Cells(rownum, CAMSSTART))
                elapsedmins = elapsedMinutes(Cells(rownum, CAMEND), Cells(rownum, CAMSSTART))
                If ((Cells(rownum, CPMSTART) <> "") And (Cells(rownum, CPMEND) = "")) Then
                    elapsedtime = elapsedtime + (ZeroSec(Now - Cells(rownum, CPMSTART)))
                    elapsedmins = elapsedmins + elapsedMinutes(Now, Cells(rownum, CPMSTART))
                Else
                    elapsedtime = elapsedtime + (ZeroSec(Cells(rownum, CPMEND) - Cells(rownum, CPMSTART)))
                    elapsedmins = elapsedmins + elapsedMinutes(Cells(rownum, CPMEND), Cells(rownum, CPMSTART))
                End If
            End If
        End If
        elapsedtime = elapsedtime + ZeroSec(Cells(rownum, CXSUM))
        elapsedmins = elapsedmins + elapsedMinutes(Cells(rownum, CXSUM), 0)
        
        ' Calculate xtra time up to now if xtratime start is set and xtratime end is not set.
        ' If both xtra time start and end are set then it is already included in xtra sum
        If ((Cells(rownum, CXSTART) <> "") And (Cells(rownum, CXEND) = "")) Then
            elapsedtime = elapsedtime + (ZeroSec(Now - Cells(rownum, CXSTART)))
            elapsedmins = elapsedmins + elapsedMinutes(Now, Cells(rownum, CXSTART))
        End If
        
        'Dim elapsedtimemins As Integer
        'elapsedtimemins = elapsedtime * 24 * 60
        'If (elapsedtimemins <> elapsedmins) Then
        '    Debug.Print "Discrepency: Row:" & rownum & " elptim:" & elapsedtimemins & " elpmin:" & elapsedmins
        'End If
        
        ' No discrepencies reported when elapsedtime is converted to mins and compared to elapsedmins!
        ' Comparing elapsedmins converted to excel date is not so clear cut. A difference is
        ' reported but it is of the order of 1E-11. The formatted time is the same in both cases
        ' Converting both values to a date using TimeSerial does produce identical values.
        ' Ignoring differences less that 1/10th second results in no discrepancies.
        ' I think I prefer to 'officially' convert the elapsed minutes to a date via TimeSerial
        ' elapsedtime can now be decommissionned!
        Dim maxddif As Double
        maxddif = (0.1 / 60 / 60 / 24)
        elminasdate = TimeSerial((elapsedmins \ 60), (elapsedmins Mod 60), 0) '(elapsedmins / 1440) ' 60 * 24 = 1440
        If (Abs(elapsedtime - elminasdate) > maxddif) Then
            Debug.Print "Discrepency: Row:" & rownum & " elptim:" & elapsedtime & " elpmin:" & elminasdate & " delta:" & elapsedtime - elminasdate
        End If
        
    End With
    ' How to convert minutes to Excel date/time: elapsedmins / 60 / 24
    ' Debug.Print rownum, elapsedtime * 24, elapsedmins / 60
    calctime = elminasdate ' elapsedtime
End Function
Sub BackupActiveWorkbook()
'
' BackupActiveWorkbook
'
' Can't use the ActiveWorkbook SaveAs method as it changes the name of the active workbook and
' it has a very annoying prompt if the backup file already exists, which it almost certainly
' will given that the whole point is that it is a backup.
'
' Use the MS Scripting Object FileSystemObject to do the copy - this is available on
' more or less every Windows.

Dim file As New FileSystemObject
Dim srcname As String
Dim backupname As String
On Error GoTo BackupActiveWorkbookErr
srcname = ActiveWorkbook.FullName


    backupname = "C:\DDrive\cpa\Diary\Tracker\" & ActiveWorkbook.Name
    
    ' Make sure disk is up to date
    ActiveWorkbook.Save
    
    
    'Set file = New FileSystemObject
    ' No return code, only an error
    ' Assume the remote drive is unavailable so make a local copy
    On Error Resume Next
    file.CopyFile srcname, backupname, True
    If Err.Number > 0 Then
        backupname = ActiveWorkbook.Path & "\backup\" & ActiveWorkbook.Name
        file.CopyFile srcname, backupname, True
    End If
BackupActiveWorkbookEnd:
    Exit Sub
    
BackupActiveWorkbookErr:
    MsgBox "BackupActiveWorkbook: Failed to backup: " & Err.Description
    Resume BackupActiveWorkbookEnd
End Sub


Sub transfer()
Dim i As Integer
Dim j As Integer
For Z = 0 To 1
For j = 0 To 4
    For i = 0 To 3
        ActiveWorkbook.Sheets("NewTracker").Cells(6 + (Z * 7) + j, 2 + i).Value = ActiveWorkbook.Sheets("Tracker").Cells(2 + i, (2 + (Z * 6)) + j).Value
    Next
Next
Next
End Sub

' Updating the times in the four main columns automatically updates the totals columns
' however this is not the case for the xtra times. Occasionally it is necessary to
' add or modify the xtra time values. This sub will recalculate the 'xtra' column based
' on the current contents of the Xtra columns
Sub recalcXtra()
Dim xranges() As String
Dim xrange() As String
Dim tokcnt As Long
Dim sxrange As String
Dim cnt As Long
Dim i As Integer
Dim stopt As Date
Dim startt As Date
Dim mins As Date
Dim totmins As Date

   row = ActiveCell.row
   ReDim xranges(0)
   ReDim xrange(0)
   ' Add to the extra time column
   Cells(row, CXSUM).Value = Cells(row, CXEND).Value - Cells(row, CXSTART).Value
   Cells(row, CXSUM).NumberFormat = "hh:mm"
   
   If Cells(row, CXMULTI).Text <> "" Then
      ' Format of this cell is;
      ' xstart-xstop, xstart-xstop
      tokcnt = Tokens2Array(Cells(row, CXMULTI).Text, ",", xranges)
      For i = 0 To tokcnt - 1
         sxrange = xranges(i)
         sxrange = Trim$(sxrange)
         cnt = Tokens2Array(sxrange, "-", xrange)
         startt = TimeValue(xrange(0))
         stopt = TimeValue(xrange(1))
         mins = stopt - startt
         totmins = totmins + mins
      Next
      
      mins = TimeValue(Cells(row, CXSUM).Text)
      totmins = totmins + mins
      Cells(row, CXSUM).Value = totmins
      Cells(row, CXSUM).NumberFormat = "hh:mm"
   End If
End Sub

Function Tokens2Array(ByVal pstring As String, pdelims As String, pTokens() As String) As Long
'15 Dec 98  Returns the count of items added, not the max. ubound. Thus a value
'  of zero now means that zero tokens were found.
'14 Dec 98  Handles empty tokens correctly. Previously empty tokens were not
'  added to the array and parsing stopped when an empty token was found.
Dim i As Long
Dim ln As Long
Dim rc As Long
Dim stmp As String
Dim Start As Long
Dim stoken As String
Dim tokcnt As Long
   On Error GoTo Tokens2ArrayErr
   
   ReDim pTokens(0)
   
   rc = 0
   Start = 1
   tokcnt = 0
   ln = -1
   Do
      Start = GetAnyToken(pstring, Start, pdelims, stoken, ln)
      ReDim Preserve pTokens(tokcnt)
      pTokens(tokcnt) = stoken
      tokcnt = tokcnt + 1
      
      'Debug.Print Mid$(pstring, start, 70)
   Loop While Start <= ln
  
Tokens2ArrayEnd:
   Tokens2Array = tokcnt
   Exit Function
 
Tokens2ArrayErr:
    Resume Tokens2ArrayEnd
End Function

'If pStart is greater than pLen an error ueEndOfStringError is raised
'Note: ptoken and plen are used for output
'If plen < 0 then it will be replaced with the full length of pstring
'otherwise only the first plen chars of pstring will be checked. plen is relative
'to position 1 in the string, NOT relative to pstart.
Function GetAnyToken(pstring As String, pstart As Long, pdelims As String, ptoken As String, plen As Long) As Long
Dim i As Long
Dim Start As Long
   On Error GoTo GetAnyTokenErr
   
   ReDim pTokens(0)
   
   If plen < 1 Then
      plen = Len(pstring)
   End If
   Start = pstart
   
   If Start < 1 Then Start = 1
   If Start <= plen Then
      'looping on pstring as we need the first occurrence of a delimiter in it
      For i = Start To plen
         If InStr(pdelims, Mid$(pstring, i, 1)) > 0 Then
            Exit For
         End If
      Next i
      If i >= Start And i <= plen Then
         ptoken = Mid$(pstring, Start, i - Start)
         Start = i + 1
      ElseIf i > plen Then
         ptoken = Mid$(pstring, Start)
         Start = plen + 1
      Else
         ptoken = ""
         Start = i + 1
      End If
   Else
      ptoken = ""
      On Error GoTo 0
      Err.Raise vbObjectError, "GetAnyToken", "End of string"
   End If
   
   
GetAnyTokenEnd:
   GetAnyToken = Start
   Exit Function
   
GetAnyTokenErr:
   Resume GetAnyTokenEnd
End Function


Function Clipboard(Optional StoreText As String) As String
'Read from clipboard if no/zero length argument passed
'Write to clipboard if non-zero length argument is passed
Dim x As Variant

  'Store as variant for 64-bit VBA support
  x = StoreText

  'Create HTMLFile Object
  With CreateObject("htmlfile")
    With .parentWindow.clipboardData
      Select Case True
        Case Len(StoreText)
            .setData "text", x
        Case Else
            Clipboard = .GetData("text")
      End Select
    End With
  End With

End Function

