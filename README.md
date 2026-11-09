# Pink Door — Frontend (Vue 3 + Vite)

A pink romantic website for my girlfriend. Behind this little pink door,
there's a place just for the two of us: an AI assistant, a few couple
games, and a photo album — because she loves taking photos.

Project name: **Pink Door** (粉色小门)

## 1:1 port of the original login page

`src/views/Login.vue` is a 1:1 port of the original single-file login
page (`deng-lu-ye-mian.html`): same Chinese copy, same layout, same
pink colours and fonts, same falling petals, same illustration
(`src/assets/hero.webp`, extracted from the original page), and the
same login behaviour. The UI text is in Chinese; the project name,
code comments and this README stay in English.

- Left: "欢迎回家，我的小公主", "这扇粉色小门后面，藏着一个只属于我们两个人的地方",
  hero badge "今天也超级喜欢你", and the three feature cards
  (AI 小助手 / 情侣小游戏 / 我们的相册) with their original descriptions.

## Deployment note (China-accessible hosting)

Fonts are self-hosted via `@fontsource/quicksand` and
`@fontsource/zcool-kuaile` (bundled at build time, no Google Fonts
requests), and the hero illustration is inlined as a base64 data URI
in `src/assets/hero.js`, so this repo is text-only and can be pushed
with the GitHub API. Deploy from this repo with build command
`npm run build` and output directory `dist`.
- Right: login card "推开这扇粉色小门", fields 你的昵称 / 我们的暗号
  (password, with show/hide eye), button "带着爱心进去", and the
  success state "欢迎回来，宝贝" with "进入我们的首页".
- No registration — this is a private site for one person, and her
  account is preset in the backend.
- The backend note is kept verbatim: `POST /api/auth/login`,
  `{ "username": "昵称", "password": "暗号" }`,
  `→ 200 { "token": "JWT", "nickname": "宝贝" }`,
  `→ 401 { "message": "暗号不对哦" }`.

## What's inside

- `src/views/Login.vue` — the login page (the main page), ported 1:1
  from the original HTML login page.
- `src/views/Home.vue` — the home page after login, in the same
  Chinese pink style, with cards for AI 小助手, 情侣小游戏 and
  我们的相册 (marked 即将上线 for now).
- `src/components/CuteIllustration.vue` — the original illustration,
  `src/assets/hero.webp` from the original login page.
- `src/components/Petals.vue` — falling cherry-blossom petals,
  same colours and motion as the original page.
- `src/api/auth.js` — the auth API layer for the Java backend.
- `src/router/index.js` — vue-router, with a guard so `/home`
  requires a login token.

## How to run

```bash
npm install
npm run dev
```

Then open http://localhost:5173

Build for production:

```bash
npm run build
npm run preview
```

## Demo login (no backend needed)

The demo login works without any backend: enter any nickname and any
secret code of at least 4 characters, and after about 0.9 seconds
you will see "欢迎回来，宝贝" and can enter the home page.

## How to connect the Java backend

1. Copy `.env.example` to `.env` and set your backend URL:
   ```
   VITE_API_BASE_URL=http://localhost:8080
   VITE_USE_MOCK_AUTH=false
   ```
2. Start the Java (Spring Boot) backend on port 8080.
3. It must implement:
   ```
   POST /api/auth/login
   Request:  { "username": "昵称", "password": "暗号" }
   Response: { "token": "JWT", "nickname": "宝贝" }
   Error:    401 { "message": "暗号不对哦" }
   ```
   The account is preset in the backend database — there is no
   registration endpoint. Hash the password on the backend (BCrypt).
   The frontend never stores the password.
4. If the backend cannot be reached, the app falls back to the demo
   login during development (see the marked mock in `src/api/auth.js`).

## Tech stack

- Vue 3, vue-router 4, Vite 5
- No UI library — plain CSS, pink romantic theme
- Backend (separate, by the owner): Java Spring Boot + MySQL for the
  main backend, Python FastAPI + LangChain for the AI assistant
  microservice (see `../docs/api.md` in the project root).
