Option Explicit
'Constants associated with the VSView printer control
Global Const VSLALIGN = 0         'Alignment types
Global Const VSCALIGN = 1
Global Const VSRALIGN = 2
Global Const VSACTSTARTDOC = 3    'Action codes
Global Const VSACTNEWPAGE = 4
Global Const VSACTENDDOC = 6
Global Const VSABORTDOC = 7
Global Const VSACTPRNPAG = 8
Global Const VSACTPRNALL = 11

Global Const INIFILE = "ACCHECK.INI"

Global gCheckType As Integer  '0=Barclays, 1=EuroCheck, 2=Bank of New York

Function mmtotwip (mm As Integer) As Long
   '1440 twips per inch
   '1440 / 25.4 per mm =

   mmtotwip = CLng((1440! / 25.4!) * CSng(mm))
End Function

