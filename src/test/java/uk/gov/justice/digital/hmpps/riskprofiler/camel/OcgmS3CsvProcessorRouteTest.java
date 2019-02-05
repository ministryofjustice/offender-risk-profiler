package uk.gov.justice.digital.hmpps.riskprofiler.camel;

import groovy.util.logging.Slf4j;
import org.apache.camel.builder.AdviceWithRouteBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnitRunner;


@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class OcgmS3CsvProcessorRouteTest extends CamelTestSupport {

    private final static String SUBMIT_ENDPOINT = "mock:result";

    @Mock
    private CsvProcessor service;

    @Override
    public RouteBuilder[] createRouteBuilders() throws Exception {
        MockitoAnnotations.initMocks(this);
        final CsvProcessorRoute fileCsvProcessorRoute = new CsvProcessorRoute(service);

        return new RouteBuilder[]{fileCsvProcessorRoute};
    }

    @Before
    public void mockEndpoints() throws Exception {
        context.getRouteDefinitions().get(0).adviceWith(context, new AdviceWithRouteBuilder() {
            @Override
            public void configure() throws Exception {
                weaveAddLast().to("mock:result");
            }
        });
    }


    @Test
    public void testUpdateStatus() throws Exception {


    }


}
