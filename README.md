# BrainDrop

AI-Powered Brain Dump Note App for Android.

## Setup

1. Clone the repo
2. Add API keys to GitHub Secrets:
   - `GROQ_API_KEY`
   - `GEMINI_API_KEY`
3. Add signing keystore to GitHub Secrets:
   - `KEY_STORE` (base64 encoded)
   - `KEY_ALIAS`
   - `KEY_PASSWORD`
   - `STORE_PASSWORD`
4. Push to `main` — GitHub Actions builds and releases the APK automatically

## Dev Workflow (Termux)

```bash
git clone https://github.com/omersusin/BrainDrop
cd BrainDrop
# Edit files with cat/mkdir — no text editor needed
git add . && git commit -m "update" && git push
```

## Architecture

- **UI**: Jetpack Compose with custom dark-first theme
- **Database**: ObjectBox with HNSW semantic search
- **AI**: Groq API (Llama 3.1 8B) + Gemini Flash API
- **ML**: TensorFlow Lite (all-MiniLM-L6-v2) + ML Kit OCR + whisper.cpp
- **Background**: WorkManager for AI processing and auto-delete

## License

MIT
