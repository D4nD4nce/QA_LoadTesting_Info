# TODO
1. дополнить описание
2. добавить примеры

# Общее описание
В некоторых случаях возникает необходимость распределения параметров по уникальным пачкам, каждую из которых будет использовать только один поток.

Способ разделения параметров между потоками, предлагаемый Jmeter по-умолчанию - через CSV Data Set считывать файл с уникальным ID в названии, который соответствует номеру потока.

Если важно, чтобы параметры были разделены между потоками поровну, но не важно как именно, можно взять один общий файл параметров и разделить на множество мелких. При этом имя каждого нового файла будет состоять из шаблона + ID файла + расширение

### Параметры

### Способ запуска

-------------
# Простой вариант:
использовать bash команду split `$ split --help`