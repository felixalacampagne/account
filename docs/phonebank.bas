Attribute VB_Name = "PHONEBANKING"
Option Explicit

Global Const KBPRINCIPLEACCOUNT = "P"
Global Const KBOTHERACCOUNT = "M"
Global Const KBACCOUNTS = KBPRINCIPLEACCOUNT + KBOTHERACCOUNT
Global Const CERAPRINCIPLEACCOUNT = "C"
Global Const CERAOTHERACCOUNT = "E"
Global Const CERAACCOUNTS = CERAPRINCIPLEACCOUNT + CERAOTHERACCOUNT

Global Const RELATEDACCOUNTS = CERAOTHERACCOUNT + KBOTHERACCOUNT
Global Const PRINCIPLEACCOUNTS = KBPRINCIPLEACCOUNT + CERAPRINCIPLEACCOUNT

Global Const ACCOUNTACCOUNTS = CERAACCOUNTS + KBACCOUNTS + "S"

'Global gdb As database

Global gAccName As String
Global gAccNumb As String
Global gAccId As Long
Global gTransCom() As String

Const PHONECHARS = "0123456789*# ."

Function IsPhoneChar(k As Integer) As Integer
'16 Feb 96  Created
'02 Jun 12 Obsolete, although may be useful for limiting entries in  account code fields
   IsPhoneChar = k
   'If InStr(PHONECHARS, Chr$(k)) > 0 Then
   '   IsPhoneChar = k
   'ElseIf k = 8 Or k = 127 Then
   '   IsPhoneChar = k
   'Else
   '   IsPhoneChar = 0
   'End If
End Function
'Converts a communication code into text. If the communication is
'included in the input string it must be separated from the code by
' hyphen ("-").
Public Function ExpandComStr(ByVal comstr As String) As String
'21 Nov 98 created


Dim comcode As String
Dim l As Long

   
   l = InStr(comstr, "-")
   If l > 0 Then
      comcode = Left$(comstr, l - 1)
      comstr = Mid$(comstr, l + 1)
   Else
      'Assume it's just the code without a communication
      comcode = comstr
      comstr = ""
   End If
   
   For l = LBound(gTransCom, 2) To UBound(gTransCom, 2)
      If comcode = gTransCom(1, l) Then
         comcode = gTransCom(0, l)
         If comstr <> "" Then
            comcode = comcode & ": " & comstr
         End If
         ExpandComStr = comcode
         Exit Function
      End If
   Next
   
   'We didn't find the code
   ExpandComStr = comcode & ": " & comstr
End Function

Function zapspace(ByVal src As String) As String
Dim i As Integer
Dim s As String
Dim c As String
   For i = 1 To Len(src)
      c = Mid$(src, i, 1)
      If c <> " " Then
         s = s & c
      End If
   Next 'i
   zapspace = s
End Function



' 25 Jun 2013 Log the available windows. For some reason the CBC window
'             is no longer found on the 64bit Win7 running IE10-32bit
'             but it is found on the 32bit Win7 running IE10-32bit.
' 29 Jun 2013 Turns out the app name is now uppercase on the Win7 64bit machine!
Function GetNamedIEApp(title As String) As Object
On Error GoTo GetNamedIEAppErr
Dim objShell As Object
Dim objWindows As Object
Dim objWindow As Object
Dim lngSingleWindow As Long
Dim intOption As Integer
Dim strMessage As String, strReturnValue As String
Dim normtitle As String
Set objShell = CreateObject("Shell.Application")
Set objWindows = objShell.Windows
lngSingleWindow = -1
Dim found As Boolean
Dim applist As String
Dim iepattern As String
   found = False

   normtitle = LCase$(title)
   iepattern = "iexplore.exe"
   For Each objWindow In objWindows
      'Build a list of windows, make sure they are Internet Explorer
      If LCase$(Right$(objWindow.fullname, 12)) = iepattern Then
         If LCase$(objWindow.LocationName) Like normtitle Then
            Set GetNamedIEApp = objWindow
            found = True
            Exit For
         Else
            applist = applist & "ISIE: " & objWindow.fullname & ", LocationName: " & objWindow.LocationName & vbCrLf
         End If
      Else
         applist = applist & "NOTIE: " & objWindow.fullname & vbCrLf
      End If
   Next

