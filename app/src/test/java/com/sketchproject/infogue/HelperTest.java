package com.sketchproject.infogue;

import com.sketchproject.infogue.utils.APIBuilder;
import com.sketchproject.infogue.utils.Helper;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Sketch Project Studio
 * Created by Angga on 02/05/2016 15.03.
 */
public class HelperTest {
    String base_url = APIBuilder.BASE_URL;
    String base_url_api = APIBuilder.BASE_URL_API;

    @Test
    public void slugTest() throws Exception {
        assertEquals("the-beautiful-day-in-1992", Helper.createSlug("The beautiful day in 1992"));
        assertEquals("super-massive-black-hole-o-creaz-mo-on-july", Helper.createSlug("Super massive black hole O'creaz MO on July"));
        assertEquals("angga-ari", Helper.createSlug("!@#$%^&*()=|}{[]';/.,`~angga%&*%*ari"));
    }

    @Test
    public void urlFeaturedTest() throws Exception {
        assertEquals(base_url_api + "featured/headline", APIBuilder.getApiFeaturedUrl("headline", 0));
        assertEquals(base_url_api + "featured/headline?page=4", APIBuilder.getApiFeaturedUrl("headline", 4));
        assertEquals(base_url_api + "featured/latest?page=4", APIBuilder.getApiFeaturedUrl("latest", 4));
        assertEquals(base_url_api + "featured/trending?page=2", APIBuilder.getApiFeaturedUrl("trending", 2));
        assertEquals(base_url_api + "featured/popular?page=7", APIBuilder.getApiFeaturedUrl("popular", 7));
        assertEquals(base_url_api + "featured/random", APIBuilder.getApiFeaturedUrl("random", -3));
    }

    @Test
    public void urlCategoryTest() throws Exception {
        assertEquals(base_url_api + "category/entertainment",
                APIBuilder.getApiCategoryUrl(Helper.createSlug("Entertainment"), null, 0));
        assertEquals(base_url_api + "category/sport?page=3",
                APIBuilder.getApiCategoryUrl(Helper.createSlug("SpoRt"), null, 3));
        assertEquals(base_url_api + "category/technology/network-computer?page=2",
                APIBuilder.getApiCategoryUrl(Helper.createSlug("TechnOlOgY"), Helper.createSlug("Network Computer"), 2));
    }

    @Test
    public void urlTagTest() throws Exception {
        assertEquals(base_url_api + "tag/new-year", APIBuilder.getApiTagUrl(Helper.createSlug("New Year")));
        assertEquals(base_url_api + "tag/spirit", APIBuilder.getApiTagUrl(Helper.createSlug("spirit")));
    }

    @Test
    public void urlFollowerTest() throws Exception {
        assertEquals(base_url_api + "contributor/anggadarkprince/followers",
                APIBuilder.getApiFollowerUrl("Followers", 0, "anggadarkprince"));
        assertEquals(base_url_api + "contributor/anggadarkprince/following",
                APIBuilder.getApiFollowerUrl("Following", 0, "anggadarkprince"));
        assertEquals(base_url_api + "contributor/angga_nitsfil/followers?contributor_id=1",
                APIBuilder.getApiFollowerUrl("Followers", 1, "angga_nitsfil"));
        assertEquals(base_url_api + "contributor/angga_nitsfil/following?contributor_id=3",
                APIBuilder.getApiFollowerUrl("Following", 3, "angga_nitsfil"));
    }

    @Test
    public void urlArticleTest() throws Exception {
        assertEquals(base_url_api + "contributor/anggadarkprince/article?contributor_id=5&my_article=false",
                APIBuilder.getApiArticleUrl(5, "anggadarkprince", false, null));
        assertEquals(base_url_api + "contributor/anggadarkprince/article?contributor_id=2&my_article=true",
                APIBuilder.getApiArticleUrl(2, "anggadarkprince", true, null));
        assertEquals(base_url_api + "contributor/anggadarkprince/article?contributor_id=2&my_article=true&query=gresik",
                APIBuilder.getApiArticleUrl(2, "anggadarkprince", true, "gresik"));
    }

    @Test
    public void urlContributorTest() throws Exception {
        assertEquals(base_url_api + "contributor/anggadarkprince", APIBuilder.getApiContributorUrl("anggadarkprince"));
    }

    @Test
    public void urlPostTest() throws Exception {
        assertEquals(base_url_api + "article/brand-new-day", APIBuilder.getApiPostUrl(Helper.createSlug("Brand New Day")));
        assertEquals(base_url_api + "article/new-world?contributor_id=3", APIBuilder.getApiPostUrl(Helper.createSlug("new world"), 3));
    }

    @Test
    public void urlSearchTest() throws Exception {
        assertEquals(base_url_api + "search?query=new post", APIBuilder.getApiSearchUrl("new post", APIBuilder.SEARCH_BOTH, 0));
        assertEquals(base_url_api + "search?query=new post&contributor_id=4", APIBuilder.getApiSearchUrl("new post", APIBuilder.SEARCH_BOTH, 4));
        assertEquals(base_url_api + "search/article?query=new post", APIBuilder.getApiSearchUrl("new post", APIBuilder.SEARCH_ARTICLE, 0));
        assertEquals(base_url_api + "search/contributor?query=angga", APIBuilder.getApiSearchUrl("angga", APIBuilder.SEARCH_CONTRIBUTOR, 0));
    }

    @Test
    public void urlCommentTest() throws Exception {
        assertEquals(base_url_api + "article/new-post/comment", APIBuilder.getApiCommentUrl(Helper.createSlug("New Post")));
    }

    @Test
    public void urlResendEmailConfirmationTest() throws Exception {
        assertEquals(base_url + "auth/resend/ugdfist392463586eifttf8", APIBuilder.getResendEmail("ugdfist392463586eifttf8"));
    }

    @Test
    public void urlContributorDetailTest() throws Exception {
        assertEquals(base_url + "contributor/anggadarkprince/detail", APIBuilder.getContributorDetailUrl("anggadarkprince"));
    }

    @Test
    public void urlConversationTest() throws Exception {
        assertEquals(base_url + "account/message/conversation/anggadarkprince", APIBuilder.getContributorConversationUrl("anggadarkprince"));
    }

    @Test
    public void urlBrowseArticleTest() throws Exception {
        assertEquals(base_url + "brand-new-world", APIBuilder.getArticleUrl(Helper.createSlug("Brand New World")));
    }

    @Test
    public void urlShareArticleTest() throws Exception {
        assertEquals("Hey checkout infogue.id article "+base_url + "brand-new-world via infogue.id",
                APIBuilder.getShareArticleText(Helper.createSlug("Brand New World")));
    }

    @Test
    public void urlShareContributorTest() throws Exception {
        assertEquals("Hey checkout infogue.id contributor "+base_url + "contributor/anggadarkprince via infogue.id",
                APIBuilder.getShareContributorText("anggadarkprince"));
    }

    @Test
    public void urlDisqusTest() throws Exception {
        assertEquals(APIBuilder.URL_DISQUS_TEMPLATE+"?shortname=info-gue&identifier=1&url="+base_url+"brand-new-world&title=Brand New World",
                APIBuilder.getDisqusUrl(1, "brand-new-world", "Brand New World", "info-gue"));
    }
}