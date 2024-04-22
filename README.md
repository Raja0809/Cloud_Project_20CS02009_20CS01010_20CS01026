# Team Members
Pakala Sreeevathsava Reddy(20cs01010)
Gujjula Raja Reddy(20cs01026)
Chukka Sai Satya Sumedha(20cs02009)
---

# Lamport Distributed Mutual Exclusion Algorithm

This project implements the Lamport distributed mutual exclusion algorithm in Java. The Lamport algorithm ensures that processes in a distributed system can access a shared resource without interference from other processes.

## Contents

1. [Files](#files)
2. [Usage](#usage)
3. [How it works](#how-it-works)
4. [Notes](#notes)

## Files

- **Main.java**: Contains the main entry point for the program. It parses command-line arguments, initializes the Lamport class, and starts the necessary threads.
- **Lamport.java**: Implements the Lamport algorithm. It includes methods for sending and receiving messages, maintaining a logical clock, handling the request queue, and printing the system configuration.
- **SyncData.java**: Represents the data structure for synchronization messages.

## Usage

To run the program, compile all Java files and execute `Main` with the following command:

```
javac *.java
java Main -p <PORT NO> -i <SYS ID> -f <CONFIG FILE>
```

- Replace `<PORT NO>` with the port number to listen on.
- Replace `<SYS ID>` with the system ID of the current process.
- Replace `<CONFIG FILE>` with the path to the configuration file containing the list of system IDs and corresponding IP addresses.

## How it works

1. The program starts by parsing command-line arguments to obtain the port number, system ID, and configuration file.
2. It initializes the Lamport class with the provided parameters and loads the configuration file to create a list of nodes.
3. Three threads are started:
   - **Listener thread**: Listens for incoming messages on the specified port and handles them accordingly.
   - **Queue handler thread**: Manages the request queue and enters the critical section when necessary.
   - **Input handler thread**: Allows the user to input commands such as REQUEST, EXIT, and STATUS.
4. The Lamport algorithm is used to ensure mutual exclusion among processes accessing the critical section.
5. The program prints the system configuration upon receiving the STATUS command.

## Notes

- The Lamport algorithm relies on logical clocks to order events in a distributed system.
- Each process maintains a request queue and a set of replies to determine when it can enter the critical section.
- The program simulates the critical section with a sleep function for demonstration purposes. In a real application, this would be replaced with the actual critical section code.
- Make sure to provide a valid configuration file with system IDs and corresponding IP addresses for the algorithm to work correctly.

---
