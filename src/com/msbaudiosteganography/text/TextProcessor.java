package com.msbaudiosteganography.text;

/**
 *
 * @author Afina, Meisyal, Irfan
 */

public class TextProcessor {
    private String passKey = "y0Ox5a";
    
    public String stringToBinaryString(String message) {
        String buffer = new String();
        
        for (int i = 0; i < message.length(); i++) {
            String bit = Integer.toBinaryString(message.charAt(i));
            
            if (bit.length() < 7) {
                bit = "0000000".substring(0, 7 - bit.length()).concat(bit);
            } else {
                bit = bit.substring(bit.length() - 7);
            }
            
            buffer = buffer.concat(bit);
        }
        
        return buffer;
    }
    
    public byte[] binaryStringToBytes(String binary) {
        byte[] buffer = new byte[binary.length()];
        
        for (int i = 0; i < binary.length(); i++) {
            if (binary.charAt(i) == '0') {
                buffer[i] = 0;
            } else {
                buffer[i] = 1;
            }
        }
        
        return buffer;
    }
    
    public byte[] convertBinaryStringToBytes(String message) {
        return binaryStringToBytes(stringToBinaryString(message));
    }
    
    public byte[] convertBinaryStringToBytesWithPassKey(String message) {
        return binaryStringToBytes(stringToBinaryString(passKey + message + 
            passKey));
    }
    
    public String bytesToBinaryString(byte[] byteMessage) {
        String buffer = new String();
        String bit;
        
        for (int i = 0; i < byteMessage.length; i++) {
            if (byteMessage[i] == 0) {
                bit = "0";
            } else {
                bit = "1";
            }
            
            buffer = buffer.concat(bit);
        }
        
        return buffer;
    }
    
    public String binaryStringToText(String binaryString) {
        String text = new String();
        String sevenBits = new String();
        char symbol;
        
        for (int i = 0; i < binaryString.length(); i++) {
            if (binaryString.charAt(i) == '0') {
                sevenBits = sevenBits.concat("0");
            } else {
                sevenBits = sevenBits.concat("1");
            }
            
            if (i % 7 == 6) {
                symbol = (char) Integer.parseInt(sevenBits, 2);
                text = text.concat(String.valueOf(symbol));
                sevenBits = "";
            } 
        }
        
        return  text;
    }
    
    public String convertByteToText(byte[] byteMessage) {
        return binaryStringToText(bytesToBinaryString(byteMessage));
    }
    
    public String removePassKey(String message) {
        return message.substring(6, message.length() - 6);
    }
}
