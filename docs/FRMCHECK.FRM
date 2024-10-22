VERSION 5.00
Object = "{00028C4A-0000-0000-0000-000000000046}#5.0#0"; "TDBG5.OCX"
Begin VB.Form FrmChecked 
   BorderStyle     =   1  'Fixed Single
   Caption         =   "Checked Entries"
   ClientHeight    =   5475
   ClientLeft      =   7980
   ClientTop       =   4380
   ClientWidth     =   8325
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
   Icon            =   "FRMCHECK.frx":0000
   LinkTopic       =   "Form1"
   MaxButton       =   0   'False
   MinButton       =   0   'False
   PaletteMode     =   1  'UseZOrder
   ScaleHeight     =   5475
   ScaleWidth      =   8325
   Begin TrueDBGrid50.TDBGrid tgcheckent 
      Bindings        =   "FRMCHECK.frx":030A
      Height          =   4815
      Left            =   120
      OleObjectBlob   =   "FRMCHECK.frx":0324
      TabIndex        =   1
      Top             =   120
      Width           =   8055
   End
   Begin VB.CommandButton cmdClose 
      Appearance      =   0  'Flat
      Cancel          =   -1  'True
      Caption         =   "Close"
      Height          =   315
      Left            =   6720
      TabIndex        =   0
      Top             =   5040
      Width           =   1215
   End
   Begin VB.Data DataChecked 
      Appearance      =   0  'Flat
      Caption         =   "CheckedEntries"
      Connect         =   "Access 2000;"
      DatabaseName    =   ""
      DefaultCursorType=   0  'DefaultCursor
      DefaultType     =   2  'UseODBC
      Exclusive       =   0   'False
      Height          =   372
      Left            =   1080
      Options         =   0
      ReadOnly        =   0   'False
      RecordsetType   =   1  'Dynaset
      RecordSource    =   ""
      Top             =   5040
      Visible         =   0   'False
      Width           =   1140
   End
   Begin VB.Label lblMsg 
      AutoSize        =   -1  'True
      Caption         =   "Calculating checked balance..."
      Height          =   195
      Left            =   2760
      TabIndex        =   2
      Top             =   2280
      Width           =   2670
   End
End
Attribute VB_Name = "FrmChecked"
Attribute VB_GlobalNameSpace = False
Attribute VB_Creatable = False
Attribute VB_PredeclaredId = True
Attribute VB_Exposed = False
Const CREDCOL = 4
Const DEBCOL = 3
Const BALCOL = 5

Const PROFSECT = "CHECKEDENT"
Const MODNAME = "FRMCHECK.FRM"

Private Sub cmdClose_Click()
Unload Me
End Sub

Private Sub Form_Activate()
Dim bal As Currency

On Error GoTo FrmcheckedActivateErr
Dim cl As Recordset
'Calculate the balances for the selected entries
bal = 0
Me.MousePointer = vbHourglass
DoEvents


   Set cl = DataChecked.Recordset.Clone()
   If cl.RecordCount > 0 Then
      cl.MoveFirst
      Do While Not cl.EOF
          If Not IsNull(cl!credit) Then
            bal = bal + cl!credit
          End If
          If Not IsNull(cl!debit) Then
            bal = bal - cl!debit
          End If
          'Debug.Print cl!Credit, cl!Debit, bal
          cl.Edit
          cl!CheckedBalance = bal
          cl.Update
          cl.MoveNext
      Loop
      tgcheckent.Visible = True
      tgcheckent.Refresh
      DataChecked.Recordset.MoveLast
   Else
      lblMsg.Caption = "No checked entries found"
   End If
   
FrmcheckedActivateEnd:
   On Error Resume Next

   Me.MousePointer = vbNormal
   Exit Sub
   
FrmcheckedActivateErr:
   Call DisplayErr("FrmCheckedActivate", "", Err.Number, Err.Description)
   Resume FrmcheckedActivateEnd
   Resume
End Sub

Private Sub Form_Load()
gFuncName = MODNAME & "." & "Form_Load "


On Error Resume Next
Call RestoreTGCols(tgcheckent, PROFSECT, gFullIniName)
'Set the currency column format
SetCurColFmt tgcheckent, DataChecked.Recordset, gCurrAcc.AccFmt

tgcheckent.Visible = False
DataChecked.DatabaseName = FrmAccount.DataAccount.DatabaseName
DataChecked.RecordSource = gTransCrit & " AND (transaction.checked=-1) order by sequence asc"
DataChecked.Refresh


End Sub

Private Sub Form_Unload(Cancel As Integer)
gFuncName = MODNAME & "." & "Form_Unload "
   Call SaveTGCols(tgcheckent, PROFSECT, gFullIniName)
End Sub

