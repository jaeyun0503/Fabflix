## CS 122B Project 2


Jae Yun Kim, Rong Bao

Link: https://youtu.be/dC8jXuvvAtE

Jae Yun Kim: Part 2, 3, 4
Rong Bao: Part 1, 2


LIKE/ILIKE predicate is used to search or browse movies, allowing users to search for movies using various conditions after logging in. It supports fuzzy matching and pagination.

In **MovieListServlet.java** :

&nbsp;&nbsp;&nbsp;&nbsp; **Initialize query string**

```java
String query =
                    "SELECT DISTINCT m.id, m.title, m.year, m.director, COALESCE(r.rating, 0) AS rating, m.price " +
                            "FROM movies AS m LEFT JOIN ratings r ON m.id = r.movieId " +
                            "LEFT JOIN stars_in_movies sim ON m.id = sim.movieId " +
                            "LEFT JOIN stars s on sim.starId = s.id " +
                            "WHERE 1=1 ";
```

&nbsp;&nbsp;&nbsp;&nbsp; Retrive id, title, year, director, price from **movies**. If the movie does not have a rating in **ratings**, then assign a default value of 0 to the rating column. Then left join **ratings**, **stars_in_movies**, **stars**.

&nbsp;&nbsp;&nbsp;&nbsp; (1)**Title/Director/Star Filter**. Suppose we want to search for movies whose titles are like $\%input\_string\%$
```java
if (title != null && !title.trim().isEmpty()) {
       query += "AND m.title LIKE ? ";
   }
   statement.setString(pos++, "%" + title + "%");
```

&nbsp;&nbsp;&nbsp;&nbsp; Same for searching movies by the directors, stars

&nbsp;&nbsp;&nbsp;&nbsp; (2)**Starting Letter Filter**. Matching movie titles starting with the specified letter
```java
if (letter != null && !letter.trim().isEmpty() && !letter.equals("*")) {
    query += "AND LOWER(m.title) LIKE ? ";
}
statement.setString(pos++, letter.toLowerCase() + "%");

```
&nbsp;&nbsp;&nbsp;&nbsp; For movies starting with non-alphanumeric characters, use this:


```java
if (letter.equals("*")) {
    query += "AND m.title REGEXP '^[^A-Za-z0-9]' ";
}
```


&nbsp;&nbsp;&nbsp;&nbsp; (3)**Genre filter**: Matching movies with the specified genre:


```java
if (genre != null && !genre.trim().isEmpty()) {
    query += "AND m.id IN (SELECT mg.movieId FROM genres_in_movies mg " +
            "JOIN genres g ON mg.genreId = g.id WHERE g.name = ?) ";
}
statement.setString(pos++, genre.trim());

```