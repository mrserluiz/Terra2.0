# Polecenia światów i tłumaczenia

Ten rozdział dotyczy 7.0.22-BETA; wcześniejsze zachowują wskazaną wersję odniesienia.

```text
/terra2 lis Cpack
/terra2 lis Tpack
/terra2 lis all
/terra2 unlock terra2_teste OVERWORLD
/terra2 unlock terra2_mix OVERWORLD;Debris
```

Pakiety dla świata terra2_teste są sprawdzane w tle. Żaden świat nie jest tworzony ani zmieniany.

Zarejestrowane Community Packs: OVERWORLD, TARTARUS, HYDRAXIA

Świat terra2_teste dopuszczony do użycia OVERWORLD. Ustawienia zapisane; generowanie włączone dla dozwolonych światów.

Żaden świat nie został utworzony ani wygenerowany ponownie. Dla nowego świata: /mv create terra2_teste normal -g Terra2:PACKS

Świat world jest chroniony. To polecenie nie usuwa ochrony. Sprawdź YAML ręcznie; główne światy vanilla pozostają zablokowane przez silnik.

Świat terra2_teste jest załadowany. Nie można zastąpić jego generatora podczas pracy; najpierw bezpiecznie wyładuj świat.

Edycja YAML nie omija ochrony silnika dla głównych światów vanilla. Tylko pierwszy pakiet może stanowić bazę terenu Community. Zapisane uprawnienia nie nadpisują istniejących chunków i nie zastępują już powiązanego planu generowania.

## Autouzupełnianie, uprawnienia i bezpieczeństwo

`Tab` · `terra2.settings.reload` · `OP`

`world` · `nether` · `end` · `world_nether` · `world_the_end` · `generation.protected-worlds`

Wybierz język serwera w `plugins/Terra2/terra2-settings.yml`. Polecenia i identyfikatory pakietów nie zmieniają się. Istniejąca szczegółowa diagnostyka zachowuje oryginalny język.

```yaml
language: pl_PL
```

`en_US` · `es_ES` · `id_ID` · `it_IT` · `fr_FR` · `de_DE` · `pt_BR` · `ru_RU` · `pl_PL` · `vi_VN` · `tr_TR` · `zh_CN` · `ja_JP`

Wykryto Community Packs; uruchom serwer ponownie, aby je zarejestrować: HYDRAXIA

[← README](README.md)