found = False
If Not found Then
   Call DebugLog("PhoneBanking.GetNamedIEApp", "IENOMATCH", 0, applist & "No match for pattern '" & iepattern & "' and title '" & title & "'")
End If

GetNamedIEAppEnd:
Set objWindow = Nothing
Set objWindows = Nothing
Set objShell = Nothing
Exit Function
GetNamedIEAppErr:
Call DisplayErr("PhoneBanking.GetNamedIEApp", "", Err.Number, Err.Description)
Resume GetNamedIEAppEnd
End Function


Sub SetFields2(ietitle As String, formname As String, elements() As String, values() As String)
On Error Resume Next
Dim objIE As Object
Dim objParent As Object
Dim objInputElement As Object
Dim lngRow As Long

Set objIE = GetNamedIEApp(ietitle)
'Make sure an IE object was hooked
If TypeName(objIE) = "Nothing" Then
  MsgBox "Could not hook Internet Explorer object", vbCritical, "GetFields() Error"
  GoTo Clean_Up
End If

For lngRow = 0 To UBound(elements)       '2 To ActiveSheet.UsedRange.Rows.Count
  If values(lngRow) <> "" Then           'ActiveSheet.Cells(lngRow, cElement_SetValue) <> "" Then

    Set objParent = objIE.Document.Forms(CVar(formname))
    With objParent

        objParent.item(CVar(elements(lngRow))).Value = values(lngRow)
    End With
    If Err.Number <> 0 Then
      Debug.Print "Error Writing: Row " & lngRow, elements(lngRow), values(lngRow), Err.Description
      
      Err.Clear
    End If
  End If
Next lngRow
Clean_Up:
Set objParent = Nothing
Set objIE = Nothing
End Sub

' fields = collection of clsProperty objects
' Returns 0 if successful
'         1 if failed
Function SetFields3(ietitle As String, formname As String, fields As Collection) As Integer
On Error GoTo Setfields3Err
Dim objIE As Object
Dim objParent As Object
Dim objInputElement As Object
Dim prop As clsProperty
Dim fname As Variant
Dim listallfields As Boolean
Dim msg As String
Dim item As Object
   SetFields3 = 1

   
   
   Set objIE = GetNamedIEApp(ietitle)
   'Make sure an IE object was hooked
   If TypeName(objIE) = "Nothing" Then
      MsgBox "Could not find the Internet Explorer window called '" & ietitle & "'", vbCritical, "Account"
      GoTo Clean_Up
   End If

   ' 28 Apr 2011 Form has no name and a variable id. Luckily there is only one form!
   '             Elements have variable ids but appear to be accessible using their names
   '             (at least with IE9)
   If formname = "" Then
       fname = CVar(0)
   Else
       fname = CVar(formname)
   End If
   Set objParent = objIE.Document.Forms(fname)
   
   listallfields = False
   
   For Each prop In fields                  'lngRow = 0 To UBound(elements)       '2 To ActiveSheet.UsedRange.Rows.Count
      If "" & prop.Key <> "" Then
         ' 02-Jun-2012 Some fields no longer supported with Eurozone transfer form
         On Error Resume Next
         objParent.item(CVar(prop.Key)).Value = prop.Value
         If Err.Number <> 0 Then
            Call DebugLog("PhoneBanking.SetFields3", "", Err.Number, "Error Writing: " & prop.Key & " '" & prop.Value & "': " & Err.Description)
            listallfields = True
            Err.Clear
         Else
            objParent.item(CVar(prop.Key)).fireEvent ("onblur") 'Causes the CBC-Online fields to format themselves
            Err.Clear 'In case a fields doesn't support the event and it causes an error
         End If
         On Error GoTo Setfields3Err
      End If
    Next

   If listallfields Then
      For Each item In objParent
         msg = "Field id:" & item.id & " name: " & item.Name & " NodeName: " & item.nodename & " Type:" & item.Type
         Call DebugLog("PhoneBanking.SetFields3", "ListFields", 0, msg)
      Next
   End If

