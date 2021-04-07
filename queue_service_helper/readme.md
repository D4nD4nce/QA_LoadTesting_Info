Утилита стартует в виде фонового процесса. Убить можно вручную, либо путем перезапуска машины.

Запуск очереди под Win выполняется исполнением файла run.bat. В нём можно указать порт, на котором очередь будет запущена.

При запуске приложения создаётся пустой экземпляр очереди, в котором можно создавать списки. Далее в каждый список можно грузить значение. При чтении значения из списка оно удаляется, благодаря этому достигается уникальность использования каждого значения.

---
Создать именованный список с параметрами:

http://localhost:8088/listoffer?param=${login}&value=${doc_id}   

где param - имя списка в очереди, value - записываемое значение

---
Забрать значение из списка:

http://localhost:8088/listpoll?param=${login}

---
Значение заменяется каждый раз при обновлении и удаляется при считывании:

http://localhost:8088/valueput?param=par1&value=1

http://localhost:8088/valuepoll?param=par1

---
Инфа о всех списках (имена и количество записей в каждом):

http://localhost:8088/listinfo

---
Еще примеры:

http://localhost:8088/listoffer?param=eplatonova&value=1

http://localhost:8088/listpoll?param=eplatonova