package com.wy.model.data;/**
 * @description
 * @author: WuYe
 * @vesion:1.0
 * @Data : 2020/3/7 17:30
 */

/**
 * @program: LTPC2020-3-4-version2
 *
 * @description:公用部分数据
 *
 * @author: WuYe
 *
 * @create: 2020-03-07 17:30
 **/
public class GeneralData {
    protected  int header;//前导码
    protected  byte reservedArea1;
    protected byte targetBoardAddress;//目标板地址 0-30,  5个区块部分为7,6,6,6,6个64通道元器件
    protected short Packlength;//
    protected  byte reservedArea2;
    protected int sourceBoardAddress;//8位源板地址
    protected byte packageNunmber;//包编号
    protected byte Type;//包类型
    protected int Flag;//前两位是标志位+6位通道号+8位采集长度
    protected short packageNumber;//包编号

    public int getHeader() {
        return header;
    }

    public void setHeader(int header) {
        this.header = header;
    }

    public byte getReservedArea1() {
        return reservedArea1;
    }

    public void setReservedArea1(byte reservedArea1) {
        this.reservedArea1 = reservedArea1;
    }

    public byte getTargetBoardAddress() {
        return targetBoardAddress;
    }

    public void setTargetBoardAddress(byte targetBoardAddress) {
        this.targetBoardAddress = targetBoardAddress;
    }

    public short getPacklength() {
        return Packlength;
    }

    public void setPacklength(short packlength) {
        Packlength = packlength;
    }

    public byte getReservedArea2() {
        return reservedArea2;
    }

    public void setReservedArea2(byte reservedArea2) {
        this.reservedArea2 = reservedArea2;
    }

    public int getSourceBoardAddress() {
        return sourceBoardAddress;
    }

    public void setSourceBoardAddress(int sourceBoardAddress) {
        this.sourceBoardAddress = sourceBoardAddress;
    }

    public byte getPackageNunmber() {
        return packageNunmber;
    }

    public void setPackageNunmber(byte packageNunmber) {
        this.packageNunmber = packageNunmber;
    }

    public byte getType() {
        return Type;
    }

    public void setType(byte type) {
        Type = type;
    }

    public int getFlag() {
        return Flag;
    }

    public void setFlag(int flag) {
        Flag = flag;
    }

    public short getPackageNumber() {
        return packageNumber;
    }

    public void setPackageNumber(short packageNumber) {
        this.packageNumber = packageNumber;
    }
}
