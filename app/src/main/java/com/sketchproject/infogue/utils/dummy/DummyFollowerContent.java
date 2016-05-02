package com.sketchproject.infogue.utils.dummy;

import com.sketchproject.infogue.models.Contributor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 */
@SuppressWarnings("unused")
public class DummyFollowerContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Contributor> ITEMS = new ArrayList<>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Contributor> ITEM_MAP = new HashMap<>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    public static List<Contributor> generateDummy(int offset) {
        return generateDummy(offset, COUNT);
    }

    public static List<Contributor> generateDummy(int offset, int total) {
        List<Contributor> items = new ArrayList<>();
        int index = offset * total;

        for (int i = index; i < (index + total); i++) {
            items.add(createDummyItem(i));
        }

        return items;
    }

    private static void addItem(Contributor item) {
        ITEMS.add(item);
        ITEM_MAP.put(String.valueOf(item.getId()), item);
    }

    private static Contributor createDummyItem(int position) {
        String[] username = {"user1", "user2", "user3", "user4", "user5"};
        String[] name = {"Reni Dwi Mulyani", "Imelda Dwi Agustine", "Angga Ari Wijaya", "Dhika Ageng", "Shangrilla Putri"};
        String[] location = {"Gresik, Indonesia", "Jember, Indonesia", "Surabaya, Indonesia", "Jakarta, Indonesia", "Bandung, Indonesia"};
        String[] about = {
                "Lorem ipsum dolor sit amet, ipsum evertitur pro et. Nobis alterum detraxit pro te",
                "Eu ius lorem etiam consectetuer, eam dicam mucius assueverit ei",
                "Placerat principes te nam. Quidam ponderum pro ne, vel soluta essent ne. At pri tibique voluptaria, no lorem mundi sed",
                "Cum cu facilis abhorreant comprehensam, dicta prompta et eos"
        };

        Contributor contributor = new Contributor(position + 1, username[(int) Math.floor(Math.random() * username.length)]);
        contributor.setName(name[(int) Math.floor(Math.random() * name.length)]);
        contributor.setLocation(location[(int) Math.floor(Math.random() * location.length)]);
        contributor.setAbout(about[(int) Math.floor(Math.random() * about.length)]);
        contributor.setAvatar("http://infogue.id/images/contributors/avatar_" + (int) Math.ceil(Math.random() * 15) + ".jpg");
        contributor.setCover("http://infogue.id/images/covers/cover_" + (int) Math.ceil(Math.random() * 5) + ".jpg");
        contributor.setArticle((int) Math.round(Math.random() * 100));
        contributor.setFollowers((int) Math.round(Math.random() * 100));
        contributor.setFollowing((int) Math.round(Math.random() * 100));
        contributor.setIsFollowing(Math.random() < 0.5);

        return contributor;
    }
}
