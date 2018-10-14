package com.epfl.da.Models;

import java.net.InetAddress;

public class ProcessModel {
    public int port;
    public InetAddress address;
    public int id;

    public ProcessModel(int id, InetAddress address, int port){
        this.port = port;
        this.address = address;
        this.id = id;
    }
}
