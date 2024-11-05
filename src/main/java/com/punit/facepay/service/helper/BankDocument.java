package com.punit.facepay.service.helper;

import java.util.HashMap;
import java.util.Map;

public class BankDocument {

    String documenttype = "Cheque";
    int lenght =0;
    double confidence = 0.0;



    Map<String, Boolean> resultSet;

    public BankDocument(int lenght) {
        this.lenght = lenght;
        resultSet = new HashMap<>();
    }

    String validations = "";

    public int getMatchCount() {
        return matchCount;
    }

    public int increaseMatchCount() {
        return ++matchCount;
    }


    public void setMatchCount(int matchCount) {
        this.matchCount = matchCount;
    }

    int matchCount = 0;

    public String getValidations() {
        return validations;
    }

    public void setValidations(String validations) {
        this.validations = validations;
    }


    public String getDocumenttype() {
        return documenttype;
    }

    public void setDocumenttype(String documenttype) {
        this.documenttype = documenttype;
    }
    public Map<String, Boolean> getResultSet() {
        return resultSet;
    }

    public void addResult( String key, boolean value) {
         resultSet.put(key, value);
    }

    public void setResultSet(Map<String, Boolean> resultSet) {
        this.resultSet = resultSet;
    }

    public void calculateConfidence(int totalKeywords) {

        if (lenght <200){
            this.confidence = (double) (this.matchCount+1) / (totalKeywords+1) * 100;
        }else{
            this.confidence = (double) (this.matchCount) / (totalKeywords) * 100;

        }
    }

}
