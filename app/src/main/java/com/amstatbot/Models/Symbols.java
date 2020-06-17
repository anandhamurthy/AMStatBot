package com.amstatbot.Models;

public class Symbols {

    String Company, Symbol;

    public Symbols() {
    }

    public Symbols(String company, String symbol) {
        Company = company;
        Symbol = symbol;
    }

    public String getCompany() {
        return Company;
    }

    public void setCompany(String company) {
        Company = company;
    }

    public String getSymbol() {
        return Symbol;
    }

    public void setSymbol(String symbol) {
        Symbol = symbol;
    }
}
