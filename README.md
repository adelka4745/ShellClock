<div align="center">

# 🕰️ ShellClock

**Современные часы для Android, написанные на Kotlin + Jetpack Compose.**
*Минимализм, скорость и Material You.*

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.0-blue.svg?style=flat-square&logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-1.5.0-green.svg?style=flat-square&logo=jetpackcompose)](https://developer.android.com/jetpack/compose)
[![Android](https://img.shields.io/badge/Android-8.0%2B-brightgreen.svg?style=flat-square&logo=android)](https://www.android.com)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg?style=flat-square)](https://opensource.org/licenses/MIT)

[Скачать APK](https://github.com/adelka4745/ShellClock/releases/latest) • [Сообщить об ошибке](https://github.com/adelka4745/ShellClock/issues) • [Исходный код](https://github.com/adelka4745/ShellClock)

</div>

---

## ✨ Почему ShellClock?

Устали от приложений с рекламой, которые тормозят и съедают батарею? **ShellClock** — это легковесное, полностью бесплатное и Open-Source приложение, созданное с любовью к чистоте кода и удобству.

Приложение разработано с использованием **Jetpack Compose** и придерживается принципов **Material Design 3**, обеспечивая невероятно плавный интерфейс.

---

## 🚀 Основные возможности

Приложение включает в себя четыре модуля, доступных через удобную нижнюю панель навигации:

| Модуль | Описание |
| :--- | :--- |
| 🕒 **Часы (Clock)** | Классическое отображение текущего времени. Идеально в качестве настольных часов. |
| 🌍 **Мировое время (World)** | Следите за временем в разных часовых поясах (более 100 городов мира). |
| ⏱️ **Секундомер (Stopwatch)** | Точный секундомер. Отлично подходит для тренировок или готовки. |
| ⌛ **Таймер (Timer)** | Настраиваемый таймер обратного отсчета с системным звуком уведомления. |

### 🎨 Кастомизация
*   **Темы оформления:** Светлая, Тёмная, OLED-чёрная и Авто (по системе).
*   **Эксклюзивные темы:** «Северное сияние» (глубокий синий) и «Закат» (тёплый оранжевый).
*   **Настройки:** Гибкий выбор формата времени (12/24ч), показ секунд и сортировка городов.
*   **Масштабирование:** Регулировка размера элементов интерфейса под ваш вкус.

---

## 📸 Скриншоты

*(Здесь вы можете добавить 2-3 красивых скриншота. Чтобы они отображались, загрузите картинки в папку `screenshots` в репозитории и напишите так:)*

<p align="center">
  <img src="screenshots/clock_screen.png" width="30%" />
  <img src="screenshots/world_screen.png" width="30%" />
  <img src="screenshots/settings_screen.png" width="30%" />
</p>

---

## 🛠 Технологический стек

*   **Язык:** Kotlin
*   **UI Framework:** Jetpack Compose (Modern Declarative UI)
*   **Design System:** Material Design 3 (включая Dynamic Colors для Android 12+)
*   **Архитектура:** Unidirectional Data Flow (UDF) / MVVM
*   **Управление состоянием:** Compose State (`remember`, `mutableStateOf`)
*   **Навигация:** Управление состоянием экранов (Scaffold + BottomBar)
*   **Сеть (Экран "Описание"):** `HttpURLConnection` + парсинг JSON (`org.json`) для получения списка релизов с GitHub API.

---

## 📦 Установка

### Для пользователей:
1. Перейдите в раздел **[Releases](https://github.com/adelka4745/ShellClock/releases)**.
2. Скачайте последний файл `.apk`.
3. Установите на устройство (разрешите установку из неизвестных источников).

### Для разработчиков:
1. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/adelka4745/ShellClock.git
