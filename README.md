# Fake Scoreboard (Fabric, Minecraft 1.21.11)

Klientowy mod, ktory **lokalnie** podmienia wartosci na scoreboardzie (Kasa, Shardy, Czas gry itd.).
Zmiany widzisz tylko Ty - serwer, inni gracze i prawdziwe statystyki sie nie zmieniaja.

## Uzycie
- `Right Shift` (zmienisz w Sterowaniu) albo komenda `/fakesb` otwiera GUI.
- Przelacznik ON/OFF przy kazdej linii + pole z wartoscia (np. `$999K`).
- Kolor akcentu: suwaki RGB, gotowe kolory, tryb teczowy. Animacje mozna wylaczyc.
- Config: `config/fakescoreboard.json`.

## Build
Potrzebujesz JDK 21.
```
gradle wrapper --gradle-version 9.2.1   # jednorazowo
./gradlew build
```
Jar: `build/libs/`. Na GitHubie buduje sie sam (Actions) - jar w zakladce Artifacts.

## GitHub
```
git init && git add . && git commit -m "Initial commit"
git branch -M main
git remote add origin https://github.com/TWOJ_NICK/fake-scoreboard.git
git push -u origin main
```

## Jesli cos sie nie buduje
Wersje (Yarn/Loader/Fabric API) sprawdz na https://fabricmc.net/develop/ i wpisz w `gradle.properties`.
Linie scoreboardu przechwytuja `TeamMixin` i `AbstractTeamMixin` (metody `decorateName`).
