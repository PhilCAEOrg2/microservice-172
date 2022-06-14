package i5.las2peer.services.res;

import static i5.las2peer.services.res.TestUtil.*;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;

import com.github.viclovsky.swagger.coverage.core.generator.Generator;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Assert;

import javax.ws.rs.core.MediaType;

import i5.las2peer.p2p.LocalNode;
import i5.las2peer.p2p.LocalNodeManager;
import i5.las2peer.api.p2p.ServiceNameVersion;
import i5.las2peer.security.ServiceAgentImpl;
import i5.las2peer.security.UserAgentImpl;
import i5.las2peer.security.AnonymousAgentImpl;
import i5.las2peer.testing.MockAgentFactory;
import i5.las2peer.connectors.webConnector.WebConnector;
import i5.las2peer.connectors.webConnector.client.ClientResponse;
import i5.las2peer.connectors.webConnector.client.MiniClient;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.JSONArray;


/**
 * Service7 - Test Class
 *
 * This class provides a basic testing framework for the microservice Service7. It was
 * generated by the CAE (Community Application Framework).
 */
public class ResTest {

  private static final String HTTP_ADDRESS = "http://127.0.0.1";
  private static final int HTTP_PORT = WebConnector.DEFAULT_HTTP_PORT;

  private static LocalNode node;
  private static WebConnector connector;
  private static ByteArrayOutputStream logStream;

  private static UserAgentImpl testAgentAdam;
  private static UserAgentImpl testAgentAbel;
  private static UserAgentImpl testAgentEve;

  private static final String testPassAdam = "adamspass";
  private static final String testPassAbel = "abelspass";
  private static final String testPassEve = "evespass";

  // version does not matter in tests
  private static final ServiceNameVersion testTemplateService = new ServiceNameVersion(Res.class.getCanonicalName(),"0.1");

  private static final String mainPath = "res";


  /**
   * Called before the tests start.
   * Sets up the node and initializes connector and users that can be used throughout the tests.
   * @throws Exception
   */
  @BeforeClass
  public static void startServer() throws Exception {

    // start node
    node = new LocalNodeManager().newNode();
	 
    testAgentAdam = MockAgentFactory.getAdam();
    testAgentAdam.unlock(testPassAdam); // agent must be unlocked in order to be stored
    node.storeAgent(testAgentAdam);

    testAgentAbel = MockAgentFactory.getAbel();
    testAgentAbel.unlock(testPassAbel);
    node.storeAgent(testAgentAbel);

    testAgentEve = MockAgentFactory.getEve();
    testAgentEve.unlock(testPassEve);
    node.storeAgent(testAgentEve);
	  
    node.launch();

    ServiceAgentImpl testService = ServiceAgentImpl.createServiceAgent(testTemplateService, "a pass");
    testService.unlock("a pass");

    node.registerReceiver(testService);

    // start connector
    logStream = new ByteArrayOutputStream();

    connector = new WebConnector(true, HTTP_PORT, false, 1000);
    connector.setLogStream(new PrintStream(logStream));
    connector.start(node);
    Thread.sleep(1000); // wait a second for the connector to become ready

    // download swagger.json
    InputStream in = new URL(connector.getHttpEndpoint() + "/" + mainPath + "/swagger.json").openStream();
    Files.copy(in, Paths.get("export/swagger.json"), StandardCopyOption.REPLACE_EXISTING);
    
     
  }


  /**
   * 
   * Test for the NewTestCase_ID883650 method.
   * 
   */
  @Test
  public void testNewTestCase_ID883650() {
    MiniClientCoverage c = new MiniClientCoverage(mainPath);
    c.setConnectorEndpoint(connector.getHttpEndpoint());
    
        
    try {
      c.setLogin(AnonymousAgentImpl.IDENTIFIER, "");
      ClientResponse result = c.sendRequest("GET", "/test", "", "text/plain", "*/*", new HashMap<>(), new Object[0]);
      System.out.println("Result of request with id: 116877: " + result.getResponse().trim());
    
    } catch (Exception e) {
      e.printStackTrace();
      fail("Exception: " + e);
    }
    

    
  }




  /**
   * Called after the tests have finished. Shuts down the server and prints out the connector log
   * file for reference.
   * @throws Exception
   */
  @AfterClass
  public static void shutDownServer() throws Exception {
	
	 

    connector.stop();
    node.shutDown();

    connector = null;
    node = null;

    System.out.println("Connector-Log:");
    System.out.println("--------------");

    System.out.println(logStream.toString());

    new Generator().setInputPath(Paths.get("swagger-coverage-output/"))
            .setSpecPath(Paths.get("export/swagger.json").toUri())
            .run();
  }

}
