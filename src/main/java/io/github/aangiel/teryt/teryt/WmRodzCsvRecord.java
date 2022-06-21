package io.github.aangiel.teryt.teryt;

import com.opencsv.bean.CsvBindByName;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class WmRodzCsvRecord extends CsvRecord {

    @CsvBindByName(required = true, column = "\uFEFFRM", locale = "pl_PL")
    private String townType;

    @CsvBindByName(required = true, column = "NAZWA_RM", locale = "pl_PL")
    private String typeName;

    @CsvBindByName(required = true, column = "STAN_NA", locale = "pl_PL")
    private String stateDate;
}
