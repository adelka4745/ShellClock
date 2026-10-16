# Реализация цифровых часов

Добавление функциональности цифровых часов, которые обновляются в реальном времени, с центрированием на главном экране.

## Предлагаемые изменения

### UI компоненты

#### [NEW] [ClockScreen.kt](file:///C:/Users/user/AndroidStudioProjects/ShellClock/app/src/main/java/shellinc/sovashellteam/shellclock/ClockScreen.kt)
Создание нового Composable-компонента для отображения часов.

#### [MODIFY] [MainActivity.kt](file:///C:/Users/user/AndroidStudioProjects/ShellClock/app/src/main/java/shellinc/sovashellteam/shellclock/MainActivity.kt)
Замена стандартного приветствия на компонент часов.

## План верификации

### Автоматизированные тесты
- Проверка корректного форматирования времени (HH:mm:ss).

### Ручная проверка
- Запуск приложения на эмуляторе или устройстве.
- Проверка обновления секунд каждую секунду.