SetFields3 = 0
Clean_Up:
Set objParent = Nothing
Set objIE = Nothing
Exit Function
Setfields3Err:
   Call DisplayErr("PhoneBanking.SetFields3", "", Err.Number, Err.Description)
   Resume Clean_Up
   

End Function

' Safe way to set the Property value in case the property is not present
Sub setPropertyValue(Key As String, Value As String, props As Collection)
On Error GoTo setPropertyValueEnd
Dim prop As clsProperty

    Set prop = props.item(Key)
    prop.Value = Value
    
setPropertyValueEnd:
End Sub

Sub test_iefill()
Dim formname As String
Dim elements() As String
Dim values() As String

formname = "aForm"
ReDim elements(9)
ReDim values(UBound(elements))

elements(0) = "Dyl0702_MemoDt"
elements(1) = "Dyl0702_OpdBd"
elements(2) = "Dyl0701_CrRekNrFmt"
elements(3) = "Dyl0702_BgnNm"
elements(4) = "Dyl0702_BgnStrNm"
elements(5) = "Dyl0702_BgnPostKd"
elements(6) = "Dyl0702_BgnGemNm"
elements(7) = "Dyl0701_MedTk1"
elements(8) = "Dyl0701_MedTk2"
elements(9) = "OGM" 'Transfer reference

values(0) = "10-06-2010"
values(1) = "2,00"
values(2) = "732000274176"
values(3) = "Chris Armstrong"
values(4) = "Rue de la Chapelle 18"
values(5) = "4280"
values(6) = "Merdorp"
values(7) = "Test ref line 1"
values(8) = "Test ref line 2"
values(9) = ""
SetFields2 "CBC-Online", formname, elements(), values()

'Appname=CBC-Online
'formname=aForm
'paydate=Dyl0702_MemoDt
'amount=Dyl0702_OpdBd
'creditaccount=Dyl0701_CrRekNrFmt
'CREDITADDRESS1=Dyl0702_BgnNm
'CREDITADDRESS2=Dyl0702_BgnStrNm
'UCOMLINE1=Dyl0701_MedTk1
'UCOMLINE2=Dyl0701_MedTk2
'SCOM=OGM


End Sub

Public Function CheckOGM(ogm As String) As String
Dim cleanogm As String
Dim ogmval As String
Dim ogmchk As String
Dim i As Integer
Dim sum As Double
Dim c As String
Dim l As Double


cleanogm = zapspace(ogm)

If Len(cleanogm) = 12 Then
   ogmval = Left$(cleanogm, 10)
   ogmchk = Right$(cleanogm, 2)
   sum = 0
   For i = 1 To 10
      c = Mid$(ogmval, i, 1)
      If Not IsNumeric(c) Then
         GoTo CheckOGMEnd
      End If
   Next
   If sum > -1 Then
      sum = CDbl(ogmval)
      ' Can't use Mod - it gives Overflow and appears to only work with integer
      'sum = CDbl(sum) Mod CDbl(97)
      

      sum = sum - (Int(sum / 97) * 97)
      
      If sum = 0 Then
         sum = 97
      End If
      
      l = Val(ogmchk)
      If l = 0 Then
         ogmchk = "" & sum
      ElseIf l <> sum Then
         MsgBox "Checksum for " & ogm & " is not correct: " & ogmchk & " vs " & sum
         ogmchk = "" & sum
      End If
      
      cleanogm = ogmval & ogmchk
      cleanogm = Left$(cleanogm, 3) & " " & Mid$(cleanogm, 4, 4) & " " & Mid$(cleanogm, 8)
   End If
End If

CheckOGMEnd:
CheckOGM = cleanogm
End Function

Public Function GetComment(dbstr As String) As String
Dim i As Integer
Dim code As Integer
Dim cmnt As String
    cmnt = dbstr
    If dbstr <> "" Then
        i = InStr(dbstr, "-")
        If i = 2 Or i = 3 Then
            cmnt = Mid$(dbstr, i + 1)
            code = Val(Left$(dbstr, i - 1))
            code = Trim$(code)
            If code = 1 Then
               cmnt = zapspace(cmnt)
            End If
        End If
    End If
   GetComment = cmnt
End Function
