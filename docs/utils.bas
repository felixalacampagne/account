Attribute VB_Name = "UTILS"
'************************************************
'************************************************
'************************************************
'*******                                   ******
'*******     Only make changes to the      ******
'*******           ORIGINAL                ******
'*******                                   ******
'*******                                   ******
'************************************************
'************************************************
'************************************************
'*******                                   ******
'******* Version:                          ******
'******* 13 Jan 2001                       ******
'*******                                   ******
'************************************************
'*******  Another fine piece of code from  ******
'*******    SmallCat Utilities (c 2001)    ******
'************************************************

'
'13 Jan 01  Added BailOut to start/stop the WinShut program to perform a
'           system shutdown
'13 Oct 00  Changed the day code value to be date, time and day
'           to make the name more useful
'11 Oct 00  Save/RestoreFormSize saves to the registry
'30 Jul 99  ExpandVars %date+?% variable now allows for offsets of
'           more than one to be specified in a single command
'21 Jul 99  ExpandVars parameters changed to allow an ini
'           file to be specified as the source of the variables
'14 Jun 99  Added user variables to ExpandVars
'31 May 99  Fix dayofweek not using Monday as first day
'25 May 99  ExpandVars variable expanded to allow for more date options
'15 Dec 98  Tokens2Array returns count of tokens found 0 means no items found
'           GetAnyToken raises an error if the start is beyond the end of
'           the string
'14 Dec 98  Fix bug in restore form size when screen is saved off screen
'14 Dec 98  Tokens2Array no longer ignores empty tokens, and no longer stops parsing
'  when the first empty token is encountered
'14 Nov 98  Added ExpandVars function
'15 Sep 98  Added LVM constants, TextFromDB
'28 Jul 98  Added the debugging stuff.


Public Enum UtilErrors
   ueEndOfStringError = vbObjectError
End Enum

Global Const LVM_FIRST = &H1000
Global Const LVM_SETEXTENDEDLISTVIEWSTYLE = LVM_FIRST + 54
Global Const LVM_GETEXTENDEDLISTVIEWSTYLE = LVM_FIRST + 55
Global Const LVS_EX_FULLROWSELECT = &H20
Global Const LVS_EX_CHECKBOXES = &H4
Global Const LVS_EX_UNDERLINEHOT = &H800
Global Const LVS_EX_UNDERLINECOLD = &H1000

Global Const LB_FINDSTRINGEXACT = &H1A2&
Global Const LB_FINDSTRING = &H18F&
Global Const LB_OKAY = 0
Global Const LB_ERR = -1
Global Const LB_ERRSPACE = -2
Global Const WM_CLOSE = &H10

Const SSFORMSIZE = "FormSize"


Const TIME_ZONE_ID_UNKNOWN = 0
Const TIME_ZONE_ID_STANDARD = 1
Const TIME_ZONE_ID_DAYLIGHT = 2


Private Type SYSTEMTIME
   wYear As Integer
   wMonth As Integer
   wDayOfWeek As Integer
   wDay As Integer
   wHour As Integer
   wMinute As Integer
   wSecond As Integer
   wMilliseconds As Integer
End Type
Private Type TIME_ZONE_INFORMATION
   Bias As Long
   wideStandardName((32 * 2) - 1) As Byte
   StandardDate As SYSTEMTIME
   StandardBias As Long
   wideDaylightName((32 * 2) - 1) As Byte
   DaylightDate As SYSTEMTIME
   DaylightBias As Long
End Type

Declare Function GetTimeZoneInformation Lib "kernel32" (lpTimeZoneInformation As TIME_ZONE_INFORMATION) As Long
Declare Function SystemTimeToTzSpecificLocalTime Lib "kernel32" ( _
  lpTimeZone As TIME_ZONE_INFORMATION, _
  lpUniversalTime As SYSTEMTIME, _
  lpLocalTime As SYSTEMTIME) As Long
Declare Function GetLocalTime Lib "kernel32" (lpLocalTime As SYSTEMTIME) As Long
  
Declare Function GetPrivateProfileString Lib "kernel32" Alias "GetPrivateProfileStringA" (ByVal lpApplicationName As String, ByVal lpKeyName As String, ByVal lpDefault As String, ByVal lpReturnedString As String, ByVal nSize As Long, ByVal lpFileName As String) As Long
Declare Function WritePrivateProfileString Lib "kernel32" Alias "WritePrivateProfileStringA" (ByVal lpApplicationName As String, ByVal lpKeyName As Any, ByVal lpString As Any, ByVal lpFileName As String) As Long
Declare Function WritePrivateProfileSection Lib "kernel32" Alias "WritePrivateProfileSectionA" (ByVal lpAppName As String, ByVal lpKeyValuePairs As String, ByVal lpIniFileName As String) As Long
Declare Function GetTempPath Lib "kernel32" Alias "GetTempPathA" (ByVal nBufferLength As Long, ByVal lpBuffer As String) As Long
Declare Function GetDiskFreeSpaceEx Lib "kernel32" Alias "GetDiskFreeSpaceExA" (ByVal lpRootPathName As String, curBytesAvailable As Currency, curBytesTotal As Currency, curBytesFreeTotal As Currency) As Long

