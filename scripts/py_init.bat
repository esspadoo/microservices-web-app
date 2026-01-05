@echo off
SET SCRIPT_DIR=%~dp0
SET VENV_DIR=%SCRIPT_DIR%venv

IF NOT EXIST "%VENV_DIR%" (
    python -m venv "%VENV_DIR%"
)

call "%VENV_DIR%\Scripts\activate.bat"

pip install --upgrade pip
IF EXIST "%SCRIPT_DIR%\requirements.txt" (
    pip install -r "%SCRIPT_DIR%\requirements.txt"
)

python "%SCRIPT_DIR%\owi_filter.py"
python "%SCRIPT_DIR%\owi_import.py"
