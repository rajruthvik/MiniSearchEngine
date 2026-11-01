# 🔍 Mini Search Engine

A lightweight, keyword-based search engine built using **Java, Docker, and a frontend-backend architecture**. The application allows users to search through multiple text documents efficiently using an inverted indexing mechanism powered by Java HashMaps.

The project demonstrates fundamental concepts of information retrieval, data structures, backend development, and containerization.

## 🚀 Features

- **Inverted Indexing:** Builds an efficient word-to-document mapping using nested HashMaps.
- **Keyword Search:** Searches individual keywords across indexed text documents.
- **Multi-Keyword Search:** Supports AND-based search to find documents containing all specified keywords.
- **Term Frequency Ranking:** Ranks search results based on how frequently a keyword appears in each document.
- **Text Processing:** Performs lowercase normalization, punctuation removal, and tokenization.
- **Dockerized Deployment:** Uses Docker to containerize the application and simplify deployment.
- **Frontend-Backend Architecture:** Separates the user interface from the search processing logic.

## 🛠️ Tech Stack

| Component | Technology |
|---|---|
| Backend | Java |
| Data Structures | HashMap, HashSet, ArrayList |
| Search Algorithm | Inverted Indexing |
| Frontend | [Your frontend technology] |
| Containerization | Docker |
| File Processing | Java I/O, BufferedReader |

## ⚙️ How It Works

The search engine uses an **Inverted Index** to efficiently retrieve documents containing specific keywords.

Instead of scanning every document whenever a user searches, the application creates a searchable index during initialization.

**Index Structure:**

`Word → (File Name → Frequency)`

Example:

```text
{
    "java": {
        "document1.txt": 5,
        "document2.txt": 2
    },
    "python": {
        "document1.txt": 3,
        "document3.txt": 4
    }
}
```

### Search Operations

1. **Indexing:** Reads all `.txt` files from the specified directory and extracts individual words.
2. **Single Keyword Search:** Retrieves documents containing the searched keyword and ranks them by term frequency.
3. **Multiple Keyword Search:** Uses set intersection to identify documents containing every requested keyword.
4. **Result Ranking:** Sorts matching documents based on keyword frequency.

## 📊 Algorithmic Complexity

| Operation | Time Complexity |
|---|---|
| Building the Index | O(T) average |
| Single Keyword Lookup | O(1) average |
| Sorting Search Results | O(K log K) |
| Multi-Keyword Search | O(M × K) average |

Where:
- T = Total number of words across all documents.
- K = Number of matching documents.
- M = Number of search keywords.

## 🐳 Docker Deployment

The application is containerized using Docker to provide a consistent runtime environment and simplify deployment.

```bash
# Build Docker image
docker build -t mini-search-engine .

# Run the container
docker run -p 8080:8080 mini-search-engine
```

*Adjust the port according to your application's actual configuration.*

## 📁 Project Structure

```text
MiniSearchEngine/
│
├── backend/
│   ├── src/
│   └── Dockerfile
│
├── frontend/
│   └── ...
│
├── data/
│   ├── document1.txt
│   ├── document2.txt
│   └── document3.txt
│
├── docker-compose.yml
└── README.md
```

## 🎯 Learning Outcomes

- Understanding and implementing inverted indexing.
- Applying HashMaps and HashSets to optimize search operations.
- Working with Java file handling and text processing.
- Understanding frontend-backend communication.
- Containerizing applications using Docker.
- Analyzing time complexity and optimizing data retrieval.

## 🔮 Future Improvements

- Implement TF-IDF-based document relevance scoring.
- Add partial keyword and phrase-based search.
- Introduce stop-word removal and stemming.
- Support PDF and other document formats.
- Implement persistent indexing to avoid rebuilding the index on every restart.
- Improve search relevance using more advanced ranking algorithms.
