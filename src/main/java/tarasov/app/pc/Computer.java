package tarasov.app.pc;

public class Computer {
    private String name;
    private String specifications;
    private int id;

    public Computer(int id, String name, String specifications) {
        this.name = name;
        this.specifications = specifications;
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSpecifications() {
        return specifications;
    }
}
