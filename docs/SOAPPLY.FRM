VERSION 5.00
Begin VB.Form frmSOApply 
   Caption         =   "Standing Orders"
   ClientHeight    =   1395
   ClientLeft      =   1095
   ClientTop       =   1380
   ClientWidth     =   7230
   ControlBox      =   0   'False
   BeginProperty Font 
      Name            =   "MS Sans Serif"
      Size            =   8.25
      Charset         =   0
      Weight          =   700
      Underline       =   0   'False
      Italic          =   0   'False
      Strikethrough   =   0   'False
   EndProperty
   ForeColor       =   &H80000008&
   Icon            =   "SOAPPLY.frx":0000
   LinkTopic       =   "Form1"
   MaxButton       =   0   'False
   MinButton       =   0   'False
   PaletteMode     =   1  'UseZOrder
   ScaleHeight     =   1395
   ScaleWidth      =   7230
   Begin VB.CommandButton cmdDoit 
      Appearance      =   0  'Flat
      Caption         =   "DoIt"
      Height          =   195
      Left            =   360
      TabIndex        =   3
      Top             =   960
      Visible         =   0   'False
      Width           =   975
   End
   Begin VB.CommandButton cmdCancel 
      Appearance      =   0  'Flat
      Cancel          =   -1  'True
      Caption         =   "Cancel"
      Height          =   255
      Left            =   5160
      TabIndex        =   2
      Top             =   960
      Width           =   1935
   End
   Begin VB.TextBox txtSO 
      Height          =   375
      Left            =   120
      TabIndex        =   1
      Top             =   480
      Width           =   6975
   End
   Begin VB.Label Label1 
      Caption         =   "Applying Standing Order:"
      Height          =   255
      Left            =   120
      TabIndex        =   0
      Top             =   120
      Width           =   5415
   End
End
Attribute VB_Name = "frmSOApply"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = True
Attribute VB_Exposed = False
Option Explicit

Dim mAbort As Integer
Dim mInProgress As Integer

Private Sub ApplyStandingOrders()
'06 Aug 00  Orders the transactions which suddenly seems to be necessary
'30 Oct 99  Allows for -ve amounts (which mean credits!!)
'20 Mar 96  Orders the standing orders by Pay Date
'17 Jul 96  Changed the format for the On: date.
Dim dsSO As Recordset
Dim dsTrans As Recordset
Dim paydate As Variant
Dim entdate As Variant
Dim amt As Currency
Dim bal As Currency
Dim s As String
Dim InTrans As Integer
Dim period As String

   On Error GoTo ApplySoErr

   Set dsSO = gDB.OpenRecordset("SELECT * FROM StandingOrders WHERE SOEntryDate < " & Str$(CDbl(Now)) & " ORDER BY SONextPayDate ASC")
   If dsSO.RecordCount < 1 Then
      GoTo ApplySOEnd
   Else
      Me.Show
   End If

   'N.B. This doesn't add the SOs in a very natural way
   'when more than one payment is outstanding as it will
   'enter all payments for one standing order before
   'moving to the next. This might need to be modified.
   
   dsSO.MoveFirst
   Do While Not dsSO.EOF
      Set dsTrans = gDB.OpenRecordset("SELECT * FROM transaction WHERE AccountId = " & (dsSO!SOACCID) & " order by sequence asc")
      bal = 0
      If dsTrans.RecordCount > 0 Then
         dsTrans.MoveLast
         bal = CurrFromDB((dsTrans!Balance))
      End If
      Select Case dsSO!SOPeriod
      Case "M"
         period = "m"
      Case "W"
         period = "ww"
      Case "D"
         period = "d"
      Case Else
         period = "m"
      End Select
      
      Do While (dsSO!SOEntryDate) < Now
         amt = CurrFromDB((dsSO!SOAmount))
         bal = bal - amt
         s = ExpandSOComment(TextFromDB((dsSO!SODESC)), (dsSO!SONextPayDate))
         If s <> "" Then
            s = s & " "
         End If
         s = s & "On:" & Format$(Now, "dd mmm yy")
         paydate = dsSO!SONextPayDate
         entdate = dsSO!SOEntryDate
         txtSO = s & " " & paydate
         
         BeginTrans
         InTrans = True
         dsTrans.AddNew
         dsTrans!AccountId = dsSO!SOACCID
         dsTrans!Date = paydate
         dsTrans!Type = "SO"
         
         'Make sure the amount goes into the correct column
         If amt > 0 Then
            dsTrans!debit = amt
         Else
            dsTrans!credit = amt * (-1)
         End If
         dsTrans!Balance = bal
         dsTrans!Comment = s
         dsTrans!Type = TextFromDB((dsSO!SOTFRTYPE))
         dsTrans.Update
         
         paydate = DateAdd(period, (dsSO!SOCOUNT), paydate)
         entdate = DateAdd(period, (dsSO!SOCOUNT), entdate)
         dsSO.Edit
         dsSO!SONextPayDate = paydate
         dsSO!SOEntryDate = entdate
         dsSO.Update
         CommitTrans
         InTrans = False
         dsSO.bookmark = dsSO.LastModified
         DoEvents
         If mAbort Then GoTo ApplySOEnd
      Loop  'See if there is another outstanding payment on this order
      dsSO.MoveNext
   Loop 'Process next order

ApplySOEnd:
   On Error Resume Next
   dsSO.Close
   dsTrans.Close
   Exit Sub

ApplySoErr:
   If Err <> 3078 Then  'Cant find table... no standing orders
      Call DisplayErr("ApplyStandingOrders", "", Err, Error$)
   End If
   If InTrans Then Rollback
   Resume ApplySOEnd
   Resume
End Sub

Private Sub cmdCancel_Click()
   mAbort = True
End Sub

Private Sub cmdDoIt_Click()
   If Not mInProgress Then
      mInProgress = True
      Call ApplyStandingOrders
      Unload Me
   End If

End Sub

Private Function ExpandSOComment(com As String, pdate As Variant)
'05 May 96  Takes the date as a parameter so that the
'  comment can refer to the pay date rather than the
'  entry date
Dim i As Integer
Dim e As Integer
Dim srtn As String
Dim sl As String
Dim sr As String
   On Error Resume Next
   srtn = com
   
   'This handles mm, dd, yy, yyyy, short date,
   'general date etc. It should enable #mmm yy# also
   Do
      i = InStr(srtn, "#")
      If i = 0 Then
         Exit Do
      End If
      e = InStr(i + 1, srtn, "#")
      If e = 0 Then
         Exit Do
      End If

      sl = ""
      sr = ""

      If i > 1 Then
         sl = Left$(srtn, i - 1)
      End If
      If e < Len(srtn) Then
         sr = Mid$(srtn, e + 1)
      End If
      
      Err = 0
      srtn = sl & Format$(pdate, Mid$(srtn, i + 1, e - i - 1)) & sr
      If Err <> 0 Then Exit Do
   Loop

   ExpandSOComment = srtn
End Function

Private Sub Form_Activate()
   If Not mInProgress Then
      mInProgress = True
      Call ApplyStandingOrders
      Unload Me
   End If
End Sub

Private Sub Form_Unload(Cancel As Integer)
   mInProgress = False
End Sub

