Attribute VB_Name = "sndkey"
Option Explicit
Declare Function EnumWindows Lib "user32" (ByVal lpEnumFunc As Long, lParam As Any) As Long
Declare Function EnumDesktopWindows Lib "user32" (ByVal hDesktop As Long, ByVal lpfn As Long, ByVal lParam As Long) As Long

Declare Function ExitWindowsEx Lib "user32" (ByVal uint As Long, ByVal dwReserved As Long) As Integer
Const EWX_LOGOFF = 0
Const EWX_SHUTDOWN = 1
Const EWX_REBOOT = 2
Const EWX_FORCE = 4
Const EWX_POWEROFF = 8
      
'The GetLastError function returns the calling thread's last-error
'code value. The last-error code is maintained on a per-thread basis.
'Multiple threads do not overwrite each other's last-error code.
Private Declare Function GetLastError Lib "Kernel32" () As Long

Declare Function GetWindowText Lib "user32" Alias "GetWindowTextA" (ByVal hWnd As Long, ByVal lpString As String, ByVal cch As Long) As Long


Const SMTO_NORMAL = 0
Const SMTO_BLOCK = &H1
Const SMTO_ABORTIFHUNG = &H2
Const ENDSESSION_LOGOFF = &H80000000
Const WM_QUERYENDSESSION = &H11&
Const WM_ENDSESSION = &H16
Const SYNCHRONIZE = (&H100000)
Const STANDARD_RIGHTS_REQUIRED = (&HF0000)
Const PROCESS_ALL_ACCESS = STANDARD_RIGHTS_REQUIRED Or SYNCHRONIZE Or &HFFF

Dim mTitles() As String



Function GetWindows() As Variant
Dim rc As Long
Dim vrc As Variant
   'Only need to do this for windows 95
   ReDim mTitles(0)
   ' rc = EnumWindows(AddressOf EnumWindowsCallback, 0)
   rc = EnumWindows(AddressOf EnumWindowsCallback, 0)
   vrc = mTitles
   GetWindows = vrc
End Function



Function EnumWindowsCallback(ByVal hWnd As Long, lParam As Long) As Long
Dim rc As Integer
Dim sbuf As String
Dim cch As Long
   If mTitles(UBound(mTitles)) <> "" Then
      ReDim Preserve mTitles(UBound(mTitles) + 1)
   End If
   
   cch = 255
   sbuf = String$(cch, Chr$(0))
   rc = GetWindowText(hWnd, sbuf, cch)
   If rc > 0 Then
      sbuf = Left$(sbuf, rc)
   Else
      sbuf = ""
   End If
   
   If sbuf <> "" Then
      mTitles(UBound(mTitles)) = UCase$(sbuf)
      'Debug.Print sbuf
   End If
   
   EnumWindowsCallback = 1
End Function

'Based on the keyplayer codes but doesn't allow shutdown/reboot
'and it doesn't update any displays. The WAIT command uses a
'sleep statement which will make the main app appear to have
'hung
Sub DoKeys(keys As String)
Dim lines() As String
Dim wrkline As Long
Dim waitsecs As Long
Dim i As Integer
Dim j As Integer

   On Error Resume Next
   ReDim lines(0)
   Call DebugLog("sndkey.DoKeys", "", 0, "Sending key string: " & keys)
   Call Tokens2Array(keys, vbCrLf, lines)

   For wrkline = 0 To UBound(lines)
      If lines(wrkline) <> "" Then
      
         Call DebugLog("sndkey.DoKeys", "", 0, "Sending line: " & lines(wrkline))
         If Left$(lines(wrkline), 5) = "%APP%" Then
            If ActivateApp(Mid$(lines(wrkline), 6)) = False Then
               Call DebugLog("sndkey.DoKeys", "", 0, "Failed to activate APP " & Mid$(lines(wrkline), 6))
               'Send the keys anyway!!
               'Exit For
            End If
            For j = 0 To 9
               DoEvents
               Sleep 500
            Next
         ElseIf Left$(lines(wrkline), 6) = "%WAIT%" Then
            'restart the time before continuing
            waitsecs = HMStoSecs(Mid$(lines(wrkline), 7))
            Sleep waitsecs * 1000
         ElseIf lines(wrkline) <> "" Then
            SendKeys lines(wrkline), True
            For i = 0 To 9
               DoEvents
               Sleep 1
            Next
         End If
      End If
   Next
   
End Sub

Function ActivateApp(apptitle As String) As Boolean
Dim varray As Variant
Dim i As Integer


Dim ssearch As String
   On Error GoTo ActivateAppErr
   varray = GetWindows()
   ssearch = "*" & UCase$(apptitle) & "*"
   For i = 0 To UBound(varray)
      If varray(i) Like ssearch Then
         Call DebugLog("sndkey.ActivateApp", "", 0, "Activating " & varray(i))
         AppActivate varray(i), False

         ActivateApp = True
         Exit Function
      End If
   Next

ActivateAppEnd:
   ActivateApp = False
   Exit Function
ActivateAppErr:
   Call DebugLog("sndkey.ActivateApp", "", Err.Number, Err.Description)
   Resume ActivateAppEnd
   Resume
End Function
