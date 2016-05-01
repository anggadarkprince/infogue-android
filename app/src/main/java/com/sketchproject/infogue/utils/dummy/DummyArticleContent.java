package com.sketchproject.infogue.utils.dummy;

import com.sketchproject.infogue.models.Article;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 */
public class DummyArticleContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Article> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Article> ITEM_MAP = new HashMap<>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    public static List<Article> generateDummy(int offset) {
        return generateDummy(offset, COUNT);
    }

    public static List<Article> generateDummy(int offset, int total) {
        List<Article> items = new ArrayList<>();
        int index = offset * total;

        for (int i = index; i < (index + total); i++) {
            items.add(createDummyItem(i));
        }

        return items;
    }

    private static void addItem(Article item) {
        ITEMS.add(item);
        ITEM_MAP.put(String.valueOf(item.getId()), item);
    }

    private static Article createDummyItem(int position) {
        String[] slug = {"article-slug-1", "article-slug-2", "article-slug-3", "article-slug-4", "article-slug-5"};
        String[] titles = {
                "Girl gone has rises the revenue up to $100 million dollar, bring new competitors",
                "Indonesia for the first time become olympic host now and later",
                "New technology from the future more intuitive more elegant",
                "Running and healthy build your body perfectly"};
        String[] content = {
                "Last nigh Box Office release update about the new arrival film Girl Gone. They announced latest revenue for 3 weeks after the date release. At the first turn this film looks like similar with the mainstream story about lost girl",
                "They announced latest revenue for 3 weeks after the date release. At the first turn this film looks like similar with the mainstream story about lost girl",
                "At the first turn this film looks like similar with the mainstream story about lost girl. They announced latest revenue for 3 weeks after the date release."
        };
        String[] categories = {"News", "Sport", "Entertainment", "Technology", "Science"};
        String[] subcategories = {"Government", "Lifestyle", "Soccer", "Research", "Relationship"};
        String[] months = {"January", "February", "March", "April", "May", "June"};
        List<String> tags = new ArrayList<>();
        tags.add("Hello");
        tags.add("New");
        tags.add("Trending");
        tags.add("2016");
        tags.add("Vacation");
        tags.add("New President");
        tags.add("Anime Spring");
        String[] statuses = {Article.STATUS_PENDING, Article.STATUS_PUBLISHED, Article.STATUS_DRAFT, Article.STATUS_UPDATED, Article.STATUS_REJECTED};

        Article article = new Article(position + 1, slug[(int) Math.floor(Math.random() * slug.length)], titles[(int) Math.floor(Math.random() * titles.length)]);
        article.setFeatured("http://infogue.id/images/featured/featured_" + (int) Math.ceil(Math.random() * 25) + ".jpg");
        article.setCategory(categories[(int) Math.floor(Math.random() * categories.length)]);
        article.setSubcategory(subcategories[(int) Math.floor(Math.random() * subcategories.length)]);
        article.setContent(content[(int) Math.floor(Math.random() * content.length)]);
        article.setPublishedAt((int) Math.ceil(Math.random() * 31) + " " + months[(int) Math.floor(Math.random() * months.length)] + " 2016");
        article.setTags(tags);
        article.setView((int) Math.round(Math.random() * 1000));
        article.setRating((int) Math.ceil(Math.random() * 5));
        article.setStatus(statuses[(int) Math.floor(Math.random() * statuses.length)]);
        return article;
    }
}
