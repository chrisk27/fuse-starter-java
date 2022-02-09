package org.galatea.starter.entrypoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasValue;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import junitparams.JUnitParamsRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.galatea.starter.ASpringTest;
import org.galatea.starter.domain.HistoricalPricesRepository;
import org.galatea.starter.domain.IexHistoricalPricesDB;
import org.hamcrest.number.BigDecimalCloseTo;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

@TestConfiguration
class FixedClockConfig {
  @Primary
  @Bean
  Clock fixedClock() {
    return Clock.fixed(
        Instant.parse("2022-02-04T16:30:25Z"),
        ZoneId.of("America/New_York"));
  }
}


@RequiredArgsConstructor
@Slf4j
// We need to do a full application start up for this one, since we want the feign clients to be instantiated.
// It's possible we could do a narrower slice of beans, but it wouldn't save that much test run time.
@SpringBootTest(classes = FixedClockConfig.class)
// this gives us the MockMvc variable
@AutoConfigureMockMvc
// we previously used WireMockClassRule for consistency with ASpringTest, but when moving to a dynamic port
// to prevent test failures in concurrent builds, the wiremock server was created too late and feign was
// already expecting it to be running somewhere else, resulting in a connection refused
@AutoConfigureWireMock(port = 0, files = "classpath:/wiremock")
// Use this runner since we want to parameterize certain tests.
// See runner's javadoc for more usage.
@RunWith(JUnitParamsRunner.class)
public class IexRestControllerTest extends ASpringTest {

  @Autowired
  private MockMvc mvc;

  @Autowired
  HistoricalPricesRepository testHPRepository;

  @Autowired
  private Clock clock;

  // Test the fuse REST endpoint
  @Test
  public void testGetSymbolsEndpoint() throws Exception {
    MvcResult result = this.mvc.perform(
        // note that we were are testing the fuse REST end point here, not the IEX end point.
        // the fuse end point in turn calls the IEX end point, which is WireMocked for this test.
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/iex/symbols?token=xyz1")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        // some simple validations, in practice I would expect these to be much more comprehensive.
        .andExpect(jsonPath("$[0].symbol", is("A")))
        .andExpect(jsonPath("$[1].symbol", is("AA")))
        .andExpect(jsonPath("$[2].symbol", is("AAAU")))
        .andReturn();
  }

  // Test the getLastTradedPrice and potential null params
  @Test
  public void testGetLastTradedPrice() throws Exception {

    MvcResult result = this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/iex/lastTradedPrice?token=xyz1&symbols=FB")
            // This URL will be hit by the MockMvc client. The result is configured in the file
            // src/test/resources/wiremock/mappings/mapping-lastTradedPrice.json
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].symbol", is("FB")))
        .andExpect(jsonPath("$[0].price").value(new BigDecimal("186.3011")))
        .andReturn();
  }

  @Test
  public void testGetLastTradedPriceEmpty() throws Exception {
    MvcResult result = this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/iex/lastTradedPrice?token=xyz1")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest())
        .andReturn();
  }

