package PerfectLink.Models;

import Enums.ProtocolTypeEnum;

import java.net.InetAddress;

public class MessageWrapper{
    public int destPort;
    public InetAddress destAddress;
    public int content;
    public int messageId;
    public ProtocolTypeEnum protocol;
    public int originalProcessId;
    public int originalMessageId;
    public int fifoId;
    public int[] vectorClock;

    public MessageWrapper(int destPort, InetAddress destAddress, int content, int messageId, ProtocolTypeEnum protocol, int originalProcessId, int originalMessageId, int fifoId, int[] vectorClock)
    {
        this.destPort = destPort;
        this.destAddress = destAddress;
        this.content = content;
        this.messageId = messageId;
        this.protocol = protocol;
        this.originalProcessId = originalProcessId;
        this.originalMessageId = originalMessageId;
        this.fifoId = fifoId;
        this.vectorClock = vectorClock;
    }
}