'Public Declare Function SendMessageByString& Lib "User" Alias "SendMessage" (ByVal hWnd%, ByVal wMsg%, ByVal wParam%, ByVal lParam$)
'Public Declare Function SendMessageByNum% Lib "User" Alias "SendMessage" (ByVal hWnd%, ByVal wMsg%, ByVal wParam%, ByVal lParam&)
Public Declare Function SendMessage Lib "user32" Alias "SendMessageA" (ByVal hWnd As Long, ByVal wMsg As Long, ByVal wParam As Long, lParam As Any) As Long
Public Declare Function SendMessageString Lib "user32" Alias "SendMessageA" (ByVal hWnd As Long, ByVal wMsg As Long, ByVal wParam As Long, ByVal sParam As String) As Long
Public Declare Function PostMessage Lib "user32" Alias "PostMessageA" (ByVal hWnd As Long, ByVal wMsg As Long, ByVal wParam As Long, ByVal lParam As Long) As Long
Declare Function ShellExecute Lib "shell32.dll" Alias "ShellExecuteA" _
                   (ByVal hWnd As Long, ByVal lpszOp As String, _
                    ByVal lpszFile As String, ByVal lpszParams As String, _
                    ByVal LpszDir As String, ByVal FsShowCmd As Long) _
                    As Long

      Private Declare Function GetDesktopWindow Lib "user32" () As Long

      Const SW_SHOWNORMAL = 1

      Const SE_ERR_FNF = 2&
      Const SE_ERR_PNF = 3&
      Const SE_ERR_ACCESSDENIED = 5&
      Const SE_ERR_OOM = 8&
      Const SE_ERR_DLLNOTFOUND = 32&
      Const SE_ERR_SHARE = 26&
      Const SE_ERR_ASSOCINCOMPLETE = 27&
      Const SE_ERR_DDETIMEOUT = 28&
      Const SE_ERR_DDEFAIL = 29&
      Const SE_ERR_DDEBUSY = 30&
      Const SE_ERR_NOASSOC = 31&
      Const ERROR_BAD_FORMAT = 11&
                    
Declare Function OpenProcess Lib "kernel32" (ByVal dwDesiredAccess As Long, _
  ByVal bInheritHandle As Byte, ByVal dwProcessId As Long) As Long
Declare Function GetExitCodeProcess Lib "kernel32" (ByVal hProcess As Long, _
  ByRef lpExitCode As Long) As Integer
Declare Function TerminateProcess Lib "kernel32" (ByVal hProcess As Long, ByVal uExitCode As Integer) As Long
Global Const STILL_ACTIVE = &H103&
'#define STATUS_PENDING                   ((DWORD   )0x00000103L) in WINNT.H
'#define STILL_ACTIVE                        STATUS_PENDING in winbase.h
'Values returned in lpExitCode can be
'STILL_ACTIVE
'The exit value specified in the ExitProcess or TerminateProcess function.
'The return value from the main or WinMain function of the process.
'The exception value for an unhandled exception that caused the process to terminate.

Declare Function CloseHandle Lib "kernel32" (ByVal dwHandle As Long) As Long
Global Const SYNCHRONIZE = &H100000
Global Const STANDARD_RIGHTS_REQUIRED = &HF0000

'#define PROCESS_TERMINATE         (0x0001)
'#define PROCESS_CREATE_THREAD     (0x0002)
'#define PROCESS_VM_OPERATION      (0x0008)
'#define PROCESS_VM_READ           (0x0010)
'#define PROCESS_VM_WRITE          (0x0020)
'#define PROCESS_DUP_HANDLE        (0x0040)
'#define PROCESS_CREATE_PROCESS    (0x0080)
'#define PROCESS_SET_QUOTA         (0x0100)
'#define PROCESS_SET_INFORMATION   (0x0200)
Global Const PROCESS_QUERY_INFORMATION = &H400&
Global Const PROCESS_ALL_ACCESS = STANDARD_RIGHTS_REQUIRED Or SYNCHRONIZE Or &HFFF&

Declare Function GetSystemMetrics Lib "user32" (ByVal nIndex As Long) As Long
Const SM_CXSCREEN = 0        ' Width of screen
Const SM_CYSCREEN = 1        ' Height of screen
Const SM_CXFULLSCREEN = 16   ' Width of window client area
Const SM_CYFULLSCREEN = 17   ' Height of window client area
Const SM_CYMENU = 15         ' Height of menu
Const SM_CYCAPTION = 4       ' Height of caption or title
Const SM_CXFRAME = 32        ' Width of window frame
Const SM_CYFRAME = 33        ' Height of window frame
Const SM_CXHSCROLL = 21      ' Width of arrow bitmap on
                             '  horizontal scroll bar
Const SM_CYHSCROLL = 3       ' Height of arrow bitmap on
                             '  horizontal scroll bar
Const SM_CXVSCROLL = 2       ' Width of arrow bitmap on
                             '  vertical scroll bar
Const SM_CYVSCROLL = 20      ' Height of arrow bitmap on
                             '  vertical scroll bar
