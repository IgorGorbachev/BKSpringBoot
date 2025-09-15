' ==============================================
' Скрипт для скрытого запуска Spring Boot приложения
' ==============================================

Set WshShell = CreateObject("WScript.Shell")

' Полный путь к вашему bat-файлу
batPath = "F:\IdeaProjects\BKSpringBoot\start-app.bat"

' Запускаем bat-файл в скрытом режиме
WshShell.Run "cmd /c """ & batPath & """", 0, False

' Освобождаем ресурсы
Set WshShell = Nothing

' Сообщение о запуске (необязательно)
MsgBox "App BazaClientov strated", vbInformation, "Spring Boot Launcher"