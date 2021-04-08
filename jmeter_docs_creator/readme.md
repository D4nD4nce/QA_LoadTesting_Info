Руководство описывает работу плгинов для Jmeter (javaRequest) - __NTSamplerDynamicDocsCreator__ и __NTSamplerStaticDocsCreator__.

## Общее описание:
Данные плагины позволяют создавать документы форматов: doc, docx, pdf, xls, xlsx произвольного размера с рандомным содержанием. Размер файлов задается через параметры. Принцип расчета при этом - 1 символ = 1 байт. Данная методика имеет низкую точность, так как большая часть перечисленных форматов - по сути своей архивы, и используют те или иные инструменты сжатия. В результате желаемый размер почти всегда будет больше фактического на 10 - 50%, при этом чем больше желаемый размер, тем больше будет разница.

В данный момент создание реализовано на основе шаблона: вначале вычисляется требуемый размер, затем на его основе набирается массив ключевых слов. После этого все части соединяются по принципу `[первая часть шаблона] + [рандомный массив слов в виде строки через пробел] + [вторая часть шаблона]`. Весь результат в виде текста без форматирования помещается в документ. Если это Excel, то текст дополнительно разбивается по ячейкам. Используемые шаблоны и дефолтный словарь, из которого набирается массив ключевых слов можно найти внизу страницы. В текущей версии шаблоны неизменяемы, а массив ключевых слов можно подставить свой.

> Единственное исключение - PDF. Данный формат в текущей реализации не позволяет наполнять документ кириллицей, поэтому при создании будет заполнен латинскими буквами и цифрами в соответствии с заданным размером. Шаблоны при этом использованы не будут.

У всех форматов есть минимальный размер. Еще меньше создать не получится:
- DOC, DOCX - 3KB
- XLS, XLSX - 4KB
- PDF - 1KB

---
## Установка:
Необходимы Java 1.8 и выше, Apache Jmeter не ниже 5.0.0

