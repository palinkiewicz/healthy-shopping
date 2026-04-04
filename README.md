# HealthyShopping

HealthyShopping to lekka aplikacja na system Android, będąca alternatywą dla oficjalnej aplikacji ZdroweZakupy. Głównym celem projektu jest dostarczenie szybkiego i przejrzystego interfejsu ułatwiającego weryfikację składu produktów spożywczych bezpośrednio podczas zakupów.

Projekt powstał w 100% w oparciu o podejście vibe-coding.

## Główne funkcje

* **Skaner kodów kreskowych**: Wbudowany, błyskawicznie działający skaner wykorzystujący aparat urządzenia. Pozwala na natychmiastowe odczytanie kodu produktu i wyświetlenie jego szczegółowej analizy.
* **Wyszukiwarka produktów**: Umożliwia ręczne odnalezienie produktów po nazwie, z uwzględnieniem opóźnionego zapytania (debounce), co optymalizuje zużycie sieci. Przydatne w sytuacji, gdy kod kreskowy jest nieczytelny lub nie mamy go pod ręką.
* **Ekran ustawień**: Pozwala na dostosowanie działania aplikacji do preferencji użytkownika. Obejmuje między innymi dostosowywanie interfejsu (np. dynamiczne kolory, grupowanie składników, paski postępu wartości odżywczych).

## Źródło danych

Aplikacja opiera się na wykorzystaniu dostępnego API usługi ZdroweZakupy, odpowiedzialnego za dostarczanie rzetelnych informacji o produktach, szczegółowych list składników oraz ocen ich wpływu na zdrowie. Wszystkie dane prezentowane w aplikacji pochodzą bezpośrednio z tego źródła.

## Technologie

Aplikacja została zbudowana z wykorzystaniem nowoczesnych narzędzi i bibliotek w ekosystemie Androida, między innymi:
* Kotlin
* Jetpack Compose (Material Design 3)
* CameraX oraz ML Kit (do skanowania kodów kreskowych)
* Retrofit & Kotlin Serialization (do komunikacji z API)
* Jetpack Navigation (do obsługi ekranów oraz nawigacji z dolnym paskiem)

## Uruchomienie projektu

1. Sklonuj repozytorium na swój dysk lokalny.
2. Otwórz projekt w środowisku Android Studio.
3. Poczekaj na zakończenie synchronizacji Gradle.
4. Uruchom aplikację na emulatorze lub fizycznym urządzeniu z systemem Android.
