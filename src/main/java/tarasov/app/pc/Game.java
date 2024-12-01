package tarasov.app.pc;

public class Game {
    private String name;
    private String description;
    private String genre;
    private int genre_id;
    private int id;

    public Game(int id, String name, String description, int genre_id, String genre) {
        this.name = name;
        this.description = description;
        this.genre = genre;
        this.genre_id = genre_id;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getGenre() {
        return genre;
    }

    public int getGenre_id() {
        return genre_id;
    }

    public int getId() {
        return id;
    }
}
