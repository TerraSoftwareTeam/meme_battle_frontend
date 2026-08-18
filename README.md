# 🃏 Meme Battle

<div align="right">
  <a href="README_RU.md">🇷🇺 Читать на русском</a>
</div>

**Meme Battle** is a multiplayer card game where the winner is the one who best matches memes to situations. Inspired by the concept of Cards Against Humanity, but built entirely around memes and user-generated content.

---

## 🎮 How It Works

Players gather in a lobby, pick card decks and start a match. Each round, a **situation** card appears on screen — a text or media prompt. Every player secretly picks a **meme** from their hand that they think fits the situation best. Once all cards are submitted, **voting** begins — players anonymously choose the best answer. The player whose card gets the most votes earns a point for the round. The one with the highest score across all rounds wins.

---

## ✨ Key Features

- **Two Game Modes**
  - *Situation → Meme* — pick the meme that fits the situation
  - *Meme → Situation* — the opposite: come up with a context for the meme

- **Custom Packs**
  Any player can create their own pack of memes or situations, upload images, set an age rating and language. Packs can be published for the whole community or kept private.

- **Lobby with Settings**
  Create a lobby with the desired number of rounds and hand size, pick packs — and wait for friends. Public lobbies are visible to anyone who wants to join.

- **Anonymous Voting**
  Cards during voting are not tied to player names — the outcome is decided by the quality of the meme, not by who submitted it.

- **Adaptive UI**
  The interface automatically adjusts to screen size: on desktop and tablet the Players and Info panels are always visible on the sides; on mobile they slide in on demand.

- **Cross-Platform**
  The app runs on **Android** and in the **browser** (WasmJS) from a single Kotlin Multiplatform codebase.

---

## 📸 Screenshots

### Gameplay — Web

| Card Selection | Voting | Round Results |
|:-:|:-:|:-:|
| ![Card Selection](screenshots/gameplay_web_big_1.png) | ![Voting](screenshots/gameplay_web_big_2.png) | ![Results](screenshots/gameplay_web_big_3.png) |

### Lobby & Waiting

| Waiting for Players (Web) | Lobby List (Android) |
|:-:|:-:|
| ![Lobby Web](screenshots/gameplay_expectation_web_big_1.png) | ![Lobby Android](screenshots/Lobby_phone_1.jpg) |

### Pack Catalogue

| Catalogue | Pack Details |
|:-:|:-:|
| ![Catalogue](screenshots/Catalogue_web_big_1.png) | ![Pack Details](screenshots/Catalogue_web_big_2.png) |

### Mobile Client

<div align="center">
  <img src="screenshots/Gameplay_phone_1.jpg" width="320" alt="Android Gameplay"/>
</div>

---

## 🛠 Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin Multiplatform |
| UI | Compose Multiplatform |
| Architecture | MVI (Decompose + MVIKotlin) |
| DI | Koin |
| Network | Ktor + WebSocket (Centrifugo) |
| Targets | Android, WasmJS |

---

## 🗂 Project Structure

```
MemeBattle/
├── app/                  # Android entry point
├── webApp/               # WasmJS entry point
├── shared/               # Shared DI and navigation
├── core/
│   ├── localization/     # String resources (EN / RU)
│   ├── ui/               # Shared UI components
│   └── ...
├── feature/
│   ├── home/             # Main screen and lobbies
│   ├── gameplay/         # Game flow
│   └── packs/            # Pack management
└── network/              # Network clients and DTOs
```

---

## 🚀 Running Web Version with Docker

Build and run the WasmJS web application in an Nginx container:

```bash
# Build and start via Docker Compose
docker compose up --build -d
```

The application will be accessible at `http://localhost:8080`.