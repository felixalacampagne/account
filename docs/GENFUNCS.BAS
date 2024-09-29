Attribute VB_Name = "GENFUNCS"
Option Explicit
Const MODNAME = "GENFUNCS.BAS"

Const SSFORMSIZE = "FormSize"
Const SSGRIDORDR = "GridOrder"

Global Const INIFILENAME = "ACCOUNT32.INI"
Global Const MDAFILENAME = "ACCLIVE.MDA"

Global gFuncName As String
Global gFullIniName As String

Const SAVSEARCHSEP = "|"

Function AUcase(ka As Integer) As Integer
'Converts an ascii integer into an ascii uppercase integer
Dim s As String
Dim a As Integer
   a = ka
   'If KeyAscii > 31 Then
      s = Chr$(a)
      s = UCase$(s)
      a = Asc(s)
   'End If

   AUcase = a
End Function

Function CurrFromDB(pcr As Variant) As Currency
   If IsNull(pcr) Then
      CurrFromDB = CCur(0)
   Else
      CurrFromDB = CCur(pcr)
   End If
End Function






Sub RestoreSearch(search As String, pfrm As Form)
Dim i As Integer, j As Integer
Dim tmpstr As String
Dim rc As Integer
Dim s As String
On Error Resume Next

   tmpstr = search
   For i = 0 To pfrm.Controls.Count - 1
      If TypeOf pfrm.Controls(i) Is CheckBox Then
         j = InStr(tmpstr, SAVSEARCHSEP)
         pfrm.Controls(i).value = CInt(Left$(tmpstr, j - 1))
      ElseIf TypeOf pfrm.Controls(i) Is TextBox Then
         j = InStr(tmpstr, SAVSEARCHSEP)
         pfrm.Controls(i).Text = Left$(tmpstr, j - 1)
      ElseIf TypeOf pfrm.Controls(i) Is ComboBox Then
         j = InStr(tmpstr, SAVSEARCHSEP)
         pfrm.Controls(i).ListIndex = CInt(Left$(tmpstr, j - 1))
      End If

      If Err <> 0 Then
         Exit Sub
      End If

      If j > 0 Then
         tmpstr = Right$(tmpstr, Len(tmpstr) - j)
         j = 0
      End If

   Next i

End Sub



'Sub SaveFormSize(frm As Form, profname As String)
'gFuncName = MODNAME & "." & "SaveFormSize "
'Dim ProfStr As String
'Dim X As Integer
'
'   'Save form size for next startup
'   ProfStr = Format$(frm.Left, "00000") & "," & Format$(frm.Top, "00000") & "," & Format$(frm.Width, "00000") & "," & Format$(frm.Height, "00000")
'   X = WritePrivateProfileString(profname, SSFORMSIZE, ProfStr, gFullIniName)
'
'End Sub

Sub SaveSearch(search As String, pfrm As Form)
Dim i As Integer, j As Integer
Dim tmpstr As String
Dim s As String
Dim vdt As Variant
Dim rc As Integer

   For i = 0 To pfrm.Controls.Count - 1
      If TypeOf pfrm.Controls(i) Is CheckBox Then
         tmpstr = tmpstr & Str$(pfrm.Controls(i).value) & SAVSEARCHSEP
      ElseIf TypeOf pfrm.Controls(i) Is TextBox Then
         tmpstr = tmpstr & pfrm.Controls(i).Text & SAVSEARCHSEP
      ElseIf TypeOf pfrm.Controls(i) Is ComboBox Then
         tmpstr = tmpstr & pfrm.Controls(i).ListIndex & SAVSEARCHSEP
      End If
   Next i

   search = tmpstr
End Sub



Sub SetCurColFmt(ctl As TDBGrid, ds As Recordset, CurFmt As String)
gFuncName = MODNAME & "." & "SetCurColFmt "
Dim i As Integer
Dim FldName As String
Dim FldType As Integer

    For i = 0 To ctl.Columns.Count - 1
        FldName = ctl.Columns(i).DataField
        FldType = ds.Fields(FldName).Type
        If FldType = dbCurrency Then
            ctl.Columns(i).NumberFormat = CurFmt
        End If
    Next

End Sub


