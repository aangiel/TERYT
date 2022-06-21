package io.github.aangiel.teryt.teryt;

import com.opencsv.bean.CsvBindByName;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class UlicCsvRecord extends CsvRecord {

    @CsvBindByName(required = true, column = "﻿WOJ", locale = "pl_PL")
    private String voivodeshipCode;

    @CsvBindByName(required = true, column = "POW", locale = "pl_PL")
    private String countyCode;

    @CsvBindByName(required = true, column = "GMI", locale = "pl_PL")
    private String communityCode;

    @CsvBindByName(required = true, column = "RODZ_GMI", locale = "pl_PL")
    private String communityType;

    @CsvBindByName(required = true, column = "SYM", locale = "pl_PL")
    private String townSymbol;

    @CsvBindByName(required = true, column = "SYM_UL", locale = "pl_PL")
    private String streetSymbol;

    @CsvBindByName(required = false, column = "CECHA", locale = "pl_PL")
    private String type;

    @CsvBindByName(required = true, column = "NAZWA_1", locale = "pl_PL")
    private String nameOne;

    @CsvBindByName(required = false, column = "NAZWA_2", locale = "pl_PL")
    private String nameTwo;

    @CsvBindByName(required = true, column = "STAN_NA", locale = "pl_PL")
    private String stateDate;
    private String simcKey = null;
    private String key = null;

    public String createSimcKey() {
        if (this.simcKey == null)
            this.simcKey = String.join(":", voivodeshipCode,
                    countyCode, communityCode, communityType, townSymbol);

        return this.simcKey;

    }

    public String createKey(String catalogName) {
        if (this.key == null)
            this.key = String.join(":", catalogName, stateDate, voivodeshipCode,
                    countyCode, communityCode, communityType, townSymbol, streetSymbol);

        return this.key;

    }
}
