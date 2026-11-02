package net.yura.domination.mapstore;

import java.util.ArrayList;
import java.util.List;
import junit.framework.TestCase;
import net.yura.domination.test.TestUtil;

/**
 * WARNING!!! this is not technically a UI test, but its grouped with the UI tests
 * as it seems to require me4se UI libs to run
 *
 * @author yura mamyrin
 */
public class MapUpdateServiceTest extends TestCase {

    /*
     * possible things to test:
     * 
     *  downloading a new map
     *  downloading a update to an existing map (existing map has a older version)
     *  downloading a map when the existing map is missing a file
     * 
     */
    public MapUpdateServiceTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        TestUtil.setupMapsForTest();
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Test of init method, of class MapUpdateService.
     * we use the maps ****QA**** server
     */
    public void testInit() throws Exception {
        System.out.println("init");
        
        List mapsUIDs = new ArrayList();

        // ALL THESE MAPS NEED TO ACTUALLY BE IN THE MAPS DIR
        mapsUIDs.add("RiskEurope.map");
        mapsUIDs.add("ameroki.map");
        mapsUIDs.add("eurasien.map");
        mapsUIDs.add("geoscape.map");
        mapsUIDs.add("lotr.map");
        mapsUIDs.add("luca.map");
        mapsUIDs.add("risk.map");
        mapsUIDs.add("roman_empire.map");
        mapsUIDs.add("sersom.map");
        mapsUIDs.add("teg.map");
        mapsUIDs.add("tube.map");
        mapsUIDs.add("uk.map");
        mapsUIDs.add("world.map");
        
        MapUpdateService instance = MapUpdateService.getInstance();
        instance.init(mapsUIDs,"http://mapsqa.yura.net/maps?format=xml");
        
        List result = instance.mapsToUpdate;
        
        List check = new ArrayList();
        check.add("ameroki.map");
        
        assertResultContains(result,check);
    }

    private void assertResultContains(List result, List check) {
        assertEquals( check.size(), result.size() );
        
        for (int c=0;c<check.size();c++) {
            
            String name = (String)check.get(c);
            boolean found=false;
            
            for (int a=0;a<result.size();a++) {
                Map map = (Map)result.get(a);
                
                if (map.getMapUrl().endsWith("/"+name)) {
                    found = true;
                }
            }
            
            if (!found) {
                fail("not found: "+name);
            }
        }
    }
}