  // Test Historical Price Endpoint for calls of different parameter combinations
  @Test
  public void testGetHistoricalPricesDate() throws Exception {
    MvcResult result = this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/iex/historicalPrices?token=xyz1&date=20200124&symbol=AAPL")
            // This URL will be hit by the MockMvc client. The result is configured in the file
            // src/test/resources/wiremock/mappings/mapping-historicalPrices-date.json
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].symbol", is("AAPL")))
        .andExpect(jsonPath("$[0].close").value(new BigDecimal("75.0875")))
        .andExpect(jsonPath("$[0].volume").value(135647456))
        .andReturn();
  }

  @Test
  public void testGetHistoricalPricesRange() throws Exception {
    MvcResult result = this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/iex/historicalPrices?token=xyz1&range=5m&symbol=IBM")
            // This URL will be hit by the MockMvc client. The result is configured in the file
            // src/test/resources/wiremock/mappings/mapping-historicalPrices-range.json
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(jsonPath("$[0].symbol", is("IBM")))
        .andExpect(jsonPath("$[0].open").value(new BigDecimal("139.5")))
        .andExpect(jsonPath("$[1].volume").value(4235101))
        .andExpect(jsonPath("$[1].date", is("2021-08-30")))
        .andReturn();
  }

  @Test
  public void testGetHistoricalPricesSymbolEmpty() throws Exception {
    MvcResult result = this.mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/iex/historicalPrices?token=xyz1&date=20200126&range=5m")
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest())
        .andReturn();
  }

  @Test
  public void testGetHistoricalPricesDateAndRangeEmpty() throws Exception {
    MvcResult result = this.mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/iex/historicalPrices?token=xyz1&symbol=TSLA")
                .accept(MediaType.APPLICATION_JSON_VALUE))
        // This URL will be hit by the MockMvc client. The result is configured in the file
        // src/test/resources/wiremock/mappings/mapping-historicalPrices-noDateOrRange.json
        .andExpect(status().isOk())
        .andExpect(jsonPath("$[0].close").value(new BigDecimal ("177.57")))
        .andExpect(jsonPath("$[0].date", is("2021-12-31")))
        .andReturn();
  }

  @Test
  public void testGetHistoricalPricesAllEmpty() throws Exception {
    MvcResult result = this.mvc.perform(
            org.springframework.test.web.servlet.request.MockMvcRequestBuilders
                .get("/iex/historicalPrices?token=xyz1")
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isBadRequest())
        .andReturn();
  }

  // Test how the controller works with both the external API and the internal database
  @Test
  public void testInsertionIntoRepository() throws Exception {
    List<IexHistoricalPricesDB> entries0 = testHPRepository.findBySymbol("T");
    assertThat(entries0).isEmpty();

    //Puts info into the database after calling the external API
    MvcResult result = this.mvc.perform(
        MockMvcRequestBuilders
            .get("/iex/historicalPrices?range=5d&symbol=T&token=xyz1")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andReturn();

    List<IexHistoricalPricesDB> entries1 = testHPRepository.findBySymbol("T");
    assertThat(entries1).hasSize(3);
  }

  @Test
  public void testFindBySymbolAndDate() throws Exception {
    this.mvc.perform(
            MockMvcRequestBuilders
                .get("/iex/historicalPrices?range=5d&symbol=MRNA&token=xyz1")
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andReturn();

    IexHistoricalPricesDB sample1 =
        testHPRepository.findBySymbolAndDate("MRNA","2022-02-01");
    IexHistoricalPricesDB sample2 =
        testHPRepository.findBySymbolAndDate("MRNA","2022-01-11");
    IexHistoricalPricesDB sample3 =
        testHPRepository.findBySymbolAndDate("MMA", "2022-02-01");

    //Negative tests
    assertThat(sample2).isNull();
    assertThat(sample3).isNull();

    //Positive tests
    assertThat(sample1.getClose()).isEqualTo(new BigDecimal("172.74"));
    assertThat(sample1.getVolume()).isEqualTo(7329761);
  }

  @Test
  public void testDontWriteEmptyToDB() throws Exception {
    this.mvc.perform(
            MockMvcRequestBuilders
                .get("/iex/historicalPrices?date=20220117&symbol=BIIB&token=xyz1")
                .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty())
        .andReturn();

    assertThat(testHPRepository.existsBySymbol("BIIB")).isFalse();
  }

  @Test
  public void testDoNotCallIexIfWeekend() throws Exception {
    this.mvc.perform(
        MockMvcRequestBuilders
            .get("/iex/historicalPrices?date=20220205&symbol=NVS&token=xyz1")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty())
        .andReturn();
  }

  @Test
  public void testDoNotCallRepositoryIfWeekend() throws Exception {
    testHPRepository.save(new IexHistoricalPricesDB("DUMM", new BigDecimal("300"),
        new BigDecimal("320"), new BigDecimal("290"), new BigDecimal("305"),
        1267L,"2022-02-05"));

    this.mvc.perform(
        MockMvcRequestBuilders
            .get("/iex/historicalPrices?date=20220205&symbol=DUMM&token=xyz1")
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isEmpty())
        .andReturn();
  }

  @Test
  public void testSetClockMethod() {
    LocalDate today = LocalDate.now(clock);
    assertThat(today.toString()).isEqualTo("2022-02-04");
  }

  @Test
  public void testOnlyCallIexForUnknownDates() throws Exception {
    //First, insert two objects from database - assume it's from a previous call
    testHPRepository.save(new IexHistoricalPricesDB("AMZN", new BigDecimal("2776.91"),
        new BigDecimal("2884.95"), new BigDecimal("2766.66"), new BigDecimal("2834.75"),
        11276568L,"2022-02-03"));
    testHPRepository.save(new IexHistoricalPricesDB("AMZN", new BigDecimal("3012.25"),
        new BigDecimal("3101.5"), new BigDecimal("2977.27"), new BigDecimal("3131"),
        4366488L,"2022-02-02"));
    //check to make sure they are saved
    List<IexHistoricalPricesDB> entries1 = testHPRepository.findBySymbol("AMZN");
    assertThat(entries1).hasSize(2);

    //Make a new call that would require the two previous entries plus one more
    MvcResult result = this.mvc.perform(
        org.springframework.test.web.servlet.request.MockMvcRequestBuilders
            .get("/iex/historicalPrices?range=3d&symbol=AMZN&token=xyz1")
            // This URL will be hit by the MockMvc client. One entry of the result is configured in
            // the file
            // src/test/resources/wiremock/mappings/mapping-historicalPrices-oneDateFromIex.json.
            // The other two entries should be from the database.
            .accept(MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().isOk())

        // Test all outputs to ensure everything works

        // From IEX
        .andExpect(jsonPath("[0].symbol", is("AMZN")))
        .andExpect(jsonPath("[0].close").value(new BigDecimal("3023.87")))
        .andExpect(jsonPath("[0].high").value(new BigDecimal("3034.16")))
        .andExpect(jsonPath("[0].low").value(new BigDecimal("2952.55")))
        .andExpect(jsonPath("[0].open").value(new BigDecimal("3000")))
        .andExpect(jsonPath("[0].volume").value(2960992))
        .andExpect(jsonPath("[0].date", is("2022-02-01")))


        // From database
        .andExpect(jsonPath("[1].symbol", is("AMZN")))
        .andExpect(jsonPath("[1].close").value(new BigDecimal("3012.25")))
        .andExpect(jsonPath("[1].high").value(new BigDecimal("3101.5")))
        .andExpect(jsonPath("[1].low").value(new BigDecimal("2977.27")))
        .andExpect(jsonPath("[1].open").value(new BigDecimal("3131.0")))
        .andExpect(jsonPath("[1].volume").value(4366488))
        .andExpect(jsonPath("[1].date", is("2022-02-02")))

        .andExpect(jsonPath("[2].symbol", is("AMZN")))
        .andExpect(jsonPath("[2].close").value(new BigDecimal("2776.91")))
        .andExpect(jsonPath("[2].high").value(new BigDecimal("2884.95")))
        .andExpect(jsonPath("[2].low").value(new BigDecimal("2766.66")))
        .andExpect(jsonPath("[2].open").value(new BigDecimal("2834.75")))
        .andExpect(jsonPath("[2].volume").value(11276568))
        .andExpect(jsonPath("[2].date", is("2022-02-03")))

        .andReturn();

  }

}
