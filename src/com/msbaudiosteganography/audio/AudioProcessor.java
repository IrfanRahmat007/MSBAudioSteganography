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
    File file;
    public AudioProcessor(File audioFile) throws IOException {
        if (audioFile!=null)
        {
            try {
                file=audioFile;
                wavFile = WavFile.openWavFile(audioFile);
            } catch (WavFileException ex) {
                Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }
    public void WriteMsg(byte[] msg)
    {
        int msgLength = msg.length;
        int[] buffer = new int[msgLength];
        try {
            wavFile.readFrames(buffer, msgLength);
                    } catch (IOException ex) {
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (WavFileException ex) {
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        WAVReader reader = new WAVReader();
        try {
            AudioInputStream AIS = reader.getAudioInputStream(file);
            
        } catch (UnsupportedAudioFileException ex) {
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(AudioProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
