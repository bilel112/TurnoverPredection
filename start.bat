@echo off

start cmd /k "cd /d C:\Users\bilel\Music\turnover\turnover\ml\api && ..\notebooks\.venv\Scripts\python.exe -m uvicorn main:app --host 127.0.0.1 --port 8001 --reload"

start cmd /k "cd /d C:\Users\bilel\Music\turnover\turnover\frontend && npm run dev"

start cmd /k "cd /d C:\Users\bilel\Music\turnover\turnover && .\mvnw.cmd spring-boot:run"

exit