# ⚡ Fuzzy Finder FX

![Java](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![JavaFX](https://img.shields.io/badge/JavaFX-Native%20UI-blue?style=for-the-badge)
![Performance](https://img.shields.io/badge/Performance-Zero%20Allocation-brightgreen?style=for-the-badge)
A **blazing fast**, zero-allocation fuzzy search engine for Java, strictly ported from the core of `fzf`.

Designed to bridge the gap between **High-Performance Backend** and **Rich Frontend**. It filters 100,000+ files in real-time and renders selected files with **VS Code-style Syntax Highlighting** inside a fully themable UI.

---

## 🎮 The Interface

![Fuzzy Finder Demo](https://raw.githubusercontent.com/MouadBensafir/Fuzzy-Finder/refs/heads/main/tutorial.gif)
*(Split view: Real-time search on the left, Rich syntax highlighting on the right. Toggles seamlessly between Dark and Light modes.)*

---

## 🚀 Why This?

Most file searchers are fast *or* pretty. We chose **both**.

* **⚡ Zero Allocation Search:** Uses a `ThreadLocal` **Slab Allocator**. The search loop allocates **0 bytes** of memory per file. No GC pauses.
* **🧠 Smart Ranking:** Implements the **Smith-Waterman** algorithm (V2). It rewards CamelCase (`cC` matches `CamelCase`) and file boundaries.
* **🌗 Dark & Light Themes:** Built-in theme engine. The UI and the Code Preview (WebView) adapt instantly to your preferred mode (Solarized Light / Dracula Dark).
* **🎨 Rich Code Preview:** We embed a **JavaFX WebView** to render code with full syntax coloring (Java, Python, C++, etc.) .

---

## 🏗️ The Architecture

We use a **Hybrid Pipeline** to ensure the app stays responsive even when handling heavy data.

### 1. The "Slab" Engine (Backend) 🛡️
Instead of asking Java to create new arrays for every file search (which kills performance), we allocate a single "Slab" of memory per thread at startup.
* **File 1:** Borrow Slab -> Write Matrix -> Calculate Score -> Reset.
* **File 2:** Borrow Slab -> Write Matrix -> Calculate Score -> Reset.

### 2. The Hybrid Renderer (Frontend) 🎨
* **List View:** Uses native JavaFX virtualization for raw scrolling speed.
* **Code View:** Uses an embedded `WebView`. When you switch themes, we inject dynamic CSS into the WebView to change the syntax highlighting colors on the fly without reloading the file.

---

## 📋 Prerequisites

* **Java JDK 17** or higher.
* **Maven 3.8+**.
* **JavaFX SDK** (Ensure the `javafx-web` module is included).

---

## 📦 Installation

Clone the repository and import it into your IDE:

```bash
git clone [https://github.com/MouadBensafir/Fuzzy-Finder.git](https://github.com/MouadBensafir/Fuzzy-Finder.git)
cd Fuzzy-Finder
mvn javafx:run
```