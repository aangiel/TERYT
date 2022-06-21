package io.github.aangiel.teryt.teryt;

import com.opencsv.bean.CsvBindByName;
import lombok.EqualsAndHashCode;
import lombok.Getter;

@Getter
@EqualsAndHashCode
public class SimcCsvRecord extends CsvRecord {

    private String key = null;
    private String tercKey = null;
    private String baseSymbolKey = null;

    private String ulicKey = null;

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
    private String hasUsualName;

    @CsvBindByName(required = true, column = "NAZWA", locale = "pl_PL")
    private String name;

    @CsvBindByName(required = true, column = "SYM", locale = "pl_PL")
    private String symbol;

    @CsvBindByName(required = true, column = "SYMPOD", locale = "pl_PL")
    private String baseSymbol;

    @CsvBindByName(required = true, column = "STAN_NA", locale = "pl_PL")
    private String stateDate;

    public String createKey(String catalogName) {
        if (this.key == null)
            this.key =  String.join(":", catalogName, stateDate, voivodeshipCode,
                countyCode, communityCode, communityType,
                townType, hasUsualName, symbol, baseSymbol);

        return this.key;
    }

    public String createTercKey(String catalogName, String tercDate) {
        if (this.tercKey == null)
            this.tercKey = String.join(":", catalogName, tercDate, voivodeshipCode,
                countyCode, communityCode, communityType);

        return this.tercKey;
    }

    public String createUlicKey() {
        if (this.ulicKey == null)
            this.ulicKey = String.join(":", voivodeshipCode,
                    countyCode, communityCode, communityType, symbol);

        return this.ulicKey;
    }

    public String createBaseSymbolKey() {
        if (this.baseSymbolKey == null)
            this.baseSymbolKey = String.join(":", voivodeshipCode, countyCode, communityCode, communityType, baseSymbol, baseSymbol);

        return this.baseSymbolKey;
    }

}
