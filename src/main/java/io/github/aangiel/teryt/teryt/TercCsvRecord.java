package io.github.aangiel.teryt.teryt;

import com.opencsv.bean.CsvBindByName;
import lombok.Getter;

import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class TercCsvRecord extends CsvRecord {

    private String key = null;
    private String parentKey = null;

    @CsvBindByName(required = true, column = "﻿WOJ", locale = "pl_PL")
    private String voivodeshipCode;

    @CsvBindByName(required = false, column = "POW", locale = "pl_PL")
    private String countyCode;

    @CsvBindByName(required = false, column = "GMI", locale = "pl_PL")
    private String communityCode;

    @CsvBindByName(required = false, column = "RODZ", locale = "pl_PL")
    private String communityType;

    @CsvBindByName(required = true, column = "NAZWA", locale = "pl_PL")
    private String name;

    @CsvBindByName(required = true, column = "NAZWA_DOD", locale = "pl_PL")
    private String extraName;

    @CsvBindByName(required = true, column = "STAN_NA", locale = "pl_PL")
    private String stateDate;

    public String createKey(String catalogName) {
        if (key == null)
            this.key = Stream.of(catalogName, stateDate, voivodeshipCode, countyCode, communityCode, communityType)
                    .filter(Predicate.not(String::isEmpty))
                    .collect(Collectors.joining(":"));

        return this.key;
    }

    public String retrieveParentKey(String catalogName) {
        if (this.parentKey == null) {
            var pk = Stream.of(catalogName, stateDate, voivodeshipCode, countyCode, communityCode)
                    .filter(Predicate.not(String::isEmpty))
                    .collect(Collectors.joining(":"));
            this.parentKey = pk.substring(0, pk.lastIndexOf(":"));
        }

        return parentKey;
    }

}
