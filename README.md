# Todo App - Clean Java Backend Implementation

A clean, single-implementation Todo application with Java backend and HTML/CSS frontend. This project has been cleaned up to remove redundant code and provide a single, reliable way to run the application.

## Features

- **Java HTTP Server**: Professional Maven-based server that serves HTML/CSS and provides REST API endpoints
- **Persistent Storage**: JSON file-based storage with optional Firebase integration
- **Modern Web UI**: Responsive HTML/CSS interface with real-time updates
- **Cross-Platform**: Works on Windows, Linux, and Mac

## Quick Start

### Windows
```batch
start-server.bat
```

### Linux/Mac
```bash
chmod +x start-server.sh
./start-server.sh
```

### Manual Build & Run
```bash
mvn clean package
java -jar target/todo-java-1.0.0.jar
```

**Then open http://localhost:8080 in your browser.**

## Project Structure

```
.
├── src/main/java/com/todoapp/     # Java backend source code
│   ├── TodoServer.java            # Main HTTP server
│   ├── TodoBackend.java           # Business logic
│   ├── TaskService.java           # Task management
│   ├── FirebaseService.java       # Firebase integration
│   └── LocalStorageService.java   # File-based storage
├── src/main/resources/
│   └── firebase-config.properties # Firebase configuration template
├── index.html                     # Web application frontend
├── style.css                      # Application styling
├── pom.xml                        # Maven configuration
├── start-server.bat              # Windows startup script
└── start-server.sh               # Unix startup script
```

## How It Works

The application consists of:
1. **Frontend**: `index.html` + `style.css` - Modern web interface
2. **Backend**: Java HTTP server that provides REST API endpoints
3. **Storage**: Local JSON files with optional Firebase sync

## Application Features

- ✅ **Task Management**: Add, edit, delete, and organize tasks
- ✅ **Due Dates**: Set and track task deadlines
- ✅ **Filtering**: View all tasks, active only, or completed only
- ✅ **Search**: Find tasks quickly with real-time search
- ✅ **Persistence**: All data saved to local JSON files
- ✅ **Firebase Sync**: Optional cloud synchronization
- ✅ **Responsive UI**: Works on desktop and mobile browsers

## Firebase Integration (Optional)

To enable Firebase cloud sync:

1. **Create Firebase Project**:
   - Go to https://console.firebase.google.com
   - Create a new project
   - Enable Firestore Database

2. **Generate Service Account Key**:
   - Project Settings → Service Accounts
   - Click "Generate new private key"
   - Save the JSON file to your project directory

3. **Configure in Application**:
   - Start the server and open http://localhost:8080
   - Click "Sign in" button
   - Enter the path to your service account JSON file
   - Enter your user ID (any unique identifier)

## Requirements

- **Java 8+**: Required for building and running the server
- **Maven 3.3+**: For dependency management and building
- **Modern Web Browser**: Chrome, Firefox, Safari, or Edge

## API Endpoints

The Java server provides these REST API endpoints:

- `GET /api/tasks` - Get all tasks
- `POST /api/tasks/add` - Add new task
- `POST /api/tasks/update` - Update existing task
- `POST /api/tasks/delete` - Delete task
- `POST /api/tasks/clear` - Clear all completed tasks
- `POST /api/firebase/init` - Initialize Firebase connection
- `GET /api/status` - Get current sync status

## Data Storage

- **Local Files**: `data/tasks.json` (created automatically)
- **Firebase**: `users/{userId}/tasks` collection in Firestore
- **Backup**: Local files serve as backup when Firebase is unavailable

## What Was Cleaned Up

This project had significant redundancy that has been removed:

❌ **Removed Files**:
- `app.js` - Broken client-side Firebase implementation  
- `firebase.js` - Incomplete Firebase configuration
- `simple-server.py` - Redundant Python server
- `SimpleTodoServer.java` - Duplicate standalone Java server
- `serve.bat`, `run-simple.bat`, `start-todo.bat` - Redundant batch files
- `.vscode/` - Editor-specific configuration

✅ **Single Clean Implementation**:
- One Maven-based Java server with proper structure
- One HTML frontend that actually uses the server APIs
- Two startup scripts (Windows .bat + Unix .sh)
- Clear project organization
