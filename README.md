# Data Acquisitor
Modu� akwizytora danych pobieranych z urz�dze� pomiarowych. Akwizytor stanowi warstw� po�rednicz�c� mi�dzy urz�dzeniami pomiarowymi a zewn�trzn� baz� danych.

## Instalacja
Upewnij si� �e w folderze dll znajduj� si� pliki dll (rxtxSerial,rxtxParallel) wersji odpowiedniej dla zainstalowanej na maszynie wersji JAVA (32 lub 64 bit).
W �rodowisku upewnij si� �e �cie�ka do folderu zostanie przekazana jako parametr przy pr�bie odpalenia programu.
Przyk�adowo:
>-Djava.library.path="G:\Projects\studia\SemProj\MeteringComReader\dll"

W przypadku odpalania aplikacji z paczki .jar pliki dll odpowiedniej wersji powinny znajdowa� si� w tym samym folderze co plik .jar.