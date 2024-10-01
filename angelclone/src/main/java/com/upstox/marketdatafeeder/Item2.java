package com.upstox.marketdatafeeder;


public class Item2 {
    private String text;
    private String last;
    private String qty;
    private String avg;
    private String ltpmin;
    private String ltpmax;

    private String bal;

    private String radio;

    private String type;

    private String id;


    private String ltp;

    private String profit;

    private String percentprofit;


    private String ltpprofit;

    private String todayloss;


    private String tploss;

    private String tlossradio;

    private String current;




    public Item2(){}
    public Item2(String text, String last, String qty, String avg, String ltpmin, String ltpmax, String bal, String radio, String type, String id, String ltp, String profit, String percentprofit, String ltpprofit, String todayloss, String tploss, String tlossradio, String current) {
        this.text = text;
        this.last = last;
        this.qty = qty;
        this.avg = avg;
        this.ltpmin = ltpmin;
        this.ltpmax = ltpmax;
        this.bal = bal;
        this.radio = radio;

        this.type = type;
        this.id = id;
        this.ltp = ltp;
        this.profit = profit;
        this.percentprofit = percentprofit;
        this.ltpprofit = ltpprofit;
        this.todayloss = todayloss;
        this.tploss = tploss;
        this.tlossradio = tlossradio;
        this.current = current;
    }


    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    public String getQty() {
        return qty;
    }

    public void setQty(String qty) {
        this.qty = qty;
    }

    public String getAvg() {
        return avg;
    }

    public void setAvg(String avg) {
        this.avg = avg;
    }

    public String getLtpmin() {
        return ltpmin;
    }

    public void setLtpmin(String ltpmin) {
        this.ltpmin = ltpmin;
    }

    public String getLtpmax() {
        return ltpmax;
    }

    public void setLtpmax(String ltpmax) {
        this.ltpmax = ltpmax;
    }

    public String getBal() {
        return bal;
    }

    public void setBal(String bal) {
        this.bal = bal;
    }

    public String getRadio() {
        return radio;
    }

    public void setRadio(String radio) {
        this.radio = radio;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLtp() {
        return ltp;
    }

    public void setLtp(String ltp) {
        this.ltp = ltp;
    }

    public String getProfit() {
        return profit;
    }

    public void setProfit(String profit) {
        this.profit = profit;
    }

    public String getPercentprofit() {
        return percentprofit;
    }

    public void setPercentprofit(String percentprofit) {
        this.percentprofit = percentprofit;
    }

    public String getLtpprofit() {
        return ltpprofit;
    }

    public void setLtpprofit(String ltpprofit) {
        this.ltpprofit = ltpprofit;
    }

    public String getTodayloss() {
        return todayloss;
    }

    public void setTodayloss(String todayloss) {
        this.todayloss = todayloss;
    }

    public String getTploss() {
        return tploss;
    }

    public void setTploss(String tploss) {
        this.tploss = tploss;
    }

    public String getTlossradio() {
        return tlossradio;
    }

    public void setTlossradio(String tlossradio) {
        this.tlossradio = tlossradio;
    }

    public String getCurrent() {
        return current;
    }

    public void setCurrent(String current) {
        this.current = current;
    }
}
