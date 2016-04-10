package com.sketchproject.infogue.fragments.dummy;

import com.sketchproject.infogue.R;

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
        String[] slug = {"article-slug-1", "article-slug-2", "article-slug-3", "article-slug-4", "article-slug-5"};
        String[] titles = {"Girl gone has rises the revenue up to $100 million dollar",
                "Indonesia for the first time become olympic host",
                "New technology from the future",
                "Running and healthy"};
        String[] content = {"Last nigh Box Office release update about the new arrival film Girl Gone. They announced latest revenue for 3 weeks after the date release. At the first turn this film looks like similar with the mainstream story about lost girl",
                "They announced latest revenue for 3 weeks after the date release. At the first turn this film looks like similar with the mainstream story about lost girl",
                "At the first turn this film looks like similar with the mainstream story about lost girl. They announced latest revenue for 3 weeks after the date release."
        };
        String[] categories = {"News",
                "Sport",
                "Entertainment",
                "Technology",
                "Science"};
        String[] months = {"January",
                "February",
                "March",
                "April",
                "May"};
        int[] featured = {R.drawable.dummy_featured_1, R.drawable.dummy_featured_2,
                R.drawable.dummy_featured_3, R.drawable.dummy_featured_4};

        return new DummyItem(
                String.valueOf(position),
                slug[(int) Math.floor(Math.random() * slug.length)],
                titles[(int) Math.floor(Math.random() * titles.length)],
                content[(int) Math.floor(Math.random() * content.length)],
                categories[(int) Math.floor(Math.random() * categories.length)],
                (int) Math.ceil(Math.random() * 31)+" "+months[(int) Math.floor(Math.random() * months.length)]+" 2016",
                featured[(int) Math.floor(Math.random() * featured.length)],
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
        public String slug;
        public String title;
        public String content;
        public String date;
        public String category;
        public String details;
        public int featured;

        public DummyItem(String id, String slug, String title, String content, String category, String date, int featured, String details) {
            this.id = id;
            this.slug = slug;
            this.title = title;
            this.content = content;
            this.date = date;
            this.category = category;
            this.details = details;
            this.featured = featured;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
