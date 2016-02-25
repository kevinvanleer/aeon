package vanleer.android.aeon;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class GooglePlacesSearchUnitTest {
	@Test
	public void findType_simple() {
		GooglePlacesSearch testSearch = new GooglePlacesSearch(null, "","");
		GooglePlacesSearch.PrivateTests privateAccess = testSearch.new PrivateTests();

		String result = privateAccess.findType("cafe");
		assertEquals("cafe", result);
	}

	@Test
	public void findType_two_words() {
		GooglePlacesSearch testSearch = new GooglePlacesSearch(null, "","");
		GooglePlacesSearch.PrivateTests privateAccess = testSearch.new PrivateTests();

		String result = privateAccess.findType("gas station");
		assertEquals("gas_station", result);
	}

	@Test
	public void findType_plural() {
		GooglePlacesSearch testSearch = new GooglePlacesSearch(null, "","");
		GooglePlacesSearch.PrivateTests privateAccess = testSearch.new PrivateTests();

		String result = privateAccess.findType("cafes");
		assertEquals("cafe", result);
	}


	@Test
	public void findType_extra_words_end() {
		GooglePlacesSearch testSearch = new GooglePlacesSearch(null, "","");
		GooglePlacesSearch.PrivateTests privateAccess = testSearch.new PrivateTests();

		String result = privateAccess.findType("cafes st charles");
		assertEquals("cafe", result);
	}

    @Test
    public void remainingQuery_simple_none() {
        GooglePlacesSearch testSearch = new GooglePlacesSearch(null, "","");
        GooglePlacesSearch.PrivateTests privateAccess = testSearch.new PrivateTests();

        String remaining = privateAccess.remainingQuery("test_type", "test_type");
        assertEquals(null, remaining);
    }

    @Test
    public void remainingQuery_simple_all() {
        GooglePlacesSearch testSearch = new GooglePlacesSearch(null, "","");
        GooglePlacesSearch.PrivateTests privateAccess = testSearch.new PrivateTests();

        String remaining = privateAccess.remainingQuery(null, "test query");
        assertEquals("test query", remaining);

        remaining = privateAccess.remainingQuery("", "test query");
        assertEquals("test query", remaining);
    }

    @Test
    public void remainingQuery_trailing() {
        GooglePlacesSearch testSearch = new GooglePlacesSearch(null, "","");
        GooglePlacesSearch.PrivateTests privateAccess = testSearch.new PrivateTests();

        String remaining = privateAccess.remainingQuery("test_type", "test type place");
        assertEquals("place", remaining);
    }

    @Test
    public void remainingQuery_leading() {
        GooglePlacesSearch testSearch = new GooglePlacesSearch(null, "","");
        GooglePlacesSearch.PrivateTests privateAccess = testSearch.new PrivateTests();

        String remaining = privateAccess.remainingQuery("test_type", "place test type");
        assertEquals("place", remaining);
    }

    @Test
    public void remainingQuery_middle() {
        GooglePlacesSearch testSearch = new GooglePlacesSearch(null, "","");
        GooglePlacesSearch.PrivateTests privateAccess = testSearch.new PrivateTests();

        String remaining = privateAccess.remainingQuery("test_type", "some test type place");
        assertEquals("some place", remaining);
    }


    @Test
    public void remainingQuery_two_words() {
        GooglePlacesSearch testSearch = new GooglePlacesSearch(null, "","");
        GooglePlacesSearch.PrivateTests privateAccess = testSearch.new PrivateTests();

        String remaining = privateAccess.remainingQuery("test_type", "test type the place");
        assertEquals("the place", remaining);
    }
}