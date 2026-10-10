# Identyfikowanie i wyszukiwanie biomów i struktur — 7.0.23-BETA

```text
/terra2 biome
/terra2 structures [radius]
/terra2 locate biome <ID> [radius]
/terra2 locate structure <ID> [radius]
```

`terra2.settings.reload` · OP · Tab

Promień: 32..8192 bloków.

Indeks jest zapisywany według UUID i seed świata w `plugins/Terra2/structure-index/`, co pięć sekund i przy normalnym wyłączeniu. Stare packi pokazują ID features etapu `structures`; szablony i stare chunki nie są odtwarzane. Struktury natywne pokazują oryginalne ID. Maksymalnie 100.000 pozycji na świat. Wyszukiwanie biomów na twoim Y, siatka 32 bloków, do 16.384 zapytań lub dwóch sekund między zapytaniami. Bez generowania chunków.

Zindeksowane punkty struktur: `[ID, XYZ]`. Limit indeksu osiągnięty: `true/false`. Tylko wygenerowane/zindeksowane pozycje; nie dowodzi bycia wewnątrz.

Znaleziono próbkę: `ID` [X, Y, Z]. Na twoim Y; siatka 32 bloków, najbliższy nie jest gwarantowany.

Brak próbki przed limitem promienia/czasu/zapytań (N próbek). Nie dowodzi to braku biomu.
