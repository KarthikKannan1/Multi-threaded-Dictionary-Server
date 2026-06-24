# Multi-threaded Dictionary Server

A client-server dictionary application in Java that supports multiple concurrent clients performing word lookups and edits in real time. Built with raw sockets and threads, it uses reader-writer locking for safe concurrency and persists all data to disk so changes survive server restarts and crashes.

Developed for COMP90015 (Distributed Systems - Assignment 1), Master of Computer Science, University of Melbourne.

## Features

- **Five dictionary operations:** search a word's meaning(s), add a new word, remove a word, add a meaning to an existing word, and update an existing meaning.
- **Concurrent clients:** each client connects over TCP and runs on its own server-side thread, so operations don't queue behind one another.
- **Thread-safe access:** a `ReentrantReadWriteLock` allows unlimited simultaneous reads while serializing writes, so concurrent reads and writes on the dictionary never conflict.
- **Crash-resistant persistence:** the dictionary is loaded from a JSON file on startup and saved after every write, so data survives restarts or crashes.
- **JSON messaging protocol:** client and server exchange compact JSON requests and responses over the socket.
- **Two GUIs:** a server dashboard showing live status, active connection count, logs, and a manual start/stop control; and a client interface for performing all five operations.

## Architecture

The system follows a centralized client-server model with a **thread-per-connection** design:

- **`Dictionary.java`** — the shared data store (`HashMap<String, List<String>>`) guarded by a `ReentrantReadWriteLock`. Reads acquire the read lock; the four write operations acquire the write lock. Loads from and saves to `dictionary.json`.
- **`ClientHandler.java`** — runs per client on its own thread. Reads JSON requests from the socket, dispatches them to the shared `Dictionary`, and writes back JSON responses.
- **`DictionaryServerGUI.java`** — the server dashboard. Accepts incoming connections in a background thread, spawns a `ClientHandler` per client, and updates the live connection count and logs.
- **`DictionaryClientGUI.java`** — the user-facing client. Builds JSON requests for each operation and displays responses.
- **`dictionary.json`** — durable storage for all words and meanings.

A full design report with class and interaction diagrams and detailed design rationale is included: see [Design Report](report.pdf).

**Class diagram**

<img width="726" height="517" alt="image" src="https://github.com/user-attachments/assets/447ba432-1b4a-480c-a2b0-a711bd593481" />

**Interaction diagram**

<img width="721" height="482" alt="image" src="https://github.com/user-attachments/assets/cf679611-0de1-4c6a-b5cb-1889a08b68e4" />

## How to Run

Requires Java (JDK 11 or later recommended). The pre-built jars and a starter `dictionary.json` are included in the repo.

```bash
# Start the server: java -jar DictionaryServer.jar <port> <dictionary-file>
java -jar DictionaryServer.jar 3005 dictionary.json

# Start the client: java -jar DictionaryClient.jar <server-address> <port> <sleep-ms>
java -jar DictionaryClient.jar localhost 3005 1000
```

- `<port>` — port the server listens on (e.g. `3005`).
- `<dictionary-file>` — path to the JSON dictionary file.
- `<server-address>` — server host (`localhost` if running on the same machine).
- `<sleep-ms>` — simulated operation delay in milliseconds, used to make concurrent behaviour observable.

Start the server first, then launch one or more clients.

## Design Highlights

- **TCP over UDP** — chosen for guaranteed, ordered, reliable delivery without building a custom reliability layer, and for its persistent connection that suits a full client-server session.
- **JSON protocol** — compact, human-readable, and easy to parse with Java's `org.json` library; using the same format for both messaging and storage keeps the implementation consistent.
- **Reader-writer locking** — allows true concurrent reads for better performance under simultaneous lookups, with finer-grained control than blanket `synchronized` blocks. Locks are always released in a `finally` block.
- **Configurable delay** — a sleep argument simulates network latency, making it easy to verify that concurrent writes are correctly serialized.

## Tech Stack

Java · TCP Sockets · Multithreading · `ReentrantReadWriteLock` · JSON (`org.json`) · Swing

## License

Released under the [MIT License](LICENSE).

## Author

Karthik Kannan — Master of Computer Science, University of Melbourne.
