# Описание работы
Утилита является реализацией очереди, работающей по принципу "first in - first out". Запускается как процесс, существует в виде веб-сервиса, который принимает GET HTTP запросы определнного вида. Убить можно вручную, либо путем перезапуска машины.

Запуск очереди под Win выполняется исполнением файла `run.bat`. В нём можно указать порт, на котором очередь будет запущена.

При запуске приложения создаётся пустой экземпляр очереди, в котором можно создавать списки. Далее в каждый список можно грузить значение. При чтении значения из списка оно удаляется, благодаря этому достигается уникальность использования каждого значения.

### Команды
- listoffer - `http://localhost:8088/listoffer?param=list_name&value=list_value` -> создается именованный список с параметрами, где:
  - `list_name` - имя списка в очереди
  - `list_value` - записываемое значение
- listpoll - `http://localhost:8088/listpoll?param=list_name` -> забирается значение из списка `list_name`
- Значение заменяется каждый раз при обновлении и удаляется при считывании:
  - valueput - `http://localhost:8088/valueput?param=par1&value=1`
  - valuepoll - `http://localhost:8088/valuepoll?param=par1`
- listinfo - `http://localhost:8088/listinfo` -> Инфа о всех списках (имена и количество записей в каждом)
- Еще примеры:
  - `http://localhost:8088/listoffer?param=eplatonova&value=1`
  - `http://localhost:8088/listpoll?param=eplatonova`