Скопировать плагин и библиотеки в папку `..\apache-jmeter-x.x.x\lib\ext\` :
- original-jmeter_param_creator-1.0-SNAPSHOT.jar
- activation-1.1.1.jar
- barcodes-7.0.2.jar
- commons-codec-1.13.jar
- commons-collections4-4.4.jar
- commons-compress-1.19.jar
- commons-logging-1.2.jar
- commons-math3-3.6.1.jar
- curvesapi-1.06.jar
- font-asian-7.0.2.jar
- forms-7.0.2.jar
- hyph-7.0.2.jar
- io-7.0.2.jar
- jaxb-api-2.3.1.jar
- jaxb-core-2.3.0.1.jar
- jaxb-impl-2.3.2.jar
- junit-4.12.jar
- kernel-7.0.2.jar
- layout-7.0.2.jar
- log4j-1.2.17.jar
- pdfa-7.0.2.jar
- poi-4.1.2.jar
- poi-examples-4.1.2.jar
- poi-excelant-4.1.2.jar
- poi-ooxml-4.1.2.jar
- poi-ooxml-schemas-4.1.2.jar
- poi-scratchpad-4.1.2.jar
- sign-7.0.2.jar
- SparseBitSet-1.2.jar
- xmlbeans-3.1.0.jar

> список библиотек может быть увеличен по мере добавления возможности генерации новых форматов документов

В созданный тест-план добавить элемент - javaSampler, в списке className которого выбрать: __NTSamplerDynamicDocsCreator__ или __NTSamplerStaticDocsCreator__.

---
## Использование NTSamplerDynamicDocsCreator: генерация документов "на лету", во время теста при помощи шаблона и словаря.
> При первой установке для ознакомления рекомендуется запустить сэмплер с параметрами по умолчанию в режиме 1-2х потоков и 1-2 итераций, после чего продолжить чтение данного документа.

Данный сэмплер позволяет создавать документы динамически во время теста. Документ создается и заполняется текстом на основе заданных параметров и существует в виде файла на диске до конца итерации. Все свойства созданного документа возвращаются в виде response из java request.

### Параметры:
1. description_general - общее описание плагина. константа
1. description_example - пример указания параметров для создания файлов одного из расширений. константа
1. parameter_new_documents_folder - имя папки, в которой будут создаваться новые документы (для всех потоков, форматов и итераций). Если папка не существует - она будет автоматически создана в директории, из которой запущено приложение (`%work_directory%\Jmeter\bin`). (функционал будет доработан)
1. parameter_document_name_template - шаблон имени создаваемого документа. Необходимо делать уникальным для каждой тред-группы. Шаблон одинаков для всех потоков, форматов и итераций. Создание уникального имени реализовано методом:
`[шаблон имени]_[номер потока].[расширение документа]`
1. parameter_thread_number - номер потока jmeter.
1. parameter_source_dictionary (опционально) - имя файла с ключевыми словами/лексемами. Подробности ниже.
1. {docx, xlsx, pdf, ...} - список расширений, которые доступны при создании нового документа. Эти поля строго привязаны к формату из поля description_example. Подробнее они описаны ниже.

### Особенности:
- На каждой итерации происходит удаление документа, который был создан на предыдущей. Мусор в папке с создаваемыми документами может оставаться только при:
  - уменьшении кол-ва потоков;
  - изменении шаблона имени создаваемого документа при перезапуске;
  - изменении имени / создании новой папки
- Размер документа (это всегда связка minSize - maxSize или т.н. скоуп размеров из которого выбирается рандомное значение) указывается в KB, должен быть целочисленным.
- Сэмплер JavaRequest в своём ResponseBody возвращает ряд параметров, которые удобно затем использовать в скрипте при работе с созданным файлом (любой параметр можно легко получить при помощи Regular Expression Extractor / Boundary Extractor):
  - newFileFullName - полный путь к созданному файлу (корень\папки\имя_файла)
  - newFileName - имя созданного файла с расширением, без указания родительских папок
  - newFileSize - размер созданного документа (KB), выбранный программой из скоупа (minSize - maxSize). Отличается от фактического (см. шапку страницы) размера созданного файла.
  - percentSummary - сумма вероятностей (%) всех форматов и скоупов (вспомогательный, для проверки правильности ввода параметров)
  - foundFormats - все форматы, для которых найдены валидные параметры создания (вспомогательный, для проверки правильности ввода параметров)
  - actualFileSize - реальный размер созданного документа. в байтах
  - chosen format - формат/расширение , выбранные для создания этим потоком в этой итерации (debug)
  - chosen sizeMin - минимальный размер в выбранном скоупе этим потоком в этой итерации (debug) - KB
  - chosen sizeMax - максимальный размер в выбранном скоупе этим потоком в этой итерации (debug) - KB
  - chosen percent - вероятность выбора данных параметров этим потоком в этой итерации (debug) - %
- Словарь ключевых слов - дефолтная версия находится внутри плагина, однако может быть заменена. Для замены необходимо в значение параметра `parameter_source_dictionary` поместить абсолютный путь к файлу, заполненному ключевыми словами, где 1 строка = 1 слово.

### Правила указания параметров создания документа, разбор параметров полей (docx, xlsx, pdf, ...):
`[doc size min]-[doc size max],[create doc possibility];[doc size min]-[doc size max],[create doc possibility];...` где:
- doc size min - минимальный размер документа или minSize (KB)
- doc size max - максимальный размер документа или maxSize (KB)
- create doc possibility - вероятность создания документа с данным расширением и скоупом размеров. Указывается в процентах (%).

Минимальный и максимальный размеры образуют собой скоуп, из которого программой будет рандомно выбран точный размер создаваемого документа. Для одного расширения/формата файла может быть указано несколько связок (скоуп размеров + вероятность их выбора). Они отделяются друг от друга символом `;`. После последней связки знак `;` не ставится. В строке параметра могут присутствовать вспомогательные буквы (KB) и символы (%). Они не обязательны, но можно добавлять для удобочитаемости.

> __! Важно !__
> 
> Сумма указанных вероятностей всех скоупов всех форматов должна быть равна 100%. (это легко проверить с помощью возвращаемого параметра percentSummary при тестовом запуске), Если меньше - при попадании рандомизатора за границы вероятности будет создан документ по-умолчанию: DOCX 10Kb, Если больше - рандомизатор будет работать в пределах 100%, и для части форматов вероятность создания будет ниже/выше указанной, либо они не будут созданы вовсе.

Удалить созданный файл можно с помощью элемента JSR223 Sampler, добавленного в любое место после непосредственного создания документа (например, в конец тест-плана), со следующим кодом:
```Groovy
/**
 * check if new file was created and delete one
 * using params:
 * ${newFilePath} - absolute path to new file, that might be deleted
 */
 
// get and check param
String fileName = vars.get("newFilePath")
if (fileName == null || fileName.isEmpty())
    return
 
// define file as object
def thisFile = new File(fileName)
 
