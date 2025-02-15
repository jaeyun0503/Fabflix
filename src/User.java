public class User {

    private final String username;
    private int id;

    private String title;
    private String year;
    private String director;
    private String star;
    private int page = 1;
    private int resultsPerPage = 25;
    private String sortBy = "ratinghltitlelh";

    private String genre;

    private String letter;

    public User(String username) {
        this.username = username;
    }
    public String getUsername() {
        return username;
    }

    public int getId() { return id; }

    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getDirector() { return director; }
    public void setDirector(String director) { this.director = director; }

    public String getStar() { return star; }
    public void setStar(String star) { this.star = star; }

    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }

    public int getResultsPerPage() { return resultsPerPage; }
    public void setResultsPerPage(int resultsPerPage) { this.resultsPerPage = resultsPerPage; }

    public String getSortBy() { return sortBy; }
    public void setSortBy(String sortBy) { this.sortBy = sortBy; }

    public String getGenre() { return genre;}
    public void setGenre(String genre) {this.genre = genre;}

    public String getLetter() { return letter;}
    public void setLetter(String letter) {this.letter = letter;}

}
