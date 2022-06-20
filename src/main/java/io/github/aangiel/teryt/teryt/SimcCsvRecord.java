package io.github.aangiel.teryt.teryt;

import com.opencsv.bean.CsvBindByName;

public class SimcCsvRecord extends CsvRecord {

    @CsvBindByName(required = true, column = "﻿WOJ", locale = "pl_PL")
    private String voivodeshipCode;

    @CsvBindByName(required = true, column = "POW", locale = "pl_PL")
    private String countyCode;

    @CsvBindByName(required = true, column = "GMI", locale = "pl_PL")
    private String communityCode;

    @CsvBindByName(required = true, column = "RODZ_GMI", locale = "pl_PL")
    private String communityType;

    @CsvBindByName(required = true, column = "RM", locale = "pl_PL")
    private String townType;

    @CsvBindByName(required = true, column = "MZ", locale = "pl_PL")
    private String commonName;

    @CsvBindByName(required = true, column = "NAZWA", locale = "pl_PL")
    private String name;

    @CsvBindByName(required = true, column = "SYM", locale = "pl_PL")
    private String symbol;

    @CsvBindByName(required = true, column = "SYMPOD", locale = "pl_PL")
    private String baseSymbol;

    @CsvBindByName(required = true, column = "STAN_NA", locale = "pl_PL")
    private String stateDate;
}
