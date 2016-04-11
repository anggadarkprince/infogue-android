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
public class DummyFollowerContent {

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

    public static List<DummyItem> generateDummy(int offset) {
        List<DummyItem> items = new ArrayList<>();
        int index = offset * COUNT;

        for (int i = index; i < (index + COUNT); i++) {
            items.add(createDummyItem(i));
        }

        return items;
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static DummyItem createDummyItem(int position) {
        String[] usernames = {"user1", "user2", "user3", "user4", "user5"};
        String[] names = {"Reni Dwi Mulyani", "Imelda Dwi Agustine", "Angga Ari Wijaya", "Dhika Ageng", "Shangrilla Putri"};
        String[] locations = {"Gresik, Indonesia", "Jember, Indonesia", "Surabaya, Indonesia", "Jakarta, Indonesia", "Bandung, Indonesia"};
        int[] avatars = {R.drawable.dummy_avatar, R.drawable.dummy_featured_1, R.drawable.dummy_featured_2,
                R.drawable.dummy_featured_3, R.drawable.dummy_featured_4};

        return new DummyItem(
                String.valueOf(position),
                usernames[(int) Math.floor(Math.random() * usernames.length)],
                names[(int) Math.floor(Math.random() * names.length)],
                locations[(int) Math.floor(Math.random() * locations.length)],
                avatars[(int) Math.floor(Math.random() * avatars.length)],
                Math.random() < 0.5
        );
    }

    /**
     * A dummy item representing a piece of content.
     */
    public static class DummyItem {
        public final String id;
        public final String username;
        public final String name;
        public final String location;
        public final int avatar;
        public final boolean isFollowing;

        public DummyItem(String id, String username, String name, String location, int avatar, boolean isFollowing) {
            this.id = id;
            this.username = username;
            this.name = name;
            this.location = location;
            this.avatar = avatar;
            this.isFollowing = isFollowing;
        }

        @Override
        public String toString() {
            return username;
        }
    }
}
