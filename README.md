<div align="center">
  <img src="app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.webp" alt="Ikona aplikacji HealthyShopping" width="128" />

  # HealthyShopping

  <a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/palinkiewicz/healthy-shopping">
    <img src="https://raw.githubusercontent.com/ImranR98/Obtainium/main/assets/graphics/badge_obtainium.png" alt="Get it on Obtainium" height="60" />
  </a>
</div>

HealthyShopping to lekka aplikacja na system Android, będąca alternatywą dla oficjalnej aplikacji ZdroweZakupy. Głównym celem projektu jest dostarczenie szybkiego i przejrzystego interfejsu ułatwiającego weryfikację składu produktów spożywczych bezpośrednio podczas zakupów.

## Główne funkcje

* **Skaner kodów kreskowych**: Wbudowany, błyskawicznie działający skaner wykorzystujący aparat urządzenia (CameraX + ML Kit). Pozwala na natychmiastowe odczytanie kodu produktu i wyświetlenie jego szczegółowej analizy.
* **Wyszukiwarka produktów**: Umożliwia ręczne odnalezienie produktów po nazwie, z uwzględnieniem opóźnionego zapytania (debounce), co optymalizuje zużycie sieci. Przydatne w sytuacji, gdy kod kreskowy jest nieczytelny lub nie mamy go pod ręką.
* **Porównywarka produktów**: Pozwala zestawić ze sobą wybrane produkty i porównać ich skład oraz wartości odżywcze.
* **Historia przeglądania**: Szybki dostęp do ostatnio przeglądanych produktów.
* **Ekran ustawień**: Pozwala na dostosowanie działania aplikacji do preferencji użytkownika — motywy kolorystyczne (w tym tryb ciemny i czysta czerń), grupowanie składników, kolejność sekcji na ekranie szczegółów i inne.

## Źródło danych

Aplikacja opiera się na wykorzystaniu publicznego API usługi [ZdroweZakupy](https://zdrowezakupy.org) (`https://api.zdrowezakupy.org/`), odpowiedzialnego za dostarczanie rzetelnych informacji o produktach, szczegółowych list składników oraz ocen ich wpływu na zdrowie. Wszystkie dane prezentowane w aplikacji pochodzą bezpośrednio z tego źródła.

## Technologie

Aplikacja została zbudowana z wykorzystaniem nowoczesnych narzędzi i bibliotek w ekosystemie Androida, między innymi:
* Kotlin
* Jetpack Compose (Material Design 3)
* CameraX oraz ML Kit (do skanowania kodów kreskowych)
* Retrofit & Kotlin Serialization (do komunikacji z API)
* Jetpack Navigation (do obsługi ekranów oraz nawigacji z dolnym paskiem)

Architektura aplikacji opiera się na wzorcu MVVM (ViewModel + `StateFlow`), a jedyną warstwą trwałości danych jest `SharedPreferences` (ustawienia, historia, lista porównań).

## Uruchomienie projektu

### Wymagania

* JDK 17 (lub nowszy) — projekt może również korzystać z JDK dostarczanego wraz z Android Studio
* Android SDK
* Urządzenie lub emulator z systemem Android

### Budowanie i instalacja

```bash
git clone https://github.com/palinkiewicz/healthy-shopping.git
cd healthy-shopping

./gradlew assembleDebug      # Budowanie wersji debug (APK)
./gradlew installDebug       # Budowanie i instalacja na podłączonym urządzeniu/emulatorze
```

### Testy i analiza statyczna

```bash
./gradlew test                     # Testy jednostkowe (JVM)
./gradlew connectedAndroidTest     # Testy instrumentowane (wymagane urządzenie/emulator)
./gradlew lint                     # Analiza statyczna (Android Lint)
```

> [!NOTE]
> Plik `gradle.properties` wskazuje ścieżkę JDK poprzez `org.gradle.java.home=/opt/android-studio/jbr`. Jeżeli na Twoim systemie JDK znajduje się w innej lokalizacji, dostosuj tę właściwość lub usuń ją, aby użyć domyślnego JDK.

Wersje zależności zarządzane są centralnie w katalogu wersji (`gradle/libs.versions.toml`).
