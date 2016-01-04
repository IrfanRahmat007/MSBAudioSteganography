package com.msbaudiosteganography.audio;

import com.msbaudiosteganography.text.TextProcessor;
import com.msbaudiosteganography.wavfile.*;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 *
 * @author Afina, Meisyal, Irfan
 */

public class AudioProcessor {
    private String passKey = "y0Ox5a";
    int maxBuff;
    int distance;
    int MSBInt8=4;
    int MSBInt16=7; //Choice 16/2 - 14
    int MSBInt32=23;  //Choice 32/2 - 30
    int MSBLong=55;   //Choice 64/2 - 62
    WavFile wavFile;
    WavFile outFile;
    File file;
    int channel;
    long totalFrame;
    long remainingFrame;
    long sampleRate;
    int validBits;
    double sqe;
    String logString;
    boolean fail;
    
    
    public AudioProcessor(File audioFile, File outputFile) throws IOException, WavFileException {
        maxBuff = 1024;
        fail=false;
        distance = maxBuff / 8;
        logString = "";
        sqe = 0;
        if (audioFile != null) {
            try {
                file = audioFile;
                wavFile = WavFile.openWavFile(audioFile);
                wavFile.display();
                channel = wavFile.getNumChannels();
                totalFrame = wavFile.getNumFrames();
                remainingFrame = wavFile.getFramesRemaining();
                sampleRate = wavFile.getSampleRate();
                validBits = wavFile.getValidBits();
                if(outputFile != null) {
                    outFile = WavFile.newWavFile(outputFile, channel, totalFrame,
                            validBits, sampleRate);
                }
            } catch (WavFileException ex) {
                throw ex;
            }
        }
    }
//    public AudioProcessor(int buffSize, int buffDivider,File audioFile, File outputFile) throws IOException, WavFileException {
//        maxBuff = buffSize;
//        distance = maxBuff/buffDivider;
//        logString = "";
//        if (audioFile != null) {
//            try {
//                file = audioFile;
//                wavFile = WavFile.openWavFile(audioFile);
//                wavFile.display();
//                channel = wavFile.getNumChannels();
//                totalFrame = wavFile.getNumFrames();
//                remainingFrame = wavFile.getFramesRemaining();
//                sampleRate = wavFile.getSampleRate();
//                validBits = wavFile.getValidBits();
//                if(outputFile != null) {
//                    outFile = WavFile.newWavFile(outputFile, channel, totalFrame,
//                            validBits, sampleRate);
//                }
//            } catch (WavFileException ex) {
//                throw ex;
//            }
//        }
//    }
    
    public boolean getStatus()
    {
        return !fail;
    }
    public boolean checkSize(byte[] msg)
    {
        boolean result=false;
        int size=(msg.length)*getDistance()/channel;
        if((totalFrame*channel/getDistance())>=msg.length)
        {
            result=true;
            Log("Cover size is sufficient to proceed. \nSize : " + size);
        }
        else
        {
            
            Log("Cover size is too small!!!\nMinimum frame needed for this message\n including passkey is "+size+"\nTotal frame : "+totalFrame);
        }
        return result;
    }
    
    public void clearLog()
    {
        logString="";
    }
    public String getLog()
    {
        return logString;
    }
    
    private void getSQEInt(int[] bufferOrig, int[] bufferMod)
    {
        double err;
        for(int i = 0;i < bufferOrig.length;i++)
        {
            err=bufferMod[i]-bufferOrig[i];
            err=err*err;
            sqe=sqe+err;
        }
    }
    private void getSQELong(long[] bufferOrig, long[] bufferMod)
    {
        double err;
        for(int i = 0;i < bufferOrig.length;i++)
        {
            err=bufferMod[i]-bufferOrig[i];
            err=err * err;
            sqe=sqe + err;
        }
    }
    
    private double getMSE()
    {
        double result;
        result = (double) sqe / (double) totalFrame;
        return result;
    }
    
    public double getPSNR()
    {
        double MSE = getMSE();
        double result = 0;
        if(validBits==8)
        {
            result=(double) 20 * java.lang.Math.log10(Byte.MAX_VALUE) - (double) 10 * java.lang.Math.log10(MSE);
        }
        else if (validBits==16)
        {
            result=(double) 20 * java.lang.Math.log10(Short.MAX_VALUE) - (double) 10 * java.lang.Math.log10(MSE);
        }
        else if (validBits==32)
        {
            result=(double) 20 * java.lang.Math.log10(Integer.MAX_VALUE) - (double) 10*java.lang.Math.log10(MSE);
        }
        else if (validBits==64)
        {
            result=(double) 20 * java.lang.Math.log10(Long.MAX_VALUE) - (double) 10 * java.lang.Math.log10(MSE);
        }
        return result;
    }
    public int getDistance() {
        return distance;
    }
    
