package ru.kluchikhin.testproject;

import java.util.List;

public class Category {
    private int _id;
    private int id;
    private String title;
    private List<Category> subs;

    public Category(int _id, int id, String title) {
        this._id = _id;
        this.id = id;
        this.title = title;
    }

    public int get_Id() { return _id; }

    public int getId() { return id; }

    public String getTitle() {
        return title;
    }

    public List<Category> getSubs() {
        return subs;
    }

    @Override
    public String toString() {
        return title;
    }
}
