Attribute VB_Name = "module_reload"
Option Explicit
' 2025-10-22 15:56
Const ModuleStatement As String = "statement_load"
Const ModuleReconcile As String = "mysql_reconcile"

' Updates (and Adds) the module files into the workbook
Sub UpdModulesInWorkbook(moduleArray As Variant, modulepath As String, wb As Workbook)
Dim modname As String
Dim modpath As String
Dim i As Integer

   With wb
      For i = 0 To UBound(moduleArray)
         modname = moduleArray(i)
         modpath = modulepath & "\vba_" & modname & ".bas"
         
         'Must remove the module if it exists - ignore the error if it doesn't exist
         On Error Resume Next
         .VBProject.VBComponents.Remove .VBProject.VBComponents(modname)
         On Error GoTo 0
         'import the new module, save the workbook.
         .VBProject.VBComponents.Import modpath
         .Save
      Next
   End With
End Sub

' Adds/Replaces the 'Account' modules in all workbooks in a given path
' from the path given in the sheet running this macro, ie. only the
' workbook performing the update requires the module source path
Sub MultiFileReplaceVBAModule()
Dim mods As Variant
Dim workbookPath As String
Dim srcpath As String
Dim fname As String
Dim fpath As String
Dim curwbid As String
Dim wbTarget As Workbook
Dim actwb As Workbook
Dim rownum As Integer
Dim column As Integer

   Set actwb = ActiveWorkbook

   srcpath = Range("Settings!modulepath")
   curwbid = UCase(actwb.FullName)  ' this is the full pathname of the current workbook file
   
   mods = getModulesArray()
   
   workbookPath = Range("Settings!workbookpath") & "\"

   rownum = Range("Settings!workbookpath").row + 1 ' Selection.Row + 1
   column = Range("Settings!workbookpath").column

   With actwb.Worksheets("Settings")
      Do While .Cells(rownum, column).Text <> ""
         workbookPath = .Cells(rownum, column).Text & "\"

         ' Search for excel files that we want to update the modules in
         fname = Dir(workbookPath & "*.xlsm")
         Do While fname <> ""
            fpath = workbookPath & fname
            If UCase(fpath) <> curwbid Then
               Debug.Print "Updating modules in " & fpath
               Set wbTarget = Workbooks.Open(fpath)
               UpdModulesInWorkbook mods, srcpath, wbTarget
               wbTarget.Close savechanges:=True
            Else
               Debug.Print "Updating modules in this workbook"
               UpdModulesInWorkbook mods, srcpath, actwb
            End If
            fname = Dir
         Loop

         rownum = rownum + 1
      Loop
   End With

End Sub

Function getModulesArray() As Variant
Dim mods As Variant
   mods = Array(ModuleStatement, ModuleReconcile)
   getModulesArray = mods
End Function

Sub UpdModsInActivebook()
Dim srcpath As String
   srcpath = Range("Settings!modulepath")
   UpdModulesInWorkbook getModulesArray(), ActiveWorkbook
End Sub

Sub ExportModulesInActivebook()
Dim srcpath As String
Dim modpath As String
Dim wb As Workbook
Dim i As Integer
   srcpath = Range("Settings!modulepath")

   Set wb = ActiveWorkbook
   
   With wb.VBProject.VBComponents
      For i = 1 To .Count
         modpath = srcpath & "\vba_" & .Item(i).Name & ".bas"
         
         ' The type names require library 'Microsoft Visual Basic for Applications Extensibility'
         ' vbext_ct_StdModule   = 1
         ' vbext_ct_ClassModule = 2
         If .Item(i).Type = vbext_ct_StdModule Or .Item(i).Type = vbext_ct_ClassModule Then
            Debug.Print "Saving " & .Item(i).Name & " type: " & .Item(i).Type & " to " & modpath
            .Item(i).Export modpath
         End If
      Next
   End With
End Sub