    public String getSourceInfo() {
        String info;
        PrintStream infoOut;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        infoOut = new PrintStream(baos);
        wavFile.display(infoOut);
        info = baos.toString();
        
        try {
            infoOut.close();
            baos.close();
        } catch (IOException ex) {
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return info;
    }
    
    public String getDestinationInfo() {
        if (outFile == null) {
            return null;
        }
        
        String info;
        PrintStream infoOut;
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        
        infoOut = new PrintStream(baos);
        outFile.display(infoOut);
        info = baos.toString();
        
        try {
            infoOut.close();
            baos.close();
        } catch (IOException ex) {
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return info;
    }
    
    // For wav file with 16-bit depth
    public void WriteMsgInt16(byte[] msg) {
        int[] buffer = new int[maxBuff];
        int[] bufferOrig = new int[maxBuff];
        int msgLength = msg.length;
        int buffCounter;
        int msgCounter;
        buffCounter = msgCounter = 0;

        try {  
            maxBuff=wavFile.readFrames(buffer, maxBuff/channel)*channel;
            bufferOrig=buffer.clone();
            while (msgCounter < msgLength) {
                while (buffCounter < maxBuff) {
                    if(msgCounter==msgLength)
                    {
                        break;
                    }
                    if (msg[msgCounter] == 0) {
                            if ((buffer[buffCounter] & (1 << MSBInt16)) != 0) {     //If bit was set. LSB bisa pakai 0
                                // The bit was set
                                buffer[buffCounter] ^= 1 << MSBInt16; // Invert Bit
                            }
                        } else {
                            if ((buffer[buffCounter] & (1 << MSBInt16)) == 0) { // If bit was not set
                                // The bit was not set
                                buffer[buffCounter] ^= 1 << MSBInt16; // Invert Bit
                            }
                        }
                    buffCounter += getDistance();
                    msgCounter++;
                }
                getSQEInt(bufferOrig, buffer);
                outFile.writeFrames(buffer, maxBuff/channel);
                maxBuff = wavFile.readFrames(buffer, maxBuff/channel)*channel;
                bufferOrig=buffer.clone();
                buffCounter = 0;
            }
            
            while (maxBuff != 0) {
                outFile.writeFrames(buffer, maxBuff/channel);
                maxBuff = wavFile.readFrames(buffer, maxBuff/channel)*channel;
            }
            
            outFile.close();
        } catch (IOException ex) {
            fail=true;
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WavFileException ex) {
            fail = true;
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    // For wav file with 8-bit depth
    public void WriteMsgInt8(byte[] msg) {
        int[] buffer = new int[maxBuff];
        int[] bufferOrig = new int[maxBuff];
        int msgLength = msg.length;
        int buffCounter;
        int msgCounter;
        buffCounter = msgCounter = 0;
        
        try {
            maxBuff = wavFile.readFrames(buffer, maxBuff / channel) * channel;
            bufferOrig = buffer.clone();
            while (msgCounter < msgLength) {
                while (buffCounter < maxBuff) {
                    if(msgCounter==msgLength)
                    {
                        break;
                    }
                    if (msg[msgCounter] == 0) {
                        if ((buffer[buffCounter] & (1 << MSBInt8)) != 0) {     //If bit was set. LSB bisa pakai 0
                            // The bit was set
                            buffer[buffCounter] ^= 1 << MSBInt8; // Invert Bit
                        }
                    } else {
                        if ((buffer[buffCounter] & (1 << MSBInt8)) == 0) { // If bit was not set
                            // The bit was not set
                            buffer[buffCounter] ^= 1 << MSBInt8; // Invert Bit
                        }
                    }
                    
                    buffCounter += getDistance();
                    msgCounter++;
                }
                getSQEInt(bufferOrig, buffer);
                outFile.writeFrames(buffer, maxBuff / channel);
                maxBuff = wavFile.readFrames(buffer, maxBuff / channel) * channel;
                bufferOrig = buffer.clone();
                buffCounter = 0;
            }
            
            while (maxBuff != 0) {
                outFile.writeFrames(buffer, maxBuff / channel);
                maxBuff = wavFile.readFrames(buffer, maxBuff / channel) * channel;
            }
            
            outFile.close();
        } catch (IOException ex) {
            fail = true;
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WavFileException ex) {
            fail = true;
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }        
    }
    
    // For wav file with 32-bit depth
    public void WriteMsgInt32(byte[] msg) {
        int[] buffer = new int[maxBuff];
        int[] bufferOrig = new int[maxBuff];
        int msgLength = msg.length;
        int buffCounter;
        int msgCounter;
        buffCounter = msgCounter = 0;
        
        try {
            maxBuff = wavFile.readFrames(buffer, maxBuff / channel) * channel;
            bufferOrig = buffer.clone();
            while (msgCounter < msgLength) {
                while (buffCounter < maxBuff) {
                    if(msgCounter==msgLength)
                    {
                        break;
                    }
                    if (msg[msgCounter] == 0) {
                        if ((buffer[buffCounter] & (1 << MSBInt32)) != 0) {     //If bit was set. LSB bisa pakai 0
                            // The bit was set
                            buffer[buffCounter] ^= 1 << MSBInt32; // Invert Bit
                        }
                    } else {
                        if ((buffer[buffCounter] & (1 << MSBInt32)) == 0) { // If bit was not set
                            // The bit was not set
                            buffer[buffCounter] ^= 1 << MSBInt32; // Invert Bit
                        }
                    }
                    
                    buffCounter += getDistance();
                    msgCounter++;
                }
                getSQEInt(bufferOrig, buffer);
                outFile.writeFrames(buffer, maxBuff / channel);
                maxBuff = wavFile.readFrames(buffer, maxBuff / channel) * channel;
                bufferOrig = buffer.clone();
                buffCounter = 0;
            }
            
            while (maxBuff != 0) {
                outFile.writeFrames(buffer, maxBuff / channel);
                maxBuff = wavFile.readFrames(buffer, maxBuff / channel) * channel;
            }
            
            outFile.close();
        } catch (IOException ex) {
            fail = true;
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WavFileException ex) {
            fail = true;
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    // For wav file with 64-bit depth
    public void WriteMsgLong(byte[] msg) {
        long[] buffer = new long[maxBuff];
        long[] bufferOrig = new long[maxBuff];
        int msgLength = msg.length;
        int buffCounter;
        int msgCounter;
        buffCounter = msgCounter = 0;
        
        try {
            maxBuff = wavFile.readFrames(buffer, maxBuff / channel) * channel;
            bufferOrig = buffer.clone();
            while (msgCounter < msgLength) {
                while (buffCounter < maxBuff) {
                    if(msgCounter==msgLength)
                    {
                        break;
                    }
                    if (msg[msgCounter] == 0) {
                        if ((buffer[buffCounter] & (1 << MSBLong)) != 0) {     //If bit was set. LSB bisa pakai 0
                            // The bit was set
                            buffer[buffCounter] ^= 1 << MSBLong; // Invert Bit
                        }
                    } else {
                        if ((buffer[buffCounter] & (1 << MSBLong)) == 0) { // If bit was not set
                            // The bit was not set
                            buffer[buffCounter] ^= 1 << MSBLong; // Invert Bit
                        }
                    }
                    
                    buffCounter += getDistance();
                    msgCounter++;
                }
                getSQELong(bufferOrig, buffer);
                outFile.writeFrames(buffer, maxBuff / channel);
                maxBuff = wavFile.readFrames(buffer, maxBuff / channel) * channel;
                bufferOrig = buffer.clone();
                buffCounter = 0;
            }
            
            while (maxBuff != 0) {
                outFile.writeFrames(buffer, maxBuff / channel);
                maxBuff = wavFile.readFrames(buffer, maxBuff / channel) * channel;
            }
            
            outFile.close();
        } catch (IOException ex) {
            fail = true;
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WavFileException ex) {
            fail = true;
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
    }
    
    public void WriteMsg(byte[] msg) {
        if(!checkSize(msg))
        {
            Log("Operation aborted!!!");
            fail=true;
        }
        else
        {
            Log("Writing message to file...");
            if (validBits == 8) {
                WriteMsgInt8(msg);
            } else if (validBits == 16) {
                WriteMsgInt16(msg);
            } else if (validBits == 32) {
                WriteMsgInt32(msg);
            } else {
                WriteMsgLong(msg);
            }
            if(!fail)
            {
                Log("Write operation success.");
            }
            
        }
    }
    
    public boolean CheckPassCounter(int listCounter, int passCounter, ArrayList<Integer> msgList, byte pass[]) {
        if (msgList.get(listCounter) == pass[passCounter]) {
            return true;
        }
        return false;
    }
    
    public byte[] ReadMsgInt16(TextProcessor tp) {
        byte[] msg;
        ArrayList<Integer> msgList = new ArrayList<Integer>();
        byte[] bPasskey = tp.convertBinaryStringToBytes(passKey);
        int buffer[] = new int[maxBuff];
        int buffCounter;
        int stopCounter;
        int passCounter;
        int bitCounter=0;
        buffCounter = stopCounter = passCounter = 0;
        
        try {
            maxBuff = wavFile.readFrames(buffer, maxBuff / channel) * channel;
            
            while (maxBuff > 0) {
                if (stopCounter == bPasskey.length) {
                    break;
                }
                
                while (buffCounter < maxBuff) {
                    if (stopCounter == bPasskey.length) {
                        break;
                    }
                    
                    if ((buffer[buffCounter] & (1 << MSBInt16)) != 0) {
                        // The bit was set
                        msgList.add(1);
                    } else {
                        msgList.add(0);
                    }
                    
                    if (bitCounter < bPasskey.length) {
                        if (CheckPassCounter(bitCounter, passCounter, msgList, bPasskey) == false) {
                            fail = true;
                            Log("Passkey not found. Operation aborted.");
                            return null;
                        } else {
                            passCounter++;
                        }
                    } else {
                        if (CheckPassCounter(bitCounter, stopCounter, msgList, bPasskey) == true) {
                            stopCounter++;
                        } else {
                            stopCounter = 0;
                        }
                    }
                    if (passCounter == bPasskey.length) {
                        Log("Passkey found. Reading message...");
                        passCounter=0;
                    }
                    
                    buffCounter += getDistance();
                    bitCounter++;
                }
                
                maxBuff = wavFile.readFrames(buffer, maxBuff/channel)*channel;
                buffCounter=0;
            }
            
            wavFile.close();
        } catch (IOException ex) {
            fail = true;
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WavFileException ex) {
            fail = true;
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        msg = new byte[msgList.size()];
        Iterator<Integer> iterator = msgList.iterator();
        
        for (int i = 0; i < msgList.size(); i++) {
           msg[i] = iterator.next().byteValue();
        }
        
        return msg;
    }
    
    public byte[] ReadMsgInt8(TextProcessor tp) {
        byte[] msg;
        ArrayList<Integer> msgList = new ArrayList<Integer>();
        byte[] bPasskey = tp.convertBinaryStringToBytes(passKey);
        int buffer[] = new int[maxBuff];
        int buffCounter;
        int stopCounter;
        int passCounter;
        int bitCounter = 0;
        buffCounter = stopCounter = passCounter = 0;
        
        try {
            maxBuff = wavFile.readFrames(buffer, maxBuff / channel) * channel;
            
            while (maxBuff > 0) {
                if (stopCounter == bPasskey.length) {
                    break;
                }
                
                while (buffCounter < maxBuff) {
                    if (stopCounter == bPasskey.length) {
                        break;
                    }
                    
                    if ((buffer[buffCounter] & (1 << MSBInt8)) != 0) {
                        // The bit was set
                        msgList.add(1);
                    } else {
                        msgList.add(0);
                    }

                    if (bitCounter < bPasskey.length) {
                        if (CheckPassCounter(bitCounter, passCounter, msgList, bPasskey) == false) {
                            fail = true;
                            Log("Passkey not found. Operation aborted.");
                            return null;
                        } else {
                            passCounter++;
                        }
                    } else {
                        if (CheckPassCounter(bitCounter, stopCounter, msgList, bPasskey) == true) {
                            stopCounter++;
                        } else {
                            stopCounter = 0;
                        }
                    }
                    if (passCounter == bPasskey.length) {
                        Log("Passkey found. Reading message...");
                        passCounter = 0;
                    }

                    buffCounter += getDistance();
                    bitCounter++;
                }
                
                maxBuff = wavFile.readFrames(buffer, maxBuff / channel) * channel;
                buffCounter = 0;
            }
            
            wavFile.close();
        } catch (IOException ex) {
            fail = true;
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WavFileException ex) {
            fail = true;
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        msg = new byte[msgList.size()];
        Iterator<Integer> iterator = msgList.iterator();
        for (int i = 0; i < msgList.size(); i++) {
            msg[i] = iterator.next().byteValue();
        }
        
        return msg;
    }
    
    public byte[] ReadMsgInt32(TextProcessor tp) {
        byte[] msg;
        ArrayList<Integer> msgList = new ArrayList<Integer>();
        byte[] bPasskey = tp.convertBinaryStringToBytes(passKey);
        int buffer[] = new int[maxBuff];
        int buffCounter;
        int stopCounter;
        int passCounter;
        int bitCounter = 0;
        buffCounter = stopCounter = passCounter = 0;
        
        try {
            maxBuff = wavFile.readFrames(buffer, maxBuff / channel) * channel;
            
            while (maxBuff > 0) {
                if (stopCounter == bPasskey.length) {
                    break;
                }
                
                while (buffCounter < maxBuff) {
                    if (stopCounter == bPasskey.length) {
                        break;
                    }
                    
                    if ((buffer[buffCounter] & (1 << MSBInt32)) != 0) {
                        // The bit was set
                        msgList.add(1);
                    } else {
                        msgList.add(0);
                    }

                    if (bitCounter < bPasskey.length) {
                        if (CheckPassCounter(bitCounter, passCounter, msgList, bPasskey) == false) {
                            fail = true;
                            Log("Passkey not found. Operation aborted.");
                            return null;
                        } else {
                            passCounter++;
                        }
                    } else {
                        if (CheckPassCounter(bitCounter, stopCounter, msgList, bPasskey) == true) {
                            stopCounter++;
                        } else {
                            stopCounter = 0;
                        }
                    }
                    if (passCounter == bPasskey.length) {
                        Log("Passkey found. Reading message...");
                        passCounter = 0;
                    }

                    buffCounter += getDistance();
                    bitCounter++;
                }
                
                maxBuff = wavFile.readFrames(buffer, maxBuff / channel) * channel;
                buffCounter = 0;
            }
            
            wavFile.close();
        } catch (IOException ex) {
            fail = true;
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WavFileException ex) {
            fail = true;
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }

        msg = new byte[msgList.size()];
        Iterator<Integer> iterator = msgList.iterator();
        
        for (int i = 0; i < msgList.size(); i++) {
            msg[i] = iterator.next().byteValue();
        }
        
        return msg;
    }
    
    public byte[] ReadMsgLong(TextProcessor tp) {
        byte[] msg;
        ArrayList<Integer> msgList = new ArrayList<Integer>();
        byte[] bPasskey = tp.convertBinaryStringToBytes(passKey);
        long buffer[] = new long[maxBuff];
        int buffCounter;
        int stopCounter;
        int passCounter;
        int bitCounter=0;
        buffCounter = stopCounter = passCounter = 0;
        
        try {
            maxBuff = wavFile.readFrames(buffer, maxBuff / channel) * channel;
            
            while (maxBuff > 0) {
                if (stopCounter == bPasskey.length) {
                    break;
                }
                
                while (buffCounter < maxBuff) {
                    if (stopCounter == bPasskey.length) {
                        break;
                    }
                    
                    if ((buffer[buffCounter] & (1 << MSBLong)) != 0) {
                        // The bit was set
                        msgList.add(1);
                    } else {
                        msgList.add(0);
                    }
                    
                    if (bitCounter < bPasskey.length) {
                        if (CheckPassCounter(bitCounter, passCounter, msgList, bPasskey) == false) {
                            fail = true;
                            Log("Passkey not found. Operation aborted.");
                            return null;
                        } else {
                            passCounter++;
                        }
                    } else {
                        if (CheckPassCounter(bitCounter, stopCounter, msgList, bPasskey) == true) {
                            stopCounter++;
                        } else {
                            stopCounter = 0;
                        }
                    }
                    if(passCounter==bPasskey.length)
                    {
                        Log("Passkey found. Reading message...");
                        passCounter=0;
                    }
                    
                    buffCounter += getDistance();
                    bitCounter++;
                }
                
                maxBuff = wavFile.readFrames(buffer, maxBuff / channel) * channel;
                buffCounter=0;
            }
            
            wavFile.close();
        } catch (IOException ex) {
            fail = true;
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WavFileException ex) {
            fail = true;
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        msg= new byte[msgList.size()];
        Iterator<Integer> iterator = msgList.iterator();
        
        for(int i=0;i<msgList.size();i++) {
           msg[i]=iterator.next().byteValue();
        }
        
        return msg;
    }
    private void Log(String log) {
        if(logString.endsWith("\n") || logString.endsWith("\r") || logString.endsWith("\r\n"))
        {
            logString=logString+log;
        }
        else
        {
            logString=logString+"\n"+log;
        }
        
    }
    
    public byte[] ReadMsg() {
        TextProcessor tp = new TextProcessor();
        byte[] result;
        Log("Reading passkey from file...");
        if (validBits == 8) {
            result = ReadMsgInt8(tp);
        } else if (validBits == 16) {
            result = ReadMsgInt16(tp);
        } else if (validBits == 32) {
            result = ReadMsgInt32(tp);
        } else {
            result = ReadMsgLong(tp);
        }
        
        if(!fail)
        {
            Log("Read operation success.");
        }
        
        return result;
        
    }
}
