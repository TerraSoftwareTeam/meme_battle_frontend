import os

# Directories to ignore
IGNORE_DIRS = {'.git', 'build', '.gradle', 'iosApp.xcodeproj', 'gradle'}

def process_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
    except UnicodeDecodeError:
        # Skip binary files
        return

    original_content = content
    
    # Do not replace UIKit if it's the Apple framework, but our target is 'uikit' and 'UIkit'
    # For package names:
    content = content.replace("com.dev.uikit", "com.dev.memebattle")
    
    # For general terms (case sensitive):
    content = content.replace("uikit", "memebattle")
    content = content.replace("UIkit", "MemeBattle")
    content = content.replace("UIKIT", "MEMEBATTLE")
    
    if content != original_content:
        with open(filepath, 'w', encoding='utf-8') as f:
            f.write(content)
        print(f"Updated {filepath}")

for root, dirs, files in os.walk('.'):
    # Modify dirs in-place to skip ignored directories
    dirs[:] = [d for d in dirs if d not in IGNORE_DIRS and not d.startswith('build')]
    
    for file in files:
        if file == 'refactor.py':
            continue
        filepath = os.path.join(root, file)
        process_file(filepath)
