package vanleer.android.aeon;

import org.junit.Test;

import vanleer.android.aeon.GooglePlacesSearch;

import static org.junit.Assert.*;

/**
 * To work on unit tests, switch the Test Artifact in the Build Variants view.
 */
public class ExampleUnitTest {
	@Test
	public void addition_isCorrect() throws Exception {
		assertEquals(4, 2 + 2);
	}

	@Test
	public void _findType_simple() {
		GooglePlacesSearch testSearch = new GooglePlacesSearch(null, "","");
		GooglePlacesSearch.PrivateTests privateAccess = testSearch.new PrivateTests();

		String result = privateAccess._findType("cafe");
		assertEquals(result, "cafe");
	}

	@Test
	public void findType_simple() {
		GooglePlacesSearch testSearch = new GooglePlacesSearch(null, "","");
		GooglePlacesSearch.PrivateTests privateAccess = testSearch.new PrivateTests();

		android.util.Pair<String,String> result = privateAccess.findType("cafe");
		assertEquals("cafe", result.first);
		assertEquals(null, result.second);
	}
}