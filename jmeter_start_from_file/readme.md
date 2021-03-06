# TODO
1. ~Добавить исходники.~

# Общее описание
Многие проекты с большим количеством скриптов и сложным профилем требуют регулярной правки настроек запуска. Ситуация может усугубляться тем, что настройки у каждого скрипта - разные, тестов - несколько, а сроки - как обычно поджимают.
Чтобы облегчить задачу правки профиля для каждого скрипта перед каждым запуском, можно использовать различные костыли. Ниже в статье описаны одни из них, оформленные в понятную схему запуска с использованием пары утилит.
![общая схема](https://github.com/D4nD4nce/QA_LoadTesting_Info/blob/main/jmeter_start_from_file/description_imgs/general_schema.png)

#### Описание элементов.
- __Общий файл параметров__. Содержит в себе параметры запуска для каждого скрипта. Может быть нескольких видов с разным расширением:
  - CSV - более "легкий" формат, для обработки которого используется утилита с меньшим весом. Однако, несмотря на возможность обработки данного файла через excel, нет возможности сохранить форматирование + возможны дополнительные ограничения. При этом сам по себе CSV файл нечитабелен без дополнительных средств.
  - XLSX - более "тяжелый" формат, для обработки которого нужны дополнительные библиотеки, из-за чего вес утилиты для его обработки существенно увеличивается. Зато есть возможность форматирования, использования формул и других функций Excel.
- __Утилита обработки параметров__. Самописное средство на java. Запускается через консоль или командой, принимает минимум 2 аргумента: путь к файлу параметров (элемент из пункта 1) и имя скрипта, параметры для которого необходимо создать. В первой версии утилита весит мало, но может обрабатывать только CSV. Во второй - весит больше, но может принимать все перечисленные форматы. После окончания своей работы утилита создает новый файл `*.properties`, в котором заданы все параметры запуска для одного из скриптов, взятые из общего файла параметров.
- __Файл параметров Jmeter__. Короткий файл в простом формате, где перечислены имена и значения параметров запуска для одного из скриптов Jmeter. Генерируется автоматически при помощи утилиты обработки параметров (элемент из пункта 2). При этом имя файла формируется так: `[имя скрипта, полученное как аргумент в утилите обработки параметров].properties`.
- __Утилита запуска теста__. Запускается пользователем. Простой скрипт на языке ОС, в котором задаются команды:
  - запуск с аргументами утилиты обработки параметров.
  - запуск скриптов нагрузочного тестирования с необходимыми аргументами, в том числе - с указанием созданного файла `*.properties`.
- __Скрипт Jmeter__. Должен быть подготовлен к чтению параметров из переменных среды, заданных в файле `*.properties`.

#### Описание логики запуска:
- Точка входа - __утилита запуска теста__, написанная как простейший action script на языке выбранной ОС.
- Первым делом должна быть запущена __утилита обработки параметров__, которой в качестве аргументов подается __общий файл параметров__ и название скрипта, параметры к которому необходимо сформировать. На выходе в той же папке, где находится утилита, создается файл с параметрами к одному из скриптов с расширением `*.properties`.
- Далее выполняется запуск __скрипта Jmeter__ с аргументами: режим консоли, указание файла, куда будут записываться логи, а также указание файла, из которого будут взяты переменные среды `*.properties`.
- В __утилите запуска теста__ может быть прописана логика формирования параметров и запуска нескольких скриптов сразу.

---
### Более подробное описание схемы запуска и тонкости работы.
=================================
#### 1. Общий файл параметров 
Структура и особенности:
- столбцы → виды параметров, строки → наборы параметров для каждого из скриптов;
- первая строка → имена параметров. Остальные строки → их значения;
- первый столбец → всегда имя скрипта, к которому относятся параметры из следующих полей данной строки, должно быть уникальным. Порядок и формат остальных столбцов не важен;
- количество названий параметров (первая строка) и количество значений (любая другая строка) всегда должны быть равны. Для пустых значений следует использовать 0 или NULL или любое другое строковое значение, как удобно;
- количество строк и столбцов не лимитировано. Но: столбцов должно быть не менее 2-х → имя скрипта + хотя бы 1 параметр; строк также должно быть не менее 2-х → названия параметров + хотя бы один набор значений параметров для скрипта.
- Если это CSV файл:
  - названия и значения параметров могут быть почти любыми. Исключение - двойные кавычки, в любом виде и месте они будут проигнорированы, т.е. не станут частью значения, переданного в Jmeter;
  - в качестве разделителя должна быть использована запятая. Если запятая есть внутри параметра - данный параметр должен быть заключен в двойные кавычки. Это единственный релевантный вариант использования двойных кавычек.
- Если это XLSX файл:
  - названия и значения параметров могут быть любыми. Исключение - двойные кавычки, будут проигнорированы;
  - поддерживаются особые форматы полей: например даты и формулы. Поля с процентным или дробным форматом будут переданы в Jmeter как значения с плавающей точкой;
  - если в значении поля используется формула, то она будет вызвана и пересчитана в момент обработки значения для последующей передачи в Jmeter;
  - поля типа `дата` всегда будут обработаны в формате: `yyyy.mm.dd-hh:mm`;
  - в документе всегда будет обработан только первый лист, независимо от названия, остальные будут проигнорированы;
  - таблица параметров может содержать полностью пустые строки или столбцы, они будут проигнорированы;
  - будут проигнорированы все строки, в которых первый столбец (имя скрипта) пуст;
  - при обнаружении ошибки формулы в одном из полей, скрипт обработки параметров выдаст в консоль уведомление об этом.

=================================
#### 2. Утилита обработки параметров
Существует в двух версиях, одна читает только CSV (облегченная), другая читает все перечисленные форматы.

Примеры команды запуска, если данная утилита и __общий файл параметров__ лежат в одном каталоге, а имя одного из скриптов - UC02: 
- `$ java -jar jmeter_parser.jar scripts_params.csv UC02`
- `$ java -jar [путь к утилите обработки параметров].jar [путь к общему файлу параметров].csv [имя скрипта]`

При этом важно понимать, что в качестве аргумента как `[имя скрипта]` должно быть указано именно то значение, которое можно найти в 1-ом столбце __общего файла параметров__. `[имя скрипта]` в данном случае - условный Primary Key, используемый как индекс для поиска параметров только выбранного скрипта. В качестве Primary Key может быть использовано любое значение, только в данном и следующих примерах выбрано имя скрипта для удобства логики всей связки запуска теста. То же самое значение Primary Key используется как имя при создании файла с параметрами конкретного скрипта: `[имя скрипта].properties`

Схема использования Primary Key:
![схема использования ключа](https://github.com/D4nD4nce/QA_LoadTesting_Info/blob/main/jmeter_start_from_file/description_imgs/primary_key_explanation_2.png)

Версия утилиты 1 (CSV only):
1. аргументов всегда 2 (имя файла параметров и имя скрипта), ни больше, ни меньше;
2. при каждом запуске утилита в консоль в виде лога записывает ход работы. Туда же будут выведены ошибки в случае возникновения;
3. все запятые интерпретируются как разделители. Запятая игнорируется, только если содержащее ее значение параметра заключено в двойные кавычки;
4. все присутствующие в названиях или в значениях параметров двойные кавычки удаляются.

Версия утилиты 2 (all formats):
1. работает со всеми из перечисленных форматов;
2. также удаляет все двойные кавычки из имен и значений параметров;
3. в консоль выводится подробный лог работы;
4. аргументов может быть более двух. Будут созданы файлы `*.properties` для каждого из перечисленных скриптов (по каждому ключу), пример: `java -jar jmeter_parser.jar scripts_params.csv UC02 UC03 UC05`

=================================
#### 3. Файл параметров одного из скриптов Jmeter
Файл с расширением `*.properties`, создается автоматически __утилитой обработки параметров__. Каждая строка файла это 1 параметр в формате имя=значение. Например:
```
ksed_pacingTargetThroughput=0.025
ksed_iterationCount=12
ksed_rampUpStepsCount=4
ksed_holdTargetRateTime=43200
ksed_targetConcurrency=12
ksed_xlsParams=0
ksed_AFFlag=true
ksed_docxParams=15-20KB,10%;20-25,15%;25-30,25%;35-40,50%
script_name=UC02
```
При этом:
- имена параметров - берутся из первой строки общего файла параметров.
- значения параметров - берутся из строки общего файла параметров, которой соответствует использованный ключ (Primary Key) при запуске утилиты обработки параметров.

=================================
#### 4. Утилита запуска теста
Пока что есть пример только для linux - sh скрипт. Который, впрочем, можно запустить и под Windows с помощью любой оболочки bash (например, из-под гита). Код можно сохранить в файл без расширения:
```
#!/bin/bash
 
run_script() {
    java -jar jmeter_parser.jar scripts_params.csv $1
    nohup sh /KSED/apache-jmeter-5.1/bin/jmeter -n -p $1.properties -t /KSED/new_git/scripts_from_git/$1.jmx > $1.log &
}
 
echo "Start Test Incomind"
run_script "UC02"
run_script "UC17"
sleep 500
run_script "UC03"
run_script "UC18"
sleep 500
run_script "UC19"
sleep 500
run_script "UC20"
sleep 500
run_script "UC21"
sleep 500
run_script "UC22"
echo "Task Completed"
```
Вначале объявляется функция, в которой вызывается и средство генерации параметров, и сам скрипт. У функции один аргумент - название скрипта. Название скрипта при этом является и ключом для поиска параметров, и названием для генерируемого файла `*.properties`, и названием файла логов этого скрипта. После этого выполняется вызов функции для каждого из запускаемых скриптов.

=================================
#### 5. Jmeter скрипт
Jmeter при запуске с использованием файла `*.properties` фактически использует параметры из этого файла как переменные среды. Они инициализируются в момент запуска, до инициализации остальных переменных скрипта и тред групп. Также данные переменные одинаковы и статичны для всех потоков, но не являются константами, т.е. их можно менять в ходе работы скрипта (если возникнет такая потребность). Продолжая пример с использованием представленных выше общих файлов параметров, предлагается типичная схема их использования в скриптах, которую можно переделать под свои нужды.

Для корректного использования параметров необходимо добавить в готовый скрипт несколько UDV элементов:
1. Тест-элемент `user defined variables`. Располагается где-то в шапке тест плана скрипта. В нем задаются значения параметров по умолчанию, которые будут использованы скриптом, если он был запущен без использования файла `.*properties` и не имеет нужных переменных среды. Это может пригодиться, например, при отладке в режиме GUI. Можно заметить, что названия параметров при этом очень схожи: в файлах параметров использован тот же набор, но с приставкой `ksed_*` , здесь - с приставкой `temp_*`;
![пример 1](https://github.com/D4nD4nce/QA_LoadTesting_Info/blob/main/jmeter_start_from_file/description_imgs/jmeter_params_1.png)
2. Тест-элемент `user defined variables`. Должен быть размещен после(ниже) предыдущего. В нем происходит запрос переменных среды из файла `*.properties` (с приставкой `ksed_*`), и сохранение результата каждого запроса в обычную переменную с похожим названием (с приставкой `param_*`). А если в переменных среды не будет найдено необходимых значений - будут запрошены значения переменных по умолчанию, которые были заданы в предыдущем тест-элементе (с приставкой `temp_*`).
![пример 2](https://github.com/D4nD4nce/QA_LoadTesting_Info/blob/main/jmeter_start_from_file/description_imgs/jmeter_params_2.png)
Итоговая строчка имеет простую схему: `${__property([имя запрашиваемой переменной среды], [имя переменной, в которую будет сохранен результат], [имя переменной, из которой будет взято значение по умолчанию в случае, если переменная среды не найдена])}` - кроме того, эта функция еще и возвращает результат своей работы. Описание работы этой функции есть в документации Jmeter.
3. Тред группа. Должна быть размещена после двух предыдущих файлов, так как использует получаемые в них значения. Здесь видно, что полученные из переменных среды значения используются как параметры запуска Concurrency Thread Group.
![пример 3](https://github.com/D4nD4nce/QA_LoadTesting_Info/blob/main/jmeter_start_from_file/description_imgs/jmeter_params_3.png)

Подытоживая:
- имена запрашиваемых в Jmeter переменных среды и названия параметров из общего файла параметров (1-я строка) должны быть идентичны.
- в Jmeter можно использовать данные параметры среды напрямую, никуда дополнительно не сохраняя их значения, но тогда скрипт не будет работать без файла `.*properties`, его будет гораздо сложнее отлаживать в режиме GUI.

---
### Файлы:
- Пример CSV файла - scripts_params.csv
- Пример XLSX файла - scripts_params.xlsx
- Утилита версии 1 - jmeter_parser_CSV.jar
- Утилита версии 2 - jmeter_parser_ALL.jar
