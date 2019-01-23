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
import uk.gov.justice.digital.hmpps.riskprofiler.dao.DataRepository;


@Slf4j
@RunWith(MockitoJUnitRunner.class)
public class S3CsvProcessorRouteTest extends CamelTestSupport {

    private final static String SUBMIT_ENDPOINT = "mock:result";

    @Mock
    private DataRepository service;

    @Override
    public RouteBuilder[] createRouteBuilders() throws Exception {
        MockitoAnnotations.initMocks(this);
        final FileCsvProcessorRoute fileCsvProcessorRoute = new FileCsvProcessorRoute(service);

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

//        template.send(ProcessDataFeedRoute.DIRECT_UPDATE_STATUS, exchange -> {
//        });
//
//        assertMockEndpointsSatisfied();
//        final MockEndpoint mockEndpoint = getMockEndpoint(SUBMIT_ENDPOINT);
//        mockEndpoint.assertIsSatisfied();
//
//        final List<Exchange> receivedExchanges = mockEndpoint.getReceivedExchanges();
//        assertEquals(1, receivedExchanges.size());
//        String appData = receivedExchanges.get(0).getIn().getBody(String.class);
//
//        verify(service).doHandleCsvData(eq);
    }


}
