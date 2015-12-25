/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.msbaudiosteganography.audio;
import com.msbaudiosteganography.wavfile.*;
import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.UnsupportedAudioFileException;
/**
 *
 * @author Andrias
 */
public class AudioProcessor {
    
    WavFile wavFile;
    WavFile outFile;
    File file;
    int channel;
    long totalFrame;
    long remainingFrame;
    long sampleRate;
    int validBits;
    public AudioProcessor(File audioFile, File outputFile) throws IOException {
        if (audioFile!=null)
        {
            try {
                file=audioFile;
                wavFile = WavFile.openWavFile(audioFile);
                channel=wavFile.getNumChannels();
                totalFrame=wavFile.getNumFrames();
                remainingFrame=wavFile.getFramesRemaining();
                sampleRate=wavFile.getSampleRate();
                validBits=wavFile.getValidBits();
                outFile=WavFile.newWavFile(outputFile, channel, totalFrame, validBits, sampleRate);
            } catch (WavFileException ex) {
                Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    public int getDistance(int now)
    {
        return 4;
    }
    public void WriteMsgInt16(byte[] msg) //untuk wav dengan bit depth 16 bit
    {
        int[] buffer = new int[32];
        int msgLength = msg.length;
        int buffCounter;
        int msgCounter;
        buffCounter=msgCounter=0;
        try {  
            wavFile.readFrames(buffer, msgLength);
        } catch (IOException ex) {
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WavFileException ex) {
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        while(msgCounter!=msgLength)
        {
            buffer[buffCounter] |= Short.MIN_VALUE;
        }
    }
    public void WriteMsgInt8(byte[] msg) //untuk wav dengan bit depth 8 bit
    {
        int[] buffer = new int[32];
        int msgLength = msg.length;
        int buffCounter;
        int msgCounter;
        buffCounter=msgCounter=0;
        try {  
            wavFile.readFrames(buffer, msgLength);
        } catch (IOException ex) {
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WavFileException ex) {
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        while(msgCounter!=msgLength)
        {
            buffer[buffCounter] |= Byte.MIN_VALUE;
        }
    }
    public void WriteMsgInt32(byte[] msg) //untuk wav dengan bit depth 32 bit
    {
        int[] buffer = new int[32];
        int msgLength = msg.length;
        int buffCounter;
        int msgCounter;
        buffCounter=msgCounter=0;
        try {  
            wavFile.readFrames(buffer, msgLength);
        } catch (IOException ex) {
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WavFileException ex) {
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        while(msgCounter!=msgLength)
        {
            buffer[buffCounter] |= Integer.MIN_VALUE;
        }
    }
    public void WriteMsgLong(byte[] msg) //untuk wav dengan bit depth 64 bit
    {
        long[] buffer = new long[32];
        int msgLength = msg.length;
        int buffCounter;
        int msgCounter;
        buffCounter=msgCounter=0;
        try {  
            wavFile.readFrames(buffer, msgLength);
        } catch (IOException ex) {
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WavFileException ex) {
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        while(msgCounter!=msgLength)
        {
            buffer[buffCounter] |= Long.MIN_VALUE;
        }
    }
    public void WriteMsg(byte[] msg)
    {
        
        if(validBits==8)
        {
            WriteMsgInt8(msg);
        }
        else if (validBits==16)
        {
            WriteMsgInt16(msg);
        }
        else if (validBits==32)
        {
            WriteMsgInt32(msg);
        }
        else
        {
            WriteMsgLong(msg);
        }
    }
    public void WriteAudio(int[] data, File Output)
    {
        
    }
    public void WriteAudio(long[] data, File Output)
    {
        
    }

}
