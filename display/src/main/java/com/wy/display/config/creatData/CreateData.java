package com.wy.display.config.creatData;

import com.wy.display.config.readXML.ReadConfig;
import com.wy.model.decetor.LtpcChannel;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import java.io.*;
import java.util.List;
import java.util.Random;

/**
 * @author WuYe
 * @vesion 1.0 2019/12/11
 * /
 * /**
 * @program: Ltpc-Moudle
 * @description:
 * @author: WuYe
 * @create: 2019-12-11 13:30
 **/
public class CreateData  extends Service<Integer> {
    private File file;
    private  int trigger;

    public CreateData(File file, int trigger) {
        this.file = file;
        this.trigger = trigger;
    }


    private  void outAdcPck(DataOutputStream dos, LtpcChannel channel, int triggerNum, long external_trigger_timestamp) throws IOException {

            Random random = new Random();
        boolean b = random.nextBoolean();
        if (channel.getSourceBoardNum()!=0&&b) {
            byte[] bytes1 = {
                    (byte) 0x1e, (byte) 0xad, (byte) 0xc0, (byte) 0xde
                    , (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0
                    , (byte) 0x0, (byte) (channel.getSourceBoardNum()), (byte) 0x0, (byte) 0x0

                    , (byte) ((0x2<<6) | (channel.getChannelId() & 0x3f) ), (byte) 0x12, (byte)0x0, (byte) 0x0
                    , (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0
                    , (byte) (triggerNum>>8), (byte)  (triggerNum&0xff), (byte)(external_trigger_timestamp>>40),(byte)((external_trigger_timestamp>>40)&0xff)
                    , (byte)((external_trigger_timestamp>>24)&0xff), (byte)((external_trigger_timestamp>>16)&0xff), (byte)((external_trigger_timestamp>>8)&0xff), (byte)(external_trigger_timestamp&0xff)

            };
            byte[] bytes2 = new byte[288*2+4];
            for(int i=0;i<288;i++){
                int nextInt = random.nextInt(1000);
                bytes2[2*i]=(byte) (nextInt>>8);
                bytes2[2*i+1]=(byte) (nextInt&0xff);
            }
            bytes2[288*2]=(byte) 0x5a;
            bytes2[288*2+1]=(byte) 0x5a;
            bytes2[288*2+2]=(byte) 0x5a;
            bytes2[288*2+3]=(byte) 0x5a;
            dos.write(bytes1);
            dos.write(bytes2);
        }
    }

    private  void outEtdPck(DataOutputStream dos, int triggerNum, long external_trigger_timestamp) throws IOException {
        byte[] bytes = {
                  (byte) 0x1e, (byte) 0xad, (byte) 0xc0, (byte) 0xde
                , (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0
                , (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0

                , (byte) 0xc0, (byte) 0x12, (byte)(triggerNum>>8), (byte) (triggerNum&0xff)
                , (byte) 0x0, (byte) 0x0, (byte) 0x0, (byte) 0x0
                , (byte) 0x0, (byte) 0x0, (byte)(external_trigger_timestamp>>40),(byte)((external_trigger_timestamp>>40)&0xff)
                , (byte)((external_trigger_timestamp>>24)&0xff), (byte)((external_trigger_timestamp>>16)&0xff), (byte)((external_trigger_timestamp>>8)&0xff), (byte)(external_trigger_timestamp&0xff)

                , (byte) 0x5a, (byte) 0x5a, (byte) 0x5a, (byte) 0x5a
        };
        dos.write(bytes);
    }
    @Override
    protected Task<Integer> createTask() {
        return new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                List<LtpcChannel> ltpcChannels = ReadConfig.getLtpcDetector().getChannels();
                DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
                long External_trigger_timestamp=0xff00ffff0000L;
                if (trigger<1){
                    throw new RuntimeException("trigger wrong!!!");
                }
                for(int i=0;i<trigger;i++){
                    updateProgress(i,trigger);
                    outEtdPck( dos,i,External_trigger_timestamp+100*i);
                    for (int j=0;j<ltpcChannels.size();j++) {
                        outAdcPck(dos, ltpcChannels.get(j),i,External_trigger_timestamp+100*i);
                    }
                }
                dos.close();
                updateProgress(trigger,trigger);
                return null;
            }
        };
    }
}
