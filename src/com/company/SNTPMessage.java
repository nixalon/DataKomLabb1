package com.company;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class SNTPMessage {
    private byte leapIndicator = 0;
    private byte versionNumber = 4;
    private byte mode = 0;

    private short stratum = 0;    //Behövde vara short för att kunna ta emot bytes upp till 250ish
    private short pollInterval = 0;
    private byte precision = 0;

    private double rootDelay = 0;
    private double rootDispersion = 0;

    /*
    reference identifier, 32 bit string, 4 ascii-tecken
    80, 80, 83, 0
    P   P   S
     */
    private byte[] referenceIdentifier = {0, 0, 0, 0};

    public static double referenceTimeStamp = 0;
    public static double originateTimeStamp = 0;
    public static double receiveTimeStamp = 0;
    public static double transmitTimeStamp = 0;

    // 00100100
    // 00000000

    public SNTPMessage(byte[] buf){
        byte b = buf[0];
        leapIndicator = (byte) ((b>>6) & 0x3); //b>>6 betyder: flytta alla bytes 6 steg till höger, och de som ligger till höger försvinner ut i tomma intet)
        versionNumber = (byte)((b>>3) & 0x7);
        mode = (byte)((b>>0) & 0x7);

        stratum = unsignedByteToShort(buf[1]);
        pollInterval = unsignedByteToShort(buf[2]);
        precision = buf[3];

        //Vi får datan som för root delay som 4 bytes, dvs 34 bits i följd. Behöver göra om detta till en double.
        rootDelay = (buf[4] * 256.0)
                    + unsignedByteToShort(buf[5])
                    + (unsignedByteToShort(buf[6])) / 256.0 //Här tar vi fram decimaler, därför delar vi med värdet av biten.
                    + (unsignedByteToShort(buf[7])) / 65536.0;

        rootDispersion = (buf[8] * 256.0)
                + unsignedByteToShort(buf[9])
                + (unsignedByteToShort(buf[10])) / 256.0 //Här tar vi fram decimaler, därför delar vi med värdet av biten.
                + (unsignedByteToShort(buf[11])) / 65536.0;

        referenceIdentifier[0] = buf[12];
        referenceIdentifier[1] = buf[13];
        referenceIdentifier[2] = buf[14];
        referenceIdentifier[3] = buf[15];

        referenceTimeStamp = byteArrayToDouble(buf, 16);
        originateTimeStamp = byteArrayToDouble(buf, 24);
        receiveTimeStamp = byteArrayToDouble(buf, 32);
        transmitTimeStamp = byteArrayToDouble(buf, 40);
    }

    public SNTPMessage() {
        mode = 3;
        transmitTimeStamp = (System.currentTimeMillis() / 1000.0) + 2208988800.0;
    }

    private double byteArrayToDouble(byte[] buf, int index) {
        double result = 0.0;
        for (int i = 0; i < 8; i++) {
            result += unsignedByteToShort(buf[index + i]) * Math.pow(2, (3-i)*8);
        }
        return result;
    }

    private short unsignedByteToShort(byte b) {  //När första biten är 1 blir det ett negativt tal. I detta fall vill vi ha ett positivt
        if((b & 0x80) == 0x80){
            return (short) (128 + (b & 0x7f)); //0x7f = 0111 1111
        }
        return (short) b;
    }

    public byte [] toByteArray() {
        byte[] array = new byte[48];
        array[0] = (byte) (leapIndicator << 6 | versionNumber << 3 | mode);
        array[1] = (byte) stratum;
        array[2] = (byte) pollInterval;
        array[3] = precision;

        int data = (int)(rootDelay) * (0xff+1);
        array[4] = (byte) ((data >> 24) & 0xff);
        array[5] = (byte) ((data >> 16) & 0xff);
        array[6] = (byte) ((data >> 8) & 0xff);
        array[7] = (byte) (data & 0xff);

        int rd = (int)(rootDispersion * (0xff +1));
        array[8] = (byte) ((rd >>24) & 0xff);
        array[9] = (byte) ((rd >>16) & 0xff);
        array[10] = (byte) ((rd >>8) & 0xff);
        array[11] = (byte) (rd & 0xff);

        array[12] = referenceIdentifier[0];
        array[13] = referenceIdentifier[1];
        array[14] = referenceIdentifier[2];
        array[15] = referenceIdentifier[3];

        doubleToByteArray(array, 16, referenceTimeStamp);
        doubleToByteArray(array, 24, originateTimeStamp);
        doubleToByteArray(array, 32, receiveTimeStamp);
        doubleToByteArray(array, 40, transmitTimeStamp);

        return array;
    }

    private void doubleToByteArray(byte[] array, int index, double data) {
        for (int i = 0; i < 8; i++) {
            array[index + i] = (byte) (data / Math.pow(2, (3-i)*8));
            data -= (double) (unsignedByteToShort(array[index + i]) * Math.pow(2, (3-i)*8));
        }
    }

    public String toString (){
        byte[] bytes = {80, 80, 83, 0};
        String str = new String(bytes, StandardCharsets.UTF_8);

        return "Leap indicator: " + leapIndicator + " "
                + "Version: " + versionNumber + " "
                + "Reference Indicator: " + str;




    }
}
