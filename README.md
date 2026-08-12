```markdown
# 🐂 Быки и Коровы

Мобильное приложение для игры "Быки и Коровы" с поддержкой режимов: одиночная игра, игра на одном устройстве для двух игроков, а также игра по Bluetooth.

[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://developer.android.com/)
[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.java.com/)
[![Google Nearby](https://img.shields.io/badge/Google_Nearby-4285F4?style=for-the-badge&logo=google&logoColor=white)](https://developers.google.com/nearby)

## 📱 О проекте

**Быки и Коровы** — это классическая логическая игра, в которой игроки отгадывают загаданное слово, используя подсказки "быки" (буква на своём месте) и "коровы" (буква есть, но не на своём месте). Приложение предлагает три режима игры, адаптивный интерфейс и полную статистику.

### 🎮 Доступные режимы

| Режим | Описание |
|-------|----------|
| 👤 Один игрок | Компьютер загадывает слово, игрок отгадывает (3-6 букв) |
| 👥 Два игрока на одном устройстве | Игроки по очереди вводят слова на одном устройстве |
| 📡 Bluetooth | Соединение двух устройств через Bluetooth/Nearby Connections |

## 📋 Основной функционал

### 🎯 Игровой процесс
- Загадывание слов от 3 до 6 букв
- Подсчет быков и коров после каждого хода
- Подсветка нулевых букв на клавиатуре
- Таймер отслеживания времени игры
- 25 попыток для угадывания слова
- Сохранение и восстановление игры (кнопка ПИН)

### 📊 Статистика
- Всего сыгранных игр
- Количество побед
- Процент побед
- Среднее количество попыток
- История всех игр с датой и временем

### 🎨 Интерфейс
- Адаптивная клавиатура с русскими буквами
- Визуальная подсветка текущего игрока (в режиме двух игроков)
- Поддержка разных размеров экрана (телефоны, планшеты)
- Аналитика быков, коров и нулевых букв

### 🔄 Bluetooth режим
- Подключение устройств через Nearby Connections API
- Обмен загаданными словами между игроками
- Полноценная игра с соперником

## 🚀 Быстрый старт

### 1. Клонировать репозиторий
```bash
git clone https://github.com/ваш-username/bulls-and-cows.git
cd bulls-and-cows
```

### 2. Открыть проект в Android Studio
- File → Open → выбрать папку проекта
- Дождаться синхронизации Gradle

### 3. Настройка проекта
Проверьте `build.gradle` (Module: app):
```gradle
android {
    compileSdk 34
    defaultConfig {
        minSdk 21
        targetSdk 34
    }
}
```

### 4. Запуск приложения
- Подключите Android устройство или запустите эмулятор
- Нажмите Run (▶️)

## 📁 Структура проекта

```
app/src/main/
├── java/com/example/bullsandcows/
│   ├── GameActivity.java              # Основная игровая логика
│   ├── MainActivity.java              # Главное меню
│   ├── WordLengthActivity.java        # Выбор длины слова
│   ├── StatisticsActivity.java        # Статистика
│   ├── BluetoothLobbyActivity.java    # Bluetooth лобби
│   ├── WordInputActivity.java         # Ввод слова в Bluetooth
│   ├── ConnectionTypeActivity.java    # Выбор типа подключения
│   ├── GameLogger.java                # Логирование статистики
│   ├── GameApplication.java           # Application класс
│   └── AboutActivity.java             # О программе
│
├── res/
│   ├── layout/                        # XML макеты
│   │   ├── activity_game.xml
│   │   ├── activity_main.xml
│   │   ├── activity_statistics.xml
│   │   ├── activity_bluetooth_lobby.xml
│   │   ├── activity_word_input.xml
│   │   ├── activity_word_length.xml
│   │   ├── activity_connection_type.xml
│   │   └── item_history.xml
│   ├── drawable/                      # Ресурсы изображений
│   │   ├── cell_background.xml
│   │   ├── keyboard_default.xml
│   │   └── zero_gray.xml
│   ├── values/
│   │   ├── colors.xml                 # Цветовая схема
│   │   ├── strings.xml                # Текстовые ресурсы
│   │   └── styles.xml                 # Стили
│   └── mipmap/                        # Иконки приложения
│
└── AndroidManifest.xml                # Манифест приложения
```

## 🛠️ Используемые технологии

| Технология | Назначение |
|------------|------------|
| **Java** | Язык разработки |
| **Android SDK** | Платформа разработки |
| **Google Nearby Connections** | Bluetooth соединение |
| **SharedPreferences** | Сохранение статистики и игр |
| **GridLayout** | Отображение игрового поля |
| **Handler** | Таймер и асинхронные операции |
| **Gson** | Сериализация данных для статистики |

## 🎯 Игровые правила

1. **Компьютер или игрок загадывает слово** (от 3 до 6 букв, буквы не повторяются)
2. **Игрок вводит слово** для проверки
3. **Получает результат:**
   - 🐂 **Бык** — буква есть и стоит на правильном месте
   - 🐄 **Корова** — буква есть, но стоит не на своём месте
   - ⚪ **Ноль** — буквы нет в загаданном слове
4. **Цель** — угадать слово за 25 попыток

## 🔧 Настройка разрешений

В `AndroidManifest.xml` добавлены необходимые разрешения:

```xml
<!-- Для Bluetooth на Android 12+ -->
<uses-permission android:name="android.permission.BLUETOOTH_SCAN" />
<uses-permission android:name="android.permission.BLUETOOTH_ADVERTISE" />
<uses-permission android:name="android.permission.BLUETOOTH_CONNECT" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
```

## 📱 Скриншоты

| Главное меню | Игровой процесс | Статистика |
|--------------|-----------------|------------|
| ![Меню](screenshots/menu.png) | ![Игра](screenshots/game.png) | ![Статистика](screenshots/stats.png) |

*Для добавления скриншотов создайте папку `screenshots/` и поместите туда изображения.*

## 🔄 Обновление словарей

Словари для разных длин слов находятся в `GameActivity.java`:

```java
private final String[] words3 = { "дом", "лес", "мох", ... };
private final String[] words4 = { "мама", "папа", "дома", ... };
private final String[] words5 = { "птица", "сосна", "вишня", ... };
private final String[] words6 = { "яблоко", "тополь", "береза", ... };
```

## 🐛 Возможные проблемы и решения

| Проблема | Решение |
|----------|---------|
| **Ошибка разрешений Bluetooth** | Разрешите доступ к местоположению в настройках приложения |
| **Соперник не отвечает** | Проверьте, что оба устройства включили Bluetooth |
| **Не восстанавливается подсветка** | Проверьте сохранение через кнопку ПИН |
| **Статистика не сохраняется** | Проверьте права на запись в SharedPreferences |

## 📄 Лицензия

MIT License

Copyright (c) 2026 [Пыргарь Алексей Валерьевич]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions...



**Быки и Коровы** — игра для развития логического мышления и словарного запаса. Удачи в игре! 🎯
```
