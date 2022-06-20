package io.github.aangiel.teryt.teryt;

import com.opencsv.bean.CsvBindByName;

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
}
