# Data Acquisitor
Modu³ akwizytora danych pobieranych z urz¹dzeñ pomiarowych. Akwizytor stanowi warstwê poœrednicz¹c¹ miêdzy urz¹dzeniami pomiarowymi a zewnêtrzn¹ baz¹ danych.

## Instalacja
Upewnij siê ¿e w folderze dll znajduj¹ siê pliki dll (rxtxSerial,rxtxParallel) wersji odpowiedniej dla zainstalowanej na maszynie wersji JAVA (32 lub 64 bit).
W œrodowisku upewnij siê ¿e œcie¿ka do folderu zostanie przekazana jako parametr przy próbie odpalenia programu.
Przyk³adowo:
>-Djava.library.path="G:\Projects\studia\SemProj\MeteringComReader\dll"

W przypadku odpalania aplikacji z paczki .jar pliki dll odpowiedniej wersji powinny znajdowaæ siê w tym samym folderze co plik .jar.