Const SM_CXSIZE = 30         ' Width of bitmaps in title bar
Const SM_CYSIZE = 31         ' Height of bitmaps in title bar
Const SM_CXCURSOR = 13       ' Width of cursor
Const SM_CYCURSOR = 14       ' Height of cursor
Const SM_CXBORDER = 5        ' Width of window frame that cannot
                             '  be sized
Const SM_CYBORDER = 6        ' Height of window frame that cannot
                             '  be sized
Const SM_CXDOUBLECLICK = 36  ' Width of rectangle around the
                             '  location of the first click. The
                             '  second click must occur in the
                             '  same rectangular location.
Const SM_CYDOUBLECLICK = 37  ' Height of rectangle around the
                             '  location of the first click. The
                             '  second click must occur in the
                             '  same rectangular location.
Const SM_CXDLGFRAME = 7      ' Width of dialog frame window
Const SM_CYDLGFRAME = 8      ' Height of dialog frame window
Const SM_CXICON = 11         ' Width of icon
Const SM_CYICON = 12         ' Height of icon
Const SM_CXICONSPACING = 38  ' Width of rectangles the system
                             ' uses to position tiled icons
Const SM_CYICONSPACING = 39  ' Height of rectangles the system
                             ' uses to position tiled icons
Const SM_CXMIN = 28          ' Minimum width of window
Const SM_CYMIN = 29          ' Minimum height of window
Const SM_CXMINTRACK = 34     ' Minimum tracking width of window
Const SM_CYMINTRACK = 35     ' Minimum tracking height of window
Const SM_CXHTHUMB = 10       ' Width of scroll box (thumb) on
                             '  horizontal scroll bar
Const SM_CYVTHUMB = 9        ' Width of scroll box (thumb) on
                             '  vertical scroll bar
Const SM_DBCSENABLED = 42    ' Returns a non-zero if the current
                             '  Windows version uses double-byte
                             '  characters, otherwise returns
                             '  zero
Const SM_DEBUG = 22          ' Returns non-zero if the Windows
                             '  version is a debugging version
Const SM_MENUDROPALIGNMENT = 40
                             ' Alignment of pop-up menus. If zero,
                             '  left side is aligned with
                             '  corresponding left side of menu-
                             '  bar item. If non-zero, left side
                             '  is aligned with right side of
                             '  corresponding menu bar item
Const SM_MOUSEPRESENT = 19   ' Non-zero if mouse hardware is
                             '  installed
Const SM_PENWINDOWS = 41     ' Handle of Pen Windows dynamic link
                             '  library if Pen Windows is
                             '  installed
Const SM_SWAPBUTTON = 23     ' Non-zero if the left and right
                             ' mouse buttons are swapped

Declare Function GetDC Lib "user32" (ByVal hWnd As Long) As Long
Declare Function ReleaseDC Lib "user32" (ByVal hWnd As Long, ByVal hDC As Long) As Long
Declare Function GetDeviceCaps Lib "gdi32" (ByVal hDC As Long, ByVal nIndex As Long) As Long
      
Option Explicit

