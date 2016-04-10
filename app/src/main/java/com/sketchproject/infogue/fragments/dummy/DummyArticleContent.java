package com.sketchproject.infogue.fragments.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class DummyArticleContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    public static List<DummyItem> generateDummy() {
        List<DummyItem> items = new ArrayList<>();
        int length = 20 + (int) Math.round(Math.random() * 1000);

        for (int i = 1; i <= length; i++) {
            items.add(createDummyItem(i));
        }

        return items;
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static DummyItem createDummyItem(int position) {
        return new DummyItem(
                String.valueOf(position),
                "Girl gone has rises the revenue up to $100 million dollar",
                "Last nigh Box Office release update about the new arrival film Girl Gone. They announced latest revenue for 3 weeks after the date release. At the firsr tumr this film looks like similar with the mainstream story about lost girl",
                "Entertainment",
                "27 January 2016",
                makeDetails(position)
        );
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public String id;
        public String title;
        public String content;
        public String date;
        public String category;
        public String details;

        public DummyItem(String id, String title, String content, String category, String date, String details) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.date = date;
            this.category = category;
            this.details = details;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
