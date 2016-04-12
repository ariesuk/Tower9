package com.example.a.tower;

/**
 * Created by a on 2016/4/12.
 */
public class RegisteredBaseStation {
    public int STATIONID;            //INTEGER,
    public String TID;                 //VARCHAR2(20),
    public String SID;                 //VARCHAR2(20),
    public String BELONGING;          //VARCHAR2(100),
    public String LINKMAN;            //VARCHAR2(20),
    public String PHONE;              //VARCHAR2(20),
    public int TEL;                   //INTEGER,
    public String NETNAME;            //VARCHAR2(50),
    public String SERVICEATTRIBUTE; //VARCHAR2(50),
    public String TECNAME;            //VARCHAR2(30),
    public String NAME;               //VARCHAR2(50),
    public String LOCATION;          //VARCHAR2(100),
    public double LONGITUDE;         //VARCHAR2(20),
    public double LATITUDE;          //VARCHAR2(20),
    public String HEIGHT;            //VARCHAR2(20),
    public int EQUIPMENT;           //INTEGER,
    public String STATE;             //VARCHAR2(6),
    public String STARTDATA;        // DATE,
    public double STARTFRE;        // NUMBER,
    public double ENDFRE;          //NUMBER

    public RegisteredBaseStation()
    {
    }
}
