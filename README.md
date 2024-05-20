# Game of Three - Coding Challenge

## Goal

The goal is to implement a game with two independent units – the players – communicating with each other using STOMP
over Websocket.

## Description

When a player starts, it generates a random whole number and sends it to the second player to start the game. The
receiving player can choose to add one of {-1, 0, 1} to the number to make it divisible by 3, then divides it by three.
The resulting whole number is then sent back to the original sender. This process repeats until one player reaches the
number 1 (after division).

Each move should generate sufficient output, including the number added and the resulting number. Both players should be
able to play automatically without user input, with an optional manual input mode.

## Solution Overview

The project consists of two separate applications: `GameServer` and `GameClient`.

### GameServer

- **Description**: A Spring Boot application using the Spring Boot WebSocket library. It provides a REST API to
  determine which player starts the game.
- **Technology**: Kotlin, Spring Boot, WebSocket
- **Communication**: Uses STOMP WebSocket for communication with `GameClient`.
- **REST API**: Provides an endpoint to determine the starting player.

### GameClient

- **Description**: A Spring Boot command-line application that accepts commands to start the game. It connects
  to `GameServer` via STOMP WebSocket.
- **Technology**: Kotlin, Spring Boot, WebSocket

## Project Structure

The root folder is `jet-game-of-three-challenge`, containing two sub-projects: `GameServer` and `GameClient`.

```plaintext
jet-game-of-three-challenge/
│
├── GameServer/
│ ├── src/
│ │ ├── main/
│ │ │ ├── kotlin/
│ │ │ └── resources/
│ ├── pom.xml
│ └── ...
│
├── GameClient/
│ ├── src/
│ │ ├── main/
│ │ │ ├── kotlin/
│ │ │ └── resources/
│ ├── pom.xml
│ └── ...
│
└── README.md
```

## How to Run

### Prerequisites

- JDK 17 or higher
- Maven

### Steps to Run

1. **Clone the Repository**
    ```bash
    git clone https://github.com/doguscanaktemur/jet-game-of-three-challenge.git
    cd jet-game-of-three-challenge
    ```

2. **Start GameServer**
    ```bash
    cd GameServer
    mvn spring-boot:run
    ```

3. **Start GameClient in Two Terminals**
    - **Terminal 1:**
      ```bash
      cd ../GameClient
      mvn spring-boot:run
      ```

    - **Terminal 2:**
      ```bash
      cd ../GameClient
      mvn spring-boot:run
      ```

### Configuration

- **GameServer**: Configuration is managed via `application.properties` in the `src/main/resources` directory.
- **GameClient**: Configuration is managed via `application.properties` in the `src/main/resources` directory.

### Commands for GameClient

- `manual`: Start the game in manual mode.
- `automatic`: Start the game in automatic mode.
- `exit`: Exit the application.

### Example of Game Flow

1. **Starting the Game**: One player starts and generates a random number.
2. **Sending the Number**: The number is sent to the second player.
3. **Processing the Number**: The second player adjusts the number to make it divisible by 3, divides it, and sends it
   back.
4. **Repeating the Process**: This continues until the number 1 is reached.

## Alternatives Considered

Several alternatives were considered for the implementation of this game:

1. **REST API for All Communication**: Initially, I considered using REST API calls for all interactions between the
   players. However, this approach would have required synchronous communication and potentially higher latency.
2. **Message Queue (e.g., RabbitMQ)**: Another alternative was to use a message queue for communication. While this
   would have provided reliable message delivery, it introduced additional complexity in managing the message broker.
3. **Direct Socket Communication**: Using raw socket communication was another option. This would have offered low-level
   control over the communication but at the cost of increased development complexity and error handling.

Ultimately, I chose STOMP WebSocket communication for its balance between simplicity and real-time interaction
capabilities.

## Architecture Diagrams

### Overall Architecture

```plaintext
+-----------------+                +-----------------+
|    GameClient   |                |    GameServer   |
|                 |                |                 |
|  - Generates    |                |  - Provides     |
|    Random Number|                |    REST API     |
|  - Sends Number | <------------> |  - Manages      |
|                 | WebSocket      |    WebSocket    |
|  - Receives     | Communication  |    Communication|
|    Number       |                |  - Determines   |
|  - Processes    |                |    Starting     |
|    Number       |                |    Player       |
|  - Sends Result |                |                 |
+-----------------+                +-----------------+
```

### Sequence Diagram

```plaintext
Client                          Server
  |                               |
  |-------(Generate Number)------>|
  |                               |
  |<-------(Initial Number)-------|
  |                               |
  |-------(Process Number)------->|
  |                               |
  |<------(Processed Number)------|
  |                               |
  |             ...               |
  |                               |
  |<------(Number = 1)------------|
  |                               |
```

## Platform Independence

- Both applications are platform-independent and can run on any environment with JVM and Maven support.
- Ensure that `GameServer` is running before starting `GameClient`.

## Future Enhancements

- Implement a more sophisticated UI for the frontend.
- Add more test cases to cover edge scenarios.
- Enhance error handling and logging.

---

Thank you for considering my submission!
