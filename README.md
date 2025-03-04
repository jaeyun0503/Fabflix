## CS 122B Project 4

Jae Yun Kim

Link: https://youtu.be/KJpzMaGCn90

##### Connection Pooling
- Files that are involved:
  - WebContent/META-INF/context.xml
  - src/AddEntryServlet.java
  - src/AutocompleteServlet.java
  - src/CartServlet.java
  - src/Functions.java
  - src/GenreServlet.java
  - src/LoginServlet.java
  - src/MetadataServlet.java
  - src/MovieListServlet.java
  - src/PurchaseServlet.java
  - src/EmployeeLoginServlet.java
  - src/SingleMovieServlet.java
  - src/SingleStarServlet.java
  - src/StarsServlet
  - src/UpdateSecurePassword.java

- Usage of Connection Pooling
  - I used JDBC connection pooling to optimize the database connection.
  - In the `context.xml` file, I established two JDBC datasources, ReadWrite and ReadOnly. Both have limits of 100 for active connections, 30 for idle connections, and a timeout threshold of 10,000 milliseconds for connection establishment.
    - ReadWrite: Used for queries that modify the data
    - ReadOnly: Used for queries that read the data but not modify
  - Established connections to Master/Slave's database so it works with the backend SQL
##### Fuzzy Search
- Levenshtein Edit Distance Algorithm is implemented with FLAMINGO Toolkit. I used the `ed` function for calculating the edit distance between two strings. In AutoCompleteServlet.java and MovieListServlet.java, I set the edit distance threshold based on the length of the query.
  - Length of title < 4 characters, then maximum edit distance allowed is 1.
  - Length of title < 7, maximum edit distance of 2 is permitted.
  - Else 3.
- Combined full-text search with fuzzy search using an OR query. 
##### Master/Slave
- Filenames with Routing Queries
  - WebContent/META-INF/context.xml
  - src/AddRecordServlet.java
  - src/AutocompleteServlet.java
  - src/EmployeeLoginServlet.java
  - src/LoginServlet.java
  - src/MetadataServlet.java
  - src/MovieListServlet.java
  - src/PurchaseServlet.java
  - src/SingleMovieServlet.java
  - src/SingleStarServlet.java
  - src/StarsServlet.java
  - src/UpdateSecurePassword.java
- When data is written, its directed to the master database, which is then reflected in the slave's database. ReadOnly is directed to the slave database.
