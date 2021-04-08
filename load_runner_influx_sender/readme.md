# Общее описание

Для корректной работы необходимо внести изменения в уже созданный скрипт:

- Скачать "influx.h" и разместить в корне готового скрипта.
- Добавить функцию influx_defineIntegration() в начало vuser_init().
- Добавить "#include "influx.h"" в самый конец блока "Include Files" внутри "globals.h".
- Для группы "extra files" выполнить ПКМ, выбрать "Add files to script" и добавить "influx.h".
- Внутри файла "influx.h" скорректировать значения параметров: 
  - INFLUX_HOST "http://[host]:[port]", пример: #define INFLUX_HOST "http://172.30.48.58:8510"
  - INFLUX_DB_NAME "[имя базы данных инфлюкс]", пример: #define INFLUX_DB_NAME "resultsdb"
  - INFLUX_MEASUREMENT_NAME "[имя measurement influx]", пример: #define INFLUX_MEASUREMENT_NAME "jmeter"
  - NODE_NAME_DEFAULT "[имя машины, с которой подается нагрузка]", пример: #define NODE_NAME_DEFAULT "default"
  - APPLICATION_NAME "[имя приложения, из которого подается нагрузка]", пример: #define APPLICATION_NAME "loadRunner"

-> Теперь можно использовать скрипт со стандартными функциями lr_start_transaction() и lr_end_transaction() - при каждом вызове lr_end_transaction() внутри каждого потока будет осуществляться отправка запроса в Influx со всеми параметрами. Это может создавать серьезную нагрузку на сеть при подаче высокоинтенсивной нагрузки, поэтому рекомендуется использовать утилиту с осторожностью.

> Важно: утилита работает только при использовании протокола web/http.

---
Утилита: influx.h