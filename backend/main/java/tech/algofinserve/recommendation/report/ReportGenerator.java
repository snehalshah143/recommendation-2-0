package tech.algofinserve.recommendation.report;

import com.opencsv.CSVWriter;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import tech.algofinserve.recommendation.model.domain.StockAlertOutput;

public class ReportGenerator {

  public void generateStockRankReport(
      String outputFileName, List<StockAlertOutput> stockAlertOutputList)
      throws IOException, CsvRequiredFieldEmptyException, CsvDataTypeMismatchException {

    Writer writer = Files.newBufferedWriter(Paths.get(outputFileName));

    ColumnPositionMappingStrategy mappingStrategy =
        new CustomColumnPositionStrategy<StockAlertOutput>();
    mappingStrategy.setType(StockAlertOutput.class);
    // mappingStrategy.generateHeader(StockRankOutput.class);
    //   mappingStrategy.getColumnMapping();
    StatefulBeanToCsv beanToCsv =
        new StatefulBeanToCsvBuilder(writer)
            .withMappingStrategy(mappingStrategy)
            .withSeparator(',')
            .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
            .build();

    beanToCsv.write(stockAlertOutputList);
    writer.close();
  }
}
