package com.bluepath.app.model;

public class CareerItem {
    public final String id;
    public final String title;
    public final String field;
    public final String description;
    public final String[] ncsUnits;
    public final String[] workplaces;
    public final String recommendedTier;

    public CareerItem(String id, String title, String field, String description, String[] ncsUnits,
                      String[] workplaces, String recommendedTier) {
        this.id = id;
        this.title = title;
        this.field = field;
        this.description = description;
        this.ncsUnits = ncsUnits;
        this.workplaces = workplaces;
        this.recommendedTier = recommendedTier;
    }
}
