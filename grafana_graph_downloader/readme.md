__TODO:__
1. f
2. f
3. f
4. f

---
__Общее описание работы__

Архив 'get_grafana_graph.zip' распаковать в любую директорию, туда же будут выгружаться графики по умолчанию.

Содержимое:

1) lib - питоновские библиотеки, необходимые для запуска, менять не рекомендуется;
2) python37.dll - вспомогательный файл запуска python кода через .exe;
3) grafana_get_graph.exe - основной файл запуска выгрузки;
4) config.ini - настраиваемый конфиг, в нем также описана основная часть логики запуска;
5) script_values.csv - пример вспомогательного файла, также относится к конфигу.

> Для корректной работы скрипта выгрузки прежде всего необходимо убедиться, что в используемой версии grafana имеется [рендер изображений](https://grafana.com/grafana/plugins/grafana-image-renderer/) и что он правильно работает: любой график дашборда → "share" → "Direct link rendered image" → в ответ на http запрос вернется png изображение. Если в браузере отобразился график со всеми значениями за выбранный период (осторожно, может быть смещение по времени на несколько часов в зависимости от настроек Grafana или сервера, на котором она запущена) - то все отлично.

Перед началом работы нужен ключ для API авторизации в grafana или Bearer Key. Для этого необходим админский доступ к grafana (либо можно попросить ключ на чтение у того, кто доступ имеет): configuration → API keys → add API key, [как здесь](https://github.com/D4nD4nce/QA_LoadTesting_Info/blob/main/grafana_graph_downloader/examples/description_1.PNG). Имя ключа - любое, роль - достаточно viewer, время действия - бессрочно. После заполнения и активации будет показано окно, в котором первый и последний раз будет отображен сам ключ авторизации. Его необходимо сохранить, он понадобится для настройки средства выгрузки.
---
*Настройка конфиг файла (config.ini)*

Общие параметры (независимые).

1. img_width, img_height - размеры выгружаемого изображения.
1. img_directory - в эту папку будут выгружены все полученные изображения. Если не существует - создается в той же папке, что и скрипт (на одном уровне с конфигом).

Основные параметры.
> Для лучшего понимания следует активировать рендер (любой график дашборда → "share" → "Direct link rendered image") и проанализировать ссылку, получаемую в адресной строке браузера. Получится [нечто вроде этого](https://github.com/D4nD4nce/QA_LoadTesting_Info/blob/main/grafana_graph_downloader/examples/description_2.png).

1. grafana_url - адрес grafana (Пример: http://localhost:3000/)
1. bearer_key - заранее сгенереный API-ключ авторизации (Пример: `eyJrIjoiaDNjeFR0TVV1andjdUJjTGh4MjR1azJhZTRTQXBmZDgiLCJuIjoiZ2V0X2dyYXBoX3V0aWwiLCJpZCI6MX0=`), схема получения - выше.
1. dash_id - ID дашборда, из которого запланирована выгрузка графиков. Легко получить из адресной строки при открытии страницы дашборда или любого из графиков. Либо перейти: dashboard settings → JSON model → перемотать в самый низ → найти параметр uid (Пример: u5BfOi0Mz)
1. org_id - organization ID, также должен быть получен из ссылки адресной строки на странице дашборда или графика. Цифровое значение. На маленьких проектах с единственным дашбордом часто равен единице.
1. graphids - ID графиков, изображения которых необходимо выгрузить с дашборда. Цифровое значение (необязательное). Для загрузки из нескольких графиков значения данного параметра в конфиг файле должны быть указаны как отдельные цифры через пробел. Можно получить из адресной строки при открытии конкретного графика (любой график → "view" или "edit", в адресной строке выглядит примерно как "viewPanel=25"), либо запустить скрипт выгрузки с пустыми полями конфига "graphids" и "var_files" - тогда в открывшемся окне логов можно будет увидеть ID всех графиков, найденных на дашборде и сопоставленных с названиями.
1. time_from, time_till - время "с - до". Временной интервал, данные за который будут отображены на выгружаемых изображениях графиков. Строгий формат, учитывая символы и пробелы:
`yyyy/mm/dd hh:mm:ss`
1. var_files - файлы со списками названий и значений параметров (необязательное). Файлы необходимо положить в папку скрипта (на одном уровне с конфигом) и в данном параметре указывать только названия с учетом регистра, с расширениями, через пробел.

На последнем пункте стоит остановиться подробнее.

Его заполнять имеет смысл только если в дашборде настроены параметры. [Например](https://github.com/D4nD4nce/QA_LoadTesting_Info/blob/main/grafana_graph_downloader/examples/description_3.PNG) - script и transaction. Как правило, параметров несколько, они применяются к одному или нескольким графикам и в зависимости от выбранного значения влияют на отображение.
Явный пример такого дашборда можно посмотреть [здесь](https://play.grafana.org/d/000000002/influxdb-templated?orgId=1&var-datacenter=Africa&var-host=server%2F7&var-summarize=1m). Параметр datacenter можно выбрать только один, но от него напрямую зависит выборка доступных значений в параметре host. Также есть независимый параметр summarize, значения которого всегда статичны. Дашборд может быть открыт и отрендерен как изображение без указания параметров в адресной строке - тогда будут применены значения по умолчанию (обычно просто первые из каждого списка). Либо можно указывать только отдельные параметры - зависимые должны быть всегда указаны вместе, а независимые - опционально.
В случае, если бы была возможность применить скрипт выгрузки изображений к этому дашборду, файл для var_files мог бы быть заполнен так:

[пример_1](https://github.com/D4nD4nce/QA_LoadTesting_Info/blob/main/grafana_graph_downloader/examples/example_1.PNG) - 1ая строка всегда название параметра. Его можно получить из адресной строки при открытии любого графика дашборда (даже если в нем не используются параметры). Следующие строки - значения параметра, каждое на отдельной строке. Сколько значений указано - столько раз будут загружены все графики, ID которых были указаны в config параметре "graphids". Соответственно, график из примера будет выгружен 4 раза, по одному с каждым значением параметра "var-datacenter".

[пример_2](https://github.com/D4nD4nce/QA_LoadTesting_Info/blob/main/grafana_graph_downloader/examples/example_2.PNG) - здесь указаны два зависимых параметра. 1-ая строка - названия параметров, 2-ая строка и далее - ключи значений. Условно есть два столбца значений двух параметров, на каждой строке друг от друга отделены пробелом (пример не совсем удачный, так как в данном демонстративном варианте некоторые значения в себе имеют пробелы). Для параметра "var-datacenter" могут быть указаны только строго определенные "var-host" из его группы, и не могут быть указаны другие. График из примера будет выгружен 10 раз. В адресной строке при каждой выгрузке будут указаны два параметра "var-datacenter" и "var-host" со значениями, соответствующими каждой строке файла. То есть, будут выгружены все данные графика со всеми возможными для него уникальными парами значений данных параметров.

[пример_3](https://github.com/D4nD4nce/QA_LoadTesting_Info/blob/main/grafana_graph_downloader/examples/example_3.PNG) - для этого примера необходимо представить, что переменные из примера_2 помещены в файл script_values1.csv, а из примера_3 - в файл script_values2.csv. Файлы размещены внутри директории скрипта, а в параметре конфига указано: "var_files = script_values1.csv script_values2.csv". В таких условиях скрипт выполнит выгрузку графика [количество строк значений из script_values1.csv] * [количество строк значений из script_values2.csv] = 100 раз. Размещенные файлы представляют собой две независимые группы значений параметров, поэтому уникальный набор значений будет подставлен в адресную строку во всех возможных комбинациях этих групп (порядок не важен). Другими словами, вначале будут выгружены изображения со всеми вариантами значения из файла script_values2.csv (содержимое в примере_3) для параметров в строке номер 2 файла script_values1.csv (содержимое в примере_2). Затем - все значения из файла script_values2.csv для параметров в строке 3 файла script_values1.csv и тд.
---
Исходники - готовый проект Idea - в архиве python_get_grafana_graphs_sources.zip.