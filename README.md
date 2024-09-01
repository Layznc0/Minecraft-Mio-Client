PunisherX to wszechstronny system kar, który oferuje ostrzeżenia, wyciszenia, bany, ipbany (w tym tymczasowe) oraz kicki.

Dzięki funkcji Check możesz szybko sprawdzić aktualne kary nałożone na gracza. Wtyczka zawiera konfigurowalne szablony powodów kar, co ułatwia ich szybkie wymierzanie.

PunisherX oferuje pełny plik wiadomości, który umożliwia modyfikację i tłumaczenie wszystkich komunikatów z pełnym wsparciem dla Minimessage. Dodatkowo, wtyczka posiada szczegółowy plik konfiguracyjny z wieloma przydatnymi ustawieniami.

Wtyczka jest zoptymalizowana dla serwerów Paper i ich rozwidleń, takich jak Pufferfish czy Purpur. Obsługuje zarówno MySQL, jak i lokalną bazę danych SQLite, co zapewnia wysoką wydajność.

    TODO:
    * Dodać uprawnienia bypass dla poszczególnych komend
    * Dodać pominięcie sprawdzania uprawnień punisherx.check jeśli osobą sprawdzaną jest ta sama, którą użyła komendy
    * Sprawdzić działanie pernamentnych wersji wszystkich komend
    * Dodać komendę kick z obsługą wiadomości w pliku językowym
    * Dodać BanList dla komend ban i banip jako awaryjna metoda w przypadku problemu z łącznością z bazą danych
    * Dodać możliwość użycia wyłącznie BanList zamiast obsługi baz danych do ustawienia w config.yml
    * Sprawdzić wersję eksperymentalną pomysłu na dynamiczne przełączanie między bazą MySQL a SQLite w sytuacji problemu z łącznością.
    * Dodać możliwość exportu i importu dla poszczególnych baz oraz migracji przędzy nimi