# Общее описание

Данный плагин для Jmeter полностью копирует интерфейс и принцип работы из одноименного функционала (pacing) из Vugen Load Runner.
> Рекомендованная версия Jmeter - не ниже 5.0.0

### Использование
1. Файл необходимо добавить в Jmeter в качестве плагина: `..\apache-jmeter-x.x.x\lib\ext\pacing-plugin-1.0.jar`
1. После этого при открытии Jmeter в режиме GUI плагин можно найти в списке "Timers":
![пример](https://github.com/D4nD4nce/QA_LoadTesting_Info/blob/main/jmeter_pacing_sampler/description_pictures/example_gui_1.png).
2. Для корректной работы и совместимости с другими таймерами рекомендуется данный pacing sampler и другие таймеры всегда располагать под "Flow Control Action" элементом в режиме "pause":
![например так](https://github.com/D4nD4nce/QA_LoadTesting_Info/blob/main/jmeter_pacing_sampler/description_pictures/example_gui_2.png)

### Файлы
Исходники: в архиве sources.zip
Скомпилированный плагин: pacing-plugin-1.0.jar
