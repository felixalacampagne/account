Attribute VB_Name = "Module1"
Option Explicit

'String used to transfer data to/from the AccForEx form.
Global gForExComm As String 'This contains the Comment, formatted by/for AccForEx
Global gForExDeb As String  'This contains the local currency value for the transaction

Global Const CURCHARS = "0123456789."

Sub ExplodeComm(expl As String, rcp As String, cur As String, rat As String, amt As String, cms As String)
'Takes a Forex Comment string and explodes it into it's components
'The format should be
'Recipient[CurAmt @ rat+cms:cvrt]
'which assumes that the currency will not contain
'any digits...
'22 Jan 22 commission and converted amount are now shown separately on the statement
'  so the forex comment now optionally includes the converted amount without commission
'20 Mar 96 Format changed to make it more compatible
'  with standing orders which add text after the fixed
'  comment. The format is now
'  Recipient1[CurAmt @ Rat]Recipient2
'  The rcp variable will contain the [] place holder for the
'  rate info. If [..] is not found then the rate info will
'  be added at the end. The implode routine will replace
'  [] with the full rate string.
'08 Oct 96 Added commision, delimited by an optional +
'  sign after the rate string
Dim i As Integer
Dim j As Integer
Dim tmpstr As String
Dim comis As Integer

   On Error GoTo ExplodeCommErr
   rcp = "[]"
   cur = ""
   rat = "1"
   amt = "0.00"

   'Check we've got a valid string
   i = InStr(expl, "]")
   If i = 0 Then
      rcp = expl & "[]"
      Exit Sub
   End If
   
   i = InStr(expl, "[")
   If i = 0 Then
      rcp = expl & "[]"
      Exit Sub
   End If
   rcp = Left$(expl, i)

   'Skip the [
   i = i + 1
   j = i
   Do While InStr(CURCHARS, Mid$(expl, i, 1)) = 0
      i = i + 1
   Loop

   cur = Trim$(Mid$(expl, j, i - j))

   j = i
   i = InStr(Mid$(expl, j), "@")
   amt = Trim$(Mid$(expl, j, i - 1))
   
   'Extract the rate
   tmpstr = Trim$(Mid$(expl, i + j + 1))
      
   'We might have commission
   i = InStr(tmpstr, "+")
   If i = 0 Then
      i = InStr(tmpstr, "]")
      comis = 0
   Else
      comis = i
   End If
   rat = Trim$(Left$(tmpstr, i - 1))
   
   'Get the commission if there was any.
   If comis > 0 Then
      tmpstr = Mid$(tmpstr, i + 1)
      i = InStr(tmpstr, ":")
      If i = 0 Then
         i = InStr(tmpstr, "]")
      End If
      cms = Trim$(Left$(tmpstr, i - 1))
      
      ' Skip the converted value for the 'recipient' text
      i = InStr(tmpstr, "]")
   End If

   'Append the rest of the comment, including
   'the ]
   rcp = rcp & Mid$(tmpstr, i)

ExplodeCommEnd:
   Exit Sub

ExplodeCommErr:
   'Rely on the defaults
   Resume ExplodeCommEnd
   Resume
End Sub

Sub ImplodeComm(impl As String, rcp As String, cur As String, rat As String, amt As String, com As String)
'see ExplodeComm for the format of the impl string
'20 Mar 96  Format changed
'08 Oct 96  Commission added
Dim s As String
Dim i As Integer
Dim j As Integer

   'Check for the rate place holder, add it if
   'necessary
   impl = rcp
   i = InStr(impl, "[")
   If i = 0 Then
      impl = impl & "[]"
      i = Len(impl) - 1
   End If
   j = InStr(impl, "]")

   
   s = cur
   If s = "" Then
      s = "UNK"
   End If
   
   If amt = "" Then
      s = s & "0"
   Else
      s = s & amt
   End If
   s = s & " @ "

   If rat = "" Then
      s = s & "1"
   Else
      s = s & rat
   End If
   
   If com <> "" Then
      s = s & "+" & com
   End If
   
   impl = Left$(impl, i) & s & Mid$(impl, j)

End Sub