// check, delete and log
if (thisFile.exists()) {
    boolean isDeleted = thisFile.delete()
    if (isDeleted) {
        log.info("file successfully deleted: " + fileName)
    } else {
        log.error("can't delete file! " + fileName + " || iteration: " + vars.getIteration())
    }
}
```

### Добавление изображений (функционал будет доработан в новых версиях):
По умолчанию поиск изображения для добавления в новый документ ведется в директории, в которой создаются документы (parameter_new_documents_folder). Доступные форматы: pict, jpeg, jpg, png, bmp. Если изображение с нужным форматом найдено в директории и отвечает всем условиям - оно будет добавлено в документ. При этом:
1. Файл изображения должен быть меньше, чем ожидаемый (заданный в параметрах) размер документа (KB).
2. Документ будет заполнен текстом по формуле v = s - b. Где v - размер всех добавляемых к документу символов (KB), s - заданный параметр размера документа (выбранный из скоупа)(KB), b - размер изображения (KB).
3. Изображение будет автоматически приведено к размеру 200x200px, изначальный размер изображения должен быть больше или равен этому значению.
4. Если изображение будет сильно отличаться от приведенных по умолчанию параметров размера (200x200px) - сжатие его до нужных значений приведет к существенной потере размера файла (KB), и итоговый размер документа (KB) будет сильно меньше ожидаемого.

---
## Использование NTSamplerStaticDocsCreator: генерация документов из файла параметров по шаблону.
Имеет только один параметр, special-doc_with_parameters - в качестве значения должен быть указан абсолютный путь к файлу параметров. Пример входного файла параметров: [!test_parameters.csv](https://github.com/D4nD4nce/QA_LoadTesting_Info/blob/main/jmeter_docs_creator/!test_parameters.csv)

Структура файла:
```
1,C:\WORK\test\create_docs_test\newDoc1.doc,1500,doc
2,C:\WORK\test\create_docs_test\newDoc2.docx,2000,docx
3,C:\WORK\test\create_docs_test\newDoc3.pdf,2500,pdf
4,C:\WORK\test\create_docs_test\newDoc4.xls,3000,xls
5,C:\WORK\test\create_docs_test\newDoc5.xlsx,3500,xlsx
6,C:\WORK\test\create_docs_test\newDoc6.doc,3000,doc
7,C:\WORK\test\create_docs_test\newDoc7.doc,1500,doc
8,C:\WORK\test\create_docs_test\newDoc8.docx,2000,docx
9,C:\WORK\test\create_docs_test\newDoc9.pdf,2500,pdf
10,C:\WORK\test\create_docs_test\newDoc10.xls,3000,xls
11,C:\WORK\test\create_docs_test\newDoc11.xlsx,3500,xlsx
```
Пояснение:
Каждая строка/параметр состоит из нескольких частей, для их разделения используется `,`.
`[ID строки/параметра - служит только для удобства отладки при возникновении ошибок],[абсолютный путь к новому файлу, который необходимо создать, включая имя файла и расширение],[размер нового файла (в килобайтах)],[формат нового файла]`

*Расширение и формат файла могут отличаться. Если в этом нет необходимости, то они должны совпадать.*

> ! важно - в текущей реализации плагин стабильно работает только в однопоточном режиме.

### Особенности:
- При создании файлов данным способом также используются шаблоны и словарь лексем. В данной реализации они полностью соответствуют дефолтным и неизменяемы.
- Каждый путь к новому файлу перед созданием дополнительно проверяется: если по этому пути файл уже существует, то он будет пропущен (проверка на размер не выполняется), а программа перейдет к следующей строке параметров, сбросив сообщение об этом в лог Jmeter.
- Так как списки создаваемых файлов могут насчитывать сотни тысяч строк, а сами файлы при этом могут создаваться в удаленных хранилищах, процесс создания может оказаться продолжительным и иметь высокую вероятность возникновения ошибок - в том числе сетевых. Для упрощения процесса продолжения работы скрипта после аварийной остановки или ошибки, а также более прозрачной работы в целом, было обеспечено дополнительное логирование:
- На каждой итерации скрипт пишет в лог Jmeter - пример:
  - docs created - количество созданных/проверенных документов
  - docs left - количество документов, которые еще осталось создать (на основе количества всех найденных параметров = на основе количества строк в файле параметров)
  - current line - значение текущей строки (текущего параметра) файла параметров, на основе которой был создан новый документ.
- На каждой итерации сэмплер (java request) возвращает ряд параметров в своем response.
  - newFileFullName - абсолютный путь к новому документу
  - newFileName - имя нового документа с расширением
  - newFileSize - размер нового документа (из параметров)
  - currentFormat - формат нового документа
  - actualFileSize - размер нового документа (фактический)
  - isFileExisted - существовал файл или был создан новый (true/false)
  - currentLineFromFile - текущая строка из параметров, на основе которой был создан документ
  - allLinesCountFromFile - количество всех найденных параметров (строк).
  - docsCreatedCount - количество созданных/проверенных документов
  - docsLeftCount - количество документов, которые предстоит проверить/создать (на основе количества строк в файле параметров)

Чтобы проще было понимать, на какой именно строке скрипт находится сейчас (без лишних подсчетов), рекомендуется при заполнении файла параметров первым параметром (число) всегда указывать номер строки, либо просто делать его уникальным.

---
## Используемые файлы:
- Разработанный плагин: original-jmeter_param_creator-1.0-SNAPSHOT.jar
- Архив библиотек: ext_lib.zip
- Файлы используемого шаблона (пока не изменяемы) и словарь лексем: [в папке](https://github.com/D4nD4nce/QA_LoadTesting_Info/tree/main/jmeter_docs_creator/extra/samplers)