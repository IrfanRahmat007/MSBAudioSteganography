/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.msbaudiosteganography.text;

/**
 *
 * @author Afina, Meisyal, Irfan
 */

public class TextProcessor {
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
    
    public String stringToBinaryString(String message) {
        String buffer = new String();
        
        for (int i = 0; i < message.length(); i++) {
            String bit = Integer.toBinaryString(message.charAt(i));
            
            if (bit.length() < 8) {
                bit = "00000000".substring(0, 8 - bit.length()).concat(bit);
            } else {
                bit = bit.substring(bit.length() - 8);
            }
            
            buffer = buffer.concat(bit);
        }
        
        return buffer;
    }
    
    public byte[] convertBinaryStringToBytes(String message) {
        return binaryStringToBytes(stringToBinaryString(message));
    }
}
