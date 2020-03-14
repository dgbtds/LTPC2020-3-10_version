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

    public  void writeLTPC() throws Exception {
        List<LtpcChannel> ltpcChannels = ReadConfig.getLtpcDetector().getChannels();
        DataOutputStream dos = new DataOutputStream(new FileOutputStream(file));
         long External_trigger_timestamp=0xff00ffff0000L;
        if (trigger<1){
            throw new RuntimeException("trigger wrong!!!");
        }
        for(int i=0;i<trigger;i++){
            outEtdPck( dos,i,External_trigger_timestamp);
            for (int j=0;j<ltpcChannels.size();j++) {
                outAdcPck(dos, ltpcChannels.get(j),i,External_trigger_timestamp);
            }
        }
        dos.close();
        System.out.println("size ="+file.length());
    }

    private  void outAdcPck(DataOutputStream dos, LtpcChannel channel, int triggerNum, long external_trigger_timestamp) throws IOException {
        if (channel.getSourceBoardNum()!=0) {
            Random random = new Random();
            dos.writeInt(0x1EADC0DE);
            dos.writeInt(0);
            dos.writeInt(channel.getSourceBoardNum()<<16);
            int flag = (0x2<<14) | (channel.getChannelId() & 0x3f) << 8|0x12;
            dos.writeShort(flag);
            dos.writeShort(0);
            dos.writeInt(0);
            dos.writeShort(triggerNum);
            dos.writeShort((short)(external_trigger_timestamp>>32));
            dos.writeInt((int) (external_trigger_timestamp&0xffffffffL));
            for(int i=0;i<288;i++){
                dos.writeShort(random.nextInt(1000));
            }
            dos.writeInt(0x5A5A5A5A);
        }
    }

    private  void outEtdPck(DataOutputStream dos, int triggerNum, long external_trigger_timestamp) throws IOException {
        dos.writeInt(0x1EADC0DE);
        dos.writeLong(0L);
        dos.writeShort(0xc012);
        dos.writeShort(triggerNum);
        dos.writeInt(0);
        dos.writeShort(0);
        dos.writeShort((short)(external_trigger_timestamp>>32));
        dos.writeInt((int) (external_trigger_timestamp&0xffffffffL));
        dos.writeInt(0x5A5A5A5A);
    }


    @Override
    protected Task<Integer> createTask() {
        return new Task<Integer>() {
            @Override
            protected Integer call() throws Exception {
                writeLTPC();
                return null;
            }
        };
    }
}