'This starts the program associated with a file type.
'To load a web address into the default browser pass
'in the full URL as the DocName, eg. http://becqw212.swift.com.
'I'm hoping this works for RealAudio etc. type URLs
Function ShellExec(DocName As String, Optional Action As String = "Open") As Long
Dim Scr_hDC As Long
    Scr_hDC = GetDesktopWindow()
    ShellExec = ShellExecute(Scr_hDC, Action, DocName, "", "C:\", SW_SHOWNORMAL)
End Function



Function TextFromDB(dbtxt As Variant) As String
' dbtxt should be a text field from a database. If
' a db text field contains a zero length string it is
' interpreted as a NULL which causes an error when it is
' assigned to a string. This includes when the field is
' used as a string parameter. Having dbtxt as a Variant
' is the only way to avoid the problem.
Dim rc As String
   On Error Resume Next
   If IsNull(dbtxt) Then
      rc = ""
   Else
      rc = CStr(dbtxt)
   End If
   TextFromDB = rc
End Function

Function GetTempDir() As String
Dim s As String
Dim l As Long
Dim rc As Long
   s = String$(2048, Chr$(0))
   l = Len(s)
   rc = GetTempPath(l, s)
   If rc <> 0 Then
      s = Left$(s, rc)
   End If
   GetTempDir = s
End Function



Function GetDiskSpaceFreeEx(ByVal szDrive As String) As Long
' This function will return Free disk(Available to The User) in Kb,
' The reason for returning in Kb is that it will report disks larger
' than 2.1 Gig and a that expressed in bytes exceeds VB Long boundries.
'
' The return value is multiplied by 10,000 because VB gives currency(64bit)
' variables 4 decimal places automatically, the division by 1024 occurs prior
' to the multiplication in case VB is using the ultimate destination variable
' (32bit) for intermediary storage.

   Dim curBytesAvailable As Currency
   Dim curBytesTotal As Currency
   Dim curBytesFreeTotal As Currency

   Dim curTemp As Currency
   Dim lRetValue As Long
   Dim lLogErr As Long

   On Error GoTo GetDiskSpaceFreeExError

   lRetValue = 0

   If Right$(Trim(szDrive), 1) <> "\" Then szDrive = szDrive & "\"
   curTemp = GetDiskFreeSpaceEx(szDrive, curBytesAvailable, curBytesTotal, curBytesFreeTotal)
   If curTemp = 0 Then GoTo EndOfGetDiskSpaceFreeEx   ' returns 0 on failure
   lRetValue = ((curBytesAvailable / 1024) * 10000)

   GoTo EndOfGetDiskSpaceFreeEx

GetDiskSpaceFreeExError:

   Call DebugLog(Err.Number, Err.Description, "", "GetDiskSpaceFreeEx")

'   Select Case Err.Number
'      Case 6                     'OverFlow
'         'when overflow happens then return value will not have been set
'         'and will remain at its previous value, which is initialised at start to 0
'      Case Else
'         iLogError = gwriteerrorlog(Err.Number, Err.Description, "", "GetDiskSpaceFreeEx")
'   End Select
   lRetValue = 0
   Resume EndOfGetDiskSpaceFreeEx

EndOfGetDiskSpaceFreeEx:

   GetDiskSpaceFreeEx = lRetValue

End Function



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
      Err.Raise ueEndOfStringError, "GetAnyToken", "End of string"
   End If
   
   
GetAnyTokenEnd:
   GetAnyToken = Start
   Exit Function
   
GetAnyTokenErr:
   Resume GetAnyTokenEnd
End Function

Function GetPPString(sect As String, key As String, def As String, ini As String) As String
Dim s As String
Dim l As Long
   

   s = String$(8192, Chr$(0))
   l = GetPrivateProfileString(sect, key, def, s, Len(s), ini)
   If l > 0 Then
      s = Left$(s, l)
   Else
      s = ""
   End If
   
   GetPPString = s
End Function

Sub PutPPString(sect As String, key As String, sval As String, ini As String)
Dim l As Long
   l = WritePrivateProfileString(sect, key, sval, ini)
End Sub

Function DelPPSection(sect As String, ini As String)
Dim dummys As String
Dim i As Long
   dummys = "" & Chr$(0) & Chr$(0)
   i = WritePrivateProfileSection(sect, dummys, ini)
   DelPPSection = 0
End Function


Sub RestoreFormSizeINI(frm As Form, profname As String, ini As String)

Dim X As Long
Dim fs() As Single
Dim toks() As String
ReDim fs(4)
Dim i As Long
Dim tmp As String

On Error Resume Next
   tmp = GetPPString(profname, SSFORMSIZE, "", ini)
   If tmp <> "" Then
      
      'Use current values for defaults
      fs(0) = frm.Left
      fs(1) = frm.Top
      fs(2) = frm.Width
      fs(3) = frm.Height
      X = Tokens2Array(tmp, ",", toks())
      Select Case frm.BorderStyle
      Case vbFixedSingle, vbFixedDouble, vbFixedToolWindow, vbBSNone
         If X > 2 Then X = 2
      Case Else
      End Select
      
      For i = 0 To X - 1
         fs(i) = Val(toks(i))
      Next
      frm.Move fs(0), fs(1), fs(2), fs(3)
   End If

End Sub
Sub SaveFormSizeINI(frm As Form, profname As String, ini As String)

Dim ProfStr As String
Dim X As Integer

   'Save form size for next startup
      ProfStr = Format$(frm.Left, "00000") & "," & Format$(frm.Top, "00000")
   
   'Only save the size if the form can be resized
   If frm.BorderStyle = vbSizable Or frm.BorderStyle = vbSizableToolWindow Then
      ProfStr = ProfStr & "," & Format$(frm.Width, "00000") & "," & Format$(frm.Height, "00000")
   End If
   X = WritePrivateProfileString(profname, SSFORMSIZE, ProfStr, ini)

End Sub

'22 Oct 00 Only save the setting if window is not minimized or maximized
'          to avoid the form opening up below the task bar
'12 Oct 00 This now uses the registry to save the settings.
'          Use the xxxINI version if they have to go into an .INI file
Public Sub SaveFormSize(frm As Form)
Dim ProfStr As String
Dim X As Integer
   
   If frm.WindowState = vbNormal Then
      'Save form size for next startup
      ProfStr = Format$(frm.Left, "00000") & "," & Format$(frm.Top, "00000")
      
      'Only save the size if the form can be resized
      If frm.BorderStyle = vbSizable Or frm.BorderStyle = vbSizableToolWindow Then
         ProfStr = ProfStr & "," & Format$(frm.Width, "00000") & "," & Format$(frm.Height, "00000")
      End If
      SaveSetting App.EXEName, "FormSizes", frm.Name, ProfStr
   End If
End Sub

Public Sub RestoreFormSize(frm As Form)
Dim X As Long
Dim fs() As Single
Dim toks() As String
ReDim fs(4)
Dim i As Long
Dim tmp As String

On Error Resume Next
   tmp = GetSetting(App.EXEName, "FormSizes", frm.Name, "")
   If tmp <> "" Then
      'Use current values for defaults
      fs(0) = frm.Left
      fs(1) = frm.Top
      fs(2) = frm.Width
      fs(3) = frm.Height
      
      X = Tokens2Array(tmp, ",", toks())
      Select Case frm.BorderStyle
      Case vbFixedSingle, vbFixedDouble, vbFixedToolWindow, vbBSNone
         If X > 2 Then X = 2
      Case Else
      End Select
      
      For i = 0 To X - 1
         fs(i) = Val(toks(i))
      Next
      frm.Move fs(0), fs(1), fs(2), fs(3)
   Else
      CenterForm frm
   End If

End Sub

Sub CenterForm(frm As Form)
Dim fs() As Single
Dim sh As Long
Dim sw As Long
Dim prevscale As Integer
Dim cnv As Single
ReDim fs(4)
      
      ' A little jiggerypokerey to convert Twips, used by
      ' the Move command and for the form dimensions to
      ' pixels which are actually what the screen is measured in.
      prevscale = frm.ScaleMode
      frm.ScaleMode = vbTwips
      cnv = frm.ScaleWidth
      frm.ScaleMode = vbPixels
      cnv = cnv / frm.ScaleWidth
      frm.ScaleMode = prevscale
      
      fs(0) = frm.Left
      fs(1) = frm.Top
      fs(2) = frm.Width
      fs(3) = frm.Height

      sh = (GetSystemMetrics(SM_CYFULLSCREEN) / 2) * cnv
      sw = (GetSystemMetrics(SM_CXFULLSCREEN) / 2) * cnv
         
      fs(0) = sw - (fs(2) / 2)
      fs(1) = sh - (fs(3) / 2)

      frm.Move fs(0), fs(1) ', fs(2), fs(3)
      
End Sub


Function StrRepl(ByVal body As String, orig As String, repl As String, Optional cmp As VbCompareMethod = vbBinaryCompare) As String
'04 Aug 98  Skips the whole of the replacment string
Dim o As Long
Dim curpos As Long
Dim lorig As Long
Dim lrepl As Long
   curpos = 1
   lorig = Len(orig)
   lrepl = Len(repl)
   Do
      o = InStr(curpos, body, orig, cmp)
      If o > 0 Then
         body = Left$(body, o - 1) & repl & Mid$(body, o + lorig)
         curpos = o + lrepl
      End If
   Loop While o > 0

   StrRepl = body
End Function

'Simply tries to delete a file without causing an error
Function RemoveFile(fn As String)
On Error Resume Next
   Kill fn
   
   RemoveFile = True
End Function

Sub DisplayErr(rtine As String, lcn As String, errno As Long, errstr As String, Optional quiet As Boolean = False)
Dim m As String
Dim fh As Integer
Dim lf As String
On Error Resume Next
   m = rtine
   If lcn <> "" Then
      m = m & "(" & lcn & ")"
   End If
   m = m & ": "
   If errno <> 0 Then
      m = m & "Error " & Format(errno) & ", "
   End If
   m = m & errstr
   
   If Not quiet Then
       MsgBox m
   Else
      Debug.Print m
   End If
   
   fh = FreeFile
   
   lf = App.Path & "\" & App.EXEName & ".LOG"
   'Call RemoveFile(lf)
   Open lf For Append As fh
   Print #fh, "" & Now & ": " & m
   Close #fh
   
End Sub


'17 Dec 01  added the unix time variables which provide the time as
'           the number of seconds since 1/1/1970, in decimal or upper
'           or lower case hex.
'19 Mar 00  added escape so that % characters can be included in the output string
'30 Jul 99  %date% variable now allows for offsets of more than one
'21 Jul 99  variable can come from either the collection, the inifile
'           (the Variables section) or the environment strings, in that
'           order. The inifile optional parameter was added before
'           the collection which will screw up code using the
'           the collection... but it's easier to supply an empty inifile
'           than an empty collection
'14 Jun 99  uservars parameter added. The collection key should
'           correspond to the variable name and the variable value is
'           the associated item (as a string)
Function ExpandVars(src As String, Optional inifile As String = "", Optional uservars As Collection = Nothing) As String
Dim rs As String
Dim so As Long
Dim eo As Long
Dim var As String
Dim tmps As String
Dim tmpl As Long
Dim inss As String
Dim dt As Date
Dim stripvar As Boolean
Dim userval As Variant
Dim i As Integer
Dim mondate As Date
Dim doff As Integer

   On Error Resume Next
   rs = src
   eo = 0
   so = 0
   dt = Now
   Do
NextToken:
      so = InStr(eo + 1, src, "%")
      
      'This allows for escaped % symbols
      If so > 1 Then
         If Mid$(src, so - 1, 1) = "\" Then
            tmpl = InStr(1, rs, "\%")
            rs = Left$(rs, tmpl - 1) & Mid$(rs, tmpl + 1)
            eo = so
            GoTo NextToken
         End If
      End If
      
      If so > 0 Then
         eo = InStr(so + 1, src, "%")
         If eo > 0 Then
            var = LCase$(Mid$(src, so, eo - so + 1))
            inss = ""
            stripvar = True
            
            'These can't be handled by the select statement
            If var Like "%day+*%" Or var Like "%day-*%" Then
               tmps = Mid$(var, 5, Len(var) - 5)
               tmpl = Val(tmps)
               dt = DateAdd("d", tmpl, dt)
              
            Else
               Select Case var
               Case "%date%"
                  inss = Format$(dt, "yymmdd")
               Case "%date4%"
                  inss = Format$(dt, "yyyymmdd")
               Case "%time%"
                  inss = Format$(dt, "hhnnss")
               Case "%datim%"
                  inss = Format$(dt, "yymmddhhnnss")
               Case "%ww%"
                  inss = getWeekCode(dt)
               Case "%wwmon%", "%wwtue%", "%wwwed%", "%wwthu%", "%wwfri%", "%wwsat%", "%wwsun%"
               
                  'This is tricky as it means the numeric date
                  'of the Monday of the week.
                  i = Val(DatePart("w", dt, vbSaturday, vbFirstJan1))
                  Select Case var
                  Case "%wwmon%"
                     doff = 2
                  Case "%wwtue%"
                     doff = 3
                  Case "%wwwed%"
                     doff = 4
                  Case "%wwthu%"
                     doff = 5
                  Case "%wwfri%"
                     doff = 6
                  Case "%wwsat%"
                     doff = 0
                  Case "%wwsun%"
                     doff = 1
                  End Select
                  '2 is for Monday which is day 2
                  '1 is for today
                  i = i - doff - 1
                  mondate = DateAdd("d", -i, dt)
                  inss = Format(mondate, "dd")
               Case "%doy%"   'Day of year
                  inss = getDayCode(dt)
               Case "%mmm%"
                  inss = Format$(dt, "mmm")
               Case "%ddd%"
                  inss = Format$(dt, "ddd")
               Case "%yyyy%"
                  inss = Format$(dt, "yyyy")
               Case "%yy%"
                  inss = Format$(dt, "yy")
               Case "%mm%"
                  inss = Format$(dt, "mm")
               Case "%dd%"
                  inss = Format$(dt, "dd")
               Case "%xunxl%"   'Unix time - seconds since 1/1/1970
                  inss = LCase$(Hex$(UnixTime(dt)))
               Case "%xunxu%"   'Unix time - seconds since 1/1/1970
                  inss = UCase$(Hex$(UnixTime(dt)))
               Case "%unx%"   'Unix time - seconds since 1/1/1970
                  inss = "" & UnixTime(dt)
               Case "%now%"
                  dt = Now
               Case Else
                  'We leave unknown variables
                  stripvar = False
                  tmps = Mid$(var, 2, Len(var) - 2)
                  userval = GetItemFromCollection(tmps, uservars)
                  
                  'Is the variable defined in the ini file
                  If userval = "" And inifile <> "" Then
                     'Let's see if it's in the ini file
                     userval = GetPPString("Variables", tmps, "", inifile)
                  End If
                  
                  'Is it an environment string
                  If userval = "" Then
                     userval = Environ(tmps)
                  End If
                  
                  'If we didn't find an expansion then simply use
                  'the original variable
                  If userval = "" Then
                     userval = var
                  End If
                  
                  inss = "" & userval
                  stripvar = True
                  
               End Select
            End If
            
            If stripvar Then
               tmpl = InStr(1, rs, var, vbTextCompare)
               If tmpl > 0 Then
                  rs = Left$(rs, tmpl - 1) & inss & Mid$(rs, tmpl + Len(var))
               End If
            End If
         Else
            so = 0
         End If
      End If
   Loop While so > 0
   ExpandVars = rs
End Function

'Returns the week of the year as a two digit string
Public Function getWeekCode(dt As Date) As String
   getWeekCode = Format$(DatePart("ww", dt, vbSaturday, vbFirstJan1), "00")
End Function

'returns the Day of the year as a three digit string
Public Function getDayCode(dt As Date) As String
   'getDayCode = Format$(DatePart("y", dt, vbSaturday, vbFirstJan1), "000")
   getDayCode = Format$(dt, "yyyymmddhhnn") & Format$(dt, "ddd")
End Function

Function FmtSecsToMS(secs As Long) As String
Dim sign As String
Dim tmpsec As Long
   sign = ""
   If secs < 0 Then
      sign = "-"
   End If
   tmpsec = Abs(secs)
   'The \ means do integer divide
   FmtSecsToMS = sign & Format$((tmpsec \ 60&), "00") & ":" & Format$((tmpsec Mod 60&), "00")
End Function

Function FmtSecsToHMS(ByVal s As Long) As String
Dim mn As Long
Dim hr As Long

hr = s \ 3600
mn = (s Mod 3600) \ 60
s = s Mod 60
FmtSecsToHMS = Format$(hr, "00:") & Format$(mn, "00") & ":" & Format$(s, "00")

End Function


Function GetItemFromCollection(key As String, Col As Collection) As Variant
Dim rv As Variant
   On Error Resume Next
   If Col Is Nothing Then
      rv = ""
   Else
      Err.Clear
      rv = Col.Item(key)
      If Err.Number <> 0 Then
         rv = ""
      End If
   End If
   
   GetItemFromCollection = rv
End Function

' This allows for the following formats:
'     xxxx           Number of seconds
'     hh:mm          Hours and minutes
'     hh:mm:ss       Hours minutes and seconds
'
' No check is put on the values for the hours or minutes,
' ie. hh=2000 and mm=2000 are not rejected.
Function HMStoSecs(ByVal stime As String) As String
Dim i As Long
Dim vals() As Long
   ReDim vals(3)
   i = InStr(1, stime, ":")
   If i = 0 Then
      vals(0) = Val(stime)
   Else
      ' 31/12/99 This could be more elegant but I didn't sleep much last night
      vals(2) = Val(Left$(stime, i - 1))
      stime = Mid$(stime, i + 1)
      i = InStr(1, stime, ":")
      If i = 0 Then i = Len(stime) + 1
      vals(1) = Val(Left$(stime, i - 1))
      stime = Mid$(stime, i + 1)
      
      i = InStr(1, stime, ":")
      If i = 0 Then i = Len(stime) + 1
      vals(0) = Val(Left$(stime, i - 1))
      stime = Mid$(stime, i + 1)
   End If
   
   i = vals(0) + (vals(1) * 60) + (vals(2) * 3600)
   HMStoSecs = i
End Function

'Returns the current local time offset from GMT in
'minutes. For Central Europe Summertime this is +60.
Function GetGMTOffset(Optional ondate As Date) As Long
Dim tz As TIME_ZONE_INFORMATION
Dim rc As Long
Dim offset As Long
Dim tzname As String
Dim localdate As Date
Dim localtime As SYSTEMTIME
Dim gmttime As SYSTEMTIME
Dim gmtdate As Date

   localdate = ondate
   If localdate = 0 Then localdate = Now
   
   
   
   rc = GetTimeZoneInformation(tz)
   
   'The change dates in tz are not real dates and converting them into a real
   'date is too complicated. There is no system function for converting local times
   'into gmt times (why?) so we'll have to kludge it by reversing the bias' and using
   'the convert gmt to local....
   tz.Bias = tz.Bias * -1
   tz.DaylightBias = tz.DaylightBias * -1
   tz.StandardBias = tz.StandardBias * -1
   
   rc = GetLocalTime(gmttime)
   
   localtime.wYear = Year(localdate)
   localtime.wMonth = Month(localdate)
   localtime.wDay = Day(localdate)
   localtime.wHour = Hour(localdate)
   localtime.wMinute = Minute(localdate)
   localtime.wSecond = Second(localdate)
   localtime.wMilliseconds = 0
   
   'Convert the times using the reversed timezone information
   rc = SystemTimeToTzSpecificLocalTime(tz, localtime, gmttime)
   
   'Convert the result back to a VB date
   gmtdate = DateSerial(gmttime.wYear, gmttime.wMonth, gmttime.wDay) + TimeSerial(gmttime.wHour, gmttime.wMinute, gmttime.wSecond)
   
   'Calculate the difference in minutes - this gives the value to return,
   'which is the offset from GMT for the specified date.
   offset = DateDiff("n", gmtdate, localdate)
  
   'it's not clear from the documentation but the relationship
   'appears to be
   '     UTC = local time + (bias + DaylightBias)
   'when daylight saving applies and
   '     UTC = local time + (bias + StandardBias)
   'when daylight saving does not apply. Don't know
   'how StandardBias gets set since it appears that
   'the system timezone settings use .Bias for the
   'standard time offset, and .DayLightBias for
   'the extra offset to be applied during the daylight saving
   'period.
'   offset = tz.Bias
'
'   Select Case rc
'   Case TIME_ZONE_ID_UNKNOWN
'      tzname = ""
'   Case TIME_ZONE_ID_STANDARD
'      offset = offset + tz.StandardBias
'
'      tzname = tz.wideStandardName
'   Case TIME_ZONE_ID_DAYLIGHT
'      offset = offset + tz.DaylightBias
'      tzname = tz.wideDaylightName
'   Case Else
'      offset = 0
'   End Select
'   rc = InStr(tzname, Chr$(0))
'   If rc > 0 Then
'      tzname = Left$(tzname, rc - 1)
'   End If
'
'   'Seems that the bias is opposite to that expected
'   offset = offset * -1
   GetGMTOffset = offset
End Function

'Starts WINSHUT with no errors
'To start the shutdown, use a +ve countdown and 0 pid
'To stop the shutdown use 0 scs and the previously returned pid
'Returns the pid of the started shutdown procedure.
Function BailOut(scs As Integer, pid As Long) As Long
Dim llng As Long
   On Error Resume Next
   If scs = 0 And pid <> 0 Then
      llng = OpenProcess(PROCESS_ALL_ACCESS, False, pid)
      Call TerminateProcess(llng, 0)
      llng = 0
   ElseIf scs > 0 Then
      llng = 0
      llng = CLng(Shell("Winshut " & scs))
   End If
   
   BailOut = llng
End Function

'Crude routine for extracting the contents of an xml tag. No
'attempt is made to determine a path, the first matching tag
'anywhere is used
'Crude routine for extracting the contents of an xml tag. No
'attempt is made to determine a path, the first matching tag
'anywhere is used
Function GetXMLvalue(tag As String, xml As String, Start As Long) As String
Dim s As Long
Dim e As Long
Dim sValue As String
   If Start < 1 Then
      Start = 1
   End If
   s = InStr(Start, xml, "<" & tag & ">")
   
   If s > 0 Then
      s = s + Len(tag) + 2
      e = InStr(s, xml, "</" & tag & ">")
      If e > s Then
         sValue = Mid$(xml, s, e - s)
         Start = e + 3
      End If
   End If
   GetXMLvalue = sValue
End Function

Public Function getFileList(ByVal root As String, subdirs As Boolean, Optional abort As Boolean = False) As String
Dim fn As String
Dim fullname As String
Dim filelist As String

Dim dirs As New Collection
Dim vval As Variant

   If Right$(root, 1) <> "\" And Right$(root, 1) <> "/" Then
      root = root & "\"
   End If
   fn = Dir(root & "*.*")
   filelist = ""
   Do While fn <> ""
      fullname = root & fn
      If abort Then Exit Function
      
      If filelist <> "" Then
         filelist = filelist & Chr$(0)
      End If
      filelist = filelist & fullname
      fn = Dir()
   Loop
   
   On Error Resume Next
   If subdirs Then
      fn = Dir(root & "*.*", vbDirectory)
      Do While fn <> ""
         If abort Then Exit Function
         If fn <> "." And fn <> ".." Then
            
            fn = root & fn
            If (GetAttr(fn) And vbDirectory) = vbDirectory Then
               dirs.Add fn
            End If
         End If
         fn = Dir()
      Loop
      
      For Each vval In dirs
         If abort Then Exit Function
         fn = getFileList(CStr(vval), subdirs, abort)
         If filelist <> "" And fn <> "" Then
            filelist = filelist & Chr$(0) & fn
         ElseIf filelist = "" And fn <> "" Then
            filelist = fn
         End If
      Next
      
   End If
   getFileList = filelist
End Function

Function IsVBIDE() As Boolean
On Error GoTo IsVBIDEErr
Dim rc As Boolean
rc = True
   Debug.Print 10 / 0
   rc = False
   
IsVBIDEEnd:
   IsVBIDE = rc
   Exit Function
IsVBIDEErr:
   Resume IsVBIDEEnd
End Function



Sub ConvertPIXELSToTWIPS(X As Long, Y As Long)
'*************************************************************
' PURPOSE: Converts the two pixel measurements passed as
'          arguments to twips.
' ARGUMENTS:
'    X, Y: Measurement variables in pixels. These will be
'          converted to twips and returned through the same
'          variables "by reference."
'*************************************************************
Dim hDC As Long, hWnd As Long, RetVal As Long
Dim XPIXELSPERINCH, YPIXELSPERINCH
Const LOGPIXELSX = 88
Const LOGPIXELSY = 90
Const TWIPSPERINCH = 1440

   ' Retrieve the current number of pixels per inch, which is
   ' resolution-dependent.
   hDC = GetDC(0)
   XPIXELSPERINCH = GetDeviceCaps(hDC, LOGPIXELSX)
   YPIXELSPERINCH = GetDeviceCaps(hDC, LOGPIXELSY)
   RetVal = ReleaseDC(0, hDC)

   ' Compute and return the measurements in twips.
   X = (X / XPIXELSPERINCH) * TWIPSPERINCH
   Y = (Y / YPIXELSPERINCH) * TWIPSPERINCH
End Sub

'Returns the number of seconds since 1/1/1970 which is the a Unix time.
'If no time value is specified (in hh:mm:ss format) then the current time
'of day is used
Function UnixTime(dateval As Date, Optional timestring As String = "") As Long
Dim rv As String
Dim ts As String
Dim dt As Date
Dim tm As Date
Dim l As Long
Dim dref As Date
   dref = DateSerial(1970, 1, 1)
   dt = dateval
   If timestring <> "" Then
      tm = TimeValue(timestring)
      dt = Int(dt) + tm
   End If

   l = DateDiff("s", dref, dt) ' + &HCD8D&

   UnixTime = l
End Function

Sub SaveFile(fname As String, value As String)
On Error GoTo SaveFileErr

Dim fh As Integer
Dim flen As Long

   fh = FreeFile
   Open fname For Binary As #fh
   Put #fh, , value

SaveFileEnd:
   On Error Resume Next
   Close #fh
Exit Sub

SaveFileErr:
   Call DebugLog("UTILS.SaveFile", Err.source, Err.Number, Err.Description)
   Resume SaveFileEnd
End Sub

Function LoadFile(fname As String) As String
On Error GoTo LoadFileErr

Dim fh As Integer
Dim flen As Long
Dim scont As String

   fh = FreeFile
   Open fname For Binary As #fh
   flen = LOF(fh)
   scont = String(flen, Chr$(0))
   Get #fh, , scont

LoadFileEnd:
   On Error Resume Next
   Close #fh
   LoadFile = scont
   Exit Function
LoadFileErr:
   Call DebugLog("frmCBCTransmit.LoadFile", Err.source, Err.Number, Err.Description)
   Resume LoadFileEnd
End Function
